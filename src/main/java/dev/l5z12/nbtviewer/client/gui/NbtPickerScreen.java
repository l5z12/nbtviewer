// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.gui;

import java.util.List;

import dev.l5z12.nbtviewer.client.target.NbtTarget;
import dev.l5z12.nbtviewer.facade.Gfx;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Txt;
import dev.l5z12.nbtviewer.facade.Ui;
import org.lwjgl.glfw.GLFW;

/**
 * A scrollable chooser shown when a lookup (a selector, or a bare entity type) matches more than one
 * candidate. Each row is a {@link Choice}; picking one opens the tree view for that target. Written
 * once against the facades + {@link NbtScreenBase}, so it is mapping-agnostic.
 */
public final class NbtPickerScreen extends NbtScreenBase {

    /** One selectable candidate: a display label and a lazily-built target (null if it went away). */
    public interface Choice {
        Object label();

        NbtTarget build();
    }

    private final Object parent;
    private final List<Choice> choices;

    private int scrollRow;
    private int selectedIndex;
    private int listTop, listBottom, listLeft, listRight, rowH;

    public NbtPickerScreen(Object parent, Object title, List<Choice> choices) {
        super(title);
        this.parent = parent;
        this.choices = choices;
    }

    @Override
    protected void init() {
        clearWidgets();
        rowH = Gfx.lineHeight(font()) + 6;
        listTop = 36;
        listBottom = this.height - 40;
        int half = Math.min(200, this.width / 2 - 20);
        listLeft = this.width / 2 - half;
        listRight = this.width / 2 + half;
        addWidget(Ui.button(Txt.translatable("nbtviewer.gui.close"),
                this.width / 2 - 50, this.height - 28, 100, 20, this::closeSelf));
        clampScroll();
    }

    @Override
    protected void renderBackdrop(Object g) {
        Gfx.fill(g, 0, 0, this.width, this.height, 0xC8100016);
    }

    @Override
    protected void renderContent(Object g, int mouseX, int mouseY, float delta) {
        Gfx.centeredText(g, font(), this.getTitle(), this.width / 2, 12, 0xFFFFFFFF);
        Object sub = Txt.colored(Txt.translatable("nbtviewer.picker.subtitle", choices.size()), Txt.GRAY);
        Gfx.centeredText(g, font(), sub, this.width / 2, 24, 0xFFAAAAAA);

        Gfx.fill(g, listLeft - 2, listTop - 2, listRight + 2, listBottom + 2, 0x88000000);

        int rows = visibleRows();
        int textOffset = (rowH - Gfx.lineHeight(font())) / 2;
        Gfx.scissorOn(g, listLeft - 2, listTop - 2, listRight + 2, listBottom + 2);
        for (int i = 0; i < rows; i++) {
            int index = scrollRow + i;
            if (index < 0 || index >= choices.size()) break;
            int y = listTop + i * rowH;
            boolean hovered = mouseX >= listLeft && mouseX <= listRight && mouseY >= y && mouseY < y + rowH;
            if (index == selectedIndex) {
                Gfx.fill(g, listLeft - 2, y, listRight + 2, y + rowH - 1, 0x804466AA);
            } else if (hovered) {
                Gfx.fill(g, listLeft - 2, y, listRight + 2, y + rowH - 1, 0x30FFFFFF);
            }
            Gfx.text(g, font(), choices.get(index).label(), listLeft + 4, y + textOffset, 0xFFFFFFFF);
        }
        Gfx.scissorOff(g);
        renderScrollbar(g);
    }

    private void renderScrollbar(Object g) {
        int rows = visibleRows();
        int total = choices.size();
        if (total <= rows) return;
        int trackX = listRight;
        int trackHeight = listBottom - listTop;
        Gfx.fill(g, trackX, listTop, trackX + 3, listBottom, 0x40FFFFFF);
        int thumbHeight = Math.max(12, (int) ((long) trackHeight * rows / total));
        int maxScroll = total - rows;
        int thumbY = listTop + (maxScroll == 0 ? 0 : (int) ((long) (trackHeight - thumbHeight) * scrollRow / maxScroll));
        Gfx.fill(g, trackX, thumbY, trackX + 3, thumbY + thumbHeight, 0xC0FFFFFF);
    }

    @Override
    protected boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (mouseX >= listLeft - 2 && mouseX <= listRight + 2 && mouseY >= listTop && mouseY <= listBottom) {
            int index = scrollRow + (int) ((mouseY - listTop) / rowH);
            if (index >= 0 && index < choices.size()) {
                selectedIndex = index;
                open(index);
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean onKeyPressed(int keyCode, int modifiers) {
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> { move(-1); return true; }
            case GLFW.GLFW_KEY_DOWN -> { move(1); return true; }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_SPACE -> { open(selectedIndex); return true; }
            default -> { return false; }
        }
    }

    @Override
    public void onScreenScroll(double vertical) {
        scrollRow -= (int) Math.signum(vertical) * 3;
        clampScroll();
    }

    private void move(int delta) {
        if (choices.isEmpty()) return;
        selectedIndex = Math.max(0, Math.min(choices.size() - 1, selectedIndex + delta));
        if (selectedIndex < scrollRow) scrollRow = selectedIndex;
        int rows = visibleRows();
        if (selectedIndex >= scrollRow + rows) scrollRow = selectedIndex - rows + 1;
        clampScroll();
    }

    private void open(int index) {
        if (index < 0 || index >= choices.size()) return;
        NbtTarget target = choices.get(index).build();
        if (target != null) {
            Mc.setScreen(mc(), new NbtViewerScreen(this, target));
        }
    }

    private int visibleRows() {
        return Math.max(1, (listBottom - listTop) / rowH);
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, choices.size() - visibleRows());
        scrollRow = Math.max(0, Math.min(maxScroll, scrollRow));
    }

    @Override
    protected void onCloseScreen() {
        Mc.setScreen(mc(), parent);
    }

    @Override
    protected boolean pauses() {
        return false;
    }
}
