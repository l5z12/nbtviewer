// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.facade;

import java.util.ArrayList;
import java.util.List;

//? if yarn {
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.world.World;
//?} else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;*/
//?}

/**
 * NBT facade — traversal, construction and serialisation, all on {@code Object} tags. Absorbs both
 * the yarn/Mojmap type split ({@code NbtCompound}/{@code CompoundTag}, …) and the per-version API
 * churn (item codec at 1.20.5, {@code createNbtWithIdentifyingData} + {@code WriteView} at 1.21.6,
 * {@code getCompound} → Optional at 1.21.5).
 */
public final class Nbt {

    private Nbt() {
    }

    public static List<String> keys(Object compound) {
        //? if yarn {
        return new ArrayList<>(((NbtCompound) compound).getKeys());
        //?} else {
        /*return new ArrayList<>(((CompoundTag) compound).keySet());*/
        //?}
    }

    public static Object get(Object compound, String key) {
        //? if yarn {
        return ((NbtCompound) compound).get(key);
        //?} else {
        /*return ((CompoundTag) compound).get(key);*/
        //?}
    }

    public static boolean isCompound(Object tag) {
        //? if yarn {
        return tag instanceof NbtCompound;
        //?} else {
        /*return tag instanceof CompoundTag;*/
        //?}
    }

    public static boolean isList(Object tag) {
        //? if yarn {
        return tag instanceof NbtList;
        //?} else {
        /*return tag instanceof ListTag;*/
        //?}
    }

    public static boolean isString(Object tag) {
        //? if yarn {
        return tag instanceof NbtString;
        //?} else {
        /*return tag instanceof StringTag;*/
        //?}
    }

    public static boolean compoundEmpty(Object compound) {
        //? if yarn {
        return ((NbtCompound) compound).isEmpty();
        //?} else {
        /*return ((CompoundTag) compound).isEmpty();*/
        //?}
    }

    public static int compoundSize(Object compound) {
        //? if yarn {
        return ((NbtCompound) compound).getKeys().size();
        //?} else {
        /*return ((CompoundTag) compound).size();*/
        //?}
    }

    public static int listSize(Object list) {
        //? if yarn {
        return ((NbtList) list).size();
        //?} else {
        /*return ((ListTag) list).size();*/
        //?}
    }

    public static Object listGet(Object list, int i) {
        //? if yarn {
        return ((NbtList) list).get(i);
        //?} else {
        /*return ((ListTag) list).get(i);*/
        //?}
    }

    /** SNBT of a leaf tag — both mappings override {@code toString()} to the SNBT form. */
    public static String leafString(Object tag) {
        return tag.toString();
    }

    public static Object newCompound() {
        //? if yarn {
        return new NbtCompound();
        //?} else {
        /*return new CompoundTag();*/
        //?}
    }

    public static void putString(Object compound, String key, String value) {
        //? if yarn {
        ((NbtCompound) compound).putString(key, value);
        //?} else {
        /*((CompoundTag) compound).putString(key, value);*/
        //?}
    }

    public static void put(Object compound, String key, Object value) {
        //? if yarn {
        ((NbtCompound) compound).put(key, (NbtElement) value);
        //?} else {
        /*((CompoundTag) compound).put(key, (Tag) value);*/
        //?}
    }

    public static Object newList() {
        //? if yarn {
        return new NbtList();
        //?} else {
        /*return new ListTag();*/
        //?}
    }

    public static void listAdd(Object list, Object element) {
        //? if yarn {
        ((NbtList) list).add((NbtElement) element);
        //?} else {
        /*((ListTag) list).add((Tag) element);*/
        //?}
    }

    public static Object stringTag(String s) {
        //? if yarn {
        return NbtString.of(s);
        //?} else {
        /*return StringTag.valueOf(s);*/
        //?}
    }

    public static int countTags(Object tag) {
        int count = 1;
        if (isCompound(tag)) {
            for (String key : keys(tag)) count += countTags(get(tag, key));
        } else if (isList(tag)) {
            int n = listSize(tag);
            for (int i = 0; i < n; i++) count += countTags(listGet(tag, i));
        }
        return count;
    }

    // ------------------------------------------------------------------ serialisation

    public static Object itemToNbt(Object stackObj, Object levelObj) {
        //? if yarn && >=1.20.5 {
        ItemStack stack = (ItemStack) stackObj;
        if (stack.isEmpty()) return new NbtCompound();
        var ops = net.minecraft.registry.RegistryOps.of(NbtOps.INSTANCE, ((World) levelObj).getRegistryManager());
        NbtElement encoded = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
        return encoded instanceof NbtCompound compound ? compound : new NbtCompound();
        //?} else if yarn {
        /*ItemStack stack = (ItemStack) stackObj;
        if (stack.isEmpty()) return new NbtCompound();
        return stack.writeNbt(new NbtCompound());*/
        //?} else {
        /*ItemStack stack = (ItemStack) stackObj;
        if (stack.isEmpty()) return new CompoundTag();
        var ops = net.minecraft.resources.RegistryOps.create(NbtOps.INSTANCE, ((Level) levelObj).registryAccess());
        Tag encoded = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow();
        return encoded instanceof CompoundTag compound ? compound : new CompoundTag();*/
        //?}
    }

    public static Object blockEntityToNbt(Object beObj, Object levelObj) {
        //? if yarn && >=1.21.6 {
        /*return ((BlockEntity) beObj).createNbtWithIdentifyingData(((World) levelObj).getRegistryManager());*/
        //?} else if yarn && >=1.20.5 {
        return ((BlockEntity) beObj).createNbtWithId(((World) levelObj).getRegistryManager());
        //?} else if yarn {
        /*return ((BlockEntity) beObj).createNbtWithId();*/
        //?} else {
        /*return ((BlockEntity) beObj).saveWithFullMetadata(((Level) levelObj).registryAccess());*/
        //?}
    }

    public static Object entityToNbt(Object entityObj) {
        //? if yarn && <1.21.6 {
        return ((Entity) entityObj).writeNbt(new NbtCompound());
        //?} else if yarn {
        /*return new NbtCompound();*/
        //?} else {
        /*Entity entity = (Entity) entityObj;
        try (var reporter = new net.minecraft.util.ProblemReporter.ScopedCollector(dev.l5z12.nbtviewer.NbtViewer.LOGGER)) {
            var output = net.minecraft.world.level.storage.TagValueOutput.createWithContext(reporter, entity.level().registryAccess());
            entity.saveWithoutId(output);
            return output.buildResult();
        }*/
        //?}
    }

    public static Object childCompound(Object parent, String key) {
        if (parent == null) return null;
        //? if yarn && <1.21.5 {
        return ((NbtCompound) parent).contains(key) ? ((NbtCompound) parent).getCompound(key) : null;
        //?} else if yarn {
        /*return ((NbtCompound) parent).getCompound(key).orElse(null);*/
        //?} else {
        /*return ((CompoundTag) parent).getCompound(key).orElse(null);*/
        //?}
    }
}
