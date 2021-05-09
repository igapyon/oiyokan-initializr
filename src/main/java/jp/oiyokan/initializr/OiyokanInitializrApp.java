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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jp.oiyokan.OiyokanConstants;

/**
 * Oiyokan Initializr.
 */
@SpringBootApplication
public class OiyokanInitializrApp {
    private static final Log log = LogFactory.getLog(OiyokanInitializrApp.class);

    public static void main(String[] args) {
        // [IYI1001] Oiyokan Initializr Begin.
        log.info(OiyokanInitializrMessages.IYI1001 + ": (v" + OiyokanInitializrConstants.VERSION + ")");
        log.info("Oiyokan Lib: v" + OiyokanConstants.VERSION);

        SpringApplication.run(OiyokanInitializrApp.class, args);
    }
}
