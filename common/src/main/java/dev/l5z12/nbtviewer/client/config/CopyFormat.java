// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.config;

public enum CopyFormat {
    /** Multi-line, indented SNBT. */
    PRETTY,
    /** Single-line, compact SNBT (what {@code NbtElement#toString()} produces). */
    COMPACT;

    public CopyFormat next() {
        CopyFormat[] v = values();
        return v[(ordinal() + 1) % v.length];
    }
}
