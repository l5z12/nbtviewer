// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.config;

public enum OverlayCorner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT;

    public OverlayCorner next() {
        OverlayCorner[] v = values();
        return v[(ordinal() + 1) % v.length];
    }

    public boolean isTop() {
        return this == TOP_LEFT || this == TOP_RIGHT;
    }

    public boolean isLeft() {
        return this == TOP_LEFT || this == BOTTOM_LEFT;
    }
}
