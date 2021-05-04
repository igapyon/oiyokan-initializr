package jp.oiyokan.initializr.ctrl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;
import org.springframework.http.MediaType;
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
public class ThIndexCtrl {
    private static final Log log = LogFactory.getLog(ThIndexCtrl.class);

    @RequestMapping(value = { "/", "/index.html" })
    public String index(Model model, OiyoSettingsDatabase database, BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);

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

        return "index";
    }

    @RequestMapping(value = { "/entry" }, params = { "connTest" }, method = { RequestMethod.GET, RequestMethod.POST })
    public String connTest(Model model, OiyoSettingsDatabase database, BindingResult result) throws IOException {
        model.addAttribute("databaseBean", database);

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
            OiyokanInitializrUtil.traverseTable(oiyoInfo, oiyoSettings);
        } catch (ODataApplicationException ex) {
            // [IYI2201] ERROR: Fail to connect database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString(), ex);
        } catch (SQLException ex) {
            // [IYI2202] ERROR: Fail to close database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2202 + ": " + ex.toString(), ex);
        }

        return "index";
    }

    @RequestMapping(value = { "/entry" }, params = "download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public String entry(Model model, OiyoSettingsDatabase database, HttpServletResponse response) throws IOException {
        model.addAttribute("databaseBean", database);

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

        boolean convertCamel = false; // EntitySetなどの名称を Camel case にするかどうか。通常は false で良い
        boolean isSfdcMode = true; // Support Salesforce or not.

        //////////////////////////////////////////////////////////
        // Process settings

        try {
            OiyokanInitializrUtil.traverseTable(oiyoInfo, oiyoSettings);
        } catch (ODataApplicationException ex) {
            // [IYI2201] ERROR: Fail to connect database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString(), ex);
        } catch (SQLException ex) {
            // [IYI2202] ERROR: Fail to close database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2202 + ": " + ex.toString(), ex);
        }

        OiyokanInitializrUtil.tuneSettings(oiyoInfo, oiyoSettings, convertCamel, isSfdcMode);

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

            response.setHeader("Content-Disposition", "attachment; filename=oiyokan-demo.zip");

            final OutputStream outStream = response.getOutputStream();
            IOUtils.copy(new ByteArrayInputStream(zipFile), outStream);
            outStream.flush();

            // [IYI5102] Check the `oiyokan-demo.zip`.
            log.info(OiyokanInitializrMessages.IYI5102 + ": oiyokan-demo.zip");
        } catch (IOException ex) {
            // [IYI5201] ERROR: Fail to generate zip file.
            log.error(OiyokanInitializrMessages.IYI5201 + ": " + ex.toString(), ex);
        }

        // [IYI1002] Oiyokan Initializr End.
        log.info(OiyokanInitializrMessages.IYI1002);

        return null;
    }
}
