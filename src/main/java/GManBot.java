import javafx.stage.Stage;
import javafx.application.Application;

public class GManBot extends Application {
    public static void main(String[] args) {
        // Configuration config = new Configuration.Builder().setName("GManTestBot").buildConfiguration();
        System.out.println("Its Alive!");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        MainWindow.buildUI(primaryStage);
    }
}
