package twitch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Path;

public class SongAnnouncer {

    public static void main(String[] args) {
        startSongAnnouncer();
    }

    public static void startSongAnnouncer() {
        try {
            HttpServer server = HttpServer.create( new InetSocketAddress(9100), 0);
            server.createContext("/songs", new songOverlayHttpHandler());
            server.createContext("/songPlaying", new songHttpHandler());
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void watchSongFile(Path songFileLocation) {
        songFileLocation.getParent()
    }

    static class songHttpHandler implements HttpHandler {
        private static int number = 0;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            number++;
            String response = "My little counter: " + number;

            System.out.println("Updating Song: " + response);
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            exchange.close();
        }
    }

    static class songOverlayHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(exchange.getRequestURI().toString());
            String response = "<html>" +
                    "<head>" +
                    "   <style>" +
                    "       body { font: 32px \"Helvetica Neue\",Helvetica,Arial,sans-serif; color: #FFFDF2; " +
                    "       font-weight: 600;" +
                    "       text-shadow: 0px 0px 20px #000000, 0px 0px 15px #000000, 0px 0px 15px #000000, 0px 0px 15px #000000, 0px 0px 15px #000000; }" +
                    "   </style>" +
                    "</head>" +
                    "<body>" +
                    "   <div id =\"song\"> " +
                    "       testing" +
                    "   </div>" +
                    "<script>" +
                    "   function runUpdates() {" +
                    "       var request = new XMLHttpRequest();" +
                    "       request.onreadystatechange = function() {" +
                    "           if (request.readyState === XMLHttpRequest.DONE && request.status === 200) {" +
                    "               updateSong(request.responseText);" +
                    "           }" +
                    "       };" +
                    "       request.open('GET', 'http://127.0.0.1:9100/songPlaying', true);" +
                    "       request.setRequestHeader(\"Content-Type\", \"text/plain\");" +
                    "       request.send();" +
                    "       setTimeout(runUpdates, 2000)" +
                    "   }" +
                    "   function updateSong(newSong) {" +
                    "       document.getElementById(\"song\").innerHTML=newSong;" +
                    "   } " +
                    "runUpdates();" +
                    "</script>" +
                    "</body>" +
                    "</html>";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            exchange.close();
        }
    }
}
