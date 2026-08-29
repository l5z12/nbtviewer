// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.gui;

import java.util.function.Supplier;

import dev.l5z12.nbtviewer.client.config.ConfigManager;
import dev.l5z12.nbtviewer.client.config.NbtViewerConfig;
import dev.l5z12.nbtviewer.facade.Gfx;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Txt;
import dev.l5z12.nbtviewer.facade.Ui;

/** Lightweight, dependency-free options screen. Written once against the facades + {@link
 * NbtScreenBase}, so it is identical on every mapping. */
public final class NbtConfigScreen extends NbtScreenBase {

    private static final int[] LINE_PRESETS = {8, 12, 16, 20, 26, 32, 40, 60};

    private final Object parent;
    private final NbtViewerConfig c = ConfigManager.get();

    public NbtConfigScreen(Object parent) {
        super(Txt.translatable("nbtviewer.config.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        clearWidgets();
        int colW = 200;
        int gap = 10;
        int totalW = colW * 2 + gap;
        int left = (this.width - totalW) / 2;
        int rightCol = left + colW + gap;
        int y = 44;
        int rowH = 24;

        // Left column — overlay
        option(left, y, colW, () -> onOff("nbtviewer.config.overlay", c.overlayEnabled), () -> c.overlayEnabled = !c.overlayEnabled);
        option(left, y += rowH, colW, () -> value("nbtviewer.config.corner", c.overlayCorner.name()), () -> c.overlayCorner = c.overlayCorner.next());
        option(left, y += rowH, colW, () -> value("nbtviewer.config.source", c.overlaySource.name()), () -> c.overlaySource = c.overlaySource.next());
        option(left, y += rowH, colW, () -> value("nbtviewer.config.visibility", c.overlayVisibility.name()), () -> c.overlayVisibility = c.overlayVisibility.next());
        option(left, y += rowH, colW, () -> value("nbtviewer.config.max_lines", Integer.toString(c.overlayMaxLines)), () -> c.overlayMaxLines = cyclePreset(c.overlayMaxLines));
        option(left, y += rowH, colW, () -> value("nbtviewer.config.scale", String.format("%.2f", c.overlayScale)), () -> c.overlayScale = cycleScale(c.overlayScale));
        option(left, y += rowH, colW, () -> onOff("nbtviewer.config.overlay_bg", c.overlayBackground), () -> c.overlayBackground = !c.overlayBackground);

        // Right column — formatting + jade
        int ry = 44;
        option(rightCol, ry, colW, () -> onOff("nbtviewer.config.sort", c.sortKeys), () -> c.sortKeys = !c.sortKeys);
        option(rightCol, ry += rowH, colW, () -> onOff("nbtviewer.config.color", c.colorize), () -> c.colorize = !c.colorize);
        option(rightCol, ry += rowH, colW, () -> value("nbtviewer.config.copy_format", c.copyFormat.name()), () -> c.copyFormat = c.copyFormat.next());
        option(rightCol, ry += rowH, colW, () -> value("nbtviewer.config.expand_depth", Integer.toString(c.autoExpandDepth)), () -> c.autoExpandDepth = (c.autoExpandDepth + 1) % 8);
        option(rightCol, ry += rowH, colW, () -> onOff("nbtviewer.config.gui_pause", c.guiPauseGame), () -> c.guiPauseGame = !c.guiPauseGame);
        option(rightCol, ry += rowH, colW, () -> onOff("nbtviewer.config.jade", c.jadeEnabled), () -> c.jadeEnabled = !c.jadeEnabled);
        option(rightCol, ry += rowH, colW, () -> onOff("nbtviewer.config.jade_sync", c.jadeSyncFullBlockData), () -> c.jadeSyncFullBlockData = !c.jadeSyncFullBlockData);
        option(rightCol, ry += rowH, colW, () -> onOff("nbtviewer.config.nearest_entity", c.nearestEntityFallback), () -> c.nearestEntityFallback = !c.nearestEntityFallback);
        option(rightCol, ry += rowH, colW, () -> value("nbtviewer.config.sticky", stickyLabel()), () -> c.stickyTargetMs = cycleSticky(c.stickyTargetMs));

        addWidget(Ui.button(Txt.translatable("nbtviewer.config.done"),
                this.width / 2 - 100, this.height - 28, 200, 20, this::closeSelf));
    }

    private void option(int x, int y, int w, Supplier<Object> label, Runnable onClick) {
        Object[] holder = new Object[1];
        holder[0] = Ui.button(label.get(), x, y, w, 20, () -> {
            onClick.run();
            ConfigManager.save();
            Ui.setMessage(holder[0], label.get());
        });
        addWidget(holder[0]);
    }

    private static int cyclePreset(int current) {
        for (int v : LINE_PRESETS) {
            if (v > current) return v;
        }
        return LINE_PRESETS[0];
    }

    private static final int[] STICKY_PRESETS = {0, 1000, 2000, 2500, 3000, 5000};

    private String stickyLabel() {
        return c.stickyTargetMs == 0
                ? "OFF"
                : String.format(java.util.Locale.ROOT, "%.1fs", c.stickyTargetMs / 1000.0);
    }

    private static int cycleSticky(int current) {
        for (int v : STICKY_PRESETS) {
            if (v > current) return v;
        }
        return STICKY_PRESETS[0];
    }

    private static double cycleScale(double current) {
        double next = Math.round((current + 0.25) * 100.0) / 100.0;
        return next > 2.0 ? 0.5 : next;
    }

    private static Object onOff(String key, boolean value) {
        Object out = Txt.append(Txt.translatable(key), Txt.literal(": "));
        return Txt.append(out, Txt.colored(
                Txt.translatable(value ? "nbtviewer.on" : "nbtviewer.off"), value ? Txt.GREEN : Txt.RED));
    }

    private static Object value(String key, String value) {
        Object out = Txt.append(Txt.translatable(key), Txt.literal(": "));
        return Txt.append(out, Txt.colored(Txt.literal(value), Txt.AQUA));
    }

    @Override
    protected void renderBackdrop(Object g) {
        Gfx.fill(g, 0, 0, this.width, this.height, 0xC8100016);
    }

    @Override
    protected void renderContent(Object g, int mouseX, int mouseY, float delta) {
        Gfx.centeredText(g, font(), this.getTitle(), this.width / 2, 18, 0xFFFFFFFF);
    }

    @Override
    protected void onCloseScreen() {
        ConfigManager.save();
        Mc.setScreen(mc(), parent);
    }
}
