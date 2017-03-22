package core;

import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.io.CharStreams;
import org.pircbotx.hooks.events.MessageEvent;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Path;

/**
 *
 */
public class GBUtility {
    private static Robot robot;

    static {
        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); }
    }

    public static <E> E getElementWithHighestCount(Multiset<E> multiSet) {
        return Multisets.copyHighestCountFirst(multiSet).iterator().next();
    }

    public static String getMultisetLeaderText(Multiset<String> multiSet, int numberOfLeaders) {
        String returnText = "";
        int entryNumber = 1;
        for (String s : Multisets.copyHighestCountFirst(multiSet).elementSet()) {
            if (entryNumber != 1) returnText += ", ";
            returnText += s + " " + multiSet.count(s);
            if (entryNumber == numberOfLeaders) break;
            entryNumber++;
        }
        return returnText;
    }

    /**
     * Writes the string as if the user typed it on his keyboard.
     * @param stringToPaste
     */
    public static void copyAndPasteString(String stringToPaste) {
        StringSelection text = new StringSelection(stringToPaste);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, text);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_V);
    }

    public static void writeTextToFile(String text, String filePath, boolean append) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                boolean fileCreated = file.createNewFile();
                if (!fileCreated) return;
            } catch (IOException e) {
                e.printStackTrace(); return;
            }
        }
        try(FileWriter writer = new FileWriter(filePath, append)) {
            writer.write(text + "\n");
        } catch (IOException e) {
            System.out.println("Could not save text to: " + filePath + " - TEXT: " + text);
            e.printStackTrace();
        }
    }

    /**
     * Saves text for bob to read, usually related to configuration changes needed, such as adding to the translation maps
     * @param text The text to appeand at the end of output/TextToBob.txt
     */
    public static void textToBob(String text) {
        writeTextToFile(text, "output/textToBob.txt", true);
     }
}
