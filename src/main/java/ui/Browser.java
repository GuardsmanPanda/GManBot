package ui;

import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class Browser {
    private static Stage stage = new Stage();

    // Open the given html resource (e.g. "/html/hello.html")
    public static void open(String resourcePath) {
        Browser b = new Browser();
        Class bClass = b.getClass();
        String path = bClass.getResource(resourcePath).toExternalForm();

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        webEngine.setOnError((err) -> System.out.println("Error: " + err));
        webEngine.setOnStatusChanged((status) -> System.out.println("Browser status: " + status));
        webEngine.load(path);

        Scene scene = new Scene(webView);
        stage.setScene(scene);

        stage.show();
    }
}
