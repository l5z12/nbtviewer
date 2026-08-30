// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.nbt;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import dev.l5z12.nbtviewer.NbtViewer;
import net.fabricmc.loader.api.FabricLoader;

/**
 * Writes SNBT to a timestamped {@code .snbt} file under {@code <gameDir>/nbtviewer-exports/}. This is
 * the escape hatch for data too large for the clipboard or chat, and gives a persistent record you
 * can diff or reopen later.
 *
 * <p>Pure Java IO plus Fabric's {@code getGameDir()} (a Fabric-loader API, not a mapping type), so it
 * needs no per-version or per-mapping code and compiles identically on every node.
 */
public final class NbtExporter {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private NbtExporter() {
    }

    /** The directory exports are written to (created on demand by {@link #write}). */
    public static Path directory() {
        return FabricLoader.getInstance().getGameDir().resolve("nbtviewer-exports");
    }

    /**
     * Writes {@code snbt} to a new file whose name is derived from {@code label} plus a timestamp.
     * Returns the created path, or {@code null} if writing failed (logged, never thrown).
     */
    public static Path write(String label, String snbt) {
        try {
            Path dir = directory();
            Files.createDirectories(dir);
            Path file = dir.resolve(sanitize(label) + "-" + LocalDateTime.now().format(STAMP) + ".snbt");
            Files.write(file, snbt.getBytes(StandardCharsets.UTF_8));
            return file;
        } catch (IOException | RuntimeException e) {
            NbtViewer.LOGGER.warn("Failed to export NBT to {}", directory(), e);
            return null;
        }
    }

    /** Reduce an arbitrary label to a short, filesystem-safe stem. */
    private static String sanitize(String raw) {
        String lower = raw == null ? "" : raw.toLowerCase(Locale.ROOT);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lower.length() && sb.length() < 48; i++) {
            char c = lower.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9')) {
                sb.append(c);
            } else if (c == '.' || c == '_' || c == '-' || c == ' ' || c == ':' || c == '/') {
                sb.append('_');
            }
        }
        String cleaned = sb.toString().replaceAll("_+", "_").replaceAll("^_|_$", "");
        return cleaned.isEmpty() ? "nbt" : cleaned;
    }
}
