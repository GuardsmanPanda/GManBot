package twitch;

import com.google.common.io.Files;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.cap.EnableCapHandler;
import org.pircbotx.exception.IrcException;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.JoinEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.PrivateMessageEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.StreamSupport;

public class TwitchChat {
    private static boolean connected = false;
    private static final PircBotX bot;
    private static final String channel = "#guardsmanbob";

    // Configure bot
    static {
        Configuration config = new Configuration.Builder()
                .setName("BotManG").setMessageDelay(1900)
                .setOnJoinWhoEnabled(false)
                .setAutoNickChange(false)
                .setAutoReconnect(true)
                .addCapHandler(new EnableCapHandler("twitch.tv/membership"))
                .addCapHandler(new EnableCapHandler("twitch.tv/commands"))
                .addCapHandler(new EnableCapHandler("twitch.tv/tags"))
                .addListener(new ChatUtility())
                .addAutoJoinChannel(channel)
                .buildForServer("irc.chat.twitch.tv", 6667, getPassword());
        bot = new PircBotX(config);
    }


    /**
     * Attempts to connect to the chat server, this may take several seconds so this method should be called before
     * using the bot to send or receive messages.
     */
    public static void connect() {
        new Thread(() -> {
            try {
                bot.startBot();
            } catch (IrcException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }



    /**
     * Sends an action to the chat channel.
     * This method can silently fail for any reason.
     * @param action The action to send
     */
    public static synchronized void sendAction(String action) {
        bot.send().action(channel, action);
    }

    /**
     * Sends a message to the chat channel on twitch.
     * This method can silently fail for any reason.
     */
    public static synchronized void sendMessage(String message) {
        bot.send().message(channel, message);
    }

    public static synchronized void sendWhisper(String twitchName, String message) {
        bot.send().message(twitchName, message);
    }

    public static void addListener(ListenerAdapter listener) {
        bot.getConfiguration().getListenerManager().addListener(listener);
    }

    public static void removeListener(ListenerAdapter listener) {
        bot.getConfiguration().getListenerManager().removeListener(listener);
    }



    /**
     * Loads the twitchChat Oauth token from Data/twitchchatpassword.txt
     * @return the twitchChat OAuth token
     */
    private static String getPassword() {
        File passwordfile = new File("Data/twitchchatpassword.txt");
        if (passwordfile.exists()) {
            try {
                return Files.readFirstLine(passwordfile, Charset.defaultCharset());
            } catch (IOException e) {
                System.out.println("Error when attempting to read the twitchchatpassword text file!");
                e.printStackTrace();
            }
        } else {
            System.out.println("Twitchchatpassword must be located in Data/twitchchatpassword.txt");
        }
        return "";
    }

    private static class ChatUtility extends ListenerAdapter {

        @Override
        public void onConnect(ConnectEvent event) {
            connected = true;
            System.out.println("Connected to Twitch Chat Server");
        }

        @Override
        public void onJoin(JoinEvent event) {
            if (event.getUser().getNick().equalsIgnoreCase(bot.getNick())) {
                System.out.println("Joined Channel " + event.getChannel().getName());
            }
        }

        @Override
        public void onMessage(MessageEvent event) {
            /*
            String displayName = event.getTags().get("display-name");
            if (displayName.isEmpty()) {
                System.out.println("empty display name for message - " + event.getTags().toString() + " - nick: " + event.getUser().getNick());
                displayName = event.getUser().getNick();
            }

            String color = event.getTags().get("color");
            String message = "";

            for (String tag : event.getTags().keySet()) {
                message += "[" + tag;
                message += "=" + event.getTags().get(tag);
                message += "] ";
            }

            message += String.format("<%s> %s",
                    displayName,
                    event.getMessage()
                );
            System.out.println(message);
*/
        }


        @Override
        public void onPrivateMessage(PrivateMessageEvent event) {
            System.out.println("PM from:" + event.getUser().getNick() + " - " + event.getMessage());
        }
    }
}
