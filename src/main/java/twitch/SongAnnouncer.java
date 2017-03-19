package twitch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.*;

public class SongAnnouncer {
    private static String currentSong = "Guardsman Bob";

    public static void main(String[] args) {
        startSongAnnouncer();
        watchSongFile(Paths.get("C:/Users/Dons/IdeaProjects/GManBot2/winamp.txt"));
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
        try {
            WatchService fileWatcher = FileSystems.getDefault().newWatchService();
            songFileLocation.getParent().register(fileWatcher, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey key = fileWatcher.take();
                for (WatchEvent event : key.pollEvents()) {
                    Path eventFilePath = (Path) event.context();
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && eventFilePath.endsWith(songFileLocation.getFileName())) {
                        String songName = Files.readAllLines(songFileLocation).get(0);
                        if (!currentSong.equalsIgnoreCase(songName)) {
                            System.out.println("New Song: " + songName);
                            currentSong = songName;
                        }
                    }
                }
                if (key.reset() == false) {
                    System.out.println("Song file Watching went horrible wrong!");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class songHttpHandler implements HttpHandler {
        private static int number = 0;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String response = currentSong;

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
                    "       font-weight: 700;" +
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
