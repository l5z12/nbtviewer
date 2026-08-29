// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.keybind;

import org.lwjgl.glfw.GLFW;

//? if yarn {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
//?} else {
/*import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;*/
//?}

/**
 * Every action is its own rebindable key ("configure what hotkey reviews what").
 * Only "open (auto)" is bound by default (N); the rest are unbound so they never clash —
 * bind them in Options ▸ Controls ▸ NBT Viewer.
 *
 * <p>The bindings are held as {@code Object} and read through {@link #isPressed}/{@link #wasPressed}/
 * {@link #boundKeyLabel}, so callers stay mapping-agnostic while this file absorbs the
 * {@code KeyBinding} (yarn) vs {@code KeyMapping} (Mojmap) split and the 1.21.9 {@code Category} API.
 */
public final class NbtKeyBindings {

    private static final Object CATEGORY =
            //? if yarn && >=1.21.9 {
            /*net.minecraft.client.option.KeyBinding.Category.create(net.minecraft.util.Identifier.of("nbtviewer", "general"));*/
            //?} else if yarn {
            "key.category.nbtviewer";
            //?} else {
            /*net.minecraft.client.KeyMapping.Category.register(net.minecraft.resources.Identifier.fromNamespaceAndPath("nbtviewer", "general"));*/
            //?}

    public static Object openAuto;
    public static Object openHeldItem;
    public static Object openHoveredSlot;
    public static Object openTargetBlock;
    public static Object openTargetEntity;
    public static Object copyAuto;
    public static Object copyHeldItem;
    public static Object toggleOverlay;
    public static Object cycleOverlaySource;
    public static Object holdOverlay;
    public static Object openConfig;

    private NbtKeyBindings() {
    }

    public static void register() {
        openAuto = reg("open_auto", GLFW.GLFW_KEY_N);
        openHeldItem = reg("open_held", GLFW.GLFW_KEY_UNKNOWN);
        openHoveredSlot = reg("open_slot", GLFW.GLFW_KEY_UNKNOWN);
        openTargetBlock = reg("open_block", GLFW.GLFW_KEY_UNKNOWN);
        openTargetEntity = reg("open_entity", GLFW.GLFW_KEY_UNKNOWN);
        copyAuto = reg("copy_auto", GLFW.GLFW_KEY_UNKNOWN);
        copyHeldItem = reg("copy_held", GLFW.GLFW_KEY_UNKNOWN);
        toggleOverlay = reg("toggle_overlay", GLFW.GLFW_KEY_UNKNOWN);
        cycleOverlaySource = reg("cycle_overlay_source", GLFW.GLFW_KEY_UNKNOWN);
        holdOverlay = reg("hold_overlay", GLFW.GLFW_KEY_UNKNOWN);
        openConfig = reg("open_config", GLFW.GLFW_KEY_UNKNOWN);
    }

    private static Object reg(String name, int key) {
        //? if yarn && >=1.21.9 {
        /*return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.nbtviewer." + name,
                InputUtil.Type.KEYSYM, key, (net.minecraft.client.option.KeyBinding.Category) CATEGORY));*/
        //?} else if yarn {
        return KeyBindingHelper.registerKeyBinding(new KeyBinding("key.nbtviewer." + name,
                InputUtil.Type.KEYSYM, key, (String) CATEGORY));
        //?} else {
        /*return KeyMappingHelper.registerKeyMapping(new KeyMapping("key.nbtviewer." + name,
                InputConstants.Type.KEYSYM, key, (net.minecraft.client.KeyMapping.Category) CATEGORY));*/
        //?}
    }

    /** {@code true} while the key is held down (used for the hold-to-show overlay). */
    public static boolean isPressed(Object binding) {
        //? if yarn {
        return ((KeyBinding) binding).isPressed();
        //?} else {
        /*return ((KeyMapping) binding).isDown();*/
        //?}
    }

    /** Consumes one queued press; {@code true} once per key-down (used for one-shot actions). */
    public static boolean wasPressed(Object binding) {
        //? if yarn {
        return ((KeyBinding) binding).wasPressed();
        //?} else {
        /*return ((KeyMapping) binding).consumeClick();*/
        //?}
    }

    /** Localised name of the currently-bound key, e.g. "N" (for hint text). */
    public static String boundKeyLabel(Object binding) {
        //? if yarn {
        return ((KeyBinding) binding).getBoundKeyLocalizedText().getString();
        //?} else {
        /*return ((KeyMapping) binding).getTranslatedKeyMessage().getString();*/
        //?}
    }
}
