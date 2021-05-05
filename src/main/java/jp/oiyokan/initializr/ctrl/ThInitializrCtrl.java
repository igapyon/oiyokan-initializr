package jp.oiyokan.initializr.ctrl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.initializr.OiyokanInitializrConstants;
import jp.oiyokan.initializr.OiyokanInitializrMessages;
import jp.oiyokan.initializr.OiyokanInitializrUtil;

@Controller
public class ThInitializrCtrl {
    private static final Log log = LogFactory.getLog(ThInitializrCtrl.class);

    @Autowired
    private HttpSession session;

    @RequestMapping(value = { "/initializr" }, method = { RequestMethod.GET })
    public String index(Model model, OiyoSettingsDatabase database, ThInitializrBean initializrBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        if (database.getName() == null) {
            database.setName("mydbsetting1");
        }
        if (database.getType() == null) {
            database.setType("PostgreSQL"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        }
        if (database.getDescription() == null) {
            database.setDescription("Tutorial db sample.");
        }
        if (database.getJdbcDriver() == null) {
            database.setJdbcDriver("org.postgresql.Driver"); // JDBC Driver class name.
        }
        if (database.getJdbcUrl() == null) {
            database.setJdbcUrl("jdbc:postgresql://localhost:5432/dvdrental"); // JDBC URL.
        }
        database.setJdbcUser(""); // JDBC User.
        database.setJdbcPassPlain(""); // JDBC Password.

        return "oiyokan/initializr01";
    }

    @RequestMapping(value = { "/initializr" }, params = { "connTest" }, method = { RequestMethod.POST })
    public String connTest(Model model, OiyoSettingsDatabase database, ThInitializrBean initializrBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        connTestInternal(database, initializrBean);

        return "oiyokan/initializr01";
    }

    OiyoSettings connTestInternal(OiyoSettingsDatabase database, ThInitializrBean initializrBean) throws IOException {

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // [IYI1101] Prepare database settings.
        log.info(OiyokanInitializrMessages.IYI1101);
        OiyoInfo oiyoInfo = new OiyoInfo();

        OiyoSettings oiyoSettings = new OiyoSettings();
        oiyoSettings.setNamespace("Oiyokan"); // Namespace of OData
        oiyoSettings.setContainerName("Container"); // Container of OData
        oiyoSettings.setDatabase(new ArrayList<>());
        oiyoSettings.setEntitySet(new ArrayList<>());

        oiyoSettings.getDatabase().add(database);

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
            initializrBean.setMsgError(OiyokanInitializrMessages.IYI2201);
            return null;
        }

        try {
            OiyokanInitializrUtil.traverseTable(oiyoInfo, oiyoSettings, initializrBean.isProcessView());

            // TODO message
            initializrBean.setMsgSuccess("Connection test success.");
            return oiyoSettings;
        } catch (ODataApplicationException ex) {
            // [IYI2201] ERROR: Fail to connect database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString());
            initializrBean.setMsgError(OiyokanInitializrMessages.IYI2201);
            return null;
        } catch (SQLException ex) {
            // [IYI2202] ERROR: Fail to close database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2202 + ": " + ex.toString());
            initializrBean.setMsgError(OiyokanInitializrMessages.IYI2202);
            return null;
        }
    }

    @RequestMapping(value = { "/initializr" }, params = { "preH2" }, method = { RequestMethod.POST })
    public String preH2(Model model, OiyoSettingsDatabase database, ThInitializrBean initializrBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

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

        return "oiyokan/initializr01";
    }

    @RequestMapping(value = { "/initializr" }, params = { "prePostgreSQL" }, method = { RequestMethod.POST })
    public String prePostgreSQL(Model model, OiyoSettingsDatabase database, ThInitializrBean initializrBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

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

        return "oiyokan/initializr01";
    }

    @RequestMapping(value = { "/initializr" }, params = { "preMySQL" }, method = { RequestMethod.POST })
    public String preMySQL(Model model, OiyoSettingsDatabase database, ThInitializrBean initializrBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

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

        return "oiyokan/initializr01";
    }

    @RequestMapping(value = { "/initializr" }, params = { "preSQLSV2008" }, method = { RequestMethod.POST })
    public String preSQLSV2008(Model model, OiyoSettingsDatabase database, ThInitializrBean initializrBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

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

        return "oiyokan/initializr01";
    }

    @RequestMapping(value = { "/initializr" }, params = { "preORCL18" }, method = { RequestMethod.POST })
    public String preORCL18(Model model, OiyoSettingsDatabase database, ThInitializrBean initializrBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        if (database.getName() == null) {
            database.setName("mydbsetting1");
        }
        database.setType("ORCL18"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        if (database.getDescription() == null) {
            database.setDescription("Tutorial db sample.");
        }
        database.setJdbcDriver("oracle.jdbc.driver.OracleDriver"); // JDBC Driver class name.
        database.setJdbcUrl("jdbc:oracle:thin:@10.0.2.15:1521/xepdb1");
        database.setJdbcUser("orauser"); // JDBC User.
        database.setJdbcPassPlain("passwd123"); // JDBC Password.

        initializrBean.setMsgSuccess("Typical ORCL18 preset is loaded.");

        return "oiyokan/initializr01";
    }

    /////////////////////////
    // Select table

    @RequestMapping(value = { "/initializr" }, params = { "selectTable" }, method = { RequestMethod.POST })
    public String selectTable(Model model, OiyoSettingsDatabase database, ThInitializrBean initializrBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializrBean", initializrBean);
        initializrBean.setMsgSuccess(null);
        initializrBean.setMsgError(null);

        log.info("processView:" + initializrBean.isProcessView());

        OiyoSettings oiyoSettings = connTestInternal(database, initializrBean);
        if (oiyoSettings != null) {
            // ソートなど
            OiyoInfo oiyoInfo = new OiyoInfo();
            OiyokanInitializrUtil.tuneSettings(oiyoInfo, oiyoSettings, initializrBean.isConvertCamel(),
                    initializrBean.isFilterTreatNullAsBlank);

            initializrBean.getEntitySets().clear();
            for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
                initializrBean.getEntitySets().add(new ThInitializrBean.EntitySet(entitySet.getName(), false));
            }

            // セッションを記憶
            // TODO FIXME そもそも Form Bean を作成して、通常のセッション処理を行うこと。
            session.setAttribute("database.name", database.getName());
            session.setAttribute("database.type", database.getType());
            session.setAttribute("database.description", database.getDescription());
            session.setAttribute("database.jdbcDriver", database.getJdbcDriver());
            session.setAttribute("database.jdbcUrl", database.getJdbcUrl());
            session.setAttribute("database.jdbcUser", database.getJdbcUser());
            session.setAttribute("database.jdbcPassPlain", database.getJdbcPassPlain());
            session.setAttribute("database.jdbcPassEnc", database.getJdbcPassEnc());

            return "oiyokan/initializr02";
        } else {
            return "oiyokan/initializr01";
        }
    }

    @RequestMapping(value = { "/initializr" }, params = "download", method = { RequestMethod.POST })
    public String download(Model model, ThInitializrBean initializrBean, HttpServletResponse response)
            throws IOException {

        final OiyoSettingsDatabase database = new OiyoSettingsDatabase();

        // TODO FIXME そもそも Form Bean を作成して、通常のセッション処理を行うこと。
        database.setName((String) session.getAttribute("database.name"));
        database.setType((String) session.getAttribute("database.type"));
        database.setDescription((String) session.getAttribute("database.description"));
        database.setJdbcDriver((String) session.getAttribute("database.jdbcDriver"));
        database.setJdbcUrl((String) session.getAttribute("database.jdbcUrl"));
        database.setJdbcUser((String) session.getAttribute("database.jdbcUser"));
        database.setJdbcPassPlain((String) session.getAttribute("database.jdbcPassPlain"));
        database.setJdbcPassEnc((String) session.getAttribute("database.jdbcPassEnc"));

        if (database.getJdbcUser() == null) {
            database.setJdbcUser("");
        }
        if (database.getJdbcPassPlain() == null) {
            database.setJdbcPassPlain("");
        }

        // 一覧作成のため全体をセット
        initializrBean.setProcessView(true);
        final OiyoSettings oiyoSettings = connTestInternal(database, initializrBean);

        log.info("選択したEntitySet以外を一旦消し込み");
        OUTER_LOOP: for (int index = oiyoSettings.getEntitySet().size() - 1; index >= 0; index--) {
            OiyoSettingsEntitySet look = oiyoSettings.getEntitySet().get(index);
            for (String opts : initializrBean.getCheckboxes()) {
                if (look.getName().equals(opts)) {
                    // チェックされていた。残してよし。
                    continue OUTER_LOOP;
                }
            }
            // チェックされていなかった。削除。
            oiyoSettings.getEntitySet().remove(index);
        }

        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

        //////////////////////////////////////////////////////////
        // Setup basic settings info
        OiyoInfo oiyoInfo = new OiyoInfo();

        OiyokanInitializrUtil.tuneSettings(oiyoInfo, oiyoSettings, initializrBean.isConvertCamel(),
                initializrBean.isFilterTreatNullAsBlank);

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
