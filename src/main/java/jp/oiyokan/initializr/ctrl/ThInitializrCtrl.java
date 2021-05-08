package jp.oiyokan.initializr.ctrl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttributes;

import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.initializr.OiyokanInitializrConstants;
import jp.oiyokan.initializr.OiyokanInitializrMessages;
import jp.oiyokan.initializr.OiyokanInitializrUtil;
import jp.oiyokan.util.OiyoEncryptUtil;

@Controller
@SessionAttributes(value = { "initializrBean" })
public class ThInitializrCtrl {
    private static final Log log = LogFactory.getLog(ThInitializrCtrl.class);

    @ModelAttribute(value = "initializrBean")
    public ThInitializrBean setupBean() {
        return new ThInitializrBean();
    }

    @RequestMapping(value = { "/initializr" }, method = { RequestMethod.GET })
    public String index(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        // TODO 必要に応じて分岐。

        return "oiyokan/initializrSetupDatabase";
    }

    @RequestMapping(value = { "/initializr" }, params = { "connTest" }, method = { RequestMethod.POST })
    public String connTest(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // ひとつもテーブルをマップさせずに接続のみ確認。
        Map<String, String> mapNameFilter = new HashMap<>();
        connTestInternal(initializrBean, mapNameFilter);

        return "oiyokan/initializrSetupDatabase";
    }

    boolean connTestInternal(ThInitializrBean initializrBean, Map<String, String> mapNameFilter) throws IOException {

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // [IYI1101] Prepare database settings.
        log.info(OiyokanInitializrMessages.IYI1101);

        //////////////////////////////////////////////////////////
        // Process settings
        final OiyoInfo oiyoInfo = new OiyoInfo();
        final OiyoSettingsDatabase database = initializrBean.getFirstDatabase();
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

        try {
            OiyokanInitializrUtil.traverseTable(oiyoInfo, initializrBean.getSettings(), initializrBean.isProcessView(),
                    initializrBean.isReadWriteAccess(), mapNameFilter);

            // TODO message
            initializrBean.setMsgSuccess("Connection test success.");
            return true;
        } catch (ODataApplicationException ex) {
            // [IYI2201] ERROR: Fail to connect database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString());
            initializrBean.setMsgError(OiyokanInitializrMessages.IYI2201);
            return false;
        } catch (SQLException ex) {
            // [IYI2202] ERROR: Fail to close database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2202 + ": " + ex.toString());
            initializrBean.setMsgError(OiyokanInitializrMessages.IYI2202);
            return false;
        }
    }

    @RequestMapping(value = { "/initializr" }, params = { "preH2" }, method = { RequestMethod.POST })
    public String preH2(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        final OiyoSettingsDatabase database = initializrBean.getFirstDatabase();
        if (database.getName() == null) {
            database.setName("mydbsetting1");
        }
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

    @RequestMapping(value = { "/initializr" }, params = { "prePostgreSQL" }, method = { RequestMethod.POST })
    public String prePostgreSQL(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        final OiyoSettingsDatabase database = initializrBean.getFirstDatabase();
        if (database.getName() == null) {
            database.setName("mydbsetting1");
        }
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

    @RequestMapping(value = { "/initializr" }, params = { "preMySQL" }, method = { RequestMethod.POST })
    public String preMySQL(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        final OiyoSettingsDatabase database = initializrBean.getFirstDatabase();
        if (database.getName() == null) {
            database.setName("mydbsetting1");
        }
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

    @RequestMapping(value = { "/initializr" }, params = { "preSQLSV2008" }, method = { RequestMethod.POST })
    public String preSQLSV2008(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        final OiyoSettingsDatabase database = initializrBean.getFirstDatabase();
        if (database.getName() == null) {
            database.setName("mydbsetting1");
        }
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

    @RequestMapping(value = { "/initializr" }, params = { "preORCL18" }, method = { RequestMethod.POST })
    public String preORCL18(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        final OiyoSettingsDatabase database = initializrBean.getFirstDatabase();
        if (database.getName() == null) {
            database.setName("mydbsetting1");
        }
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

    @RequestMapping(value = { "/initializr" }, params = { "saveDatabaseSettings" }, method = { RequestMethod.POST })
    public String saveDatabaseSettings(Model model, ThInitializrBean initializrBean, BindingResult result)
            throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        // TODO ここで 指定の Database 設定を保存.

        // 接続確認

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // ひとつもテーブルをマップさせずに接続のみ確認。
        Map<String, String> mapNameFilter = new HashMap<>();
        if (connTestInternal(initializrBean, mapNameFilter) == false) {
            return "oiyokan/initializrSetupDatabase";
        } else {
            return "oiyokan/initializrTop";
        }
    }

    /////////////////////////
    // Select table

    @RequestMapping(value = { "/initializr" }, params = { "selectTable" }, method = { RequestMethod.POST })
    public String selectTable(Model model, ThInitializrBean initializrBean, BindingResult result) throws IOException {
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        log.info("processView:" + initializrBean.isProcessView());

        OiyoSettings oiyoSettings = initializrBean.getSettings();
        if (oiyoSettings != null) {
            // ソートなど
            OiyoInfo oiyoInfo = new OiyoInfo();

            connTestInternal(initializrBean, null);

            OiyokanInitializrUtil.tuneSettings(oiyoInfo, oiyoSettings, initializrBean.isConvertCamel(),
                    initializrBean.isFilterTreatNullAsBlank);

            initializrBean.getEntitySets().clear();
            for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
                initializrBean.getEntitySets().add(new ThInitializrBean.EntitySet(entitySet.getName(), false));
            }

            OiyoSettingsDatabase database = initializrBean.getFirstDatabase();
            if (database.getJdbcUser() == null) {
                log.info("selectTable: database.jdbcUser was null");
                database.setJdbcUser("");
            }
            if (database.getJdbcPassPlain() == null) {
                log.info("selectTable: database.jdbcPassPlain was null");
                database.setJdbcPassPlain("");
            }
            if (database.getJdbcPassEnc() == null) {
                log.info("selectTable: database.jdbcPassEnc was null");
                database.setJdbcPassPlain("");
            }

            return "oiyokan/initializr02";
        } else {
            return "oiyokan/initializrSetupDatabase";
        }
    }

    @RequestMapping(value = { "/initializr" }, params = "download", method = { RequestMethod.POST })
    public String download(Model model, ThInitializrBean initializrBean, HttpServletResponse response)
            throws IOException {

        OiyoSettingsDatabase database = initializrBean.getFirstDatabase();
        if (database.getJdbcUser() == null) {
            log.info("download: database.jdbcUser was null");
            database.setJdbcUser("");
        }
        if (database.getJdbcPassPlain() == null) {
            log.info("download: database.jdbcPassPlain was null");
            database.setJdbcPassPlain("");
        }
        if (database.getJdbcPassEnc() == null) {
            log.info("download: database.jdbcPassEnc was null");
            database.setJdbcPassPlain("");
        }

        log.info("選択したEntitySetをマーク");
        Map<String, String> mapNameFilter = new HashMap<>();
        for (String opts : initializrBean.getCheckboxes()) {
            mapNameFilter.put(opts, opts);
        }

        // 一覧の網羅性のために VIEW も含めて処理
        initializrBean.setProcessView(true);
        final OiyoSettings oiyoSettings = initializrBean.getSettings();

        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

        //////////////////////////////////////////////////////////
        // Setup basic settings info
        OiyoInfo oiyoInfo = new OiyoInfo();

        OiyokanInitializrUtil.tuneSettings(oiyoInfo, oiyoSettings, initializrBean.isConvertCamel(),
                initializrBean.isFilterTreatNullAsBlank);

        // JSON書き込み直前に、プレーンパスワードを除去
        if (database.getJdbcPassEnc() == null || database.getJdbcPassEnc().trim().length() == 0) {
            // データベース設定を暗号化。もとのプレーンテキストパスワードは除去.
            database.setJdbcPassEnc(OiyoEncryptUtil.encrypt(database.getJdbcPassPlain(), oiyoInfo.getPassphrase()));
            database.setJdbcPassPlain(null);
        }

        //////////////////////////////////////////////////////////
        // Write settings info into oiyokan-settings.json

        String jsonString = null;
        try {
            jsonString = OiyokanInitializrUtil.oiyoSettings2String(oiyoSettings);
        } catch (IOException ex) {
            // [IYI4201] ERROR: Fail to generate json file.
            log.error(OiyokanInitializrMessages.IYI4201 + ": " + ex.toString(), ex);
        }

        try {
            final byte[] zipFile = OiyokanInitializrUtil
                    .packageZipFile(new File("./src/main/resources/oiyokan-web-template"), jsonString);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=oiyokan-demo.zip");

            final OutputStream outStream = response.getOutputStream();
            IOUtils.copy(new ByteArrayInputStream(zipFile), outStream);
            outStream.flush();

            initializrBean.setMsgSuccess(OiyokanInitializrMessages.IYI5102);

            // [IYI5102] Check the `oiyokan-demo.zip`.
            log.info(OiyokanInitializrMessages.IYI5102 + ": oiyokan-demo.zip");
        } catch (IOException ex) {
            // [IYI5201] ERROR: Fail to generate zip file.
            log.error(OiyokanInitializrMessages.IYI5201, ex);
        }

        // [IYI1002] Oiyokan Initializr End.
        log.info(OiyokanInitializrMessages.IYI1002);
        return null;
    }
}
