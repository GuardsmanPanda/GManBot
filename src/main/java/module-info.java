module gmanbot.main {
    requires jdk.incubator.httpclient;
    requires guava;
    requires pircbotx;
    requires jackson.databind;
    requires jackson.core;
    requires jackson.annotations;
    requires java.desktop;
    requires jnativehook;
    requires slf4j.api;
    requires java.logging;
    requires java.sql.rowset;
    requires yamlbeans;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.controls;
    requires twitter4j.core;
    requires jdk.httpserver;
    requires javafx.web;
    requires Java.WebSocket;
    requires derby;
    requires nv.websocket.client;
    exports core;
    exports database;
    exports twitch;
    exports ui;
    exports utility;
}