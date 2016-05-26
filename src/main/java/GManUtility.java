import com.google.common.io.CharStreams;

import java.io.*;
import java.nio.file.Path;

/**
 *
 */
public class GManUtility {


    public static void writeTextToFile(String text, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                boolean fileCreated = file.createNewFile();
                if (!fileCreated) return;
            } catch (IOException e) {
                e.printStackTrace(); return;
            }
        }
        try(FileWriter writer = new FileWriter(filePath, true)) {
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
        writeTextToFile(text, "output/textToBob.txt");
     }
}
