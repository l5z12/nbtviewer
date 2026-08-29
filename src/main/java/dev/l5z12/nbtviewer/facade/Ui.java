// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.facade;

import java.util.function.Consumer;

//? if yarn {
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;*/
//?}

/** Widget facade: button / edit-box creation, rendering and mutation, all on {@code Object}. */
public final class Ui {

    private Ui() {
    }

    public static Object button(Object text, int x, int y, int w, int h, Runnable onPress) {
        //? if yarn {
        return ButtonWidget.builder((Text) text, b -> onPress.run()).dimensions(x, y, w, h).build();
        //?} else {
        /*return Button.builder((Component) text, b -> onPress.run()).bounds(x, y, w, h).build();*/
        //?}
    }

    public static Object editBox(Object font, int x, int y, int w, int h, Object label) {
        //? if yarn {
        return new TextFieldWidget((TextRenderer) font, x, y, w, h, (Text) label);
        //?} else {
        /*return new EditBox((Font) font, x, y, w, h, (Component) label);*/
        //?}
    }

    /** Manual widget render — used only on yarn, where {@link NbtScreenBase} draws widgets itself.
     * On 26.x the retained render model draws registered widgets in {@code super.extractRenderState},
     * so this is never called there. */
    public static void renderWidget(Object ctx, Object widget, int mouseX, int mouseY, float delta) {
        //? if yarn {
        ((ClickableWidget) widget).render((DrawContext) ctx, mouseX, mouseY, delta);
        //?} else {
        /*throw new UnsupportedOperationException("widgets render via extractRenderState on 26.x");*/
        //?}
    }

    public static void setMessage(Object widget, Object text) {
        //? if yarn {
        ((ClickableWidget) widget).setMessage((Text) text);
        //?} else {
        /*((AbstractWidget) widget).setMessage((Component) text);*/
        //?}
    }

    public static void editMaxLength(Object editBox, int max) {
        //? if yarn {
        ((TextFieldWidget) editBox).setMaxLength(max);
        //?} else {
        /*((EditBox) editBox).setMaxLength(max);*/
        //?}
    }

    public static void editValue(Object editBox, String value) {
        //? if yarn {
        ((TextFieldWidget) editBox).setText(value);
        //?} else {
        /*((EditBox) editBox).setValue(value);*/
        //?}
    }

    public static String editValue(Object editBox) {
        //? if yarn {
        return ((TextFieldWidget) editBox).getText();
        //?} else {
        /*return ((EditBox) editBox).getValue();*/
        //?}
    }

    public static void editResponder(Object editBox, Consumer<String> responder) {
        //? if yarn {
        ((TextFieldWidget) editBox).setChangedListener(responder);
        //?} else {
        /*((EditBox) editBox).setResponder(responder);*/
        //?}
    }

    public static void editFocused(Object editBox, boolean focused) {
        //? if yarn {
        ((TextFieldWidget) editBox).setFocused(focused);
        //?} else {
        /*((EditBox) editBox).setFocused(focused);*/
        //?}
    }

    public static boolean editIsFocused(Object editBox) {
        //? if yarn {
        return ((TextFieldWidget) editBox).isFocused();
        //?} else {
        /*return ((EditBox) editBox).isFocused();*/
        //?}
    }
}
