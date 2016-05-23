import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

class MainWindow {
    static void buildUI(Stage stage) {
        stage.setTitle("Hello world!");
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);

        Button btn = new Button();
        btn.setText("Hello World!");

        btn.setOnAction(ActionEvent ->
            System.out.println("Hello world!")
        );

        grid.add(btn, 0, 0);

        Scene scene = new Scene(grid, 300, 250);
        stage.setScene(scene);
        stage.show();
    }
}
