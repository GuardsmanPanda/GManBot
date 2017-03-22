package twitch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import core.BobsDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.List;

//TODO: Account for stream delay of ~16seconds
public class SongAnnouncer extends ListenerAdapter {
    private static final int STREAMDELAYINSECONDS = 15;
    private static String currentSong = "Guardsman Bob";

    public SongAnnouncer(Path songFilePath) {
        startSongAnnouncer();
        watchSongFile(songFilePath);
    }


    @Override
    public void onMessage(MessageEvent event) {
        if (event.getMessage().toLowerCase().startsWith("!rate ")) {
            TwitchChatMessage tcm = new TwitchChatMessage(event);
            String songQuote = "none";
            System.out.println("Rating from " + tcm.displayName);
            try {
                int rating = Integer.parseInt(tcm.getMessageContent().split(" ")[0]);
                if (tcm.getMessageContent().contains(" ")) songQuote = tcm.getMessageContent().substring(tcm.getMessageContent().indexOf(" ")).trim();

                BobsDatabase.addSongRating(tcm.userID, tcm.displayName, currentSong, rating, songQuote);
                System.out.println("Rating: " + rating + " Quote: " + songQuote);
            } catch (NumberFormatException nfe) {
                System.out.println("No rating found!!");
                nfe.printStackTrace();
            }
        }
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
        new Thread(() -> {
            try {
            WatchService fileWatcher = FileSystems.getDefault().newWatchService();
            songFileLocation.getParent().register(fileWatcher, StandardWatchEventKinds.ENTRY_MODIFY);
            while (true) {
                WatchKey key = fileWatcher.take();
                for (WatchEvent event : key.pollEvents()) {
                    Path eventFilePath = (Path) event.context();
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && eventFilePath.endsWith(songFileLocation.getFileName())) {
                        List<String> songFileLineArray = Files.readAllLines(songFileLocation, Charset.forName("windows-1252"));
                        //if for some reason the file is empty just ignore it.
                        if (songFileLineArray.size() == 0) break;

                        String songName = songFileLineArray.get(0);
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
        }} ).start();
    }

    static class songHttpHandler implements HttpHandler {
        private static int number = 0;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String dummyRating = "9,21";
            String response = currentSong + " <span style=\"color:#82CAFA\">" + dummyRating + "</span>";

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
                    "       body { font: 32px \"Helvetica Neue\",Helvetica,Arial,sans-serif; color: #FFFFFF; " +
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
