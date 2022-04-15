package org.inventivetalent.canvas;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.StreamSupport;

public class CanvasUpdater {

    private final CanvasPlugin plugin;
    private final int min;
    private final int max;
    private final Function<Vector, Material> blockChecker;
    private final BiConsumer<Vector, Material> blockPlacer;

    public boolean paused = false;

    CanvasState canvasState;
    JsonArray lastState;
    BufferedImage[][] lastChunks;

    Map<Vector, Material> blockQueue = new HashMap<>();

    public CanvasUpdater(CanvasPlugin plugin, int min, int max, Function<Vector, Material> blockChecker, BiConsumer<Vector, Material> blockPlacer) {
        this.plugin = plugin;
        this.min = min;
        this.max = max;
        this.blockChecker = blockChecker;
        this.blockPlacer = blockPlacer;
    }

    public void schedule() {
        updateCanvasInfo().thenAccept(v -> {
            Bukkit.getScheduler().runTaskTimer(plugin, this::update, 50, 50);
            Bukkit.getScheduler().runTaskTimer(plugin, this::updateCanvasInfo, 20 * 60 * 15, 20 * 60 * 15);
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
        queueBlock(new Vector(x, y, z), material);
    }

    void queueBlock(Vector vector, Material material) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (blockChecker.apply(vector) != material) {
                blockQueue.put(vector, material);
            }
        });
    }

    public void placeBase() {
        for (int x = 0; x < canvasState.w; x++) {
            for (int y = 0; y < canvasState.h; y++) {
                queueBlock(new Vector(x, min + 1, y), Material.STONE);
                queueBlock(new Vector(x, min + 2, y), Material.WHITE_CONCRETE);
            }
        }
    }

    public void clear() {
        for (int x = 0; x < canvasState.w; x++) {
            for (int y = 0; y < canvasState.h; y++) {
                int finalX = x;
                int finalY = y;
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    for (int i = min + 3; i < max; i++) {
                        queueBlock(new Vector(finalX, i, finalY), Material.AIR);
                    }
                }, x);
            }
        }
    }

    public void loadCurrent() {
        lastState = new JsonArray();
        lastChunks = new BufferedImage[canvasState.w][canvasState.h];
        for (int x = 0; x < canvasState.w / canvasState.s; x++) {
            for (int y = 0; y < canvasState.h / canvasState.s; y++) {
                int finalX = x;
                int finalY = y;
                lastChunks[finalX][finalY] = new BufferedImage(canvasState.s, canvasState.s, BufferedImage.TYPE_INT_RGB);
                var g = lastChunks[finalX][finalY].createGraphics();
                g.setPaint(Color.WHITE);
                g.fillRect(0, 0, canvasState.s, canvasState.s);
            }
        }
        update();
    }

    public CompletableFuture<Void> updateCanvasInfo() {
        return CanvasClient.getHello().thenAccept(state -> {
            canvasState = state;
            if (lastState == null) {
                lastState = new JsonArray();
            }
        }).exceptionally(e -> {
            plugin.getLogger().log(Level.SEVERE, "", e);
            return null;
        });
    }

    public CompletableFuture<Void> update() {
        return CanvasClient.getState().thenAccept(state -> {
            if (lastState != null && lastState.equals(state)) return; // nothing changed
            System.out.println("new state!");

            if (lastChunks == null) {
                lastChunks = new BufferedImage[canvasState.w][canvasState.h];
            }

            StreamSupport.stream(state.spliterator(), true)
                    .map(JsonElement::getAsString)
                    .forEach(str -> {
                        String str1 = str.replace(".png", "");
                        String[] split0 = str1.split("_");
                        String[] split1 = split0[2].split("-");
                        int x = Integer.parseInt(split1[0]);
                        int y = Integer.parseInt(split1[1]);

                        CanvasClient.getChunkImage(str)
                                .thenAccept(image -> {
                                    processChanges(x, y, image);
                                    lastChunks[x][y] = image;
                                })
                                .exceptionally(e -> {
                                    plugin.getLogger().log(Level.SEVERE, "", e);
                                    return null;
                                });
                    });

            lastState = state;
        }).exceptionally(e -> {
            plugin.getLogger().log(Level.SEVERE, "", e);
            return null;
        });
    }

    int[][] collectChanges(int x, int y, BufferedImage newImage) {
        int[][] changedColors = new int[canvasState.s][canvasState.s];
        for (int i = 0; i < canvasState.s; i++) {
            for (int j = 0; j < canvasState.s; j++) {
                changedColors[i][j] = -1;
            }
        }
        if (newImage == null) {
            return changedColors;
        }
        for (int i = 0; i < newImage.getWidth(); i++) {
            for (int j = 0; j < newImage.getHeight(); j++) {
                if (lastChunks[x][y] == null) continue;
                int newColor = newImage.getRGB(i, j);
                int oldColor = lastChunks[x][y].getRGB(i, j);
                if (newColor != oldColor) {
                    changedColors[i][j] = newColor;
                    System.out.println("got change in chunk " + x + "," + y + " at " + i + "," + j + " from " + oldColor + " to " + newColor);
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
