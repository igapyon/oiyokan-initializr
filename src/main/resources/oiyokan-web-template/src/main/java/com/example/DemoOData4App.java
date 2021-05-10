package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jp.oiyokan.OiyokanConstants;

@SpringBootApplication
public class DemoOData4App {
    public static void main(String[] args) {
        System.err.println("oiyokan-demo app started (w/Oiyokan v" + OiyokanConstants.VERSION + ")");

        SpringApplication.run(DemoOData4App.class, args);
    }
}
