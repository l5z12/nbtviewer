// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.jade;

import dev.l5z12.nbtviewer.client.config.ConfigManager;
import dev.l5z12.nbtviewer.client.config.NbtViewerConfig;
import dev.l5z12.nbtviewer.client.nbt.NbtText;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Nbt;
import dev.l5z12.nbtviewer.facade.Txt;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

//? if yarn {
import net.minecraft.util.Identifier;
//?} else {
/*import net.minecraft.resources.Identifier;*/
//?}

/** Adds entity NBT to Jade tooltips, with optional server-side full-NBT sync. */
public class EntityNbtProvider implements IEntityComponentProvider, IServerDataProvider<EntityAccessor> {

    public static final EntityNbtProvider INSTANCE = new EntityNbtProvider();
    public static final Identifier UID = (Identifier) Mc.id("nbtviewer", "entity_nbt");
    private static final String DATA_KEY = "nbtviewer:EntityNbt";

    @Override
    public Identifier getUid() {
        return UID;
    }

    //? if yarn {
    @Override
    public void appendServerData(net.minecraft.nbt.NbtCompound data, EntityAccessor accessor) {
        appendServerData0(data, accessor);
    }
    //?} else {
    /*@Override
    public void appendServerData(net.minecraft.nbt.CompoundTag data, EntityAccessor accessor) {
        appendServerData0(data, accessor);
    }*/
    //?}

    private void appendServerData0(Object data, EntityAccessor accessor) {
        if (!ConfigManager.get().jadeSyncFullBlockData) return;
        Object nbt = Nbt.entityToNbt(accessor.getEntity());
        if (nbt != null && !Nbt.compoundEmpty(nbt)) {
            Nbt.put(data, DATA_KEY, nbt);
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
        NbtViewerConfig cfg = ConfigManager.get();
        if (!cfg.jadeEnabled || !config.get(getUid())) return;

        Object nbt = Nbt.childCompound(accessor.getServerData(), DATA_KEY);
        if (nbt == null) {
            nbt = Nbt.entityToNbt(accessor.getEntity());
        }
        if (nbt == null || Nbt.compoundEmpty(nbt)) return;

        add(tooltip, Txt.colored(Txt.literal("Entity NBT"), Txt.AQUA));
        java.util.List<Object> lines = NbtText.lines(nbt, cfg.sortKeys, cfg.colorize, 48);
        int max = cfg.jadeMaxLines;
        for (int i = 0; i < Math.min(max, lines.size()); i++) {
            add(tooltip, lines.get(i));
        }
        if (lines.size() > max) {
            add(tooltip, Txt.colored(Txt.literal("… +" + (lines.size() - max) + " lines (use /viewdata entity gui)"),
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
