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

/**
 * Oiyokan Initializr の定数.
 */
public class OiyokanInitializrMessages {
    public static final String IYI1001 = "[IYI1001] Oiyokan Initializr";
    public static final String IYI1002 = "[IYI1002] Oiyokan Library   ";
    public static final String IYI1009 = "[IYI1009] Download ZIP successfully.";
    public static final String IYI1010 = "[IYI1010] `oiyokan-settings.json` Generated successfully.";
    public static final String IYI1101 = "[IYI1101] Prepare database settings.";

    public static final String IYI2101 = "[IYI2101] Traverse tables in database.";
    public static final String IYI2102 = "[IYI2102] DEBUG: Traverse TABLE.";
    public static final String IYI2103 = "[IYI2103] DEBUG: Traverse VIEW.";
    public static final String IYI2111 = "[IYI2111] Connect to database.";
    public static final String IYI2112 = "[IYI2112] DEBUG: Read table.";
    public static final String IYI2113 = "[IYI2113] WARN: Fail to read table.";
    public static final String IYI2114 = "[IYI2114] DEBUG: Read view.";
    public static final String IYI2115 = "[IYI2115] WARN: Fail to read view.";
    public static final String IYI2201 = "[IYI2201] ERROR: Fail to connect database. Check database settings.";
    // IYI2202
    public static final String IYI2203 = "[IYI2203] UNEXPECTED: Specified DB settings NOT found.";

    public static final String IYI3101 = "[IYI3101] Tune settings info.";

    public static final String IYI4101 = "[IYI4101] Write settings info into `oiyokan-settings.json`.";
    public static final String IYI4102 = "[IYI4102] `oiyokan-settings.json` generated internally.";
    public static final String IYI4111 = "[IYI4111] Write settings info into `oiyokan-settings.json`.";
    public static final String IYI4112 = "[IYI4112] `oiyokan-settings.json` generated into `./target/generated-oiyokan/oiyokan-settings.json`.";
    public static final String IYI4201 = "[IYI4201] ERROR: Fail to generate json file.";
    public static final String IYI4202 = "[IYI4202] ERROR: Fail to generate json file.";

    public static final String IYI5101 = "[IYI5101] Generate zip file into `oiyokan-demo.zip`.";
    public static final String IYI5102 = "[IYI5102] Check the `oiyokan-demo.zip`.";
    public static final String IYI5201 = "[IYI5201] ERROR: Fail to generate zip file.";

    public static final String IYI6101 = "[IYI6101] INFO: Entry point `/initializr`(GET) clicked.";
    public static final String IYI6102 = "[IYI6102] INFO: Exit `/initializrExit`(GET) clicked.";
    public static final String IYI6103 = "[IYI6103] INFO: GENERATE `/initializr`(POST:generate) clicked.";
    public static final String IYI6104 = "[IYI6104] INFO: `/initializrEditEntity`(POST:edit) clicked.";
    public static final String IYI6105 = "[IYI6105] INFO: `/initializrEditEntity`(POST:applyChanges) clicked.";
    public static final String IYI6106 = "[IYI6106] INFO: `/initializrSelectEntity`(POST:new) clicked.";
    public static final String IYI6107 = "[IYI6107] INFO: `/initializrSelectEntity`(POST:applyEntitySelection) clicked.";
    public static final String IYI6108 = "[IYI6108] INFO: `/initializrSetupDatabase`(POST:new) clicked.";
    public static final String IYI6109 = "[IYI6109] INFO: `/initializrSetupDatabase`(POST:connTest) clicked.";
    public static final String IYI6110 = "[IYI6110] INFO: `/initializrSetupDatabase`(POST:applyDatabaseSettings) clicked.";
    public static final String IYI6111 = "[IYI6111] INFO: `/initializrSetupDatabase`(POST:preXXXXX) clicked.";
    public static final String IYI6112 = "[IYI6112] INFO: `/initializrSetupDatabase`(saveOiyokanSettingsJson) clicked.";

    public static final String IYI7101 = "[IYI7101] INFO: Setup database settings first.";
    public static final String IYI7102 = "[IYI7102] INFO: Session info of Oiyokan Initializr initialized.";
    public static final String IYI7103 = "[IYI7103] INFO: Select Entity.";
    public static final String IYI7104 = "[IYI7104] INFO: Entity selection applied.";
    public static final String IYI7105 = "[IYI7105] INFO: Connection test success.";
    public static final String IYI7106 = "[IYI7106] INFO: Database settings addded.";
    public static final String IYI7107 = "[IYI7107] INFO: Entity Change applied.";
    public static final String IYI7111 = "[IYI7111] INFO: Typical h2 preset is loaded.";
    public static final String IYI7112 = "[IYI7112] INFO: Typical PostgreSQL preset is loaded.";
    public static final String IYI7113 = "[IYI7113] INFO: Typical MySQL preset is loaded.";
    public static final String IYI7114 = "[IYI7114] INFO: Typical SQLSV2008 preset is loaded.";
    public static final String IYI7115 = "[IYI7115] INFO: Typical ORCL18 preset is loaded.";
    public static final String IYI7121 = "[IYI7121] WARN: Fail to test connect to database.";
    public static final String IYI7122 = "[IYI7122] DEBUG: Success to test connect to database.";
    public static final String IYI7131 = "[IYI7131] WARN: Same name database already exists.";
    public static final String IYI7132 = "[IYI7132] WARN: Same name EntitySet already exists.";

    public static final String IYI7501 = "[IYI7501] UNEXPECTED: EntitySet NOT found.";
    public static final String IYI7502 = "[IYI7502] UNEXPECTED: Database NOT found.";
    public static final String IYI7503 = "[IYI7503] UNEXPECTED: Fail to traverse tables.";
    public static final String IYI7504 = "[IYI7504] UNEXPECTED: Fail to process database.";
    public static final String IYI7505 = "[IYI7505] UNEXPECTED: Fail to select entity.";
    public static final String IYI7506 = "[IYI7506] UNEXPECTED: Fail to process database during select entity.";

}