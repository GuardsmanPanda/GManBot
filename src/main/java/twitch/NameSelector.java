package twitch;

import core.GBUtility;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class NameSelector extends ListenerAdapter {
    private static Map<String, LocalDateTime> activeChatUsers = new HashMap<>();
    private static final HashSet<String> selectedNames = new HashSet<>();
    private static final int ACTIVECHATDURATIONINMINUTES = 25;
    private static final Random random;

    static {
        selectedNames.add("GManBot");
        random = new Random();
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new KeyListener());
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void selectAndPrintTwitchName() {
        System.out.println("Selecting from " + activeChatUsers.size() + " active names in chat");
        activeChatUsers = activeChatUsers.entrySet().stream()
                .filter(e -> e.getValue().isAfter(LocalDateTime.now()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        System.out.println("Removed some names now " + activeChatUsers.size() + " left");
        List<String> names = new ArrayList<>();
        names.addAll(activeChatUsers.keySet());
        String winningName = names.get(random.nextInt(names.size()));

        selectedNames.add(winningName);
        activeChatUsers.remove(winningName);
        GBUtility.copyAndPasteString(winningName);

    }

    @Override
    public synchronized void onMessage(MessageEvent event) {
        String twitchName = GBUtility.getTwitchDisplayName(event);
        if (!selectedNames.contains(twitchName)) {
            activeChatUsers.put(twitchName, LocalDateTime.now().plusMinutes(ACTIVECHATDURATIONINMINUTES));
        }
    }

    private static class KeyListener implements NativeKeyListener{
        @Override
        public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
            if (nativeKeyEvent.getKeyCode() == 57420) selectAndPrintTwitchName();
        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {

        }

        @Override
        public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

        }
    }
}
