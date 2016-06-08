package ui;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import twitch.TwitchChat;

public class MainWindow {
    private static TextField messageInput = new TextField();
    private static TextField urlInput = new TextField();

    public static void launch(Stage stage) {
        stage.setTitle("World's second worst ui");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        ObservableList<Tab> tabs = tabPane.getTabs();

        tabs.add(makeChatTab());
        tabs.add(makeTwitterTab());
        tabs.add(makeHostingTab());
        tabs.add(makeWebTab());

        Scene scene = new Scene(tabPane, 300, 250);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setScene(scene);
        stage.show();
    }

    static private Tab makeChatTab() {
        Tab tab = new Tab("Chat");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);

        Button sendMessageButton = new Button("Send");
        sendMessageButton.setOnAction(ActionEvent -> sendMessage());

        messageInput.setPromptText("Chat message");
        messageInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        HBox hbox = new HBox();
        hbox.getChildren().addAll(messageInput, sendMessageButton);
        hbox.setSpacing(10);

        Button chatOverlayButton = new Button("Toggle Chat Overlay");
        chatOverlayButton.setOnAction(ActionEvent -> ChatOverlay.toggle());

        grid.add(hbox, 0, 0);
        grid.add(chatOverlayButton, 0, 1);

        tab.setContent(grid);

        return tab;
    }

    private static Tab makeTwitterTab() {
        Tab tab = new Tab("Twitter");
        return tab;
    }

    private static Tab makeHostingTab() {
        Tab tab = new Tab("Twitch Hosting");
        return tab;
    }

    private static Tab makeWebTab() {
        Tab tab = new Tab("Web View");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setVgap(10);

        Button openUrlButton = new Button("Open");
        openUrlButton.setOnAction(ActionEvent -> openUrl());

        urlInput.setPromptText("Url");
        urlInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                openUrl();
            }
        });

        HBox hbox = new HBox();
        hbox.getChildren().addAll(urlInput, openUrlButton);
        hbox.setSpacing(10);

        grid.add(hbox, 0, 0);

        tab.setContent(grid);

        return tab;
    }

    private static void sendMessage() {
        TwitchChat.sendMessage(messageInput.getText());
        messageInput.clear();
    }

    private static void openUrl() {
        Browser.open(urlInput.getText());
    }
}
