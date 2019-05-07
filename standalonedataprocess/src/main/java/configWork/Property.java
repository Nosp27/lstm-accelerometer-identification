package configWork;

import java.io.File;

public class Property {
    public String name;
    public String value;
    public ConfigType type;

    public Property(String line) throws IllegalArgumentException {
        if (!line.contains(":") || !line.contains("="))
            throw new IllegalArgumentException("wrong format");

        name = line.substring(0, line.indexOf(":"));
        type = ConfigType.valueOf(line.substring(line.indexOf(":") + 1, line.indexOf("=")).trim());
        value = line.substring(line.indexOf("=") + 1).trim();

        checkPropertyCorrect();
    }

    public Property(String name, String value, ConfigType type) {
        this.name = name;
        this.value = value;
        this.type = type;
        checkPropertyCorrect();
    }

    private void checkPropertyCorrect() throws IllegalArgumentException {
        switch (type) {
            case PATH:
                if (!new File(value).exists())
                    throw new IllegalArgumentException();
                break;
            case INT:
                try {
                    Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException();
                }
                break;
        }
    }

    @Override
    public String toString() {
        return String.format("%s:%s = %s\n", name, type, value);
    }
}