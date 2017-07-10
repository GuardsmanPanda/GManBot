package core;

import experiments.TextGeneration;
import javafx.application.Application;
import javafx.stage.Stage;
import twitch.*;
import utility.PrettyPrinter;
import webapi.Earthquakes;
import webapi.Reddit;
import webapi.SpaceLaunch;
import webapi.XKCD;

import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public class GManBot extends Application {
    public static void main(String[] args) {
        Instant startInstant = Instant.now();

        Locale.setDefault(Locale.forLanguageTag("da"));

        //Ask the twitch handler to open the chat connection, this may take several seconds
        TwitchChat.connect();
        TwitchChat.addListener(new SongAnnouncer(Paths.get("C:/Users/Dons/IdeaProjects/GManBot2/winamp.txt")));
        TwitchChat.addListener(new TwitchChatInformationGathering());
        TwitchChat.addListener(new TwitchChatModCommands());
        TwitchChat.addListener(new TwitchChatEasterEggs());
        TwitchChat.addListener(new TwitchChatExtras());
        TwitchChat.addListener(new TwitchChatStats());
        TwitchChat.addListener(new GameRatings());

        //Experimental features
        TwitchChat.addListener(new TextGeneration());

        //Start everything else
        TwitchPubSub.connect();
        XKCD.watchForNewComics();
        StreamWebOverlay.startOverlay();
        SpaceLaunch.startLaunchChecker();
        TwitchWebChatOverlay.startHttpService();
        Earthquakes.startQuakeWatch(6.2);
        Reddit.watchSubReddit("aww", 100, Reddit.TimeSpan.MONTH, 70);
        Reddit.watchSubReddit("all", 100, Reddit.TimeSpan.MONTH, 15);
        Reddit.watchSubReddit("physics", 50, Reddit.TimeSpan.YEAR, 85);
        Reddit.watchSubReddit("earthporn", 100, Reddit.TimeSpan.YEAR, 110);
        Reddit.watchSubReddit("machinelearning", 100, Reddit.TimeSpan.YEAR, 100);

        //Send startup information to chat
        String startUpTime = PrettyPrinter.timeStringFromDuration(Duration.between(startInstant, Instant.now()), true);
        TwitchChat.sendMessage("bobHype I Am Alive Again! -> Startup Time: " + startUpTime + ".");

        // Blocking call, do not include code past this point
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        ui.MainWindow.launch(primaryStage);
    }
}
