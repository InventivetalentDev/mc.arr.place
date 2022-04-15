package org.inventivetalent.canvas;

import java.awt.*;

public class Util {

    public static Color hexToColor(String hex) {
        return Color.decode(hex);
    }

    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

}
