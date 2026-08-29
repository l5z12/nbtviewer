// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.facade;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.l5z12.nbtviewer.client.hud.HudRenderer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenMouseEvents;

//? if yarn {
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
//?} else {
/*import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;*/
//?}

/**
 * Client / registry / identifier facade + Fabric hook registration + crosshair-target primitives.
 * Everything the mod touches on the running client goes through here as {@code Object}, so only this
 * file names {@code MinecraftClient} vs {@code Minecraft}, {@code World} vs {@code Level}, the two
 * {@code Identifier} packages, {@code Registries} vs {@code BuiltInRegistries}, and so on.
 */
public final class Mc {

    public static final int HIT_MISS = 0;
    public static final int HIT_BLOCK = 1;
    public static final int HIT_ENTITY = 2;

    private Mc() {
    }

    public interface ScrollTarget {
        void onScroll(Object screen, double vertical);
    }

    // ---- identifiers & registry ids
    public static Object id(String namespace, String path) {
        //? if yarn && >=1.20.5 {
        return Identifier.of(namespace, path);
        //?} else if yarn {
        /*return new Identifier(namespace, path);*/
        //?} else {
        /*return Identifier.fromNamespaceAndPath(namespace, path);*/
        //?}
    }

    public static String itemId(Object stack) {
        //? if yarn {
        return Registries.ITEM.getId(((ItemStack) stack).getItem()).toString();
        //?} else {
        /*return BuiltInRegistries.ITEM.getKey(((ItemStack) stack).getItem()).toString();*/
        //?}
    }

    // ---- client
    public static Object client() {
        //? if yarn {
        return MinecraftClient.getInstance();
        //?} else {
        /*return Minecraft.getInstance();*/
        //?}
    }

    public static Object player(Object client) {
        //? if yarn {
        return ((MinecraftClient) client).player;
        //?} else {
        /*return ((Minecraft) client).player;*/
        //?}
    }

    public static boolean hasWorld(Object client) {
        return world(client) != null;
    }

    public static Object world(Object client) {
        //? if yarn {
        return ((MinecraftClient) client).world;
        //?} else {
        /*return ((Minecraft) client).level;*/
        //?}
    }

    public static Object font(Object client) {
        //? if yarn {
        return ((MinecraftClient) client).textRenderer;
        //?} else {
        /*return ((Minecraft) client).font;*/
        //?}
    }

    public static void setClipboard(Object client, String text) {
        //? if yarn {
        ((MinecraftClient) client).keyboard.setClipboard(text);
        //?} else {
        /*((Minecraft) client).keyboardHandler.setClipboard(text);*/
        //?}
    }

    public static void actionBar(Object client, Object text) {
        //? if yarn {
        if (((MinecraftClient) client).player != null) {
            ((MinecraftClient) client).player.sendMessage((net.minecraft.text.Text) text, true);
        }
        //?} else {
        /*if (((Minecraft) client).player != null) {
            ((Minecraft) client).player.sendOverlayMessage((net.minecraft.network.chat.Component) text);
        }*/
        //?}
    }

    public static void setScreen(Object client, Object screen) {
        //? if yarn {
        ((MinecraftClient) client).setScreen((net.minecraft.client.gui.screen.Screen) screen);
        //?} else {
        /*((Minecraft) client).setScreenAndShow((net.minecraft.client.gui.screens.Screen) screen);*/
        //?}
    }

    public static void execute(Object client, Runnable task) {
        //? if yarn {
        ((MinecraftClient) client).execute(task);
        //?} else {
        /*((Minecraft) client).execute(task);*/
        //?}
    }

    public static int scaledWidth(Object client) {
        //? if yarn {
        return ((MinecraftClient) client).getWindow().getScaledWidth();
        //?} else {
        /*return ((Minecraft) client).getWindow().getGuiScaledWidth();*/
        //?}
    }

    public static int scaledHeight(Object client) {
        //? if yarn {
        return ((MinecraftClient) client).getWindow().getScaledHeight();
        //?} else {
        /*return ((Minecraft) client).getWindow().getGuiScaledHeight();*/
        //?}
    }

    public static boolean isSneaking(Object player) {
        //? if yarn {
        return ((net.minecraft.entity.player.PlayerEntity) player).isSneaking();
        //?} else {
        /*return ((net.minecraft.world.entity.player.Player) player).isShiftKeyDown();*/
        //?}
    }

    // ---- items
    public static Object mainHand(Object player) {
        //? if yarn {
        return ((net.minecraft.entity.player.PlayerEntity) player).getMainHandStack();
        //?} else {
        /*return ((net.minecraft.world.entity.player.Player) player).getMainHandItem();*/
        //?}
    }

    public static Object offHand(Object player) {
        //? if yarn {
        return ((net.minecraft.entity.player.PlayerEntity) player).getOffHandStack();
        //?} else {
        /*return ((net.minecraft.world.entity.player.Player) player).getOffhandItem();*/
        //?}
    }

    public static boolean itemEmpty(Object stack) {
        return ((ItemStack) stack).isEmpty();
    }

    public static int itemCount(Object stack) {
        return ((ItemStack) stack).getCount();
    }

    public static Object itemName(Object stack) {
        //? if yarn {
        return ((ItemStack) stack).getName();
        //?} else {
        /*return ((ItemStack) stack).getHoverName();*/
        //?}
    }

    // ---- hovered container slot (item under the cursor while a container screen is open)
    // General path (HUD auto / "/viewdata slot"): the current screen is reachable from the client on
    // yarn, but NOT from Minecraft on 26.x — so Mojmap returns null here and the container-key hook
    // uses hoveredSlotItemIn(screen) with the screen it is handed directly.
    public static Object hoveredSlotItem(Object client) {
        //? if yarn {
        if (((MinecraftClient) client).world == null) return null;
        return hoveredSlotItemIn(((MinecraftClient) client).currentScreen);
        //?} else {
        /*return null;*/
        //?}
    }

    public static Object hoveredSlotItemIn(Object screen) {
        //? if yarn {
        if (screen instanceof HandledScreen<?> handled) {
            Slot slot = handled.focusedSlot; // exposed via access widener
            if (slot != null && slot.hasStack()) return slot.getStack();
        }
        return null;
        //?} else {
        /*if (screen instanceof net.minecraft.client.gui.screens.inventory.AbstractContainerScreen<?> handled) {
            net.minecraft.world.inventory.Slot slot = handled.hoveredSlot; // exposed via class tweaker
            if (slot != null && slot.hasItem()) return slot.getItem();
        }
        return null;*/
        //?}
    }

    // ---- crosshair target
    public static Object hit(Object client) {
        //? if yarn {
        return ((MinecraftClient) client).crosshairTarget;
        //?} else {
        /*return ((Minecraft) client).hitResult;*/
        //?}
    }

    public static int hitType(Object hit) {
        if (hit == null) return HIT_MISS;
        HitResult.Type type = ((HitResult) hit).getType();
        if (type == HitResult.Type.BLOCK) return HIT_BLOCK;
        if (type == HitResult.Type.ENTITY) return HIT_ENTITY;
        return HIT_MISS;
    }

    public static Object hitBlockPos(Object hit) {
        return ((BlockHitResult) hit).getBlockPos();
    }

    public static Object hitEntity(Object hit) {
        return ((EntityHitResult) hit).getEntity();
    }

    public static int posX(Object pos) {
        return ((BlockPos) pos).getX();
    }

    public static int posY(Object pos) {
        return ((BlockPos) pos).getY();
    }

    public static int posZ(Object pos) {
        return ((BlockPos) pos).getZ();
    }

    // ---- blocks
    private static BlockState state(Object level, Object pos) {
        //? if yarn {
        return ((World) level).getBlockState((BlockPos) pos);
        //?} else {
        /*return ((Level) level).getBlockState((BlockPos) pos);*/
        //?}
    }

    public static String blockId(Object level, Object pos) {
        //? if yarn {
        return Registries.BLOCK.getId(state(level, pos).getBlock()).toString();
        //?} else {
        /*return BuiltInRegistries.BLOCK.getKey(state(level, pos).getBlock()).toString();*/
        //?}
    }

    public static Object blockName(Object level, Object pos) {
        return state(level, pos).getBlock().getName();
    }

    public static Map<String, String> blockProperties(Object level, Object pos) {
        Map<String, String> out = new LinkedHashMap<>();
        for (Property<?> property : state(level, pos).getProperties()) {
            out.put(property.getName(), propertyValue(state(level, pos), property));
        }
        return out;
    }

    private static <T extends Comparable<T>> String propertyValue(BlockState state, Property<T> property) {
        //? if yarn {
        return state.get(property).toString();
        //?} else {
        /*return state.getValue(property).toString();*/
        //?}
    }

    public static Object blockEntity(Object level, Object pos) {
        //? if yarn {
        return ((World) level).getBlockEntity((BlockPos) pos);
        //?} else {
        /*return ((Level) level).getBlockEntity((BlockPos) pos);*/
        //?}
    }

    // ---- entities
    public static String entityId(Object entity) {
        //? if yarn {
        return Registries.ENTITY_TYPE.getId(((Entity) entity).getType()).toString();
        //?} else {
        /*return BuiltInRegistries.ENTITY_TYPE.getKey(((Entity) entity).getType()).toString();*/
        //?}
    }

    public static String entityTypeString(Object entity) {
        return ((Entity) entity).getType().toString();
    }

    public static String entityUuid(Object entity) {
        //? if yarn {
        return ((Entity) entity).getUuidAsString();
        //?} else {
        /*return ((Entity) entity).getStringUUID();*/
        //?}
    }

    public static Object entityName(Object entity) {
        return ((Entity) entity).getName();
    }

    public static double entityX(Object entity) {
        return ((Entity) entity).getX();
    }

    public static double entityY(Object entity) {
        return ((Entity) entity).getY();
    }

    public static double entityZ(Object entity) {
        return ((Entity) entity).getZ();
    }

    public static boolean entityAlive(Object entity) {
        return entity != null && ((Entity) entity).isAlive();
    }

    /**
     * Nearest living entity within {@code reach} blocks whose direction from the eye is within
     * {@code coneDegrees} of the look vector, or {@code null}. Used as a fallback when the exact
     * crosshair ray misses a moving entity.
     */
    public static Object pickEntityInView(Object client, double reach, double coneDegrees) {
        double cosThreshold = Math.cos(Math.toRadians(coneDegrees));
        //? if yarn {
        MinecraftClient mc = (MinecraftClient) client;
        Entity camera = mc.getCameraEntity();
        if (camera == null || mc.world == null) return null;
        net.minecraft.util.math.Vec3d eye = camera.getEyePos();
        net.minecraft.util.math.Vec3d look = camera.getRotationVec(1.0f);
        net.minecraft.util.math.Box search = camera.getBoundingBox().stretch(look.multiply(reach)).expand(1.0);
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.world.getOtherEntities(camera, search, Entity::isAlive)) {
            net.minecraft.util.math.Vec3d toEntity = e.getBoundingBox().getCenter().subtract(eye);
            double dist = toEntity.length();
            if (dist > reach + 1.0 || dist < 1.0e-4) continue;
            if (toEntity.normalize().dotProduct(look) < cosThreshold) continue;
            if (dist < bestDist) {
                bestDist = dist;
                best = e;
            }
        }
        return best;
        //?} else {
        /*Minecraft mc = (Minecraft) client;
        Entity camera = mc.getCameraEntity();
        if (camera == null || mc.level == null) return null;
        net.minecraft.world.phys.Vec3 eye = camera.getEyePosition();
        net.minecraft.world.phys.Vec3 look = camera.getViewVector(1.0f);
        net.minecraft.world.phys.AABB search = camera.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0);
        Entity best = null;
        double bestDist = Double.MAX_VALUE;
        for (Entity e : mc.level.getEntities(camera, search, Entity::isAlive)) {
            net.minecraft.world.phys.Vec3 toEntity = e.getBoundingBox().getCenter().subtract(eye);
            double dist = toEntity.length();
            if (dist > reach + 1.0 || dist < 1.0e-4) continue;
            if (toEntity.normalize().dot(look) < cosThreshold) continue;
            if (dist < bestDist) {
                bestDist = dist;
                best = e;
            }
        }
        return best;*/
        //?}
    }

    // ---- client-command source (Fabric type; its methods still take the mapping's text type)
    public static void feedback(Object source, Object text) {
        //? if yarn {
        ((net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource) source)
                .sendFeedback((net.minecraft.text.Text) text);
        //?} else {
        /*((net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource) source)
                .sendFeedback((net.minecraft.network.chat.Component) text);*/
        //?}
    }

    public static void cmdError(Object source, Object text) {
        //? if yarn {
        ((net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource) source)
                .sendError((net.minecraft.text.Text) text);
        //?} else {
        /*((net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource) source)
                .sendError((net.minecraft.network.chat.Component) text);*/
        //?}
    }

    // ---- fabric hook registration
    public static void registerHud(HudRenderer renderer) {
        //? if !yarn && >=26 {
        /*net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(
                (net.minecraft.resources.Identifier) id("nbtviewer", "overlay"),
                (graphics, deltaTracker) -> renderer.render(graphics));*/
        //?} else {
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register((ctx, tick) -> renderer.render(ctx));
        //?}
    }

    public static void registerScreenScroll(ScrollTarget target) {
        ScreenEvents.AFTER_INIT.register((client, screen, w, h) -> {
            //? if yarn && <1.21.9 {
            ScreenMouseEvents.afterMouseScroll(screen).register(
                    (scr, mouseX, mouseY, horizontal, vertical) -> target.onScroll(screen, vertical));
            //?} else {
            /*ScreenMouseEvents.afterMouseScroll(screen).register(
                    (scr, mouseX, mouseY, horizontal, vertical, consumed) -> {
                        target.onScroll(screen, vertical);
                        return false;
                    });*/
            //?}
        });
    }
}
