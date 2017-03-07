package core;

import javafx.stage.Stage;
import javafx.application.Application;
import twitch.NameSelector;
import twitch.TwitchChat;

public class GManBot extends Application {
    public static void main(String[] args)  {
        try { Thread.sleep(100); } catch (InterruptedException e) { e.printStackTrace(); }


        //Ask the twitch handler to open the chat connection, this may take several seconds
        TwitchChat.connect();
        TwitchChat.addListener(new NameSelector());

        // Blocking call, do not include code past this point
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {
        ui.MainWindow.launch(primaryStage);
    }
}
