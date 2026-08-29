// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.config;

/** What the HUD overlay (and the "auto" command/keybind) should read. */
public enum HudSource {
    /** Whatever the crosshair / hand currently points at: block &gt; entity &gt; held item. */
    AUTO,
    HELD_ITEM,
    HOVERED_SLOT,
    TARGET_BLOCK,
    TARGET_ENTITY;

    public HudSource next() {
        HudSource[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
