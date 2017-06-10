package ui;

import core.StreamWebOverlay;
import database.BobsDatabase;
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
        showGameRatingButton.setOnAction(event -> GameRatings.updateOverlay());


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

        TextField sqlInput = new TextField(); sqlInput.setPromptText("sql..");
        Button printSQLButton = new Button("Print SQL");
        printSQLButton.setOnAction(event -> new Thread(() -> PrettyPrinter.prettyPrintCachedRowSet(BobsDatabase.getCachedRowSetFromSQL(sqlInput.getText()), 200)).start());
        sqlInput.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) printSQLButton.fire();
        });


        Button showImage = new Button("Show Image");
        showImage.setOnAction(action -> StreamWebOverlay.showEmoteImage());

        Button hideImage = new Button("Hide Image");
        hideImage.setOnAction(action -> StreamWebOverlay.hideEmoteImage());

        GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER_LEFT);

        gridPane.add(showGameRatingButton, 0, 0);
        gridPane.add(toggleNameSelectorButton, 1, 0);
        gridPane.add(sqlInput, 0, 1);
        gridPane.add(printSQLButton, 1, 1);
        gridPane.add(showImage, 0, 2);
        gridPane.add(hideImage, 1, 2);

        tab.setContent(gridPane);
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
