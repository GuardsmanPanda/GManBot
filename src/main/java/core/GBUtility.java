package core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Multiset;
import com.google.common.collect.Multisets;
import com.google.common.io.CharStreams;
import org.apache.commons.lang3.StringUtils;
import org.pircbotx.hooks.events.MessageEvent;

import javax.sql.rowset.CachedRowSet;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.*;
import java.nio.file.Path;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.stream.StreamSupport;

/**
 *
 */
public class GBUtility {
    private static Robot robot;

    static {
        try { robot = new Robot(); } catch (AWTException e) { e.printStackTrace(); }
    }

    public static void main(String[] args) {
        System.out.println(strictFill("javaisthebestever", 12) + " 9");
        System.out.println(strictFill("somethingsho", 12) + " 7");
        System.out.println(strictFill("hello", 12) + " 5");
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

    public static String strictFill(String text, int maximumLength) { return strictFill(text, " ", maximumLength); }
    public static String strictFill(String text, String fill, int maximumLength) {
        if (text.length() <= maximumLength)
            return stringFill(text, fill, maximumLength);
        else
            return text.substring(0, maximumLength - 2) + "..";
    }
    public static String stringFill(String text, int minimumLength) { return  stringFill(text, " ", minimumLength); }
    public static String stringFill(String text, String fill, int minimumLength) {
        return text + StringUtils.repeat(fill, minimumLength - text.length());
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
    public static void prettyPrintJSonNode(JsonNode node) {
        try {
            System.out.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(node));
        } catch (JsonProcessingException e) {
            System.out.println("Error printing JsonNode");
            e.printStackTrace();
        }
    }
    public static void prettyPrintCachedRowSet(CachedRowSet cachedRowSet, int rowsToPrint) {
        prettyPrintCachedRowSet(cachedRowSet, rowsToPrint, 20);
    }
    public static void prettyPrintCachedRowSet(CachedRowSet cachedRowSet, int rowsToPrint, int rowLength) {
        try {
            ResultSetMetaData metaData = cachedRowSet.getMetaData();
            String columnNames = "";
            for (int i = 1; i <= metaData.getColumnCount(); i++) columnNames += strictFill(metaData.getColumnLabel(i), rowLength) + " ";
            System.out.println(columnNames.trim());

            while (cachedRowSet.next()) {
                String rowString = "";
                for (int i = 1; i <= metaData.getColumnCount(); i++) rowString += strictFill(cachedRowSet.getString(i), rowLength) + " ";
                System.out.println(rowString.trim());
                if (cachedRowSet.getRow() >= rowsToPrint) break;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        writeTextToFile(text + System.lineSeparator(), "output/textToBob.txt", true);
     }
}
