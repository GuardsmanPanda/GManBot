package utility;

import com.google.common.base.Strings;

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 */
public class GBUtility {
    private static Robot robot;

    static {
        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); }
    }

    public static String strictFill(String text, int maximumLength) { return strictFill(text, ' ', maximumLength); }
    public static String strictFill(String text, char fill, int maximumLength) {
        if (text.length() <= maximumLength)
            return Strings.padEnd(text, maximumLength, fill);
        else
            return text.substring(0, maximumLength - 2) + "..";
    }

    /**
     * Writes the string as if the user typed it on his keyboard.
     * @param stringToPaste5
     */
    public static void copyAndPasteString(String stringToPaste) {
        StringSelection text = new StringSelection(stringToPaste);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, text);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_V);
    }

    //todo: reaplce with better filewriter from lol api
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
        writeTextToFile(text + System.lineSeparator(), "output/textToBob.txt", true);
     }
}
