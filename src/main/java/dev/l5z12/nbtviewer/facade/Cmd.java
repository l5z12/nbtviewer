// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.facade;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

/**
 * Client-command builder facade. The command-source type is stable across mappings, but the builder
 * factory was renamed {@code ClientCommandManager} → {@code ClientCommands} in the 26.x Fabric API.
 */
public final class Cmd {

    private Cmd() {
    }

    public static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
        //? if yarn {
        return net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal(name);
        //?} else {
        /*return net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal(name);*/
        //?}
    }
}
