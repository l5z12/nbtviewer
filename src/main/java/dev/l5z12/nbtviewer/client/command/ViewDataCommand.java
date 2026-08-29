// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.command;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
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
import dev.l5z12.nbtviewer.client.target.SelectorResolver;
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
                .then(blockNode())
                .then(entityNode())
                .then(playerNode())
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

    // ------------------------------------------------------------------ explicit target specs

    /** {@code /viewdata entity [chat|gui|copy]} (crosshair) plus {@code entity <spec>} where spec is
     * a network id, a UUID, an entity-type id (nearest), or a vanilla selector (@e[…], @p, …). */
    private static LiteralArgumentBuilder<FabricClientCommandSource> entityNode() {
        return sourceNode("entity", HudSource.TARGET_ENTITY)
                .then(Cmd.argument("target", StringArgumentType.greedyString())
                        .suggests(ENTITY_SUGGESTIONS)
                        .executes(ctx -> runEntitySpec(ctx.getSource(), StringArgumentType.getString(ctx, "target"))));
    }

    /** {@code /viewdata block [chat|gui|copy]} (crosshair) plus {@code block <x> <y> <z>}. */
    private static LiteralArgumentBuilder<FabricClientCommandSource> blockNode() {
        return sourceNode("block", HudSource.TARGET_BLOCK)
                .then(Cmd.argument("x", IntegerArgumentType.integer())
                        .then(Cmd.argument("y", IntegerArgumentType.integer())
                                .then(Cmd.argument("z", IntegerArgumentType.integer())
                                        .executes(ctx -> runBlockAt(ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "x"),
                                                IntegerArgumentType.getInteger(ctx, "y"),
                                                IntegerArgumentType.getInteger(ctx, "z"))))));
    }

    /** {@code /viewdata player <name>}. */
    private static LiteralArgumentBuilder<FabricClientCommandSource> playerNode() {
        return Cmd.literal("player")
                .then(Cmd.argument("name", StringArgumentType.word())
                        .suggests(PLAYER_SUGGESTIONS)
                        .executes(ctx -> runPlayer(ctx.getSource(), StringArgumentType.getString(ctx, "name"))));
    }

    private static final SuggestionProvider<FabricClientCommandSource> ENTITY_SUGGESTIONS = (ctx, builder) -> {
        List<String> options = new java.util.ArrayList<>(List.of("@s", "@p", "@a", "@r", "@e", "@n"));
        options.addAll(Mc.loadedEntityTypeIds(Mc.client()));
        return suggest(builder, options);
    };

    private static final SuggestionProvider<FabricClientCommandSource> PLAYER_SUGGESTIONS =
            (ctx, builder) -> suggest(builder, Mc.onlinePlayerNames(Mc.client()));

    private static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, List<String> options) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(remaining)) builder.suggest(option);
        }
        return builder.buildFuture();
    }

    private static int runEntitySpec(FabricClientCommandSource source, String spec) {
        Object client = Mc.client();
        Object entity = resolveEntitySpec(client, spec.trim());
        if (entity == null) {
            Mc.cmdError(source, Txt.translatable("nbtviewer.error.no_entity", spec));
            return 0;
        }
        return openTarget(source, client, TargetResolver.entityTarget(client, entity), spec);
    }

    /** A network id, a UUID, an entity-type id (nearest of that type), or a vanilla selector. */
    private static Object resolveEntitySpec(Object client, String spec) {
        if (SelectorResolver.isSelector(spec)) {
            List<Object> matches = SelectorResolver.resolve(client, spec);
            return matches.isEmpty() ? null : matches.get(0);
        }
        if (spec.matches("-?\\d+")) {
            try {
                return Mc.entityByNetworkId(client, Integer.parseInt(spec));
            } catch (NumberFormatException overflow) {
                return null;
            }
        }
        Object byUuid = Mc.entityByUuid(client, spec);
        if (byUuid != null) return byUuid;
        return Mc.nearestEntityOfType(client, spec.indexOf(':') >= 0 ? spec : "minecraft:" + spec);
    }

    private static int runBlockAt(FabricClientCommandSource source, int x, int y, int z) {
        Object client = Mc.client();
        NbtTarget target = TargetResolver.blockTargetAt(client, Mc.blockPosOf(x, y, z));
        return openTarget(source, client, target, x + " " + y + " " + z);
    }

    private static int runPlayer(FabricClientCommandSource source, String name) {
        Object client = Mc.client();
        Object player = Mc.playerByName(client, name);
        if (player == null) {
            Mc.cmdError(source, Txt.translatable("nbtviewer.error.no_player", name));
            return 0;
        }
        return openTarget(source, client, TargetResolver.entityTarget(client, player), name);
    }

    private static int openTarget(FabricClientCommandSource source, Object client, NbtTarget target, String spec) {
        if (target == null) {
            Mc.cmdError(source, Txt.translatable("nbtviewer.error.no_target", spec));
            return 0;
        }
        Mc.execute(client, () -> Mc.setScreen(client, new NbtViewerScreen(null, target)));
        return 1;
    }
}
