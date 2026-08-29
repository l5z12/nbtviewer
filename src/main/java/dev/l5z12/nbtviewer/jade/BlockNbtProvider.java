// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.jade;

import dev.l5z12.nbtviewer.client.config.ConfigManager;
import dev.l5z12.nbtviewer.client.config.NbtViewerConfig;
import dev.l5z12.nbtviewer.client.nbt.NbtFormat;
import dev.l5z12.nbtviewer.client.nbt.NbtText;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Nbt;
import dev.l5z12.nbtviewer.facade.Txt;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

//? if yarn {
import net.minecraft.util.Identifier;
//?} else {
/*import net.minecraft.resources.Identifier;*/
//?}

/**
 * Adds block-entity NBT to Jade tooltips, with optional server-side full-NBT sync. The tooltip body
 * is written once against the facades; only the Jade-API-imposed types ({@code Identifier}, the
 * {@code CompoundTag}/{@code NbtCompound} of {@code appendServerData}, and {@code ITooltip.add}'s
 * text type) are guarded.
 */
public class BlockNbtProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    public static final BlockNbtProvider INSTANCE = new BlockNbtProvider();
    public static final Identifier UID = (Identifier) Mc.id("nbtviewer", "block_nbt");
    private static final String DATA_KEY = "nbtviewer:BlockNbt";

    @Override
    public Identifier getUid() {
        return UID;
    }

    // ---- server: attach the authoritative block-entity NBT (only runs where this mod is present)
    //? if yarn {
    @Override
    public void appendServerData(net.minecraft.nbt.NbtCompound data, BlockAccessor accessor) {
        appendServerData0(data, accessor);
    }
    //?} else {
    /*@Override
    public void appendServerData(net.minecraft.nbt.CompoundTag data, BlockAccessor accessor) {
        appendServerData0(data, accessor);
    }*/
    //?}

    private void appendServerData0(Object data, BlockAccessor accessor) {
        if (!ConfigManager.get().jadeSyncFullBlockData) return;
        Object be = accessor.getBlockEntity();
        if (be != null) {
            Nbt.put(data, DATA_KEY, Nbt.blockEntityToNbt(be, accessor.getLevel()));
        }
    }

    // ---- client: render into the tooltip
    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        NbtViewerConfig cfg = ConfigManager.get();
        if (!cfg.jadeEnabled || !config.get(getUid())) return;

        Object nbt = Nbt.childCompound(accessor.getServerData(), DATA_KEY);
        if (nbt == null && accessor.getBlockEntity() != null) {
            nbt = Nbt.blockEntityToNbt(accessor.getBlockEntity(), accessor.getLevel());
        }
        if (nbt == null || Nbt.compoundEmpty(nbt)) return;

        int tags = NbtFormat.countTags(nbt);
        Object header = Txt.append(Txt.colored(Txt.literal("NBT "), Txt.AQUA),
                Txt.colored(Txt.literal("(" + tags + " tags)"), Txt.DARK_GRAY));
        add(tooltip, header);

        java.util.List<Object> lines = NbtText.lines(nbt, cfg.sortKeys, cfg.colorize, 48);
        int max = cfg.jadeMaxLines;
        for (int i = 0; i < Math.min(max, lines.size()); i++) {
            add(tooltip, lines.get(i));
        }
        if (lines.size() > max) {
            add(tooltip, Txt.colored(Txt.literal("… +" + (lines.size() - max) + " lines (use /viewdata block gui)"),
                    Txt.DARK_GRAY));
        }
    }

    private static void add(ITooltip tooltip, Object text) {
        //? if yarn {
        tooltip.add((net.minecraft.text.Text) text);
        //?} else {
        /*tooltip.add((net.minecraft.network.chat.Component) text);*/
        //?}
    }
}
