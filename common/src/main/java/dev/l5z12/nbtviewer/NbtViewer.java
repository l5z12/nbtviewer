// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared constants for NBT Viewer.
 *
 * <p>This is a client-side utility mod: it surfaces the NBT / data-component data that the
 * vanilla client already receives from the server, so you never need operator {@code /data}
 * access to inspect an item, block entity or entity.
 */
public final class NbtViewer {
    public static final String MOD_ID = "nbtviewer";
    public static final Logger LOGGER = LoggerFactory.getLogger("NBT Viewer");

    private NbtViewer() {
    }
}
