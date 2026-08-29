// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.gui;

import java.util.ArrayList;
import java.util.List;

import dev.l5z12.nbtviewer.facade.Ui;
import org.lwjgl.glfw.GLFW;

//? if yarn {
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
//?} else {
/*import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;*/
//?}

/**
 * Base screen that absorbs every screen-shaped mapping/version split so the two concrete screens
 * ({@link NbtViewerScreen}, {@link NbtConfigScreen}) are written once against {@code Object} and the
 * facades. This file carries the three input/render tiers:
 * <ul>
 *   <li>classic yarn (≤1.21.8): {@code render(DrawContext)}, {@code mouseClicked(double,double,int)},
 *       {@code keyPressed(int,int,int)};</li>
 *   <li>modern yarn (1.21.9+): input event objects {@code Click}/{@code KeyInput};</li>
 *   <li>Mojmap (26.x): retained render via {@code extractRenderState(GuiGraphicsExtractor,…)} and
 *       {@code MouseButtonEvent}/{@code KeyEvent} input.</li>
 * </ul>
 * Subclasses override the {@code renderContent} / {@code on*} / {@code onCloseScreen} / {@code pauses}
 * hooks; return {@code true} from an input hook to consume the event.
 */
public abstract class NbtScreenBase extends Screen {

    protected NbtScreenBase(Object title) {
        //? if yarn {
        super((Text) title);
        //?} else {
        /*super((Component) title);*/
        //?}
    }

    /** Widgets added through {@link #addWidget}; rendered manually on yarn (where we skip the vanilla
     * {@code super.render} so our solid backdrop is not overwritten by the default screen background). */
    private final List<Object> widgets = new ArrayList<>();

    // ------------------------------------------------------------------ subclass hooks
    /** Drawn before the widgets — the place for a full-screen background fill. */
    protected void renderBackdrop(Object graphics) {
    }

    /** Drawn after the widgets — foreground content (headers, the tree, footers, …). */
    protected void renderContent(Object graphics, int mouseX, int mouseY, float delta) {
    }

    protected boolean onMouseClick(double x, double y, int button) {
        return false;
    }

    protected boolean onMouseDrag(double x, double y, int button) {
        return false;
    }

    protected boolean onMouseRelease(double x, double y, int button) {
        return false;
    }

    protected boolean onKeyPressed(int keyCode, int modifiers) {
        return false;
    }

    protected void onCloseScreen() {
    }

    protected boolean pauses() {
        return true;
    }

    /** Mouse-wheel scroll, fed from the Fabric screen-scroll hook (its signature changed twice across
     * the version range, so it's routed through the base). Override in scrollable screens. */
    public void onScreenScroll(double vertical) {
    }

    // ------------------------------------------------------------------ uniform accessors
    protected Object mc() {
        //? if yarn {
        return this.client;
        //?} else {
        /*return this.minecraft;*/
        //?}
    }

    protected Object font() {
        //? if yarn {
        return this.textRenderer;
        //?} else {
        /*return this.font;*/
        //?}
    }

    protected void addWidget(Object widget) {
        widgets.add(widget);
        //? if yarn {
        addDrawableChild((net.minecraft.client.gui.widget.ClickableWidget) widget);
        //?} else {
        /*addRenderableWidget((net.minecraft.client.gui.components.AbstractWidget) widget);*/
        //?}
    }

    /** Subclasses that rebuild their widget set in {@code init} should clear the manual-render list
     * first (vanilla clears its own child list on re-init). */
    protected void clearWidgets() {
        widgets.clear();
    }

    protected void focus(Object widget) {
        //? if yarn {
        this.setFocused((net.minecraft.client.gui.Element) widget);
        //?} else {
        /*this.setFocused((net.minecraft.client.gui.components.events.GuiEventListener) widget);*/
        //?}
    }

    protected final void closeSelf() {
        //? if yarn {
        this.close();
        //?} else {
        /*this.onClose();*/
        //?}
    }

    // ------------------------------------------------------------------ render entry
    // Order is backdrop → widgets → foreground on both mappings. On yarn we render the widgets
    // ourselves (rather than via super.render) so the default screen background does not paint over
    // our solid backdrop; on Mojmap the retained render model draws the registered widgets in
    // super.extractRenderState.
    //? if yarn {
    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackdrop(context);
        for (Object widget : widgets) {
            Ui.renderWidget(context, widget, mouseX, mouseY, delta);
        }
        renderContent(context, mouseX, mouseY, delta);
    }
    //?} else {
    /*@Override
    public void extractRenderState(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        renderBackdrop(graphics);
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        renderContent(graphics, mouseX, mouseY, delta);
    }*/
    //?}

    // ------------------------------------------------------------------ input
    //? if yarn && >=1.21.9 {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
        return onMouseClick(click.x(), click.y(), click.button()) || super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.gui.Click click, double offsetX, double offsetY) {
        return onMouseDrag(click.x(), click.y(), click.button()) || super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.gui.Click click) {
        return onMouseRelease(click.x(), click.y(), click.button()) || super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyInput input) {
        return onKeyPressed(input.key(), modifiers()) || super.keyPressed(input);
    }*/
    //?} else if yarn {
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return onMouseClick(mouseX, mouseY, button) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return onMouseDrag(mouseX, mouseY, button) || super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return onMouseRelease(mouseX, mouseY, button) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return onKeyPressed(keyCode, modifiers()) || super.keyPressed(keyCode, scanCode, modifiers);
    }
    //?} else {
    /*@Override
    public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
        return onMouseClick(event.x(), event.y(), event.button()) || super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseDragged(net.minecraft.client.input.MouseButtonEvent event, double dragX, double dragY) {
        return onMouseDrag(event.x(), event.y(), event.button()) || super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(net.minecraft.client.input.MouseButtonEvent event) {
        return onMouseRelease(event.x(), event.y(), event.button()) || super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
        return onKeyPressed(event.key(), event.modifiers()) || super.keyPressed(event);
    }*/
    //?}

    // ------------------------------------------------------------------ close / pause
    //? if yarn {
    @Override
    public void close() {
        onCloseScreen();
    }

    @Override
    public boolean shouldPause() {
        return pauses();
    }
    //?} else {
    /*@Override
    public void onClose() {
        onCloseScreen();
    }

    @Override
    public boolean isPauseScreen() {
        return pauses();
    }*/
    //?}

    // ------------------------------------------------------------------ modifier keys (yarn only)
    // KeyInput exposes no modifiers() accessor and Screen.hasControlDown was removed on newer yarn,
    // so read the live keyboard state (isKeyPressed takes the Window at 1.21.9+, the raw handle
    // before). Mojmap gets modifiers straight off the KeyEvent instead, so needs no equivalent.
    //? if yarn && >=1.21.9 {
    /*protected static int modifiers() {
        var window = net.minecraft.client.MinecraftClient.getInstance().getWindow();
        boolean ctrl = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean shift = InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
        return (ctrl ? GLFW.GLFW_MOD_CONTROL : 0) | (shift ? GLFW.GLFW_MOD_SHIFT : 0);
    }*/
    //?} else if yarn {
    protected static int modifiers() {
        long handle = net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle();
        boolean ctrl = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_CONTROL) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_CONTROL);
        boolean shift = InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_RIGHT_SHIFT);
        return (ctrl ? GLFW.GLFW_MOD_CONTROL : 0) | (shift ? GLFW.GLFW_MOD_SHIFT : 0);
    }
    //?}
}
