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
     * Writes the string as if the user used ctrl + v
     * @param stringToPaste the string to paste to the current location
     */
    public static void copyAndPasteString(String stringToPaste) {
        StringSelection text = new StringSelection(stringToPaste);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, text);
        robot.keyPress(KeyEvent.VK_CONTROL);
        robot.keyPress(KeyEvent.VK_V);
        robot.keyRelease(KeyEvent.VK_CONTROL);
        robot.keyRelease(KeyEvent.VK_V);
    }

    public static boolean writeTextToFile(String text, String filePath, boolean append) {
        File file = new File(filePath);
        boolean fileCreated = false;

        //Check for any missing directories and silently create them if we can
        if (!file.getParentFile().exists()) {
            boolean directoriesCreated = file.getParentFile().mkdirs();
            if (!directoriesCreated) return false;
        }

        //Check if the output file exists and create it if we can
        if (!file.exists()) {
            try {
                fileCreated = file.createNewFile();
                if (!fileCreated) return false;
            } catch (IOException e) {
                e.printStackTrace(); return false;
            }
        }

        //now try writing the output string
        try(FileWriter writer = new FileWriter(filePath, append)) {
            if (fileCreated) writer.write(text);
            else writer.write(System.lineSeparator() + text);
            return true;
        } catch (IOException e) {
            System.out.println("Could not save text to: " + filePath + " - TEXT: " + text);
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Saves text for bob to read, usually related to configuration changes needed, such as adding to the translation maps
     * @param text The text to appeand at the end of output/TextToBob.txt
     */
    public static void textToBob(String text) {
        if (!writeTextToFile(text + System.lineSeparator(), "output/textToBob.txt", true)) {
            System.out.println("Could nto write to Bob file!!!");
        }
     }
}
