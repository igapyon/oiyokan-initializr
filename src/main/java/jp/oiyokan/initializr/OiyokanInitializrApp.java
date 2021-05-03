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
import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.CaseUtils;
import org.apache.olingo.server.api.ODataApplicationException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import jp.oiyokan.OiyokanConstants;
import jp.oiyokan.common.OiyoCommonJdbcUtil;
import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;
import jp.oiyokan.dto.OiyoSettingsEntitySet;
import jp.oiyokan.dto.OiyoSettingsProperty;
import jp.oiyokan.oiyogen.OiyokanSettingsGenUtil;
import jp.oiyokan.util.OiyoEncryptUtil;

/**
 * Oiyokan Initializr.
 */
public class OiyokanInitializrApp {
    private static final Log log = LogFactory.getLog(OiyokanInitializrApp.class);

    private static final char[] CAMEL_DELIMITER_CHARS = new char[] { '.', '-', '_', '@' };

    public static void main(String[] args) {
        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");

        //////////////////////////////////////////////////////////
        // Setup basic settings info

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
            // [IYI1101] Traverse tables in database.
            log.info(OiyokanInitializrMessages.IYI1101);

            traverseTable(oiyoInfo, oiyoSettings);
        } catch (ODataApplicationException ex) {
            log.error("Fail to connect database. Check database settings: " + ex.toString());
        } catch (SQLException ex) {
            log.error("Fail to close database. Check database settings: " + ex.toString());
        }

        // [IYI1201] Tune settings info."
        log.info(OiyokanInitializrMessages.IYI1201);
        tuneSettings(oiyoInfo, oiyoSettings, isSfdcMode);

        //////////////////////////////////////////////////////////
        // Write settings info into oiyokan-settings.json

        try {
            // [IYI1301] Write settings info into `oiyokan-settings.json`.
            log.info(OiyokanInitializrMessages.IYI1301);
            targetJsonFile.getParentFile().mkdirs();
            writeToFile(oiyoSettings, targetJsonFile);
        } catch (IOException ex) {
            ex.printStackTrace();
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

        // [IYI1111] Connect to database.
        log.info(OiyokanInitializrMessages.IYI1111 + ": " + database.getName());
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            ResultSet rsTables = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "TABLE" });
            for (; rsTables.next();) {
                final String tableName = rsTables.getString("TABLE_NAME");

                try {
                    final OiyoSettingsEntitySet entitySet = OiyokanSettingsGenUtil.generateSettingsEntitySet(
                            connTargetDb, tableName, OiyokanConstants.DatabaseType.valueOf(database.getType()));
                    oiyoSettings.getEntitySet().add(entitySet);
                    entitySet.setDbSettingName(database.getName());
                    entitySet.setOmitCountAll(false);

                } catch (Exception ex) {
                    log.warn("Fail to read table: " + tableName);
                }
            }

            ResultSet rsViews = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "VIEW" });
            for (; rsViews.next();) {
                final String viewName = rsViews.getString("TABLE_NAME");

                try {
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
                    log.warn("Fail to read view: " + viewName);
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
            entitySet.setName(CaseUtils.toCamelCase(entitySet.getName(), true, CAMEL_DELIMITER_CHARS));
            entitySet.setJdbcStmtTimeout(30);

            for (OiyoSettingsProperty property : entitySet.getEntityType().getProperty()) {
                property.setName(CaseUtils.toCamelCase(property.getName(), true, CAMEL_DELIMITER_CHARS));

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
}
