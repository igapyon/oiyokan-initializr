/*
 * Copyright 2021 Toshiki Iga
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.oiyokan.initializr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.CaseUtils;
import org.apache.olingo.server.api.ODataApplicationException;
import org.h2.util.IOUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsEntityType;
import jp.oiyokan.dto.OiyoSettingsProperty;
import jp.oiyokan.oiyogen.OiyokanSettingsGenUtil;
import jp.oiyokan.util.OiyoEncryptUtil;

/**
 * Oiyokan Initializr.
 */
public class OiyokanInitializrApp {
    private static final Log log = LogFactory.getLog(OiyokanInitializrApp.class);

    /**
     * EntitySetなどの名称を Camel case にするかどうか。通常は false で良い。
     */
    private static final boolean CONVERT_CAMEL = false;
    private static final char[] CAMEL_DELIMITER_CHARS = new char[] { '.', '-', '_', '@' };

    public static void main(String[] args) {
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

        OiyoSettingsDatabase database = new OiyoSettingsDatabase();
        oiyoSettings.getDatabase().add(database);
        database.setName("mydbsetting1");
        database.setType("PostgreSQL"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        database.setDescription("Tutorial db sample.");
        database.setJdbcDriver("org.postgresql.Driver"); // JDBC Driver class name.
        database.setJdbcUrl("jdbc:postgresql://localhost:5432/dvdrental"); // JDBC URL.
        database.setJdbcUser(""); // JDBC User.
        database.setJdbcPassPlain(""); // JDBC Password.

        boolean isSfdcMode = true; // Support Salesforce or not.

        // Target file name to generate.
        File targetJsonFile = new File("./target/generated-oiyokan/oiyokan-settings.json");

        //////////////////////////////////////////////////////////
        // Process settings

        try {
            // [IYI2101] Traverse tables in database.
            log.info(OiyokanInitializrMessages.IYI2101);

            traverseTable(oiyoInfo, oiyoSettings);
        } catch (ODataApplicationException ex) {
            // [IYI2201] ERROR: Fail to connect database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2201 + ": " + ex.toString(), ex);
        } catch (SQLException ex) {
            // [IYI2202] ERROR: Fail to close database. Check database settings.
            log.error(OiyokanInitializrMessages.IYI2202 + ": " + ex.toString(), ex);
        }

        // [IYI3101] Tune settings info.
        log.info(OiyokanInitializrMessages.IYI3101);
        tuneSettings(oiyoInfo, oiyoSettings, isSfdcMode);

        //////////////////////////////////////////////////////////
        // Write settings info into oiyokan-settings.json

        try {
            // [IYI4101] Write settings info into `oiyokan-settings.json`.
            log.info(OiyokanInitializrMessages.IYI4101);
            targetJsonFile.getParentFile().mkdirs();
            writeToFile(oiyoSettings, targetJsonFile);

            // [IYI4102] Check the `oiyokan-settings.json`.
            log.info(OiyokanInitializrMessages.IYI4102 + ": " + targetJsonFile.getCanonicalPath());
        } catch (IOException ex) {
            // [IYI4201] ERROR: Fail to generate json file.
            log.error(OiyokanInitializrMessages.IYI4201 + ": " + ex.toString(), ex);
        }

        try {
            // [IYI5101] Generate zip file into `oiyokan-demo.zip`.
            log.info(OiyokanInitializrMessages.IYI5101);
            File fileZipTarget = new File("./target/generated-oiyokan/oiyokan-demo.zip");
            packageToZipFile(new File("./src/main/resources/oiyokan-web-template"), targetJsonFile, fileZipTarget);

            // [IYI5102] Check the `oiyokan-demo.zip`.
            log.info(OiyokanInitializrMessages.IYI5102 + ": " + fileZipTarget.getCanonicalPath());
        } catch (IOException ex) {
            // [IYI5201] ERROR: Fail to generate zip file.
            log.error(OiyokanInitializrMessages.IYI5201 + ": " + ex.toString(), ex);
        }

        // [IYI1002] Oiyokan Initializr End.
        log.info(OiyokanInitializrMessages.IYI1002);
    }

    /**
     * Traverse tables.
     * 
     * @param oiyoInfo     OiyoInfo info for passphrase.
     * @param oiyoSettings OiyoSettings info.
     * @throws SQLException              SQL exception occured.
     * @throws ODataApplicationException OData app exception occured.
     */
    static void traverseTable(OiyoInfo oiyoInfo, OiyoSettings oiyoSettings)
            throws SQLException, ODataApplicationException {
        OiyoSettingsDatabase database = oiyoSettings.getDatabase().get(0);

        // [IYI2111] Connect to database.
        log.info(OiyokanInitializrMessages.IYI2111 + ": " + database.getName());
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            // [IYI2102] DEBUG: Traverse TABLE.
            log.debug(OiyokanInitializrMessages.IYI2102);
            ResultSet rsTables = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "TABLE" });
            for (; rsTables.next();) {
                final String tableName = rsTables.getString("TABLE_NAME");

                try {
                    // [IYI2112] DEBUG: Read table.
                    log.debug(OiyokanInitializrMessages.IYI2112 + ": " + tableName);
                    final OiyoSettingsEntitySet entitySet = OiyokanSettingsGenUtil.generateSettingsEntitySet(
                            connTargetDb, tableName, OiyokanConstants.DatabaseType.valueOf(database.getType()));
                    oiyoSettings.getEntitySet().add(entitySet);
                    entitySet.setDbSettingName(database.getName());
                    entitySet.setOmitCountAll(false);

                } catch (Exception ex) {
                    // [IYI2113] WARN: Fail to read table.
                    log.warn(OiyokanInitializrMessages.IYI2113 + ": " + tableName);
                }
            }

            // [IYI2103] DEBUG: Traverse VIEW.
            log.debug(OiyokanInitializrMessages.IYI2103);
            ResultSet rsViews = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "VIEW" });
            for (; rsViews.next();) {
                final String viewName = rsViews.getString("TABLE_NAME");

                try {
                    // [IYI2114] DEBUG: Read view.
                    log.debug(OiyokanInitializrMessages.IYI2114 + ": " + viewName);
                    final OiyoSettingsEntitySet entitySet = OiyokanSettingsGenUtil.generateSettingsEntitySet(
                            connTargetDb, viewName, OiyokanConstants.DatabaseType.valueOf(database.getType()));
                    oiyoSettings.getEntitySet().add(entitySet);
                    entitySet.setDbSettingName(database.getName());
                    entitySet.setOmitCountAll(true);

                    // VIEWは Create, Update, Delete を抑止.
                    entitySet.setCanCreate(false);
                    entitySet.setCanUpdate(false);
                    entitySet.setCanDelete(false);
                } catch (Exception ex) {
                    // [IYI2115] WARN: Fail to read view.
                    log.warn(OiyokanInitializrMessages.IYI2115 + ": " + viewName);
                }
            }
        }
    }

    /**
     * Tune settings info.
     * 
     * @param oiyoInfo     OiyoInfo info for passphrase.
     * @param oiyoSettings OiyoSettings info.
     * @param isSfdcMode   Adding Support of Salesforce.
     */
    static void tuneSettings(OiyoInfo oiyoInfo, OiyoSettings oiyoSettings, boolean isSfdcMode) {
        OiyoSettingsDatabase database = oiyoSettings.getDatabase().get(0);

        // データベース設定を暗号化。もとのプレーンテキストパスワードは除去.
        database.setJdbcPassEnc(OiyoEncryptUtil.encrypt(database.getJdbcPassPlain(), oiyoInfo.getPassphrase()));
        database.setJdbcPassPlain(null);

        for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
            entitySet.setName(adjustName(entitySet.getName()));
            entitySet.setJdbcStmtTimeout(30);

            final OiyoSettingsEntityType entityType = entitySet.getEntityType();
            entityType.setName(adjustName(entityType.getName()));

            for (int index = 0; index < entityType.getKeyName().size(); index++) {
                String keyName = entityType.getKeyName().get(index);
                entityType.getKeyName().set(index, adjustName(keyName));
            }

            for (OiyoSettingsProperty property : entityType.getProperty()) {
                property.setName(adjustName(property.getName()));

                if ("Edm.String".equals(property.getEdmType())) {
                    if (isSfdcMode) {
                        property.setFilterTreatNullAsBlank(true);
                    }
                }
            }
        }

        Collections.sort(oiyoSettings.getEntitySet(), new Comparator<OiyoSettingsEntitySet>() {
            @Override
            public int compare(OiyoSettingsEntitySet o1, OiyoSettingsEntitySet o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    /**
     * Write settings info into `oiyokan-settings.json`.
     * 
     * @param oiyoSettings   OiyoSettings info.
     * @param targetJsonFile Target JSON file to generate.
     * @throws IOException IO exception occured.
     */
    static void writeToFile(final OiyoSettings oiyoSettings, final File targetJsonFile) throws IOException {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(writer, oiyoSettings);
        writer.flush();

        FileUtils.writeStringToFile(targetJsonFile, writer.toString(), "UTF-8");
    }

    static String adjustName(String input) {
        if (!CONVERT_CAMEL) {
            for (int index = 0; index < CAMEL_DELIMITER_CHARS.length; index++) {
                input = input.replaceAll("[" + CAMEL_DELIMITER_CHARS[index] + "]", "_");
            }
            return input;
        } else {
            return CaseUtils.toCamelCase(input, true, CAMEL_DELIMITER_CHARS);
        }
    }

    private static final String[] TEMPLATE_FILES = new String[] { "pom.xml", "system.properties", //
            "src/main/java/com/example/DemoOData4App.java", //
            "src/main/java/com/example/DemoOData4Register.java", //
            "src/main/resources/oiyokan/memo.txt", //
            "src/main/resources/static/error.html", //
            "src/main/resources/static/index.html", //
            "src/main/resources/application.properties", //
            "src/test/resources/logback-test.xml",//
    };

    static void packageToZipFile(final File inputTemplateProjectDir, final File inputJsonFile, final File targetZipFile)
            throws IOException {
        final String ROOT_PATH = "./src/main/resources/oiyokan-web-template/";

        final ZipArchiveOutputStream outZip = new ZipArchiveOutputStream(targetZipFile);

        for (String fileName : TEMPLATE_FILES) {
            outZip.putArchiveEntry(new ZipArchiveEntry(fileName));
            IOUtils.copyAndCloseInput(new FileInputStream(ROOT_PATH + fileName), outZip);
            outZip.closeArchiveEntry();
        }

        outZip.putArchiveEntry(new ZipArchiveEntry("src/main/resources/oiyokan/oiyokan-settings.json"));
        IOUtils.copyAndCloseInput(new FileInputStream(inputJsonFile), outZip);
        outZip.closeArchiveEntry();

        outZip.flush();
        outZip.close();
    }
}
