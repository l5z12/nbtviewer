// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.nbt;

import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import dev.l5z12.nbtviewer.client.target.NbtTarget;
import dev.l5z12.nbtviewer.facade.Nbt;

/** Builds pasteable commands from an inspection snapshot without modifying its tags. */
public final class NbtCommands {
    private NbtCommands() {}

    public static String name(NbtTarget target) {
        return switch (target.kind) {
            case ITEM -> "give";
            case BLOCK -> "setblock";
            case ENTITY -> "summon";
        };
    }

    /** Players and fishing bobbers cannot be created with /summon. */
    public static boolean supported(NbtTarget target) {
        if (target.kind != NbtTarget.Kind.ENTITY) return true;
        String type = string(Nbt.get(target.nbt, "type"));
        return !type.equals("minecraft:player") && !type.equals("minecraft:fishing_bobber");
    }

    public static String create(NbtTarget target) {
        if (!supported(target)) throw new IllegalArgumentException("Entity cannot be summoned");
        Object root = target.nbt;
        return switch (target.kind) {
            case ITEM -> give(root);
            case BLOCK -> "/setblock ~ ~ ~ " + string(Nbt.get(root, "block"))
                    + properties(Nbt.get(root, "properties"))
                    + suffix(without(Nbt.get(root, "blockEntity"), Set.of("id", "x", "y", "z")));
            case ENTITY -> "/summon " + string(Nbt.get(root, "type")) + " ~ ~ ~"
                    + entitySuffix(Nbt.get(root, "data"));
        };
    }

    private static String give(Object root) {
        String item = string(Nbt.get(root, "id"));
        Object components = Nbt.get(root, "components");
        if (Nbt.isCompound(components) && !Nbt.compoundEmpty(components)) {
            StringJoiner entries = new StringJoiner(",", "[", "]");
            for (String key : sortedKeys(components)) {
                entries.add(key.startsWith("!") ? key : key + "=" + Nbt.leafString(Nbt.get(components, key)));
            }
            item += entries;
        } else {
            item += suffix(Nbt.get(root, "tag"));
        }
        Object count = Nbt.get(root, "count");
        if (count == null) count = Nbt.get(root, "Count");
        // Legacy stacks encode Count as a byte; command counts are unsuffixed integers.
        String amount = count == null ? "1" : Nbt.leafString(count).replaceFirst("[bBsSlL]$", "");
        return "/give @s " + item + " " + amount;
    }

    private static String properties(Object props) {
        if (!Nbt.isCompound(props) || Nbt.compoundEmpty(props)) return "";
        StringJoiner entries = new StringJoiner(",", "[", "]");
        for (String key : sortedKeys(props)) entries.add(key + "=" + string(Nbt.get(props, key)));
        return entries.toString();
    }

    private static String entitySuffix(Object data) {
        Object clean = cleanEntity(data, true);
        String snbt = suffix(clean);
        return snbt.isEmpty() ? "" : " " + snbt;
    }

    private static Object cleanEntity(Object data, boolean root) {
        Object clean = without(data, root
                ? Set.of("id", "UUID", "UUIDMost", "UUIDLeast", "Pos", "Dimension")
                : Set.of("UUID", "UUIDMost", "UUIDLeast", "Pos", "Dimension"));
        Object passengers = Nbt.get(clean, "Passengers");
        if (Nbt.isList(passengers)) {
            Object copies = Nbt.newList();
            for (int i = 0; i < Nbt.listSize(passengers); i++) {
                Nbt.listAdd(copies, cleanEntity(Nbt.listGet(passengers, i), false));
            }
            Nbt.put(clean, "Passengers", copies);
        }
        return clean;
    }

    private static Object without(Object compound, Set<String> excluded) {
        Object result = Nbt.newCompound();
        if (Nbt.isCompound(compound)) {
            for (String key : Nbt.keys(compound)) {
                if (!excluded.contains(key)) Nbt.put(result, key, Nbt.get(compound, key));
            }
        }
        return result;
    }

    private static List<String> sortedKeys(Object compound) {
        List<String> keys = Nbt.keys(compound);
        keys.sort(String::compareTo);
        return keys;
    }

    private static String suffix(Object compound) {
        return Nbt.isCompound(compound) && !Nbt.compoundEmpty(compound) ? Nbt.leafString(compound) : "";
    }

    private static String string(Object tag) {
        return tag == null ? "" : NbtFormat.unquote(Nbt.leafString(tag));
    }
}
