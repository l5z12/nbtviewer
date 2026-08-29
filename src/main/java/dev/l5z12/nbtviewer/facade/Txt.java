// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.facade;

//? if yarn {
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
//?} else {
/*import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;*/
//?}

/**
 * Text-factory facade. Callers pass text around as {@code Object} and never name {@code Text} vs
 * {@code Component}; only this file (and the drawing/widget facades that consume the value) carries
 * the yarn/Mojmap split. Colours are plain RGB so no {@code Formatting}/{@code ChatFormatting} leaks.
 */
public final class Txt {

    private Txt() {
    }

    public static final int AQUA = 0x55FFFF;
    public static final int GREEN = 0x55FF55;
    public static final int GOLD = 0xFFAA00;
    public static final int GRAY = 0xAAAAAA;
    public static final int DARK_GRAY = 0x555555;
    public static final int DARK_AQUA = 0x00AAAA;
    public static final int WHITE = 0xFFFFFF;
    public static final int YELLOW = 0xFFFF55;
    public static final int RED = 0xFF5555;

    public static Object literal(String s) {
        //? if yarn {
        return Text.literal(s);
        //?} else {
        /*return Component.literal(s);*/
        //?}
    }

    public static Object translatable(String key, Object... args) {
        //? if yarn {
        return Text.translatable(key, args);
        //?} else {
        /*return Component.translatable(key, args);*/
        //?}
    }

    public static Object empty() {
        //? if yarn {
        return Text.empty();
        //?} else {
        /*return Component.empty();*/
        //?}
    }

    public static Object append(Object base, Object add) {
        //? if yarn {
        return ((MutableText) base).append((Text) add);
        //?} else {
        /*return ((MutableComponent) base).append((Component) add);*/
        //?}
    }

    public static Object colored(Object text, int rgb) {
        //? if yarn {
        return ((MutableText) text).styled(style -> style.withColor(TextColor.fromRgb(rgb)));
        //?} else {
        /*return ((MutableComponent) text).withColor(rgb);*/
        //?}
    }

    public static Object copy(Object text) {
        //? if yarn {
        return ((Text) text).copy();
        //?} else {
        /*return ((Component) text).copy();*/
        //?}
    }

    public static String str(Object text) {
        //? if yarn {
        return ((Text) text).getString();
        //?} else {
        /*return ((Component) text).getString();*/
        //?}
    }
}
