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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Oiyokan Initializr を有効にするための空の Spring Boot App.
 */
@ComponentScan("jp.oiyokan")
@SpringBootApplication
public class OiyokanInitializrApp {
    /**
     * Spring Boot を起動するエントリポイント.
     * 
     * @param args コマンドライン引数.
     */
    public static void main(String[] args) {
        SpringApplication.run(OiyokanInitializrApp.class, args);
    }
}
