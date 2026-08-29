// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.target;

import dev.l5z12.nbtviewer.facade.Nbt;

/**
 * A resolved inspection target: a title, a subtitle line, and the NBT the client can see.
 * {@code title} is a facade text ({@code Text}/{@code Component}) and {@code nbt} a facade compound
 * ({@code NbtCompound}/{@code CompoundTag}); both are held as {@code Object} so this class carries no
 * mapping-specific types.
 */
public final class NbtTarget {

    public enum Kind {
        ITEM, BLOCK, ENTITY
    }

    public final Kind kind;
    public final Object title;
    public final String subtitle;
    public final Object nbt;

    public NbtTarget(Kind kind, Object title, String subtitle, Object nbt) {
        this.kind = kind;
        this.title = title;
        this.subtitle = subtitle;
        this.nbt = nbt == null ? Nbt.newCompound() : nbt;
    }

    public boolean isEmpty() {
        return Nbt.compoundEmpty(nbt);
    }
}
