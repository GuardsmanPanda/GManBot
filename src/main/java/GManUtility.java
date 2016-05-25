import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 */
public class GManUtility {

    /**
     * Saves text for bob to read, usually related to configuration changes needed, such as adding to the translation maps
     * @param text The text to appeand at the end of output/TextToBob.txt
     */
    public static void textToBob(String text) {
        File file = new File("output/TextToBob.txt");
        if (!file.exists()) {
            try {
                boolean fileCreated = file.createNewFile();
                if (!fileCreated) return;
            } catch (IOException e) {
                e.printStackTrace(); return;
            }
        }
        try(FileWriter writer = new FileWriter("output/TextToBob.txt", true)) {
            writer.write(text + "\n");
        } catch (IOException e) {
            System.out.println("Could not save text to bob: " + text);
            e.printStackTrace();
        }
    }
}
