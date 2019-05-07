package configWork;

import gui.configs.ConfigFrame;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final String configPath = "C:\\Users\\Nosp\\IdeaProjects\\NetworkTest\\standalonedataprocess\\src\\main\\resources\\config\\config.conf";
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void configSave() {
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
        if(properties == null)
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
    }
}
