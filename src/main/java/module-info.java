module gmanbot.main {
    requires jdk.incubator.httpclient;
    requires guava;
    requires pircbotx;
    requires jackson.databind;
    requires jackson.core;
    requires jackson.annotations;
    requires java.desktop;
    requires jnativehook;
    requires commons.logging;
    requires slf4j.api;
    requires java.logging;
    requires java.sql.rowset;
    requires yamlbeans;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires twitter4j.core;
    requires jdk.httpserver;
    exports core;
    exports twitch;
    exports ui;
}