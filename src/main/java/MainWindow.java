import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import twitch.TwitchChat;

class MainWindow {
    private static TextField messageInput = new TextField();
    private static Button sendMessageButton = new Button();

    static void buildUI(Stage stage) {
        stage.setTitle("World's second worst UI");

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        sendMessageButton.setText("Send");

        HBox hbox = new HBox();
        hbox.getChildren().addAll(messageInput, sendMessageButton);
        hbox.setSpacing(10);

        grid.add(hbox, 0, 0);

        sendMessageButton.setOnAction(ActionEvent -> sendMessage());

        messageInput.setOnKeyPressed(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                sendMessage();
            }
        });

        Scene scene = new Scene(grid, 300, 250);

        stage.setOnCloseRequest(event -> {
            System.exit(0);
        });

        stage.setScene(scene);
        stage.show();
    }

    private static void sendMessage() {
        TwitchChat.sendMessage(messageInput.getText());
        messageInput.clear();
    }
}
