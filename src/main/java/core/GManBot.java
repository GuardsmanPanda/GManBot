package core;

import javafx.application.Application;
import javafx.stage.Stage;
import twitch.*;
import webapi.SpaceLaunch;

import java.nio.file.Paths;

public class GManBot extends Application {
    public static void main(String[] args) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //Ask the twitch handler to open the chat connection, this may take several seconds
        TwitchChat.connect();
        TwitchChat.addListener(new TwitchChatInformationGathering());
        TwitchChat.addListener(new TwitchChatExtras());
        TwitchChat.addListener(new TwitchChatStats());
        TwitchChat.addListener(new SongAnnouncer(Paths.get("C:/Users/Dons/IdeaProjects/GManBot2/winamp.txt")));
        TwitchChat.addListener(new GameRatings());

        //Start everything else
        TwitchPubSub.connect();
        StreamWebOverlay.startOverlay();
        SpaceLaunch.startLaunchChecker();
        TwitchWebChatOverlay.startHttpService();

        // Blocking call, do not include code past this point
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ui.MainWindow.launch(primaryStage);
    }
}
