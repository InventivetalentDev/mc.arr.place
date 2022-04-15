package org.inventivetalent.canvas;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Colors {

    public static final Map<Integer, Material> COLOR_TO_BLOCK = new HashMap<>();

    static {
        r("#ffffff", Material.WHITE_WOOL);
        r("#d4d7d9", Material.LIGHT_GRAY_CONCRETE);
        r("#898d90", Material.GRAY_CONCRETE);
        r("#515252", Material.BLACK_CONCRETE);
        r("#000000", Material.BLACK_WOOL);
        r("#ffb470", Material.ORANGE_WOOL);
        r("#9c6926", Material.ORANGE_CONCRETE);
        r("#6d482f", Material.ORANGE_CONCRETE_POWDER);
        r("#ff99aa", Material.PINK_WOOL);
        r("#ff3881", Material.PINK_CONCRETE);
        r("#de107f", Material.PINK_CONCRETE_POWDER);
        r("#e4abff", Material.MAGENTA_WOOL);
        r("#b44ac0", Material.MAGENTA_CONCRETE);
        r("#811e9f", Material.MAGENTA_CONCRETE_POWDER);
        r("#94b3ff", Material.LIGHT_BLUE_WOOL);
        r("#6a5cff", Material.LIGHT_BLUE_CONCRETE);
        r("#493ac1", Material.LIGHT_BLUE_CONCRETE_POWDER);
        r("#51e9f4", Material.CYAN_WOOL);
        r("#3690ea", Material.BLUE_WOOL);
        r("#2450a4", Material.BLUE_CONCRETE);
        r("#00ccc0", Material.DIAMOND_BLOCK);
        r("#009eaa", Material.STRIPPED_WARPED_HYPHAE);
        r("#00756f", Material.WAXED_OXIDIZED_COPPER);
        r("#7eed56", Material.LIME_WOOL);
        r("#00cc78", Material.LIME_CONCRETE);
        r("#00a368", Material.LIME_CONCRETE_POWDER);
        r("#fff8b8", Material.YELLOW_WOOL);
        r("#ffd635", Material.YELLOW_CONCRETE);
        r("#ffa800", Material.YELLOW_CONCRETE_POWDER);
        r("#ff4500", Material.RED_WOOL);
        r("#be0039", Material.RED_CONCRETE);
        r("#6d001a", Material.RED_CONCRETE_POWDER);
    }

    public static Material get(int color) {
        return COLOR_TO_BLOCK.get(color);
    }

    static void r(String hex, Material mat) {
        COLOR_TO_BLOCK.put(Util.hexToColor(hex).getRGB(), mat);
    }

}
