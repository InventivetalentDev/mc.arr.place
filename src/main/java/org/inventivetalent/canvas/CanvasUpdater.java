package org.inventivetalent.canvas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.StreamSupport;

public class CanvasUpdater {

    private final CanvasPlugin plugin;
    private final BiConsumer<Vector, Material> blockPlacer;

    public boolean paused = false;

    CanvasState canvasState;
    JsonArray lastState;
    BufferedImage[][] lastChunks;

    Map<Vector, Material> blockQueue = new HashMap<>();

    public CanvasUpdater(CanvasPlugin plugin, BiConsumer<Vector, Material> blockPlacer) {
        this.plugin = plugin;
        this.blockPlacer = blockPlacer;
    }

    public void schedule() {
        updateCanvasInfo().thenAccept(v -> {
            Bukkit.getScheduler().runTaskTimer(plugin, this::update, 40, 40);
            Bukkit.getScheduler().runTaskTimer(plugin, this::updateCanvasInfo, 20 * 60, 20 * 60);
        });

        Bukkit.getScheduler().runTaskTimer(plugin, this::placeBlocks, 1, 1);
    }

    public void placeBlocks() {
        if (paused) return;
        if (blockQueue.isEmpty()) return;
        var list = new ArrayList<>(blockQueue.entrySet()).subList(0, Math.min(blockQueue.size(), 16));
        for (Map.Entry<Vector, Material> entry : list) {
            blockQueue.remove(entry.getKey());
        }
        for (Map.Entry<Vector, Material> entry : list) {
            blockPlacer.accept(entry.getKey(), entry.getValue());
        }
    }

    void queueBlock(int x, int y, int z, Material material) {
        Bukkit.getScheduler().runTask(plugin, () -> blockQueue.put(new Vector(x, y, z), material));
    }

    void queueBlock(Vector vector, Material material) {
        Bukkit.getScheduler().runTask(plugin, () -> blockQueue.put(vector, material));
    }

    public void placeBase() {
        for (int x = 0; x < canvasState.w; x++) {
            for (int y = 0; y < canvasState.h; y++) {
                queueBlock(new Vector(x, 1, y), Material.STONE);
                queueBlock(new Vector(x, 2, y), Material.WHITE_CONCRETE);
            }
        }
    }

    public CompletableFuture<Void> updateCanvasInfo() {
        return CanvasClient.getHello().thenAccept(state -> {
            canvasState = state;
        });
    }

    public CompletableFuture<Void> update() {
        return CanvasClient.getState().thenAccept(state -> {
            if (lastState != null && lastState.equals(state)) return; // nothing changed
            lastState = state;

            StreamSupport.stream(state.spliterator(), true)
                    .map(JsonElement::getAsString)
                    .forEach(str -> {
                        String[] split0 = str.split("_");
                        String[] split1 = split0[2].split("-");
                        int x = Integer.parseInt(split1[0]);
                        int y = Integer.parseInt(split1[1]);

                        CanvasClient.getChunkImage(str)
                                .thenAccept(image -> {
                                    processChanges(x, y, image);
                                });
                    });
        });
    }

    int[][] collectChanges(int x, int y, BufferedImage newImage) {
        int[][] changedColors = new int[newImage.getWidth()][newImage.getHeight()];
        for (int i = 0; i < newImage.getWidth(); i++) {
            for (int j = 0; j < newImage.getHeight(); j++) {
                changedColors[i][j] = -1;
            }
        }
        for (int i = 0; i < newImage.getWidth(); i++) {
            for (int j = 0; j < newImage.getHeight(); j++) {
                if (lastChunks[x][y] == null) continue;
                int newColor = newImage.getRGB(i, j);
                int oldColor = lastChunks[x][y].getRGB(i, j);
                if (newColor != oldColor) {
                    changedColors[i][j] = newColor;
                }
            }
        }
        return changedColors;
    }

    void processChanges(int x, int y, BufferedImage newImage) {
        int[][] changedColors = collectChanges(x, y, newImage);
        for (int i = 0; i < changedColors.length; i++) {
            for (int j = 0; j < changedColors[i].length; j++) {
                if (changedColors[i][j] == -1) continue;
                int color = changedColors[i][j];
                Material material = Colors.get(color);
                if (material == null) {
                    plugin.getLogger().warning("Unknown color: " + color);
                    continue;
                }
                queueBlock(x * canvasState.s + i, -1, y * canvasState.s + j, material);
            }
        }
    }

}
