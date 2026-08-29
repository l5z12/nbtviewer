// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.target;

import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import dev.l5z12.nbtviewer.client.config.ConfigManager;
import dev.l5z12.nbtviewer.client.config.HudSource;
import dev.l5z12.nbtviewer.client.config.NbtViewerConfig;
import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Nbt;

/**
 * Turns the current game state (crosshair target, held item, hovered inventory slot) into an
 * {@link NbtTarget}. Everything here reads data the vanilla client already has — nothing is
 * requested from the server, so this works on any multiplayer server without {@code /data}.
 *
 * <p>All game access goes through {@link Mc} and {@link Nbt}, so this logic is identical on every
 * mapping generation from 1.20 (yarn) through 26.x (Mojmap).
 */
public final class TargetResolver {

    private TargetResolver() {
    }

    @Nullable
    public static NbtTarget resolve(Object client, HudSource source) {
        return switch (source) {
            case HELD_ITEM -> heldItem(client);
            case HOVERED_SLOT -> hoveredSlot(client);
            case TARGET_BLOCK -> targetBlock(client);
            case TARGET_ENTITY -> targetEntity(client);
            case AUTO -> auto(client);
        };
    }

    @Nullable
    public static NbtTarget auto(Object client) {
        // Prefer a hovered inventory slot while a container is open.
        NbtTarget slot = hoveredSlot(client);
        if (slot != null) return slot;

        int type = Mc.hitType(Mc.hit(client));
        if (type == Mc.HIT_BLOCK) {
            NbtTarget block = targetBlock(client);
            if (block != null) return block;
        } else {
            // A live entity hit, or no hit at all — either way try the entity path, which includes
            // the cone + sticky fallbacks for a target that just moved out of the crosshair ray.
            NbtTarget entity = targetEntity(client);
            if (entity != null) return entity;
        }
        return heldItem(client);
    }

    // --------------------------------------------------------------------- items

    @Nullable
    public static NbtTarget heldItem(Object client) {
        Object player = Mc.player(client);
        if (player == null || !Mc.hasWorld(client)) return null;
        Object stack = Mc.mainHand(player);
        if (Mc.itemEmpty(stack)) stack = Mc.offHand(player);
        return itemTarget(stack, Mc.world(client));
    }

    @Nullable
    public static NbtTarget hoveredSlot(Object client) {
        Object stack = Mc.hoveredSlotItem(client);
        return stack == null ? null : itemTarget(stack, Mc.world(client));
    }

    /** Item under the cursor in the given container screen. Used by the container-key hook, which is
     * handed the screen directly (on 26.x the current screen is not reachable from the client). */
    @Nullable
    public static NbtTarget hoveredSlotIn(Object client, Object screen) {
        if (!Mc.hasWorld(client)) return null;
        Object stack = Mc.hoveredSlotItemIn(screen);
        return stack == null ? null : itemTarget(stack, Mc.world(client));
    }

    @Nullable
    private static NbtTarget itemTarget(Object stack, Object world) {
        if (stack == null || Mc.itemEmpty(stack)) return null;
        Object nbt = Nbt.itemToNbt(stack, world);
        String id = Mc.itemId(stack);
        String subtitle = id + " x" + Mc.itemCount(stack);
        return new NbtTarget(NbtTarget.Kind.ITEM, Mc.itemName(stack), subtitle, nbt);
    }

    // --------------------------------------------------------------------- blocks

    @Nullable
    public static NbtTarget targetBlock(Object client) {
        if (!Mc.hasWorld(client)) return null;
        Object hit = Mc.hit(client);
        if (Mc.hitType(hit) != Mc.HIT_BLOCK) return null;

        Object world = Mc.world(client);
        Object pos = Mc.hitBlockPos(hit);
        String id = Mc.blockId(world, pos);

        Object root = Nbt.newCompound();
        Nbt.putString(root, "block", id);

        Object posList = Nbt.newList();
        Nbt.listAdd(posList, Nbt.stringTag(Integer.toString(Mc.posX(pos))));
        Nbt.listAdd(posList, Nbt.stringTag(Integer.toString(Mc.posY(pos))));
        Nbt.listAdd(posList, Nbt.stringTag(Integer.toString(Mc.posZ(pos))));
        Nbt.put(root, "pos", posList);

        Object props = Nbt.newCompound();
        for (Map.Entry<String, String> entry : Mc.blockProperties(world, pos).entrySet()) {
            Nbt.putString(props, entry.getKey(), entry.getValue());
        }
        if (!Nbt.compoundEmpty(props)) Nbt.put(root, "properties", props);

        Object be = Mc.blockEntity(world, pos);
        if (be != null) {
            Nbt.put(root, "blockEntity", Nbt.blockEntityToNbt(be, world));
        }

        return new NbtTarget(NbtTarget.Kind.BLOCK, Mc.blockName(world, pos), id, root);
    }

    // --------------------------------------------------------------------- entities

    @Nullable
    public static NbtTarget targetEntity(Object client) {
        if (!Mc.hasWorld(client)) return null;
        Object entity = resolveEntity(client);
        if (entity == null) return null;
        TargetTracker.remember(entity);

        Object nbt = Nbt.entityToNbt(entity);

        Object root = Nbt.newCompound();
        Nbt.putString(root, "type", Mc.entityId(entity));
        Nbt.putString(root, "uuid", Mc.entityUuid(entity));
        Object posList = Nbt.newList();
        Nbt.listAdd(posList, Nbt.stringTag(fmt(Mc.entityX(entity))));
        Nbt.listAdd(posList, Nbt.stringTag(fmt(Mc.entityY(entity))));
        Nbt.listAdd(posList, Nbt.stringTag(fmt(Mc.entityZ(entity))));
        Nbt.put(root, "pos", posList);
        if (nbt != null && !Nbt.compoundEmpty(nbt)) Nbt.put(root, "data", nbt);

        return new NbtTarget(NbtTarget.Kind.ENTITY, Mc.entityName(entity), Mc.entityTypeString(entity), root);
    }

    /**
     * The entity to inspect, most-reliable source first: the live crosshair entity, else the nearest
     * entity within the look cone, else the last entity the crosshair was on (sticky window). The
     * latter two recover a target that moved out of the exact raycast as the key was pressed.
     */
    @Nullable
    private static Object resolveEntity(Object client) {
        NbtViewerConfig cfg = ConfigManager.get();

        Object hit = Mc.hit(client);
        if (Mc.hitType(hit) == Mc.HIT_ENTITY) {
            return Mc.hitEntity(hit);
        }
        if (cfg.nearestEntityFallback) {
            Object near = Mc.pickEntityInView(client, cfg.nearestEntityReach, cfg.nearestEntityConeDegrees);
            if (near != null) return near;
        }
        if (cfg.stickyTargetMs > 0) {
            Object recent = TargetTracker.recentEntity(cfg.stickyTargetMs);
            if (recent != null) return recent;
        }
        return null;
    }

    private static String fmt(double d) {
        return String.format(Locale.ROOT, "%.2f", d);
    }
}
