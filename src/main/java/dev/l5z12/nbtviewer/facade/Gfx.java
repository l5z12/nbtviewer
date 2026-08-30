// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.facade;

//? if yarn {
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;*/
//?}

/**
 * Drawing facade. The render context is taken as {@code Object} and cast inside — its type is the one
 * thing that changes on nearly every generation (yarn {@code DrawContext}; Mojmap 26.x
 * {@code GuiGraphicsExtractor} with its {@code text}/{@code fill}/{@code pose} API) and the HUD-scale
 * matrix flipped from {@code MatrixStack} to {@code Matrix3x2fStack} at 1.21.6.
 */
public final class Gfx {

    private Gfx() {
    }

    public static void text(Object ctx, Object font, Object text, int x, int y, int color) {
        //? if yarn {
        ((DrawContext) ctx).drawTextWithShadow((TextRenderer) font, (Text) text, x, y, color);
        //?} else {
        /*((GuiGraphicsExtractor) ctx).text((Font) font, (Component) text, x, y, color, true);*/
        //?}
    }

    public static void centeredText(Object ctx, Object font, Object text, int centerX, int y, int color) {
        //? if yarn {
        ((DrawContext) ctx).drawCenteredTextWithShadow((TextRenderer) font, (Text) text, centerX, y, color);
        //?} else {
        /*((GuiGraphicsExtractor) ctx).centeredText((Font) font, (Component) text, centerX, y, color);*/
        //?}
    }

    public static void fill(Object ctx, int x1, int y1, int x2, int y2, int color) {
        //? if yarn {
        ((DrawContext) ctx).fill(x1, y1, x2, y2, color);
        //?} else {
        /*((GuiGraphicsExtractor) ctx).fill(x1, y1, x2, y2, color);*/
        //?}
    }

    public static void scissorOn(Object ctx, int x1, int y1, int x2, int y2) {
        //? if yarn {
        ((DrawContext) ctx).enableScissor(x1, y1, x2, y2);
        //?} else {
        /*((GuiGraphicsExtractor) ctx).enableScissor(x1, y1, x2, y2);*/
        //?}
    }

    public static void scissorOff(Object ctx) {
        //? if yarn {
        ((DrawContext) ctx).disableScissor();
        //?} else {
        /*((GuiGraphicsExtractor) ctx).disableScissor();*/
        //?}
    }

    public static void pushScale(Object ctx, float scale) {
        //? if yarn && <1.21.6 {
        var matrices = ((DrawContext) ctx).getMatrices();
        matrices.push();
        matrices.scale(scale, scale, 1.0f);
        //?} else if yarn {
        /*var matrices = ((DrawContext) ctx).getMatrices();
        matrices.pushMatrix();
        matrices.scale(scale, scale);*/
        //?} else {
        /*var matrices = ((GuiGraphicsExtractor) ctx).pose();
        matrices.pushMatrix();
        matrices.scale(scale, scale);*/
        //?}
    }

    public static void popScale(Object ctx) {
        //? if yarn && <1.21.6 {
        ((DrawContext) ctx).getMatrices().pop();
        //?} else if yarn {
        /*((DrawContext) ctx).getMatrices().popMatrix();*/
        //?} else {
        /*((GuiGraphicsExtractor) ctx).pose().popMatrix();*/
        //?}
    }

    public static int textWidth(Object font, Object text) {
        //? if yarn {
        return ((TextRenderer) font).getWidth((Text) text);
        //?} else {
        /*return ((Font) font).width((Component) text);*/
        //?}
    }

    /** Rendered width, in pixels, of a plain (unstyled) string. */
    public static int textWidth(Object font, String s) {
        //? if yarn {
        return ((TextRenderer) font).getWidth(s);
        //?} else {
        /*return ((Font) font).width(s);*/
        //?}
    }

    /** The longest prefix of {@code s} whose rendered width does not exceed {@code width} px. */
    public static String trimToWidth(Object font, String s, int width) {
        if (width <= 0) return "";
        //? if yarn {
        return ((TextRenderer) font).trimToWidth(s, width);
        //?} else {
        /*return ((Font) font).plainSubstrByWidth(s, width);*/
        //?}
    }

    public static int lineHeight(Object font) {
        //? if yarn {
        return ((TextRenderer) font).fontHeight;
        //?} else {
        /*return ((Font) font).lineHeight;*/
        //?}
    }
}
