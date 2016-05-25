package UI;

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

    public static void launch(Stage stage) {
        stage.setTitle("World's second worst UI");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        ObservableList<Tab> tabs = tabPane.getTabs();

        tabs.add(makeBotTab());
        tabs.add(makeTwitterTab());

        Scene scene = new Scene(tabPane, 300, 250);
        stage.setOnCloseRequest(event -> System.exit(0));
        stage.setScene(scene);
        stage.show();
    }

    static private Tab makeBotTab() {
        Tab tab = new Tab();
        tab.setText("Bot");


        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        Button sendMessageButton = new Button();
        sendMessageButton.setOnAction(ActionEvent -> sendMessage());
        sendMessageButton.setText("Send");

        messageInput.setPromptText("Chat message");

        messageInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        HBox hbox = new HBox();
        hbox.getChildren().addAll(messageInput, sendMessageButton);
        hbox.setSpacing(10);

        grid.add(hbox, 0, 0);

        tab.setContent(grid);

        return tab;
    }

    private static Tab makeTwitterTab() {
        Tab tab = new Tab();
        tab.setText("Twitter");
        return tab;
    }

    private static void sendMessage() {
        TwitchChat.sendMessage(messageInput.getText());
        messageInput.clear();
    }
}
