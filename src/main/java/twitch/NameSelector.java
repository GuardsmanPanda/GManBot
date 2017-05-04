package twitch;

import database.BobsDatabaseHelper;
import javafx.scene.input.KeyCode;
import utility.GBUtility;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.stream.Collectors;

public class NameSelector {
    private static final HashSet<String> selectedUserIDs = new HashSet<>();
    private static final Random random = new Random();

    static {
        //ADD GManBot userID so that it wont get picked.
        selectedUserIDs.add("39837384");
    }

    public static void enableNameSelector() {
        LogManager.getLogManager().getLogger("").setLevel(Level.OFF);
        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new KeyListener());
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public static void disableNameSelector() {
        LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public synchronized static void selectAndPrintTwitchName() {
        List<String> activeUserIDs = TwitchChat.getActiveUserIDsInChannel(Duration.ofMinutes(30)).stream()
                .filter(userID -> !selectedUserIDs.contains(userID))
                .collect(Collectors.toList());
        System.out.println("Found " + activeUserIDs.size() + " Active user not already selected");
        if (activeUserIDs.size() == 0) return;

        String winningUserID = activeUserIDs.get(random.nextInt(activeUserIDs.size()));
        selectedUserIDs.add(winningUserID);
        System.out.println(winningUserID + " Won, printing " + BobsDatabaseHelper.getDisplayName(winningUserID));
        GBUtility.copyAndPasteString(BobsDatabaseHelper.getDisplayName(winningUserID));
    }

    private static class KeyListener implements NativeKeyListener {
        @Override
        public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {

        }

        @Override
        public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) {
            if (nativeKeyEvent.getKeyCode() == 57420) selectAndPrintTwitchName();
            if (nativeKeyEvent.getKeyCode() == KeyCode.NUMPAD9.getCode()) System.out.println(MouseInfo.getPointerInfo().getLocation().toString());
        }

        @Override
        public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) {

        }
    }
}
