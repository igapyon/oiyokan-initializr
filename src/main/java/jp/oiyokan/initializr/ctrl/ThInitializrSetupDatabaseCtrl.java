package jp.oiyokan.initializr.ctrl;

import java.io.IOException;
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
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.initializr.OiyokanInitializrConstants;
import jp.oiyokan.initializr.OiyokanInitializrMessages;

@Controller
@SessionAttributes("scopedTarget.settingsBean")
public class ThInitializrSetupDatabaseCtrl {
    private static final Log log = LogFactory.getLog(ThInitializrSetupDatabaseCtrl.class);

    @Autowired
    private OiyokanSettingsWrapper settingsBean;

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "new" }, method = { RequestMethod.POST })
    public String open(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        OiyoSettingsDatabase database = new OiyoSettingsDatabase();

        if (database.getName() == null) {
            database.setName("connDef1");
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
            BindingResult result) throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // ひとつもテーブルをマップさせずに接続のみ確認。
        Map<String, String> mapNameFilter = new HashMap<>();
        connTestInternal(initializrBean, database, mapNameFilter);

        return "oiyokan/initializrSetupDatabase";
    }

    static boolean connTestInternal(ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            Map<String, String> mapNameFilter) throws IOException {

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // [IYI1101] Prepare database settings.
        log.info(OiyokanInitializrMessages.IYI1101);

        //////////////////////////////////////////////////////////
        // Process settings
        try {
            // Enc pass は一旦クリア.
            database.setJdbcPassEnc("");
            if (database.getJdbcUser() == null || database.getJdbcUser().trim().length() == 0) {
                try (Connection conn = DriverManager.getConnection(database.getJdbcUrl())) {
                }
            } else {
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

        // TODO message
        initializrBean.setMsgSuccess("Connection test success.");
        return true;
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "preH2" }, method = { RequestMethod.POST })
    public String preH2(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) throws IOException {
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

        initializrBean.setMsgSuccess("Typical h2 preset is loaded.");

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "prePostgreSQL" }, method = {
            RequestMethod.POST })
    public String prePostgreSQL(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) throws IOException {
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

        initializrBean.setMsgSuccess("Typical PostgreSQL preset is loaded.");

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "preMySQL" }, method = { RequestMethod.POST })
    public String preMySQL(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) throws IOException {
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

        initializrBean.setMsgSuccess("Typical MySQL preset is loaded.");

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "preSQLSV2008" }, method = {
            RequestMethod.POST })
    public String preSQLSV2008(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) throws IOException {
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

        initializrBean.setMsgSuccess("Typical SQLSV2008 preset is loaded.");

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "preORCL18" }, method = { RequestMethod.POST })
    public String preORCL18(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) throws IOException {
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

        initializrBean.setMsgSuccess("Typical ORCL18 preset is loaded.");

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializrSetupDatabase" }, params = { "saveDatabaseSettings" }, method = {
            RequestMethod.POST })
    public String saveDatabaseSettings(Model model, ThInitializrBean initializrBean, OiyoSettingsDatabase database,
            BindingResult result) throws IOException {
        model.addAttribute("settings", settingsBean.getSettings());
        model.addAttribute("initializrBean", initializrBean);
        model.addAttribute("database", database);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        // TODO ここで 指定の Database 設定を保存.

        // 接続確認

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // ひとつもテーブルをマップさせずに接続のみ確認。
        Map<String, String> mapNameFilter = new HashMap<>();
        if (connTestInternal(initializrBean, database, mapNameFilter) == false) {
            // データベース接続失敗。やりなおし。
            return "oiyokan/initializrSetupDatabase";
        } else {
            // 接続成功した。これを保存する。
            // TODO 既存のものがあれば置き換えること。

            settingsBean.getSettings().getDatabase().add(database);
            return "oiyokan/initializrTop";
        }
    }
}
