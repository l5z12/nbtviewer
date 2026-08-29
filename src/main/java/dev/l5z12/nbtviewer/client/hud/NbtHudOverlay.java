// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.hud;

import java.util.ArrayList;
import java.util.List;

import dev.l5z12.nbtviewer.client.config.NbtViewerConfig;
import dev.l5z12.nbtviewer.client.config.OverlayCorner;
import dev.l5z12.nbtviewer.client.keybind.NbtKeyBindings;
import dev.l5z12.nbtviewer.client.nbt.NbtFormat;
import dev.l5z12.nbtviewer.client.nbt.NbtText;
import dev.l5z12.nbtviewer.client.target.NbtTarget;
import dev.l5z12.nbtviewer.client.target.TargetResolver;
import dev.l5z12.nbtviewer.facade.Gfx;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Txt;

/** Draws the current target's NBT in a screen corner. Registered through {@code Mc.registerHud}. */
public final class NbtHudOverlay implements HudRenderer {

    private final Object client;
    private final NbtViewerConfig config;

    public NbtHudOverlay(Object client, NbtViewerConfig config) {
        this.client = client;
        this.config = config;
    }

    @Override
    public void render(Object ctx) {
        if (!config.overlayEnabled || Mc.player(client) == null || !Mc.hasWorld(client)) {
            return;
        }
        if (!isVisible()) {
            return;
        }

        NbtTarget target = TargetResolver.resolve(client, config.overlaySource);
        if (target == null) {
            return;
        }

        List<Object> lines = buildLines(target);
        if (lines.isEmpty()) {
            return;
        }

        Object font = Mc.font(client);
        double scale = config.overlayScale;
        int screenW = (int) (Mc.scaledWidth(client) / scale);
        int screenH = (int) (Mc.scaledHeight(client) / scale);

        int lineHeight = Gfx.lineHeight(font) + 1;
        int boxWidth = 0;
        for (Object line : lines) {
            boxWidth = Math.max(boxWidth, Gfx.textWidth(font, line));
        }
        int boxHeight = lines.size() * lineHeight;

        int margin = 4;
        int pad = 3;
        OverlayCorner corner = config.overlayCorner;
        int x = corner.isLeft() ? margin : screenW - boxWidth - margin - pad * 2;
        int y = corner.isTop() ? margin : screenH - boxHeight - margin - pad * 2;

        Gfx.pushScale(ctx, (float) scale);

        if (config.overlayBackground) {
            Gfx.fill(ctx, x, y, x + boxWidth + pad * 2, y + boxHeight + pad * 2, 0xB0000000);
            Gfx.fill(ctx, x, y, x + boxWidth + pad * 2, y + 1, 0x40FFFFFF);
        }

        int textX = x + pad;
        int textY = y + pad;
        for (Object line : lines) {
            Gfx.text(ctx, font, line, textX, textY, 0xFFFFFFFF);
            textY += lineHeight;
        }

        Gfx.popScale(ctx);
    }

    private boolean isVisible() {
        return switch (config.overlayVisibility) {
            case ALWAYS -> true;
            case KEY_HELD -> NbtKeyBindings.isPressed(NbtKeyBindings.holdOverlay);
            case SNEAKING -> Mc.player(client) != null && Mc.isSneaking(Mc.player(client));
        };
    }

    private List<Object> buildLines(NbtTarget target) {
        List<Object> out = new ArrayList<>();

        Object header = Txt.empty();
        Txt.append(header, Txt.colored(Txt.literal("◆ "), Txt.AQUA));
        Txt.append(header, Txt.colored(Txt.copy(target.title), Txt.WHITE));
        out.add(header);

        if (config.overlayShowSize) {
            int tags = NbtFormat.countTags(target.nbt);
            int chars = NbtFormat.toSnbt(target.nbt, false, false).length();
            out.add(Txt.colored(
                    Txt.literal(target.subtitle + "  ·  " + tags + " tags  ·  " + chars + " B"),
                    Txt.DARK_GRAY));
        }

        List<Object> body = NbtText.lines(target.nbt, config.sortKeys, config.colorize, config.overlayMaxValueLength);
        int max = Math.max(1, config.overlayMaxLines);
        if (body.size() <= max) {
            out.addAll(body);
        } else {
            out.addAll(body.subList(0, max));
            out.add(Txt.colored(Txt.literal("… (+" + (body.size() - max) + " more lines — press "
                    + NbtKeyBindings.boundKeyLabel(NbtKeyBindings.openAuto) + ")"), Txt.DARK_GRAY));
        }
        return out;
    }
}
