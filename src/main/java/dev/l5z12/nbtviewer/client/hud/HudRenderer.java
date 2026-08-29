// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.hud;

/**
 * Version-agnostic HUD hook. {@code Mc.registerHud} adapts the per-version Fabric HUD API down to
 * this; the render context is passed as {@code Object} and handed straight to {@link
 * dev.l5z12.nbtviewer.facade.Gfx}.
 */
@FunctionalInterface
public interface HudRenderer {
    void render(Object ctx);
}
