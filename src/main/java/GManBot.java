import org.pircbotx.Configuration;
import twitchchat.TwitchChatHandler;

/**
 * Created by Dons on 23-05-2016.
 *
 */
public class GManBot {
    public static void main(String[] arguments) {

        System.out.println("Its Alive!");

        TwitchChatHandler.getBot().send().message("#guardsmanbob", "I live!");

    }
}
