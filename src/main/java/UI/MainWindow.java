package ui;

import core.GBUtility;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import twitch.TwitchChat;
import twitch.TwitchChatStatistics;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Map;

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
        tabs.add(makeTopSongRatingsTab());

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

    private static Tab makeTopSongRatingsTab() {
        Tab topSongRatingsTab = new Tab("Chat Song Ratings");

        Label notPlayedLabel = new Label("Days Not Played: ");

        TextField notPlayedText = new TextField("5");
        notPlayedText.setPrefColumnCount(2);

        Label minRatingsLabel = new Label("Min Amount of Ratings: ");

        CheckBox everyoneBox = new CheckBox();

        TextField minRatingsText = new TextField("7");
        minRatingsText.setPrefColumnCount(2);

        VBox songListBox = new VBox();

        Button songButton = new Button("Top Songs!");
        songButton.setOnAction(event -> {
            int minNumberOfRatings = Integer.parseInt(minRatingsText.getCharacters().toString());
            int notPlayedDays = Integer.parseInt(notPlayedText.getCharacters().toString());
            fillSongListBox(TwitchChatStatistics.getTopRatedSongsByPeopleInChat(minNumberOfRatings, LocalDateTime.now().minusDays(notPlayedDays), everyoneBox.isSelected()), songListBox);
        });

        VBox vBox = new VBox();
        HBox songBox = new HBox();
        songBox.setAlignment(Pos.CENTER_LEFT);
        songBox.setSpacing(5);
        songBox.getChildren().addAll(songButton, notPlayedLabel, notPlayedText, minRatingsLabel, minRatingsText, everyoneBox);


        vBox.getChildren().add(songBox);
        vBox.getChildren().add(songListBox);

        topSongRatingsTab.setContent(vBox);
        return topSongRatingsTab;
    }

    private static void fillSongListBox(Map<String, Double> songData, VBox songListBox) {
        songListBox.getChildren().clear();
        HBox names = new HBox();

        Text songName = new Text("Song Name                             ");
        songName.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        names.getChildren().add(songName);

        Text songRating = new Text("Rating");
        songRating.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        names.getChildren().add(songRating);

        songListBox.getChildren().add(names);

        songData.keySet().stream()
                .sorted(Comparator.comparingDouble(songData::get).reversed())
                .limit(40)
                .forEach(song -> {
                    HBox songListing = new HBox();
                    Label songLabel = new Label(GBUtility.strictFill(song, 45));
                    songLabel.setPrefWidth(260.0);
                    songListing.getChildren().add(songLabel);
                    songListing.getChildren().add(new Label(String.format("%.2f",songData.get(song))));
                    songListBox.getChildren().add(songListing);
                });
    }

    private static void sendMessage() {
        TwitchChat.sendMessage(messageInput.getText());
        messageInput.clear();
    }



    private static void openUrl() {
        Browser.open(urlInput.getText());
    }
}
