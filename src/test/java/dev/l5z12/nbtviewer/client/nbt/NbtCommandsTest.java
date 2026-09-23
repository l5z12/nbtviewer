// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.nbt;

import dev.l5z12.nbtviewer.client.target.NbtTarget;
import dev.l5z12.nbtviewer.facade.Nbt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NbtCommandsTest {
    private static Object compound(Object... pairs) {
        Object result = Nbt.newCompound();
        for (int i = 0; i < pairs.length; i += 2) {
            Object value = pairs[i + 1];
            Nbt.put(result, (String) pairs[i], value instanceof String s ? Nbt.stringTag(s) : value);
        }
        return result;
    }

    private static NbtTarget target(NbtTarget.Kind kind, Object data) {
        return new NbtTarget(kind, null, "", data);
    }

    @Test
    void plainItemDefaultsToOne() {
        assertEquals("/give @s minecraft:stone 1", NbtCommands.create(
                target(NbtTarget.Kind.ITEM, compound("id", "minecraft:stone"))));
    }

    @Test
    void legacyItemPreservesTagsAndUsesIntegerCount() {
        //? if yarn {
        Object count = net.minecraft.nbt.NbtByte.of((byte) 16);
        //?} else {
        /*Object count = net.minecraft.nbt.ByteTag.valueOf((byte) 16);*/
        //?}
        Object tags = compound("display", compound("Name", "A quoted \"name\""));
        Object item = compound("id", "minecraft:stone", "Count", count, "tag", tags);
        assertEquals("/give @s minecraft:stone" + Nbt.leafString(tags) + " 16",
                NbtCommands.create(target(NbtTarget.Kind.ITEM, item)));
    }

    @Test
    void componentsPreserveValuesAndRemovalMarkers() {
        //? if yarn {
        Object count = net.minecraft.nbt.NbtInt.of(3);
        //?} else {
        /*Object count = net.minecraft.nbt.IntTag.valueOf(3);*/
        //?}
        Object custom = compound("note", "hello \"world\"");
        Object components = compound("minecraft:custom_data", custom,
                "!minecraft:custom_name", Nbt.newCompound());
        Object item = compound("id", "minecraft:stone", "count", count, "components", components);
        assertEquals("/give @s minecraft:stone[!minecraft:custom_name,minecraft:custom_data="
                + Nbt.leafString(custom) + "] 3", NbtCommands.create(target(NbtTarget.Kind.ITEM, item)));
    }

    @Test
    void blockPreservesStatesAndPayloadWithoutOriginalCoordinates() {
        Object payload = compound("id", "minecraft:chest", "x", "10", "y", "64", "z", "20",
                "CustomName", "Chest");
        Object block = compound("block", "minecraft:chest", "properties",
                compound("waterlogged", "false", "facing", "north"), "blockEntity", payload);
        String before = Nbt.leafString(block);
        assertEquals("/setblock ~ ~ ~ minecraft:chest[facing=north,waterlogged=false]"
                + Nbt.leafString(compound("CustomName", "Chest")),
                NbtCommands.create(target(NbtTarget.Kind.BLOCK, block)));
        assertEquals(before, Nbt.leafString(block));
        assertEquals("/setblock ~ ~ ~ minecraft:stone", NbtCommands.create(
                target(NbtTarget.Kind.BLOCK, compound("block", "minecraft:stone"))));
    }

    @Test
    void summonCleansNestedPassengersWithoutChangingSnapshot() {
        Object nested = Nbt.newList();
        Nbt.listAdd(nested, compound("id", "minecraft:pig", "UUIDMost", "1", "UUIDLeast", "2"));
        Object passengers = Nbt.newList();
        Nbt.listAdd(passengers, compound("id", "minecraft:chicken", "UUID", "old",
                "Pos", Nbt.newList(), "Passengers", nested));
        Object data = compound("id", "minecraft:zombie", "UUID", "old", "Dimension", "overworld",
                "Pos", Nbt.newList(), "CustomName", "Test", "Passengers", passengers);
        Object entity = compound("type", "minecraft:zombie", "data", data);
        String before = Nbt.leafString(entity);
        String command = NbtCommands.create(target(NbtTarget.Kind.ENTITY, entity));
        assertTrue(command.startsWith("/summon minecraft:zombie ~ ~ ~ {"));
        assertTrue(command.contains("minecraft:chicken"));
        assertTrue(command.contains("minecraft:pig"));
        assertTrue(command.contains("CustomName"));
        assertFalse(command.contains("UUID"));
        assertFalse(command.contains("Pos"));
        assertFalse(command.contains("Dimension"));
        assertEquals(before, Nbt.leafString(entity));
        assertEquals("/summon minecraft:pig ~ ~ ~", NbtCommands.create(
                target(NbtTarget.Kind.ENTITY, compound("type", "minecraft:pig"))));
    }

    @Test
    void unsummonableEntitiesAreRejected() {
        for (String id : new String[]{"minecraft:player", "minecraft:fishing_bobber"}) {
            NbtTarget target = target(NbtTarget.Kind.ENTITY, compound("type", id));
            assertFalse(NbtCommands.supported(target));
            assertThrows(IllegalArgumentException.class, () -> NbtCommands.create(target));
        }
    }
}
