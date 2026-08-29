// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import dev.l5z12.nbtviewer.client.config.ConfigManager;
import dev.l5z12.nbtviewer.client.config.CopyFormat;
import dev.l5z12.nbtviewer.client.config.HudSource;
import dev.l5z12.nbtviewer.client.config.NbtViewerConfig;
import dev.l5z12.nbtviewer.client.gui.NbtConfigScreen;
import dev.l5z12.nbtviewer.client.gui.NbtViewerScreen;
import dev.l5z12.nbtviewer.client.nbt.NbtFormat;
import dev.l5z12.nbtviewer.client.nbt.NbtText;
import dev.l5z12.nbtviewer.client.target.NbtTarget;
import dev.l5z12.nbtviewer.client.target.TargetResolver;
import dev.l5z12.nbtviewer.facade.Cmd;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Txt;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/** {@code /viewdata} (alias {@code /nbtview}) — inspect items, blocks and entities from chat.
 * The command source is a Fabric type (mapping-agnostic); text is routed through {@link Mc}. */
public final class ViewDataCommand {

    private enum Mode { CHAT, GUI, COPY }

    private ViewDataCommand() {
    }

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> root = Cmd.literal("viewdata")
                .executes(ctx -> run(ctx.getSource(), HudSource.AUTO, Mode.GUI))
                .then(sourceNode("auto", HudSource.AUTO))
                .then(sourceNode("item", HudSource.HELD_ITEM))
                .then(sourceNode("held", HudSource.HELD_ITEM))
                .then(sourceNode("slot", HudSource.HOVERED_SLOT))
                .then(sourceNode("block", HudSource.TARGET_BLOCK))
                .then(sourceNode("entity", HudSource.TARGET_ENTITY))
                .then(Cmd.literal("copy")
                        .executes(ctx -> run(ctx.getSource(), HudSource.AUTO, Mode.COPY)))
                .then(Cmd.literal("gui")
                        .executes(ctx -> run(ctx.getSource(), HudSource.AUTO, Mode.GUI)))
                .then(Cmd.literal("overlay")
                        .executes(ctx -> toggleOverlay(ctx.getSource())))
                .then(Cmd.literal("config")
                        .executes(ctx -> openConfig(ctx.getSource())));

        LiteralCommandNode<FabricClientCommandSource> node = dispatcher.register(root);
        dispatcher.register(Cmd.literal("nbtview").redirect(node));
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> sourceNode(String name, HudSource source) {
        return Cmd.literal(name)
                .executes(ctx -> run(ctx.getSource(), source, Mode.CHAT))
                .then(Cmd.literal("chat").executes(ctx -> run(ctx.getSource(), source, Mode.CHAT)))
                .then(Cmd.literal("gui").executes(ctx -> run(ctx.getSource(), source, Mode.GUI)))
                .then(Cmd.literal("copy").executes(ctx -> run(ctx.getSource(), source, Mode.COPY)));
    }

    private static int run(FabricClientCommandSource source, HudSource src, Mode mode) {
        Object client = Mc.client();
        NbtViewerConfig config = ConfigManager.get();
        NbtTarget target = TargetResolver.resolve(client, src);
        if (target == null) {
            Mc.cmdError(source, Txt.translatable("nbtviewer.error.no_target", src.name().toLowerCase()));
            return 0;
        }

        switch (mode) {
            case GUI -> Mc.execute(client, () -> Mc.setScreen(client, new NbtViewerScreen(null, target)));
            case COPY -> {
                String snbt = NbtFormat.toSnbt(target.nbt, config.copyFormat == CopyFormat.PRETTY, config.sortKeys);
                Mc.setClipboard(client, snbt);
                Mc.feedback(source, Txt.colored(Txt.translatable("nbtviewer.status.copied", snbt.length()), Txt.GREEN));
            }
            case CHAT -> printToChat(source, config, target);
        }
        return 1;
    }

    private static void printToChat(FabricClientCommandSource source, NbtViewerConfig config, NbtTarget target) {
        Object header = Txt.empty();
        Txt.append(header, Txt.colored(Txt.literal("◆ "), Txt.AQUA));
        Txt.append(header, Txt.colored(Txt.copy(target.title), Txt.WHITE));
        Txt.append(header, Txt.colored(Txt.literal("  (" + target.subtitle + ")"), Txt.GRAY));
        Mc.feedback(source, header);

        String plain = NbtFormat.toSnbt(target.nbt, true, config.sortKeys);
        if (plain.length() > config.chatMaxChars) {
            Mc.feedback(source, Txt.colored(Txt.translatable("nbtviewer.chat.too_large", plain.length()), Txt.YELLOW));
            return;
        }
        Mc.feedback(source, NbtText.pretty(target.nbt, config.sortKeys, config.colorize));
    }

    private static int toggleOverlay(FabricClientCommandSource source) {
        NbtViewerConfig config = ConfigManager.get();
        config.overlayEnabled = !config.overlayEnabled;
        ConfigManager.save();
        Mc.feedback(source, Txt.colored(Txt.translatable(
                config.overlayEnabled ? "nbtviewer.status.overlay_on" : "nbtviewer.status.overlay_off"), Txt.GREEN));
        return 1;
    }

    private static int openConfig(FabricClientCommandSource source) {
        Object client = Mc.client();
        Mc.execute(client, () -> Mc.setScreen(client, new NbtConfigScreen(null)));
        return 1;
    }
}
