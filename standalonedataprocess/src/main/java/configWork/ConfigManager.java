package configWork;

import gui.configs.ConfigFrame;
import scala.sys.Prop;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final String configPath = "config.conf";
    private static Map<String, Property> properties;

    private static void configLoad() {
        properties = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Property p = new Property(line);
                    properties.put(p.name, p);
                    System.out.println("Read " + p.name);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("wrong Property type: \"" + line + "\"");
                }
            }
        } catch (FileNotFoundException e) {
            createConfigs();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void configSave() {
        try (FileWriter writer = new FileWriter(configPath)) {
            for (Property p : properties.values()) {
                writer.write(p.toString());
            }
            writer.flush();
            System.out.println("Config saving successful");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String loadProperty(String name){
        if(properties == null || !properties.containsKey(name))
            configLoad();

        System.out.println("Get property " + name);
        return properties.get(name).value;
    }

    public static void saveProperty(String name, String value, ConfigType ct){
        if(properties == null)
            configLoad();

        Property p = new Property(name, value, ct);
        properties.put(p.name, p);
        System.out.println("Set property " + name + " = " + value);
        configSave();
    }

    private static void createConfigs() {
        File configFile = new File(configPath);
        try {
            configFile.createNewFile();

            try(FileOutputStream out = new FileOutputStream(configFile);
                BufferedReader in = new BufferedReader(new InputStreamReader(ConfigManager.class.getResourceAsStream("/config.conf")))){
                String line;
                while ((line = in.readLine())!= null)
                    out.write((line + "\n").getBytes());

                configLoad();
                for(Property p : properties.values())
                    if(p.type == ConfigType.PATH)
                        new File(p.value).mkdirs();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean check() {
        if(!new File(configPath).exists()) {
            createConfigs();
            return false;
        }
        return true;
    }
}
