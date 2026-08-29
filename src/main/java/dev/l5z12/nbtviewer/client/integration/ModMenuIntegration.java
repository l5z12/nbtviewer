// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import dev.l5z12.nbtviewer.client.gui.NbtConfigScreen;

/**
 * Registers the config screen with Mod Menu. Mod Menu is a compile-only dependency; Fabric only
 * instantiates this "modmenu" entrypoint when Mod Menu is actually installed, so it is safe.
 */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return NbtConfigScreen::new;
    }
}
