// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.jade;

//? if yarn {
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
//?} else {
/*import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;*/
//?}
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

/**
 * Jade integration. Discovered automatically by Jade via the {@link WailaPlugin} annotation —
 * Jade is an optional (compile-only) dependency, so this class is only ever loaded when Jade is
 * present. Adds the client-visible NBT of the looked-at block entity (and entity) to Jade's
 * tooltip, and — in singleplayer / on servers that also have this mod — syncs the full NBT.
 *
 * <p>Only the class-literal imports differ between mappings; the Jade API package is stable.
 */
@WailaPlugin
public class NbtViewerJadePlugin implements IWailaPlugin {

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(BlockNbtProvider.INSTANCE, BlockEntity.class);
        registration.registerEntityDataProvider(EntityNbtProvider.INSTANCE, Entity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.addConfig(BlockNbtProvider.UID, true);
        registration.registerBlockComponent(BlockNbtProvider.INSTANCE, Block.class);

        registration.addConfig(EntityNbtProvider.UID, true);
        registration.registerEntityComponent(EntityNbtProvider.INSTANCE, Entity.class);
    }
}
