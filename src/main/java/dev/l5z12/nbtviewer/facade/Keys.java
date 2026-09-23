// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.facade;

//? if <26.3 {
import org.lwjgl.glfw.GLFW;
//?} else {
/*import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.sdl.SDLScancode;*/
//?}

/** Input constants: GLFW before 26.3, vanilla SDL scancodes and modifier masks thereafter. */
public final class Keys {
    private Keys() {}

    //? if <26.3 {
    public static final int N = GLFW.GLFW_KEY_N;
    public static final int B = GLFW.GLFW_KEY_B;
    public static final int C = GLFW.GLFW_KEY_C;
    public static final int S = GLFW.GLFW_KEY_S;
    public static final int F = GLFW.GLFW_KEY_F;
    public static final int UP = GLFW.GLFW_KEY_UP;
    public static final int DOWN = GLFW.GLFW_KEY_DOWN;
    public static final int LEFT = GLFW.GLFW_KEY_LEFT;
    public static final int RIGHT = GLFW.GLFW_KEY_RIGHT;
    public static final int EQUAL = GLFW.GLFW_KEY_EQUAL;
    public static final int KP_ADD = GLFW.GLFW_KEY_KP_ADD;
    public static final int MINUS = GLFW.GLFW_KEY_MINUS;
    public static final int ENTER = GLFW.GLFW_KEY_ENTER;
    public static final int KP_ENTER = GLFW.GLFW_KEY_KP_ENTER;
    public static final int SPACE = GLFW.GLFW_KEY_SPACE;
    public static final int KP_SUBTRACT = GLFW.GLFW_KEY_KP_SUBTRACT;
    public static final int UNKNOWN = GLFW.GLFW_KEY_UNKNOWN;
    public static final int CONTROL = GLFW.GLFW_MOD_CONTROL;
    public static final int SHIFT = GLFW.GLFW_MOD_SHIFT;
    //?} else {
    /*public static final int N = InputConstants.KEY_N;
    public static final int B = InputConstants.KEY_B;
    public static final int C = InputConstants.KEY_C;
    public static final int S = InputConstants.KEY_S;
    public static final int F = InputConstants.KEY_F;
    public static final int UP = InputConstants.KEY_UP;
    public static final int DOWN = InputConstants.KEY_DOWN;
    public static final int LEFT = InputConstants.KEY_LEFT;
    public static final int RIGHT = InputConstants.KEY_RIGHT;
    public static final int EQUAL = InputConstants.KEY_EQUALS;
    public static final int KP_ADD = InputConstants.KEY_ADD;
    public static final int MINUS = InputConstants.KEY_MINUS;
    public static final int ENTER = InputConstants.KEY_RETURN;
    public static final int KP_ENTER = InputConstants.KEY_NUMPADENTER;
    public static final int SPACE = InputConstants.KEY_SPACE;
    // Vanilla exposes no constant for keypad minus or the unknown scancode.
    public static final int KP_SUBTRACT = SDLScancode.SDL_SCANCODE_KP_MINUS;
    public static final int UNKNOWN = SDLScancode.SDL_SCANCODE_UNKNOWN;
    public static final int CONTROL = InputConstants.MOD_CONTROL;
    public static final int SHIFT = InputConstants.MOD_SHIFT;*/
    //?}
}
