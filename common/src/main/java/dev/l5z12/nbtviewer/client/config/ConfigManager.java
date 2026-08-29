// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.l5z12.nbtviewer.NbtViewer;
import net.fabricmc.loader.api.FabricLoader;

/** Loads / saves {@link NbtViewerConfig} to {@code config/nbtviewer.json} (Gson, no extra deps). */
public final class ConfigManager {

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("nbtviewer.json");

    private static NbtViewerConfig config = new NbtViewerConfig();

    private ConfigManager() {
    }

    public static NbtViewerConfig get() {
        return config;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                String json = new String(Files.readAllBytes(PATH), StandardCharsets.UTF_8);
                NbtViewerConfig loaded = GSON.fromJson(json, NbtViewerConfig.class);
                if (loaded != null) {
                    config = loaded;
                }
            }
        } catch (Exception e) {
            NbtViewer.LOGGER.warn("Failed to read {} — using defaults", PATH, e);
            config = new NbtViewerConfig();
        }
        config.sanitize();
        save();
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.write(PATH, GSON.toJson(config).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            NbtViewer.LOGGER.warn("Failed to write {}", PATH, e);
        }
    }
}
