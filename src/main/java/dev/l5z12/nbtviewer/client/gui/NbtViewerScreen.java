// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dev.l5z12.nbtviewer.client.config.ConfigManager;
import dev.l5z12.nbtviewer.client.config.CopyFormat;
import dev.l5z12.nbtviewer.client.config.NbtViewerConfig;
import dev.l5z12.nbtviewer.client.nbt.NbtFormat;
import dev.l5z12.nbtviewer.client.nbt.NbtText;
import dev.l5z12.nbtviewer.client.target.NbtTarget;
import dev.l5z12.nbtviewer.facade.Gfx;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Nbt;
import dev.l5z12.nbtviewer.facade.Txt;
import dev.l5z12.nbtviewer.facade.Ui;
import org.lwjgl.glfw.GLFW;

/**
 * Full-screen, searchable, collapsible NBT tree view with copy-to-clipboard. Written once against
 * the facades ({@link Gfx}/{@link Txt}/{@link Nbt}/{@link Ui}/{@link Mc}) and {@link NbtScreenBase},
 * so it is identical on every mapping from 1.20 (yarn) through 26.x (Mojmap).
 *
 * <p>Scrolling is fed in through {@link #scrollBy} from the Fabric screen-scroll hook registered in
 * {@code Mc.registerScreenScroll} (its signature changed twice across the range).
 */
public final class NbtViewerScreen extends NbtScreenBase {

    private final Object parent;
    private final NbtTarget target;
    private final NbtViewerConfig config = ConfigManager.get();

    private NbtNode root;
    private final List<NbtNode> visible = new ArrayList<>();
    private int scrollRow;
    private int selectedIndex = -1;
    private String query = "";
    private String status = "";
    private long statusUntil;

    private Object searchField;
    private Object sortButton;

    // Layout (recomputed in init)
    private int treeTop, treeBottom, treeLeft, treeRight, rowH;
    private boolean draggingScrollbar;

    public NbtViewerScreen(Object parent, NbtTarget target) {
        super(Txt.translatable("nbtviewer.gui.title"));
        this.parent = parent;
        this.target = target;
        this.root = NbtNode.root(target.nbt, config.autoExpandDepth, config.sortKeys);
    }

    @Override
    protected void init() {
        clearWidgets();
        rowH = Gfx.lineHeight(font()) + 3;
        treeTop = 36;
        treeBottom = this.height - 30;
        treeLeft = 10;
        treeRight = this.width - 10;

        // Search field (top-right)
        int searchW = Math.min(200, this.width / 3);
        searchField = Ui.editBox(font(), this.width - searchW - 10, 8, searchW, 16,
                Txt.translatable("nbtviewer.gui.search"));
        Ui.editMaxLength(searchField, 256);
        Ui.editValue(searchField, query);
        Ui.editResponder(searchField, this::onSearchChanged);
        addWidget(searchField);

        // Bottom button row
        int y = this.height - 24;
        int x = treeLeft;
        x = addButton(x, y, 70, Txt.translatable("nbtviewer.gui.copy_all"), this::copyAll);
        x = addButton(x, y, 78, Txt.translatable("nbtviewer.gui.copy_node"), this::copySelected);
        x = addButton(x, y, 74, Txt.translatable("nbtviewer.gui.copy_path"), this::copyPath);
        x = addButton(x, y, 74, Txt.translatable("nbtviewer.gui.expand_all"), () -> setAllExpanded(true));
        x = addButton(x, y, 82, Txt.translatable("nbtviewer.gui.collapse_all"), () -> setAllExpanded(false));
        sortButton = Ui.button(sortLabel(), x + 4, y, 78, 20, this::toggleSort);
        addWidget(sortButton);
        x += 82;
        addButton(x, y, 54, Txt.translatable("nbtviewer.gui.close"), this::closeSelf);

        rebuildVisible();
        clampScroll();
    }

    private int addButton(int x, int y, int w, Object text, Runnable action) {
        addWidget(Ui.button(text, x + 4, y, w, 20, action));
        return x + 4 + w;
    }

    // ------------------------------------------------------------------ rendering

    @Override
    protected void renderBackdrop(Object g) {
        Gfx.fill(g, 0, 0, this.width, this.height, 0xC8100016);
    }

    @Override
    protected void renderContent(Object g, int mouseX, int mouseY, float delta) {
        // Header
        Gfx.text(g, font(), this.target.title, treeLeft, 8, 0xFFFFFFFF);
        int tags = NbtFormat.countTags(target.nbt);
        int bytes = NbtFormat.toSnbt(target.nbt, false, false).length();
        Object sub = Txt.colored(Txt.literal(
                kindLabel() + "  ·  " + target.subtitle + "  ·  " + tags + " tags  ·  " + bytes + " B"), Txt.GRAY);
        Gfx.text(g, font(), sub, treeLeft, 22, 0xFFAAAAAA);

        // Tree panel
        Gfx.fill(g, treeLeft - 2, treeTop - 2, treeRight + 2, treeBottom + 2, 0x88000000);

        int visibleRows = visibleRows();
        Gfx.scissorOn(g, treeLeft - 2, treeTop - 2, treeRight + 2, treeBottom + 2);
        for (int i = 0; i < visibleRows; i++) {
            int index = scrollRow + i;
            if (index < 0 || index >= visible.size()) break;
            renderRow(g, visible.get(index), index, treeTop + i * rowH, mouseX, mouseY);
        }
        Gfx.scissorOff(g);

        renderScrollbar(g);

        // Footer status / hint
        Object footer;
        if (System.currentTimeMillis() < statusUntil && !status.isEmpty()) {
            footer = Txt.colored(Txt.literal(status), Txt.GREEN);
        } else {
            footer = Txt.colored(Txt.translatable("nbtviewer.gui.hint"), Txt.DARK_GRAY);
        }
        Gfx.text(g, font(), footer, treeLeft, this.height - 38, 0xFF808080);
    }

    private void renderRow(Object g, NbtNode node, int index, int y, int mouseX, int mouseY) {
        boolean hovered = mouseX >= treeLeft && mouseX <= treeRight && mouseY >= y && mouseY < y + rowH;
        if (index == selectedIndex) {
            Gfx.fill(g, treeLeft - 2, y - 1, treeRight + 2, y + rowH - 1, 0x804466AA);
        } else if (hovered) {
            Gfx.fill(g, treeLeft - 2, y - 1, treeRight + 2, y + rowH - 1, 0x30FFFFFF);
        }
        if (!query.isEmpty() && node.matches(query)) {
            Gfx.fill(g, treeLeft - 2, y - 1, treeLeft, y + rowH - 1, 0xFFEEDD44);
        }

        int indent = treeLeft + node.depth * 12;
        if (node.isContainer() && node.hasChildren()) {
            Object marker = Txt.colored(Txt.literal(node.expanded ? "-" : "+"), Txt.YELLOW);
            Gfx.text(g, font(), marker, indent, y, 0xFFFFFF55);
        }
        Gfx.text(g, font(), buildLabel(node), indent + 10, y, 0xFFFFFFFF);
    }

    private Object buildLabel(NbtNode node) {
        Object label = Txt.empty();
        if (node.listIndex) {
            Txt.append(label, Txt.colored(Txt.literal("[" + node.key + "]: "), Txt.DARK_AQUA));
        } else if (!node.key.isEmpty()) {
            Txt.append(label, NbtText.keyText(node.key, config.colorize));
            Txt.append(label, Txt.colored(Txt.literal(": "), Txt.GRAY));
        }

        if (node.isContainer()) {
            boolean compound = Nbt.isCompound(node.value);
            String braces = compound ? "{…}" : "[…]";
            if (node.size() == 0) braces = compound ? "{}" : "[]";
            Txt.append(label, Txt.colored(Txt.literal(braces), Txt.GRAY));
            Txt.append(label, Txt.colored(Txt.literal(" (" + node.size() + ")"), Txt.DARK_GRAY));
        } else {
            Txt.append(label, NbtText.leafText(node.value, config.colorize, 512));
        }
        return label;
    }

    private void renderScrollbar(Object g) {
        int total = visible.size();
        int rows = visibleRows();
        if (total <= rows) return;
        int trackX = treeRight - 2;
        int trackHeight = treeBottom - treeTop;
        Gfx.fill(g, trackX, treeTop, trackX + 3, treeBottom, 0x40FFFFFF);
        int thumbHeight = Math.max(12, (int) ((long) trackHeight * rows / total));
        int maxScroll = total - rows;
        int thumbY = treeTop + (maxScroll == 0 ? 0 : (int) ((long) (trackHeight - thumbHeight) * scrollRow / maxScroll));
        Gfx.fill(g, trackX, thumbY, trackX + 3, thumbY + thumbHeight, 0xC0FFFFFF);
    }

    // ------------------------------------------------------------------ input

    @Override
    protected boolean onMouseClick(double mouseX, double mouseY, int button) {
        if (mouseX >= treeLeft - 2 && mouseX <= treeRight + 2 && mouseY >= treeTop && mouseY <= treeBottom) {
            if (mouseX >= treeRight - 3) {
                draggingScrollbar = true;
                dragScrollbarTo(mouseY);
                return true;
            }
            int index = scrollRow + (int) ((mouseY - treeTop) / rowH);
            if (index >= 0 && index < visible.size()) {
                selectedIndex = index;
                NbtNode node = visible.get(index);
                if (node.isContainer() && node.hasChildren()) {
                    node.expanded = !node.expanded;
                    rebuildVisible();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean onMouseDrag(double mouseX, double mouseY, int button) {
        if (draggingScrollbar) {
            dragScrollbarTo(mouseY);
            return true;
        }
        return false;
    }

    @Override
    protected boolean onMouseRelease(double mouseX, double mouseY, int button) {
        draggingScrollbar = false;
        return false;
    }

    private void dragScrollbarTo(double mouseY) {
        int rows = visibleRows();
        int maxScroll = Math.max(0, visible.size() - rows);
        double fraction = (mouseY - treeTop) / (double) (treeBottom - treeTop);
        scrollRow = (int) Math.round(fraction * maxScroll);
        clampScroll();
    }

    @Override
    public void onScreenScroll(double verticalAmount) {
        scrollRow -= (int) Math.signum(verticalAmount) * 3;
        clampScroll();
    }

    @Override
    protected boolean onKeyPressed(int keyCode, int modifiers) {
        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
        if (ctrl && keyCode == GLFW.GLFW_KEY_C) {
            if (shift) copyPath(); else copySelected();
            return true;
        }
        if (ctrl && keyCode == GLFW.GLFW_KEY_F) {
            focus(searchField);
            Ui.editFocused(searchField, true);
            return true;
        }
        if (searchField != null && Ui.editIsFocused(searchField)) {
            return false; // let the base fall through to vanilla so the text field receives the key
        }
        switch (keyCode) {
            case GLFW.GLFW_KEY_UP -> { moveSelection(-1); return true; }
            case GLFW.GLFW_KEY_DOWN -> { moveSelection(1); return true; }
            case GLFW.GLFW_KEY_LEFT -> { collapseOrParent(); return true; }
            case GLFW.GLFW_KEY_RIGHT -> { expandSelected(); return true; }
            case GLFW.GLFW_KEY_EQUAL, GLFW.GLFW_KEY_KP_ADD -> { expandSelected(); return true; }
            case GLFW.GLFW_KEY_MINUS, GLFW.GLFW_KEY_KP_SUBTRACT -> { collapseSelected(); return true; }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_SPACE -> { toggleSelected(); return true; }
            default -> { return false; }
        }
    }

    // ------------------------------------------------------------------ tree ops

    private void onSearchChanged(String text) {
        query = text.toLowerCase(Locale.ROOT).trim();
        rebuildVisible();
        scrollRow = 0;
        clampScroll();
    }

    private void rebuildVisible() {
        visible.clear();
        if (query.isEmpty()) {
            root.collectVisible(visible, config.sortKeys);
        } else {
            for (NbtNode child : root.children(config.sortKeys)) {
                collectFiltered(child, query, visible);
            }
        }
        if (selectedIndex >= visible.size()) selectedIndex = visible.size() - 1;
    }

    private boolean collectFiltered(NbtNode node, String q, List<NbtNode> out) {
        if (node.isContainer()) {
            List<NbtNode> childOut = new ArrayList<>();
            boolean childMatch = false;
            for (NbtNode child : node.children(config.sortKeys)) {
                if (collectFiltered(child, q, childOut)) childMatch = true;
            }
            boolean selfMatch = node.matches(q);
            if (selfMatch || childMatch) {
                out.add(node);
                if (childMatch) {
                    node.expanded = true;
                    out.addAll(childOut);
                }
                return true;
            }
            return false;
        } else if (node.matches(q)) {
            out.add(node);
            return true;
        }
        return false;
    }

    private void setAllExpanded(boolean expanded) {
        root.setExpandedRecursive(expanded, config.sortKeys);
        root.expanded = true;
        rebuildVisible();
        clampScroll();
        setStatus(expanded ? "nbtviewer.status.expanded" : "nbtviewer.status.collapsed");
    }

    private void toggleSelected() {
        NbtNode node = selected();
        if (node != null && node.isContainer() && node.hasChildren()) {
            node.expanded = !node.expanded;
            rebuildVisible();
            clampScroll();
        }
    }

    private void expandSelected() {
        NbtNode node = selected();
        if (node != null && node.isContainer() && node.hasChildren() && !node.expanded) {
            node.expanded = true;
            rebuildVisible();
            clampScroll();
        }
    }

    private void collapseSelected() {
        NbtNode node = selected();
        if (node != null && node.isContainer() && node.expanded) {
            node.expanded = false;
            rebuildVisible();
            clampScroll();
        }
    }

    private void collapseOrParent() {
        NbtNode node = selected();
        if (node == null) return;
        if (node.isContainer() && node.expanded) {
            node.expanded = false;
            rebuildVisible();
        } else if (node.parent != null && node.parent.parent != null) {
            int idx = visible.indexOf(node.parent);
            if (idx >= 0) selectedIndex = idx;
        }
        clampScroll();
    }

    private void moveSelection(int delta) {
        if (visible.isEmpty()) return;
        selectedIndex = Math.max(0, Math.min(visible.size() - 1,
                (selectedIndex < 0 ? 0 : selectedIndex) + delta));
        ensureSelectedVisible();
    }

    private NbtNode selected() {
        return (selectedIndex >= 0 && selectedIndex < visible.size()) ? visible.get(selectedIndex) : null;
    }

    // ------------------------------------------------------------------ copy

    private void copyAll() {
        setClipboard(NbtFormat.toSnbt(target.nbt, config.copyFormat == CopyFormat.PRETTY, config.sortKeys));
    }

    private void copySelected() {
        NbtNode node = selected();
        Object value = node != null ? node.value : target.nbt;
        setClipboard(NbtFormat.toSnbt(value, config.copyFormat == CopyFormat.PRETTY, config.sortKeys));
    }

    private void copyPath() {
        NbtNode node = selected();
        String path = node != null ? node.path() : "";
        if (path.isEmpty()) path = ".";
        Mc.setClipboard(mc(), path);
        setStatusText(Txt.str(Txt.translatable("nbtviewer.status.copied_path", path)));
    }

    private void setClipboard(String text) {
        Mc.setClipboard(mc(), text);
        setStatusText(Txt.str(Txt.translatable("nbtviewer.status.copied", text.length())));
    }

    // ------------------------------------------------------------------ misc

    private void toggleSort() {
        config.sortKeys = !config.sortKeys;
        ConfigManager.save();
        this.root = NbtNode.root(target.nbt, config.autoExpandDepth, config.sortKeys);
        Ui.setMessage(sortButton, sortLabel());
        rebuildVisible();
        clampScroll();
    }

    private Object sortLabel() {
        return Txt.translatable("nbtviewer.gui.sort",
                Txt.translatable(config.sortKeys ? "nbtviewer.on" : "nbtviewer.off"));
    }

    private String kindLabel() {
        return switch (target.kind) {
            case ITEM -> "item";
            case BLOCK -> "block";
            case ENTITY -> "entity";
        };
    }

    private int visibleRows() {
        return Math.max(1, (treeBottom - treeTop) / rowH);
    }

    private void clampScroll() {
        int maxScroll = Math.max(0, visible.size() - visibleRows());
        scrollRow = Math.max(0, Math.min(maxScroll, scrollRow));
    }

    private void ensureSelectedVisible() {
        if (selectedIndex < scrollRow) scrollRow = selectedIndex;
        int rows = visibleRows();
        if (selectedIndex >= scrollRow + rows) scrollRow = selectedIndex - rows + 1;
        clampScroll();
    }

    private void setStatus(String key) {
        setStatusText(Txt.str(Txt.translatable(key)));
    }

    private void setStatusText(String text) {
        this.status = text;
        this.statusUntil = System.currentTimeMillis() + 3000;
    }

    @Override
    protected void onCloseScreen() {
        Mc.setScreen(mc(), parent);
    }

    @Override
    protected boolean pauses() {
        return config.guiPauseGame;
    }
}
