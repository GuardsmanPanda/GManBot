package UI;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import twitch.TwitchChat;

public class ChatOverlay {
    private static Stage stage = new Stage();
    private static TextArea textArea = new TextArea();
    private static Boolean initialized = false;

    private static TwitchChat.ChatListener listener = new TwitchChat.ChatListener() {
        public void onMessage(String message) {
            textArea.appendText(message + "\n");
        }
    };

    public static void show() {
        if(!initialized) {
            initialized = true;
            TwitchChat.addListener(listener);

            stage = new Stage();
            stage.setTitle("Chat overlay");

            textArea.setEditable(false);

            Scene scene = new Scene(textArea);

            stage.setScene(scene);
        }

        stage.show();
    }

    public static void hide() {
        stage.hide();
    }

    public static void shutDown() {
        TwitchChat.removeListener(listener);
    }

    public static void toggle() {
        if(stage.isShowing()) {
            hide();
        } else {
            show();
        }
    }
}
