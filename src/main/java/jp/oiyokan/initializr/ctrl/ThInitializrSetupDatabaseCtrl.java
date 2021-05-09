package jp.oiyokan.initializr.ctrl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.initializr.OiyokanInitializrMessages;
import jp.oiyokan.util.OiyoEncryptUtil;

@Controller
@SessionAttributes("scopedTarget.settingsBean")
public class ThInitializrSetupDatabaseCtrl {
    private static final Log log = LogFactory.getLog(ThInitializrSetupDatabaseCtrl.class);

    @Autowired
    private OiyokanSettingsWrapper settingsBean;

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "new" }, method = { RequestMethod.POST })
    public String open(Model model, ThInitializrBean initializrBean, BindingResult result) {
        // [IYI6108] INFO: `/initializrSetupDatabase`(POST:new) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6108);

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        OiyoSettingsDatabase database = new OiyoSettingsDatabase();

        if (database.getName() == null) {
            final int nextNumber = settingsBean.getSettings().getDatabase().size() + 1;
            database.setName("connDef" + nextNumber);
        }
        if (database.getType() == null) {
            database.setType(OiyokanConstants.DatabaseType.PostgreSQL.name()); // h2, PostgreSQL, MySQL, SQLSV2008,
                                                                               // ORCL18
        }
        if (database.getDescription() == null) {
            database.setDescription("Description of this database jdbc settings.");
        }
        if (database.getJdbcDriver() == null) {
            database.setJdbcDriver("org.postgresql.Driver"); // JDBC Driver class name.
        }
        if (database.getJdbcUrl() == null) {
            database.setJdbcUrl("jdbc:postgresql://localhost:5432/dvdrental"); // JDBC URL.
        }
        database.setJdbcUser(""); // JDBC User.
        database.setJdbcPassPlain(""); // JDBC Password.

        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "connTest" }, method = { RequestMethod.POST })
    public String connTest(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) {
        // [IYI6109] INFO: `/initializrSetupDatabase`(POST:connTest) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6109);

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // ひとつもテーブルをマップさせずに接続のみ確認。
        final Map<String, String> mapNameFilter = new HashMap<>();
        connTestInternal(initializrBean, database, mapNameFilter);

        return "oiyokan/initializrSetupDatabase";
    }

    static boolean connTestInternal(ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            Map<String, String> mapNameFilter) {

        // [IYI1101] Prepare database settings.
        log.debug(OiyokanInitializrMessages.IYI1101);

        //////////////////////////////////////////////////////////
        // Process settings
        try {
            if (database.getJdbcUser() == null || database.getJdbcUser().trim().length() == 0) {
                try (Connection conn = DriverManager.getConnection(database.getJdbcUrl())) {
                }
            } else {
                if (database.getJdbcPassEnc() != null && database.getJdbcPassEnc().trim().length() > 0) {
                    if (database.getJdbcPassPlain() == null || database.getJdbcPassPlain().trim().length() == 0) {
                        database.setJdbcPassPlain(
                                OiyoEncryptUtil.decrypt(database.getJdbcPassEnc(), new OiyoInfo().getPassphrase()));
                    }
                }
                try (Connection conn = DriverManager.getConnection(database.getJdbcUrl(), database.getJdbcUser(),
                        database.getJdbcPassPlain())) {
                }
            }
        } catch (SQLException ex) {
            // [IYI2201] ERROR: Fail to connect database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString());
            initializrBean.setMsgError(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString());
            return false;
        }

        // [IYI7105] INFO: Connection test success.
        initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7105);
        log.info(OiyokanInitializrMessages.IYI7105);
        return true;
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "applyDatabaseSettings" }, method = {
            RequestMethod.POST })
    public String applyDatabaseSettings(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) {
        // [IYI6110] INFO: `/initializrSetupDatabase`(POST:applyDatabaseSettings) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6110);

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // ひとつもテーブルをマップさせずに接続のみ確認。
        final Map<String, String> mapNameFilter = new HashMap<>();
        if (connTestInternal(initializrBean, database, mapNameFilter) == false) {
            // [IYI7121] WARN: Fail to test connect to database.
            initializrBean.setMsgError(OiyokanInitializrMessages.IYI7121);
            log.warn(OiyokanInitializrMessages.IYI7121);
            return "oiyokan/initializrSetupDatabase";
        } else {
            // [IYI7122] DEBUG: Success to test connect to database.
            initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7122);
            log.debug(OiyokanInitializrMessages.IYI7122);

            // TODO 同名の接続設定があったらこれをリジェクトすること。
            // 接続成功。これを記憶する。
            settingsBean.getSettings().getDatabase().add(database);

            // [IYI7106] INFO: Database settings addded.
            initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7106);
            log.info(OiyokanInitializrMessages.IYI7106);
            return "oiyokan/initializrTop";
        }
    }

    ///////////////
    // Pre

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "preH2" }, method = { RequestMethod.POST })
    public String preH2(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) {
        // [IYI6111] INFO: `/initializrSetupDatabase`(POST:preXXXXX) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6111);

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        database.setType("h2"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        if (database.getDescription() == null) {
            database.setDescription("Tutorial db sample.");
        }
        database.setJdbcDriver("org.h2.Driver"); // JDBC Driver class name.
        database.setJdbcUrl(
                "jdbc:h2:file:./src/main/resources/db/oiyokanTest;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE;MODE=MSSQLServer");
        database.setJdbcUser("sa"); // JDBC User.
        database.setJdbcPassPlain(""); // JDBC Password.

        // [IYI7111] INFO: Typical h2 preset is loaded.
        initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7111);
        log.info(OiyokanInitializrMessages.IYI7111);

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "prePostgreSQL" }, method = {
            RequestMethod.POST })
    public String prePostgreSQL(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) {
        // [IYI6111] INFO: `/initializrSetupDatabase`(POST:preXXXXX) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6111);

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        database.setType("PostgreSQL"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        if (database.getDescription() == null) {
            database.setDescription("Tutorial db sample.");
        }
        database.setJdbcDriver("org.postgresql.Driver"); // JDBC Driver class name.
        database.setJdbcUrl("jdbc:postgresql://localhost:5432/dvdrental");
        database.setJdbcUser(""); // JDBC User.
        database.setJdbcPassPlain(""); // JDBC Password.

        // [IYI7112] INFO: Typical PostgreSQL preset is loaded.
        initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7112);
        log.info(OiyokanInitializrMessages.IYI7112);

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "preMySQL" }, method = { RequestMethod.POST })
    public String preMySQL(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) {
        // [IYI6111] INFO: `/initializrSetupDatabase`(POST:preXXXXX) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6111);

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        database.setType("MySQL"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        if (database.getDescription() == null) {
            database.setDescription("Tutorial db sample.");
        }
        database.setJdbcDriver("com.mysql.jdbc.Driver"); // JDBC Driver class name.
        database.setJdbcUrl(
                "jdbc:mysql://localhost/mysql?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8&useCursorFetch=true&defaultFetchSize=128&useServerPrepStmts=true&emulateUnsupportedPstmts=false");
        database.setJdbcUser("root"); // JDBC User.
        database.setJdbcPassPlain("passwd123"); // JDBC Password.

        // [IYI7113] INFO: Typical MySQL preset is loaded.
        initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7113);
        log.info(OiyokanInitializrMessages.IYI7113);

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "preSQLSV2008" }, method = {
            RequestMethod.POST })
    public String preSQLSV2008(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) {
        // [IYI6111] INFO: `/initializrSetupDatabase`(POST:preXXXXX) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6111);

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        database.setType("SQLSV2008"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        if (database.getDescription() == null) {
            database.setDescription("Tutorial db sample.");
        }
        database.setJdbcDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver"); // JDBC Driver class name.
        database.setJdbcUrl("jdbc:sqlserver://localhost\\SQLExpress");
        database.setJdbcUser("sa"); // JDBC User.
        database.setJdbcPassPlain("passwd123"); // JDBC Password.

        // [IYI7114] INFO: Typical SQLSV2008 preset is loaded.
        initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7114);
        log.info(OiyokanInitializrMessages.IYI7114);

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "preORCL18" }, method = { RequestMethod.POST })
    public String preORCL18(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) {
        // [IYI6111] INFO: `/initializrSetupDatabase`(POST:preXXXXX) が開かれた.
        log.info(OiyokanInitializrMessages.IYI6111);

        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        database.setType("ORCL18"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        if (database.getDescription() == null) {
            database.setDescription("Tutorial db sample.");
        }
        database.setJdbcDriver("oracle.jdbc.driver.OracleDriver"); // JDBC Driver class name.
        database.setJdbcUrl("jdbc:oracle:thin:@10.0.1.2:1521/xepdb1");
        database.setJdbcUser("orauser"); // JDBC User.
        database.setJdbcPassPlain("passwd123"); // JDBC Password.

        // [IYI7115] INFO: Typical ORCL18 preset is loaded.
        initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI7115);
        log.info(OiyokanInitializrMessages.IYI7115);

        return "oiyokan/initializrSetupDatabase";
    }
}
