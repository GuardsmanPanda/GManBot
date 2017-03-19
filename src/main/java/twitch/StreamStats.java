package twitch;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.util.HashSet;

public class StreamStats extends ListenerAdapter {
    private static final HashSet<String> emoticons = new HashSet<>();
    private static final Multiset<String> emoticonUsage = HashMultiset.create();

    static {
       emoticons.addAll(Twitch.getSubscriberEmoticons("guardsmanbob"));
    }

    public static void writeStreamStats() {

    }

    @Override
    public void onMessage(MessageEvent event) throws Exception {
        super.onMessage(event);
        String chatMessage = event.getMessage();
        emoticons.parallelStream()
                 .filter(chatMessage::contains)
                 .forEach(emoticonUsage::add);
    }
}
