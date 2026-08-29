// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.config;

/**
 * Plain data object persisted to {@code config/nbtviewer.json} (see {@link ConfigManager}).
 * All fields are public with sensible defaults so Gson round-trips cleanly and missing keys
 * simply keep their default.
 */
public class NbtViewerConfig {

    // ------------------------------------------------------------------ HUD overlay
    public boolean overlayEnabled = false;
    public OverlayCorner overlayCorner = OverlayCorner.TOP_LEFT;
    public HudSource overlaySource = HudSource.AUTO;
    public OverlayVisibility overlayVisibility = OverlayVisibility.KEY_HELD;
    public int overlayMaxLines = 22;
    public int overlayMaxValueLength = 96;
    public double overlayScale = 1.0;
    public boolean overlayBackground = true;
    public boolean overlayShowSize = true;

    // ------------------------------------------------------------------ formatting / copy
    public CopyFormat copyFormat = CopyFormat.PRETTY;
    public boolean sortKeys = true;
    public boolean colorize = true;
    /** Hard cap on characters printed to chat by {@code /viewdata}, to avoid flooding. */
    public int chatMaxChars = 12000;

    // ------------------------------------------------------------------ GUI screen
    public int autoExpandDepth = 2;
    public boolean guiPauseGame = false;

    // ------------------------------------------------------------------ targeting robustness
    // Entities move; a single-tick crosshair raycast often misses a fast/dodgy mob at the moment
    // you press the key. These fallbacks recover the intended target.
    /** Remember the last entity/block the crosshair was on for this many ms (0 = disabled), so a
     * target that darts away just as you press the key is still resolved. */
    public int stickyTargetMs = 2500;
    /** If the crosshair isn't exactly on an entity, pick the nearest one within the look cone. */
    public boolean nearestEntityFallback = true;
    /** Reach (blocks) for the nearest-entity cone search. */
    public double nearestEntityReach = 6.0;
    /** Half-angle (degrees) of the look cone for the nearest-entity search. */
    public double nearestEntityConeDegrees = 15.0;

    // ------------------------------------------------------------------ Jade extension
    public boolean jadeEnabled = true;
    /** In singleplayer, ask the integrated server for the full block-entity NBT. */
    public boolean jadeSyncFullBlockData = true;
    public int jadeMaxLines = 30;

    public NbtViewerConfig() {
    }

    /** Clamp values that could be corrupted by hand-editing the JSON. */
    public void sanitize() {
        overlayMaxLines = clamp(overlayMaxLines, 1, 200);
        overlayMaxValueLength = clamp(overlayMaxValueLength, 8, 4096);
        overlayScale = Math.max(0.25, Math.min(3.0, overlayScale));
        chatMaxChars = clamp(chatMaxChars, 500, 100_000);
        autoExpandDepth = clamp(autoExpandDepth, 0, 20);
        jadeMaxLines = clamp(jadeMaxLines, 1, 200);
        stickyTargetMs = clamp(stickyTargetMs, 0, 30_000);
        nearestEntityReach = Math.max(1.0, Math.min(24.0, nearestEntityReach));
        nearestEntityConeDegrees = Math.max(1.0, Math.min(89.0, nearestEntityConeDegrees));
        if (overlayCorner == null) overlayCorner = OverlayCorner.TOP_LEFT;
        if (overlaySource == null) overlaySource = HudSource.AUTO;
        if (overlayVisibility == null) overlayVisibility = OverlayVisibility.KEY_HELD;
        if (copyFormat == null) copyFormat = CopyFormat.PRETTY;
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
