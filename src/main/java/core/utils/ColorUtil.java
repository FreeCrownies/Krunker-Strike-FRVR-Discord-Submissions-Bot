package core.utils;

import java.awt.*;
import java.util.Optional;

public class ColorUtil {

    public static boolean isColorCode(String input) {
        return getColor(input).isPresent();
    }

    public static Optional<Color> getColor(String input) {
        if (input == null) return Optional.empty();

        Color color;

        color = Color.getColor(input);
        if (color == null) {
            try {
                color = Color.decode(input);
            } catch (NumberFormatException ignore) {
            }
        }

        return Optional.ofNullable(color);
    }

    public static String toHexCode(Color color) {
        if (color == null) return null;
        return "#" + Integer.toHexString(color.getRGB()).substring(2);
    }

    public static String toSaveHexCode(String hexCode) {
        switch (hexCode.toLowerCase()) {
            case "#000000": hexCode = "#010101";
            case "#ffffff": hexCode = "#fefefe";
        }
        return hexCode;
    }

}
