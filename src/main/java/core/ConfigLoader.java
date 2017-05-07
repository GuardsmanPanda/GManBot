package core;

import com.esotericsoftware.yamlbeans.YamlReader;
import com.esotericsoftware.yamlbeans.YamlWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;


public class ConfigLoader {

    //TODO: currently all saved variables are Strings maybe there is a way to elegantly fix this?

    private static final String CONFIG_FILE = "Data/config.yml";

    /**
     * Returns all settings in a given module
     * @param module refers to the Module which calls this Method
     * @return returns a Hashmap with <Key,Value>
     */

    public static HashMap loadConfig(String module){
        HashMap settings = new HashMap<>();
        try {
            YamlReader reader = new YamlReader(new FileReader(CONFIG_FILE));
            HashMap config = (HashMap) reader.read();
            HashMap modules = (HashMap) config.get("modules");
            settings = (HashMap) modules.get(module);
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return settings;
    }

    /**
     * Returns a specific setting
     * @param module refers to the Module which calls this Method
     * @param key refers to the Key for the setting
     * @return returns the value as a String
     */

    public static String loadConfig(String module,String key){
        String value = "";
        try {
            YamlReader reader = new YamlReader(new FileReader(CONFIG_FILE));
            HashMap config = (HashMap) reader.read();
            HashMap modules = (HashMap) config.get("modules");
            HashMap settings = (HashMap) modules.get(module);
            value = settings.get(key).toString();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * @param module refers to the Module which calls this Method
     * @param key refers to the Key for the setting
     * @param value refers to the Value for the setting
     */

    public static void setConfigSetting(String module, String key,String value){
        try {
            YamlReader reader = new YamlReader(new FileReader(CONFIG_FILE));
            HashMap config = (HashMap) reader.read();
            HashMap modules = (HashMap) config.get("modules");
            HashMap settings = (HashMap) modules.get(module);
            settings.put(key,value);
            reader.close();
            YamlWriter writer = new YamlWriter(new FileWriter(CONFIG_FILE));
            writer.write(config);
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
