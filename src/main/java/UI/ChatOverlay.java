package UI;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import twitch.TwitchChat;

public class ChatOverlay {
    private static Stage stage = new Stage();
    private static TextArea textArea = new TextArea();
    private static Boolean initialized = false;

    private static ListenerAdapter listener = new ListenerAdapter() {
        public void onMessage(MessageEvent event) {
            String text = event.getMessage();
            String name = event.getUser().getNick();
            textArea.appendText(String.format("%s: %s\n", name, text));
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
