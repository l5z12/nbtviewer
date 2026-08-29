// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client;

import dev.l5z12.nbtviewer.NbtViewer;
import dev.l5z12.nbtviewer.client.command.ViewDataCommand;
import dev.l5z12.nbtviewer.client.config.ConfigManager;
import dev.l5z12.nbtviewer.client.config.CopyFormat;
import dev.l5z12.nbtviewer.client.config.HudSource;
import dev.l5z12.nbtviewer.client.config.NbtViewerConfig;
import dev.l5z12.nbtviewer.client.gui.NbtConfigScreen;
import dev.l5z12.nbtviewer.client.gui.NbtViewerScreen;
import dev.l5z12.nbtviewer.client.hud.NbtHudOverlay;
import dev.l5z12.nbtviewer.client.keybind.NbtKeyBindings;
import dev.l5z12.nbtviewer.client.nbt.NbtFormat;
import dev.l5z12.nbtviewer.client.target.NbtTarget;
import dev.l5z12.nbtviewer.client.target.TargetResolver;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Txt;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;

/**
 * Client entry point. All game access goes through {@link Mc}/{@link Txt}, so the body is identical
 * on every mapping; only the container-screen key hook (whose input event object differs between
 * classic yarn, 1.21.9+ yarn and Mojmap) is isolated in the guarded helpers at the bottom.
 */
public final class NbtViewerClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        NbtKeyBindings.register();

        Object client = Mc.client();
        Mc.registerHud(new NbtHudOverlay(client, ConfigManager.get()));
        Mc.registerScreenScroll((screen, vertical) -> {
            if (screen instanceof NbtViewerScreen s) s.scrollBy(vertical);
        });

        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, access) -> ViewDataCommand.register(dispatcher));

        ClientTickEvents.END_CLIENT_TICK.register(c -> onClientTick(Mc.client()));

        // Inspect the hovered item while a container screen is open (keybinds don't tick there).
        registerContainerKeyHook();

        NbtViewer.LOGGER.info("NBT Viewer ready — /viewdata, or press the 'Open (auto)' key.");
    }

    private void onClientTick(Object client) {
        if (Mc.player(client) == null) {
            return;
        }
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.openAuto)) openFor(client, HudSource.AUTO);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.openHeldItem)) openFor(client, HudSource.HELD_ITEM);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.openHoveredSlot)) openFor(client, HudSource.HOVERED_SLOT);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.openTargetBlock)) openFor(client, HudSource.TARGET_BLOCK);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.openTargetEntity)) openFor(client, HudSource.TARGET_ENTITY);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.copyAuto)) copyFor(client, HudSource.AUTO);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.copyHeldItem)) copyFor(client, HudSource.HELD_ITEM);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.toggleOverlay)) toggleOverlay(client);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.cycleOverlaySource)) cycleOverlaySource(client);
        while (NbtKeyBindings.wasPressed(NbtKeyBindings.openConfig)) Mc.setScreen(client, new NbtConfigScreen(null));
    }

    private void openFor(Object client, HudSource source) {
        NbtTarget target = TargetResolver.resolve(client, source);
        if (target == null) {
            noTarget(client, source);
            return;
        }
        Mc.setScreen(client, new NbtViewerScreen(null, target));
    }

    private void copyFor(Object client, HudSource source) {
        NbtViewerConfig config = ConfigManager.get();
        NbtTarget target = TargetResolver.resolve(client, source);
        if (target == null) {
            noTarget(client, source);
            return;
        }
        String snbt = NbtFormat.toSnbt(target.nbt, config.copyFormat == CopyFormat.PRETTY, config.sortKeys);
        Mc.setClipboard(client, snbt);
        Mc.actionBar(client, Txt.colored(Txt.translatable("nbtviewer.status.copied", snbt.length()), Txt.GREEN));
    }

    private void noTarget(Object client, HudSource source) {
        Mc.actionBar(client, Txt.colored(
                Txt.translatable("nbtviewer.error.no_target", source.name().toLowerCase()), Txt.RED));
    }

    private void toggleOverlay(Object client) {
        NbtViewerConfig config = ConfigManager.get();
        config.overlayEnabled = !config.overlayEnabled;
        ConfigManager.save();
        Mc.actionBar(client, Txt.colored(Txt.translatable(
                config.overlayEnabled ? "nbtviewer.status.overlay_on" : "nbtviewer.status.overlay_off"), Txt.GREEN));
    }

    private void cycleOverlaySource(Object client) {
        NbtViewerConfig config = ConfigManager.get();
        config.overlaySource = config.overlaySource.next();
        ConfigManager.save();
        Mc.actionBar(client, Txt.translatable("nbtviewer.status.overlay_source", config.overlaySource.name()));
    }

    // ------------------------------------------------------------------ container-screen key hook
    // The only mapping-specific code left in this file: the afterKeyPress event object is
    // (key,scancode,modifiers) pre-1.21.9, a KeyInput on 1.21.9+ yarn, and a KeyEvent on Mojmap.

    private void registerContainerKeyHook() {
        //? if yarn && >=1.21.9 {
        /*ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (screen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?>) {
                ScreenKeyboardEvents.afterKeyPress(screen).register(
                        (scr, input) -> openContainerSlot(Mc.client(), scr, input));
            }
        });*/
        //?} else if yarn {
        ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (screen instanceof net.minecraft.client.gui.screen.ingame.HandledScreen<?>) {
                ScreenKeyboardEvents.afterKeyPress(screen).register(
                        (scr, key, scancode, modifiers) -> openContainerSlot(Mc.client(), scr, key, scancode));
            }
        });
        //?} else {
        /*ScreenEvents.AFTER_INIT.register((mc, screen, w, h) -> {
            if (screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?>) {
                ScreenKeyboardEvents.afterKeyPress(screen).register(
                        (scr, keyEvent) -> openContainerSlot(Mc.client(), scr, keyEvent));
            }
        });*/
        //?}
    }

    //? if yarn && >=1.21.9 {
    /*private void openContainerSlot(Object client, Object screen, net.minecraft.client.input.KeyInput input) {
        if (keyMatches((net.minecraft.client.option.KeyBinding) NbtKeyBindings.openHoveredSlot, input)
                || keyMatches((net.minecraft.client.option.KeyBinding) NbtKeyBindings.openAuto, input)) {
            NbtTarget target = TargetResolver.hoveredSlotIn(client, screen);
            if (target != null) Mc.setScreen(client, new NbtViewerScreen(screen, target));
        }
    }

    private static boolean keyMatches(net.minecraft.client.option.KeyBinding binding, net.minecraft.client.input.KeyInput input) {
        return !binding.isUnbound() && binding.matchesKey(input);
    }*/
    //?} else if yarn {
    private void openContainerSlot(Object client, Object screen, int key, int scancode) {
        if (keyMatches((net.minecraft.client.option.KeyBinding) NbtKeyBindings.openHoveredSlot, key, scancode)
                || keyMatches((net.minecraft.client.option.KeyBinding) NbtKeyBindings.openAuto, key, scancode)) {
            NbtTarget target = TargetResolver.hoveredSlotIn(client, screen);
            if (target != null) Mc.setScreen(client, new NbtViewerScreen(screen, target));
        }
    }

    private static boolean keyMatches(net.minecraft.client.option.KeyBinding binding, int key, int scancode) {
        return !binding.isUnbound() && binding.matchesKey(key, scancode);
    }
    //?} else {
    /*private void openContainerSlot(Object client, Object screen, net.minecraft.client.input.KeyEvent keyEvent) {
        if (keyMatches((net.minecraft.client.KeyMapping) NbtKeyBindings.openHoveredSlot, keyEvent)
                || keyMatches((net.minecraft.client.KeyMapping) NbtKeyBindings.openAuto, keyEvent)) {
            NbtTarget target = TargetResolver.hoveredSlotIn(client, screen);
            if (target != null) Mc.setScreen(client, new NbtViewerScreen(screen, target));
        }
    }

    private static boolean keyMatches(net.minecraft.client.KeyMapping binding, net.minecraft.client.input.KeyEvent keyEvent) {
        return !binding.isUnbound() && binding.matches(keyEvent);
    }
    *///?}
}
