package ui;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import core.StreamWebOverlay;
import database.BobsDatabase;
import database.BobsDatabaseHelper;
import database.SongDatabase;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import twitch.GameRatings;
import twitch.NameSelector;
import twitch.TwitchChat;
import utility.GBUtility;
import utility.PrettyPrinter;
import webapi.Twitchv5;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;

public class MainWindow {
    private static TextField messageInput = new TextField();
    private static TextField urlInput = new TextField();
    private static Robot robot;

    static {
        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); }
    }



    public static void launch(Stage stage) {
        stage.setTitle("World's second worst ui");

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        ObservableList<Tab> tabs = tabPane.getTabs();

        tabs.add(makeChatTab());
        tabs.add(makeTwitterTab());
        tabs.add(makeUtilityTab());
        tabs.add(makeWebTab());
        tabs.add(makeTopSongRatingsTab());

        Scene scene = new Scene(tabPane, 400, 350);
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



    private static Tab makeUtilityTab() {
        Tab tab = new Tab("Utility");

        Button showGameRatingButton = new Button("Show Game Rating");
        showGameRatingButton.setOnAction(event -> GameRatings.updateOverlay(Twitchv5.getGameName()));

        ToggleButton toggleNameSelectorButton = new ToggleButton("NameSelector Disabled");
        toggleNameSelectorButton.setOnAction(event -> {
            if (!toggleNameSelectorButton.isSelected()) {
                toggleNameSelectorButton.setText("NameSelector Disabled");
                NameSelector.disableNameSelector();
            } else {
                toggleNameSelectorButton.setText("NameSelector Enabled");
                NameSelector.enableNameSelector();
            }
        });

        HBox hBox1 = new HBox();
        hBox1.getChildren().addAll(showGameRatingButton, toggleNameSelectorButton);


        TextField sqlInput = new TextField(); sqlInput.setPromptText("sql..");
        sqlInput.setMaxWidth(Double.MAX_VALUE);
        Button printSQLButton = new Button("Print SQL");
        printSQLButton.setOnAction(event -> new Thread(() -> PrettyPrinter.prettyPrintCachedRowSet(BobsDatabase.getCachedRowSetFromSQL(sqlInput.getText()), 200)).start());
        sqlInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) printSQLButton.fire();
        });

        HBox hBox2 = new HBox();
        hBox2.getChildren().addAll(sqlInput, printSQLButton);


        TextField donateNameField = new TextField(); donateNameField.setPromptText("twitchName..");
        TextField donateAmountField = new TextField(); donateAmountField.setPromptText("Cents..");
        Button addDonationButton = new Button("Add Donation!");
        addDonationButton.setOnAction(event -> {
            String userID = BobsDatabaseHelper.getTwitchUserID(donateNameField.getText());
            int cents = Integer.parseInt(donateAmountField.getText());
            //TODO: Make a fancy announcement in the overlay before adding donation
            if (!userID.isEmpty()) {
                System.out.println("Donation From " + userID + ", cents: " + cents);
                BobsDatabaseHelper.addCentsDonated(userID, cents);
            }
        });

        HBox hBox3 = new HBox();
        hBox3.getChildren().addAll(donateNameField, donateAmountField, addDonationButton);

        TextField countdownText = new TextField(); countdownText.setPromptText("Countdown Text..");
        TextField countdownSeconds = new TextField("0"); countdownSeconds.setPrefColumnCount(2);
        TextField countdownMinutes = new TextField("0"); countdownMinutes.setPrefColumnCount(2);
        TextField countdownHours = new TextField("0"); countdownHours.setPrefColumnCount(2);
        TextField countdownX = new TextField("1630"); countdownX.setPrefColumnCount(3);
        TextField countdownY = new TextField("150"); countdownY.setPrefColumnCount(2);
        Button countdownButton = new Button("Start Countdown");
        countdownButton.setOnAction(event -> {
            int seconds = Integer.parseInt(countdownSeconds.getText()) + Integer.parseInt(countdownMinutes.getText()) * 60 + Integer.parseInt(countdownHours.getText()) * 3600;
            ObjectNode root = JsonNodeFactory.instance.objectNode();
            root.put("type", "countdownStart");
            root.put("text", countdownText.getText());
            root.put("seconds", seconds);
            root.put("x", Integer.parseInt(countdownX.getText()));
            root.put("y", Integer.parseInt(countdownY.getText()));
            root.put("image", "/OBSOverlay/sign1.png");
            StreamWebOverlay.sendJsonToOverlay(root);
        });
        Button introButton = new Button("Intro");
        introButton.setOnAction(event -> { //2:07 + 3:45 + 3:12 = 9:04 -> 544sec
            int seconds = 4 + 544 + Integer.parseInt(countdownSeconds.getText()) + Integer.parseInt(countdownMinutes.getText()) * 60 + Integer.parseInt(countdownHours.getText()) * 3600;
            ObjectNode root = JsonNodeFactory.instance.objectNode();
            root.put("type", "countdownStart");
            root.put("text", "Stream Starting");
            root.put("seconds", seconds);
            root.put("x", 20);
            root.put("y", 150);
            root.put("image", "/OBSOverlay/sign2.png");
            StreamWebOverlay.sendJsonToOverlay(root);
        });
        Button stopCountdownButton = new Button("Stop");
        stopCountdownButton.setOnAction(event -> {
            ObjectNode root = JsonNodeFactory.instance.objectNode();
            root.put("type", "countdownStop");
            StreamWebOverlay.sendJsonToOverlay(root);
        });


        HBox hBox4 = new HBox();
        hBox4.getChildren().addAll(countdownText, countdownHours, countdownMinutes, countdownSeconds, countdownButton, introButton, stopCountdownButton, countdownX, countdownY);


        VBox vbox = new VBox();
        vbox.getChildren().addAll(hBox1, hBox2, hBox3, hBox4);
        tab.setContent(vbox);
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
        songListBox.setAlignment(Pos.CENTER_LEFT);

        Button songButton = new Button("Top Songs!");
        songButton.setOnAction(event -> {
            int minNumberOfRatings = Integer.parseInt(minRatingsText.getCharacters().toString());
            int notPlayedDays = Integer.parseInt(notPlayedText.getCharacters().toString());
            fillSongListBox(SongDatabase.getSongRatingMap(minNumberOfRatings, LocalDate.now().minusDays(notPlayedDays), everyoneBox.isSelected()), songListBox);
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

        Text songName = new Text("            Song Name                           ");
        songName.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        names.getChildren().add(songName);

        Text songRating = new Text("Rating");
        songRating.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
        names.getChildren().add(songRating);

        songListBox.getChildren().add(names);

        final int[] number = {0};
        songData.keySet().stream()
                .sorted(Comparator.comparingDouble(songData::get).reversed())
                .limit(40)
                .forEachOrdered(song -> {
                    HBox songListing = new HBox();
                    Label songNumber = new Label(((number[0] < 9) ? "   " : " ") + ++number[0] + " ");
                    Label songLabel = new Label("  " + GBUtility.strictFill(song, 45));
                    songLabel.setPrefWidth(260.0);
                    Label ratingLabel = new Label(String.format("%.2f",songData.get(song)));

                    Button queueButton = new Button("Queue");
                    queueButton.setMaxHeight(songLabel.getHeight());
                    queueButton.setStyle("-fx-padding: 1;");
                    queueButton.setOnAction(event -> {
                        int startX = MouseInfo.getPointerInfo().getLocation().x;
                        int startY = MouseInfo.getPointerInfo().getLocation().y;
                        robot.mouseMove(3254, 216);
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
                        robot.keyPress(KeyEvent.VK_CONTROL);
                        robot.keyPress(KeyEvent.VK_A);
                        robot.keyRelease(KeyEvent.VK_CONTROL);
                        robot.keyRelease(KeyEvent.VK_A);
                        try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
                        GBUtility.copyAndPasteString(song);
                        try { Thread.sleep(500); } catch (InterruptedException e) { e.printStackTrace(); }
                        robot.mouseMove(3259, 252);
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
                        robot.mouseMove(3198, 702);
                        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        try { Thread.sleep(50); } catch (InterruptedException e) { e.printStackTrace(); }
                        robot.mouseMove(startX, startY);
                    });
                    songListing.getChildren().add(songNumber);
                    songListing.getChildren().add(queueButton);
                    songListing.getChildren().add(songLabel);
                    songListing.getChildren().add(ratingLabel);


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
