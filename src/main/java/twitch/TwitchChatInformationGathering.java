package twitch;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.events.NoticeEvent;
import org.pircbotx.hooks.events.UnknownEvent;

import java.util.HashSet;

public class TwitchChatInformationGathering extends ListenerAdapter {
    private static final HashSet<String> emoticons = new HashSet<>();
    private static final Multiset<String> emoticonUsage = HashMultiset.create();

    static {
       emoticons.addAll(Twitch.getSubscriberEmoticons("guardsmanbob"));
    }


    @Override
    public void onNotice(NoticeEvent event) {
        System.out.println("NOTICE ->> " + event.getMessage() + " <> " + event.getNotice());
    }


    @Override
    public void onUnknown(UnknownEvent event)  {
        System.out.println("UNKNOWN EVENT " + event.toString());
    }

    @Override
    public void onMessage(MessageEvent event)  {
        TwitchChatMessage chatMessage = new TwitchChatMessage(event);

        emoticons.stream()
                 .filter(chatMessage.message::contains)
                 .forEach(emoticonUsage::add);
    }
}
