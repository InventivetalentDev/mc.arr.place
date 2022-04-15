package org.inventivetalent.canvas;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;

public class Colors {

    public static final Map<Integer, Material> COLOR_TO_BLOCK = new HashMap<>();

    static {
        r("#ffffff", Material.WHITE_WOOL);
        r("#d4d7d9", Material.ANDESITE);
        r("#898d90", Material.GRAY_CONCRETE);
        r("#515252", Material.BASALT);
        r("#000000", Material.BLACK_WOOL);
        r("#ffb470", Material.YELLOW_GLAZED_TERRACOTTA);
        r("#9c6926", Material.ORANGE_TERRACOTTA);
        r("#6d482f", Material.BROWN_WOOL);
        r("#ff99aa", Material.PINK_WOOL);
        r("#ff3881", Material.PINK_CONCRETE);
        r("#de107f", Material.BUBBLE_CORAL_BLOCK);
        r("#e4abff", Material.IRON_BLOCK);
        r("#b44ac0", Material.MAGENTA_WOOL);
        r("#811e9f", Material.PURPLE_GLAZED_TERRACOTTA);
        r("#94b3ff", Material.ICE);
        r("#6a5cff", Material.LIGHT_BLUE_CONCRETE);
        r("#493ac1", Material.AMETHYST_BLOCK);
        r("#51e9f4", Material.DIAMOND_BLOCK);
        r("#3690ea", Material.BLUE_WOOL);
        r("#2450a4", Material.BLUE_CONCRETE);
        r("#00ccc0", Material.BLUE_CONCRETE_POWDER);
        r("#009eaa", Material.CYAN_WOOL);
        r("#00756f", Material.WARPED_WART_BLOCK);
        r("#7eed56", Material.SLIME_BLOCK);
        r("#00cc78", Material.EMERALD_BLOCK);
        r("#00a368", Material.BLUE_GLAZED_TERRACOTTA);
        r("#fff8b8", Material.YELLOW_WOOL);
        r("#ffd635", Material.YELLOW_CONCRETE);
        r("#ffa800", Material.GOLD_BLOCK);
        r("#ff4500", Material.CUT_RED_SANDSTONE);
        r("#be0039", Material.CRIMSON_NYLIUM);
        r("#6d001a", Material.BLACK_GLAZED_TERRACOTTA);
    }

    public static Material get(int color) {
        return COLOR_TO_BLOCK.get(color);
    }

    static void r(String hex, Material mat) {
        COLOR_TO_BLOCK.put(Util.hexToColor(hex).getRGB(), mat);
    }

}
