// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.target;

import dev.l5z12.nbtviewer.facade.Mc;

/**
 * Short-term memory of the last entity the crosshair was on. Ticked every client frame so that if a
 * fast or dodgy entity darts out of the raycast in the instant you press the inspect key, the
 * intended target is still recoverable. Entity references are held only briefly and always
 * re-validated with {@link Mc#entityAlive} before use.
 */
public final class TargetTracker {

    private static Object lastEntity;
    private static long lastEntityAt;

    private TargetTracker() {
    }

    /** Call every client tick: snapshots whatever entity the crosshair is currently on. */
    public static void tick(Object client) {
        Object hit = Mc.hit(client);
        if (Mc.hitType(hit) == Mc.HIT_ENTITY) {
            remember(Mc.hitEntity(hit));
        }
    }

    /** Note an entity as the most-recent target (e.g. one found via the cone fallback). */
    public static void remember(Object entity) {
        if (Mc.entityAlive(entity)) {
            lastEntity = entity;
            lastEntityAt = System.currentTimeMillis();
        }
    }

    /** The most-recent crosshair entity if seen within {@code withinMs} and still alive, else null. */
    public static Object recentEntity(long withinMs) {
        if (lastEntity == null) {
            return null;
        }
        if (System.currentTimeMillis() - lastEntityAt > withinMs || !Mc.entityAlive(lastEntity)) {
            lastEntity = null;
            return null;
        }
        return lastEntity;
    }
}
