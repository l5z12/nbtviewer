// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.config;

public enum OverlayVisibility {
    /** Always visible while enabled. */
    ALWAYS,
    /** Visible only while the "hold to show overlay" keybind is held. */
    KEY_HELD,
    /** Visible only while sneaking. */
    SNEAKING;

    public OverlayVisibility next() {
        OverlayVisibility[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
