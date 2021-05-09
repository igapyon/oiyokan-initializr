/*
w * Copyright 2021 Toshiki Iga
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
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

/**
 * Oiyokan Initializr.
 */
public class OiyokanInitializrUtil {
    private static final Log log = LogFactory.getLog(OiyokanInitializrUtil.class);

    private static final char[] CAMEL_DELIMITER_CHARS = new char[] { '.', '-', '_', '@' };

    /**
     * Traverse tables.
     * 
     * @param oiyoInfo      OiyoInfo info for passphrase.
     * @param oiyoSettings  OiyoSettings info.
     * @param dbSettingName DB setting name.
     * @param isProcessView Process View. default:false.
     * @param mapNameFilter Filter of table name.
     * @throws SQLException              SQL exception occured.
     * @throws ODataApplicationException OData app exception occured.
     */
    public static void traverseTable(OiyoInfo oiyoInfo, OiyoSettings oiyoSettings, String dbSettingName,
            boolean isProcessView, boolean isReadWriteAccess, Map<String, String> mapNameFilter)
            throws SQLException, ODataApplicationException {
        // [IYI2101] Traverse tables in database.
        log.debug(OiyokanInitializrMessages.IYI2101);

        OiyoSettingsDatabase database = null;
        for (OiyoSettingsDatabase look : oiyoSettings.getDatabase()) {
            if (look.getName().equals(dbSettingName)) {
                database = look;
            }
        }
        if (database == null) {
            // [IYI2203] UNEXPECTED: 指定のDB settings が見つかりませんでした。
            log.error(OiyokanInitializrMessages.IYI2203 + ": " + dbSettingName);
            throw new ODataApplicationException(OiyokanInitializrMessages.IYI2203 + ": " + dbSettingName, 500,
                    Locale.ENGLISH);
        }

        // [IYI2111] Connect to database.
        log.info(OiyokanInitializrMessages.IYI2111 + ": " + database.getName());
        try (Connection connTargetDb = OiyoCommonJdbcUtil.getConnection(database)) {
            // [IYI2102] DEBUG: Traverse TABLE.
            log.debug(OiyokanInitializrMessages.IYI2102);
            ResultSet rsTables = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "TABLE" });
            for (; rsTables.next();) {
                final String tableName = rsTables.getString("TABLE_NAME");

                if (mapNameFilter != null && mapNameFilter.get(tableName) == null) {
                    // 処理対象外
                    log.trace("チェックされていないTABLEであるため処理をスキップ: " + tableName);
                    continue;
                }

                try {
                    // [IYI2112] DEBUG: Read table.
                    log.debug(OiyokanInitializrMessages.IYI2112 + ": " + tableName);
                    final OiyoSettingsEntitySet entitySet = OiyokanSettingsGenUtil.generateSettingsEntitySet(
                            connTargetDb, tableName, OiyokanConstants.DatabaseType.valueOf(database.getType()));
                    oiyoSettings.getEntitySet().add(entitySet);
                    entitySet.setDbSettingName(database.getName());
                    entitySet.setOmitCountAll(false);

                    // TABLE は Create, Update, Delete を指定の状態で設定.
                    entitySet.setCanCreate(isReadWriteAccess);
                    entitySet.setCanUpdate(isReadWriteAccess);
                    entitySet.setCanDelete(isReadWriteAccess);
                } catch (Exception ex) {
                    // [IYI2113] WARN: Fail to read table.
                    log.warn(OiyokanInitializrMessages.IYI2113 + ": " + tableName);
                }
            }

            if (isProcessView) {
                // [IYI2103] DEBUG: Traverse VIEW.
                log.debug(OiyokanInitializrMessages.IYI2103);
                ResultSet rsViews = connTargetDb.getMetaData().getTables(null, "%", "%", new String[] { "VIEW" });
                for (; rsViews.next();) {
                    final String viewName = rsViews.getString("TABLE_NAME");

                    if (mapNameFilter != null && mapNameFilter.get(viewName) == null) {
                        // 処理対象外
                        log.trace("チェックされていないVIEWであるため処理をスキップ: " + viewName);
                        continue;
                    }

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
    }

    /**
     * Tune settings info.
     * 
     * @param oiyoInfo                 OiyoInfo info for passphrase.
     * @param oiyoSettings             OiyoSettings info.
     * @param isFilterTreatNullAsBlank Adding Support of Salesforce.
     */
    public static void tuneSettings(OiyoInfo oiyoInfo, OiyoSettings oiyoSettings, boolean convertCamel,
            boolean isFilterTreatNullAsBlank) {
        // [IYI3101] Tune settings info.
        log.debug(OiyokanInitializrMessages.IYI3101);

        for (OiyoSettingsEntitySet entitySet : oiyoSettings.getEntitySet()) {
            entitySet.setName(adjustName(entitySet.getName(), convertCamel));
            entitySet.setJdbcStmtTimeout(30);

            final OiyoSettingsEntityType entityType = entitySet.getEntityType();
            entityType.setName(adjustName(entityType.getName(), convertCamel));

            for (int index = 0; index < entityType.getKeyName().size(); index++) {
                String keyName = entityType.getKeyName().get(index);
                entityType.getKeyName().set(index, adjustName(keyName, convertCamel));
            }

            for (OiyoSettingsProperty property : entityType.getProperty()) {
                property.setName(adjustName(property.getName(), convertCamel));

                if ("Edm.String".equals(property.getEdmType())) {
                    if (isFilterTreatNullAsBlank) {
                        property.setFilterTreatNullAsBlank(true);
                    }
                }
            }
        }

        Collections.sort(oiyoSettings.getEntitySet(), new Comparator<OiyoSettingsEntitySet>() {
            @Override
            public int compare(OiyoSettingsEntitySet o1, OiyoSettingsEntitySet o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }

    /**
     * Write settings info into `oiyokan-settings.json`.
     * 
     * @param oiyoSettings OiyoSettings info.
     * @return Target JSON string.
     * @throws IOException IO exception occured.
     */
    public static String oiyoSettings2String(final OiyoSettings oiyoSettings) throws IOException {
        // [IYI4101] Write settings info into `oiyokan-settings.json`.
        log.info(OiyokanInitializrMessages.IYI4101);

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.writeValue(writer, oiyoSettings);
        writer.flush();

        // [IYI4102] `oiyokan-settings.json` generated internally.
        log.info(OiyokanInitializrMessages.IYI4102);

        return writer.toString();
    }

    static String adjustName(String input, boolean convertCamel) {
        if (!convertCamel) {
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
            "src/main/resources/static/index.html", //
            "src/main/resources/static/static/css/bootstrap.min.css", //
            "src/main/resources/static/static/css/bootstrap.min.css.map", //
            "src/main/resources/static/static/js/bootstrap.min.js", //
            "src/main/resources/static/static/js/bootstrap.min.js.map", //
            "src/main/resources/static/static/js/jquery-3.6.0.min.js", //
            "src/main/resources/static/static/js/jquery-3.6.0.min.map", //
            "src/main/resources/templates/error.html", //
            "src/main/resources/application.properties", //
            "src/test/resources/logback-test.xml",//
    };

    public static byte[] packageZipFile(final File inputTemplateProjectDir, final String jsonString)
            throws IOException {
        // [IYI5101] Generate zip file into `oiyokan-demo.zip`.
        log.info(OiyokanInitializrMessages.IYI5101);

        final String ROOT_PATH = "./src/main/resources/oiyokan-web-template/";

        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final ZipArchiveOutputStream outZip = new ZipArchiveOutputStream(outStream);

        for (String fileName : TEMPLATE_FILES) {
            outZip.putArchiveEntry(new ZipArchiveEntry(fileName));
            IOUtils.copyAndCloseInput(new FileInputStream(ROOT_PATH + fileName), outZip);
            outZip.closeArchiveEntry();
        }

        if (jsonString != null) {
            outZip.putArchiveEntry(new ZipArchiveEntry("src/main/resources/oiyokan/oiyokan-settings.json"));
            IOUtils.copyAndCloseInput(new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8)), outZip);
            outZip.closeArchiveEntry();
        }

        outZip.flush();
        outZip.close();
        outStream.flush();
        return outStream.toByteArray();
    }
}
