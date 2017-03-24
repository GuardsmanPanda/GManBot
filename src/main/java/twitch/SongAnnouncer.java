package twitch;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import core.BobsDatabase;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import javax.sql.rowset.CachedRowSet;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.List;

//TODO: Account for stream delay of ~16seconds
public class SongAnnouncer extends ListenerAdapter {
    private static final int STREAMDELAYINSECONDS = 10;
    private static String currentSong = "Guardsman Bob";
    private static String displayOnStreamSong = "Guardsman Bob";
    private static float displayOnStreamSongRating = 0f;

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
                if (rating < 1 ) rating = 1;
                if (rating > 11) rating = 11;
                if (tcm.getMessageContent().contains(" ")) songQuote = tcm.getMessageContent().substring(tcm.getMessageContent().indexOf(" ")).trim();

                BobsDatabase.addSongRating(tcm.userID, tcm.displayName, currentSong, rating, songQuote);
                displayOnStreamSongRating = getSongRating(displayOnStreamSong);
                System.out.println("Rating: " + rating + " Quote: " + songQuote);
            } catch (NumberFormatException nfe) {
                // Silently kill number format exceptions
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

    public static float getSongRating(String songName) {
        CachedRowSet songRatingSet = BobsDatabase.getCachedRowSetFromSQL("SELECT songRating FROM SongRatings WHERE songName = ?", songName);
        int numberOfRatings = 0;
        int totalRating = 0;
        try {
            while (songRatingSet.next()) {
                totalRating += songRatingSet.getInt("songRating");
                numberOfRatings++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (numberOfRatings == 0) return 0f;
        return (float) totalRating/numberOfRatings;
    }

    private static void songFileChange(String newSongName) {
        BobsDatabase.addSongRating("39837384", "GManBot", newSongName, 10, "none" );
        displayOnStreamSong = newSongName;
        displayOnStreamSongRating = getSongRating(newSongName);
        if (newSongName.equalsIgnoreCase("Guardsman Bob")) return;
        new Thread(() -> {
            try { Thread.sleep(1000 * STREAMDELAYINSECONDS); } catch (InterruptedException e) { e.printStackTrace(); }
            currentSong = newSongName;
            //TwitchChat.sendMessage <- new song etc.
        }).start();
    }


    private static void watchSongFile(Path songFileLocation) {
        new Thread(() -> {
            try {
            WatchService fileWatcher = FileSystems.getDefault().newWatchService();
            songFileLocation.getParent().register(fileWatcher, StandardWatchEventKinds.ENTRY_MODIFY);
            String lastSongNameInFile = "none";
            while (true) {
                WatchKey key = fileWatcher.take();
                for (WatchEvent event : key.pollEvents()) {
                    Path eventFilePath = (Path) event.context();
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY && eventFilePath.endsWith(songFileLocation.getFileName())) {
                        List<String> songFileLineArray = Files.readAllLines(songFileLocation, Charset.forName("windows-1252"));
                        //if for some reason the file is empty just ignore it.
                        if (songFileLineArray.size() == 0) break;
                        String newSongNameInFile = songFileLineArray.get(0);
                        if (!lastSongNameInFile.equalsIgnoreCase(newSongNameInFile)) {
                            System.out.println("New Song: " + newSongNameInFile);
                            songFileChange(newSongNameInFile);
                            lastSongNameInFile = newSongNameInFile;
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

    private static class songHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String hexColor = "#0044ff";
            if      (displayOnStreamSongRating >= 10f) hexColor = "#ff2200";
            else if (displayOnStreamSongRating >=  9f) hexColor = "#ff9900";
            else if (displayOnStreamSongRating >=  8f) hexColor = "#CCff00";
            else if (displayOnStreamSongRating >=  7f) hexColor = "#33ff66";
            else if (displayOnStreamSongRating >=  6f) hexColor = "#0099ff";

            String response = displayOnStreamSong + " <span style=\"color:" + hexColor + "\">" + String.format("%.2f", displayOnStreamSongRating) + "</span>";

            exchange.sendResponseHeaders(200, response.getBytes().length);
            exchange.getResponseBody().write(response.getBytes());
            exchange.getResponseBody().close();
            exchange.close();
        }
    }

    private static class songOverlayHttpHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println(exchange.getRequestURI().toString());
            String response = "<html>" +
                    "<head>" +
                    "   <style>" +
                    "       body { font: 30px \"Helvetica Neue\",Helvetica,Arial,sans-serif; color: #FFFFFF; " +
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
