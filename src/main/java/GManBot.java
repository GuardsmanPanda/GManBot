import org.pircbotx.Configuration;

/**
 * Created by Dons on 23-05-2016.
 *
 */
public class GManBot {
    public static void main(String[] arguments) {
        Configuration config = new Configuration.Builder().setName("GManTestBot").buildConfiguration();
        System.out.println("Its Alive!");

    }
}
