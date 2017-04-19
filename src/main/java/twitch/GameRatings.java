package twitch;


import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.awt.*;

public class GameRatings extends ListenerAdapter{


    public static void main(String[] args) throws InterruptedException {
        Thread.sleep(5000);
        System.out.println(MouseInfo.getPointerInfo().getLocation().toString());
    }

    // !rategame !gamerating
    @Override
    public void onMessage(MessageEvent event) {

    }
}
