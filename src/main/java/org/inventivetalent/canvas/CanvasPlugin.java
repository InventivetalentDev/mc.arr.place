package org.inventivetalent.canvas;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CanvasPlugin extends JavaPlugin implements Listener {

    private World world;
    private CanvasUpdater updater;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void on(WorldLoadEvent event) {
        world = Bukkit.getWorlds().get(0);
        world.setTime(2400);
        world.setSpawnLocation(128, 32, 128);
        world.setPVP(false);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);

        WorldBorder border = world.getWorldBorder();
        border.setCenter(0, 0);
        border.setSize(512, 512);

        updater = new CanvasUpdater(this, (vector, material) -> Bukkit.getScheduler()
                .runTask(CanvasPlugin.this, () -> {
                    int y = vector.getBlockY();
                    if (y == -1) {
                        y = world.getHighestBlockAt(vector.getBlockX(), vector.getBlockZ()).getY() + 1;
                    }
                    world.getBlockAt(vector.getBlockX(), y, vector.getBlockZ()).setType(material);
                }));
        updater.schedule();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.isOp()) return false;
        if (args.length == 0) return false;
        if (args[0].equalsIgnoreCase("prepare")) {
            updater.placeBase();
            return true;
        }
        if (args[0].equalsIgnoreCase("loadcurrent")) {
            updater.loadCurrent();
            return true;
        }
        if (args[0].equalsIgnoreCase("pause")) {
            updater.paused = true;
            return true;
        }
        if (args[0].equalsIgnoreCase("resume")) {
            updater.paused = false;
            return true;
        }
        return false;
    }

    @Override
    public @Nullable ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return new VoidGenerator();
    }

    @Override
    public @Nullable BiomeProvider getDefaultBiomeProvider(@NotNull String worldName, @Nullable String id) {
        return new VoidBiomeProvider();
    }

}
