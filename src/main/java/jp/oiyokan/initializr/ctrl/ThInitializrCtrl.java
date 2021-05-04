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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.initializr.OiyokanInitializrConstants;
import jp.oiyokan.initializr.OiyokanInitializrMessages;
import jp.oiyokan.initializr.OiyokanInitializrUtil;

@Controller
public class ThInitializrCtrl {
    private static final Log log = LogFactory.getLog(ThInitializrCtrl.class);

    @RequestMapping(value = { "/initializr" }, method = { RequestMethod.GET })
    public String index(Model model, OiyoSettingsDatabase database, ThInitializrBean initializerBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializerBean", initializerBean);
        model.addAttribute("msgSuccess", "");
        model.addAttribute("msgError", "");

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

        return "oiyokan/initializr";
    }

    @RequestMapping(value = { "/initializr" }, params = { "connTest" }, method = { RequestMethod.POST })
    public String connTest(Model model, OiyoSettingsDatabase database, ThInitializrBean initializerBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializerBean", initializerBean);
        model.addAttribute("msgSuccess", "");
        model.addAttribute("msgError", "");

        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

        //////////////////////////////////////////////////////////
        // Setup basic settings info

        // [IYI1101] Prepare database settings.
        log.info(OiyokanInitializrMessages.IYI1101);
        OiyoInfo oiyoInfo = new OiyoInfo();
        oiyoInfo.setPassphrase(OiyokanConstants.OIYOKAN_PASSPHRASE);

        OiyoSettings oiyoSettings = new OiyoSettings();
        oiyoSettings.setNamespace("Oiyokan"); // Namespace of OData
        oiyoSettings.setContainerName("Container"); // Container of OData
        oiyoSettings.setDatabase(new ArrayList<>());
        oiyoSettings.setEntitySet(new ArrayList<>());

        oiyoSettings.getDatabase().add(database);

        //////////////////////////////////////////////////////////
        // Process settings

        try {
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
            model.addAttribute("msgError", OiyokanInitializrMessages.IYI2201);
            return "oiyokan/initializr";
        }

        try {
            OiyokanInitializrUtil.traverseTable(oiyoInfo, oiyoSettings);

            // TODO message
            model.addAttribute("msgSuccess", "Connection test success.");
        } catch (ODataApplicationException ex) {
            // [IYI2201] ERROR: Fail to connect database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString());
            model.addAttribute("msgError", OiyokanInitializrMessages.IYI2201);
        } catch (SQLException ex) {
            // [IYI2202] ERROR: Fail to close database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2202 + ": " + ex.toString());
            model.addAttribute("msgError", OiyokanInitializrMessages.IYI2202);
        }
        return "oiyokan/initializr";
    }

    @RequestMapping(value = { "/initializr" }, params = "download", method = { RequestMethod.POST })
    public String download(Model model, OiyoSettingsDatabase database, ThInitializrBean initializerBean,
            HttpServletResponse response) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializerBean", initializerBean);
        model.addAttribute("msgSuccess", "");
        model.addAttribute("msgError", "");

        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

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
            OiyokanInitializrUtil.traverseTable(oiyoInfo, oiyoSettings);
        } catch (ODataApplicationException ex) {
            // [IYI2201] ERROR: Fail to connect database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString());
            database.setDescription(OiyokanInitializrMessages.IYI2201);
        } catch (SQLException ex) {
            // [IYI2202] ERROR: Fail to close database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2202 + ": " + ex.toString());
            database.setDescription(OiyokanInitializrMessages.IYI2202);
        }

        OiyokanInitializrUtil.tuneSettings(oiyoInfo, oiyoSettings, initializerBean.isConvertCamel(),
                initializerBean.isFilterTreatNullAsBlank);

        //////////////////////////////////////////////////////////
        // Write settings info into oiyokan-settings.json

        String jsonString = null;
        try {
            jsonString = OiyokanInitializrUtil.oiyoSettings2String(oiyoSettings);
        } catch (IOException ex) {
            // [IYI4201] ERROR: Fail to generate json file.
            log.error(OiyokanInitializrMessages.IYI4201 + ": " + ex.toString(), ex);
            database.setDescription(OiyokanInitializrMessages.IYI4201);
        }

        try {
            final byte[] zipFile = OiyokanInitializrUtil
                    .packageZipFile(new File("./src/main/resources/oiyokan-web-template"), jsonString);

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=oiyokan-demo.zip");

            final OutputStream outStream = response.getOutputStream();
            IOUtils.copy(new ByteArrayInputStream(zipFile), outStream);
            outStream.flush();

            model.addAttribute("msgSuccess", OiyokanInitializrMessages.IYI5102);

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

    @RequestMapping(value = { "/initializr" }, params = { "preH2" }, method = { RequestMethod.POST })
    public String preH2(Model model, OiyoSettingsDatabase database, ThInitializrBean initializerBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializerBean", initializerBean);
        model.addAttribute("msgSuccess", "");
        model.addAttribute("msgError", "");

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

        model.addAttribute("msgSuccess", "Typical h2 preset is loaded.");
        return "oiyokan/initializr";
    }

    @RequestMapping(value = { "/initializr" }, params = { "prePostgreSQL" }, method = { RequestMethod.POST })
    public String prePostgreSQL(Model model, OiyoSettingsDatabase database, ThInitializrBean initializerBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializerBean", initializerBean);
        model.addAttribute("msgSuccess", "");
        model.addAttribute("msgError", "");

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

        model.addAttribute("msgSuccess", "Typical PostgreSQL preset is loaded.");
        return "oiyokan/initializr";
    }

    @RequestMapping(value = { "/initializr" }, params = { "preMySQL" }, method = { RequestMethod.POST })
    public String preMySQL(Model model, OiyoSettingsDatabase database, ThInitializrBean initializerBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializerBean", initializerBean);
        model.addAttribute("msgSuccess", "");
        model.addAttribute("msgError", "");

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

        model.addAttribute("msgSuccess", "Typical MySQL preset is loaded.");
        return "oiyokan/initializr";
    }

    @RequestMapping(value = { "/initializr" }, params = { "preSQLSV2008" }, method = { RequestMethod.POST })
    public String preSQLSV2008(Model model, OiyoSettingsDatabase database, ThInitializrBean initializerBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializerBean", initializerBean);
        model.addAttribute("msgSuccess", "");
        model.addAttribute("msgError", "");

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

        model.addAttribute("msgSuccess", "Typical SQLSV2008 preset is loaded.");
        return "oiyokan/initializr";
    }

    @RequestMapping(value = { "/initializr" }, params = { "preORCL18" }, method = { RequestMethod.POST })
    public String preORCL18(Model model, OiyoSettingsDatabase database, ThInitializrBean initializerBean,
            BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);
        model.addAttribute("initializerBean", initializerBean);
        model.addAttribute("msgSuccess", "");
        model.addAttribute("msgError", "");

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

        model.addAttribute("msgSuccess", "Typical ORCL18 preset is loaded.");
        return "oiyokan/initializr";
    }
}
