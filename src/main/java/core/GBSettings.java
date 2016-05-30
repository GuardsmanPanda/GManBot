package core;

import java.io.File;

/**
 * This class expects passwords and other settings to be located in /Data/passwords.txt and settings.txt respectively
 * And in the format:
 * Setting1: value1
 * Setting2: value2
 *
 * The getSetting and getPassword methods can be called with Setting1 and Setting2 as parameters and will return their respective values
 */
public class GBSettings {
    private static File passwordFile = new File("Data/passwords.txt");
    private static File settingFile = new File("Data/settings.txt");


}
