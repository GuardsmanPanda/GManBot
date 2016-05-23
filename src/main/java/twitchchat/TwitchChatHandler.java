package twitchchat;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;

/**
 * Created by Dons on 23-05-2016.
 *
 */
public class TwitchChatHandler {
    private static final PircBotX bot;

    // Configure bot
    static {
        Configuration config = new Configuration.Builder()
                .setName("BotManG").setMessageDelay(1900)
                .addAutoJoinChannel("#guardsmanbob")
                .setAutoReconnect(true)
                .buildForServer("irc.chat.twitch.tv", 6667, getPassword());
        bot = new PircBotX(config);
    }

    public static PircBotX getBot() { return bot; }

    //TODO: Access Token goes here!!
    private static String getPassword() {
        return "oauth:j2mcko4wa9dnafqja3g4y10288mwta";
    }
}
