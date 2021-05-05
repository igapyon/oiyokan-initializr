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
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.server.api.ODataApplicationException;

import jp.oiyokan.common.OiyoInfo;
import jp.oiyokan.dto.OiyoSettings;
import jp.oiyokan.dto.OiyoSettingsDatabase;

/**
 * Oiyokan Initializr.
 */
public class OiyokanInitializrMain {
    private static final Log log = LogFactory.getLog(OiyokanInitializrMain.class);

    public static void main(String[] args) {
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

        OiyoSettingsDatabase database = new OiyoSettingsDatabase();
        oiyoSettings.getDatabase().add(database);
        database.setName("mydbsetting1");
        database.setType("PostgreSQL"); // h2, PostgreSQL, MySQL, SQLSV2008, ORCL18
        database.setDescription("Tutorial db sample.");
        database.setJdbcDriver("org.postgresql.Driver"); // JDBC Driver class name.
        database.setJdbcUrl("jdbc:postgresql://localhost:5432/dvdrental"); // JDBC URL.
        database.setJdbcUser(""); // JDBC User.
        database.setJdbcPassPlain(""); // JDBC Password.

        boolean processView = false;
        boolean convertCamel = false; // EntitySetなどの名称を Camel case にするかどうか。通常は false で良い
        boolean isSfdcMode = true; // Support Salesforce or not.

        //////////////////////////////////////////////////////////
        // Process settings

        try {
            OiyokanInitializrUtil.traverseTable(oiyoInfo, oiyoSettings, processView, true/* TODO 現状読み書き許容 */);
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
            File fileZipTarget = new File("./target/generated-oiyokan/oiyokan-demo.zip");
            final byte[] zipFile = OiyokanInitializrUtil
                    .packageZipFile(new File("./src/main/resources/oiyokan-web-template"), jsonString);

            fileZipTarget.getParentFile().mkdirs();
            FileUtils.writeByteArrayToFile(fileZipTarget, zipFile);

            // [IYI5102] Check the `oiyokan-demo.zip`.
            log.info(OiyokanInitializrMessages.IYI5102 + ": " + fileZipTarget.getCanonicalPath());
        } catch (IOException ex) {
            // [IYI5201] ERROR: Fail to generate zip file.
            log.error(OiyokanInitializrMessages.IYI5201 + ": " + ex.toString(), ex);
        }

        // [IYI1002] Oiyokan Initializr End.
        log.info(OiyokanInitializrMessages.IYI1002);
    }
}
