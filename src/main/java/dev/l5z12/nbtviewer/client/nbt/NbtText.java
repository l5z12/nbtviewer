// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.nbt;

import java.util.ArrayList;
import java.util.List;

import dev.l5z12.nbtviewer.facade.Nbt;
import dev.l5z12.nbtviewer.facade.Txt;

/**
 * Renders NBT into coloured text (facade {@code Text}/{@code Component}), matching the familiar
 * vanilla {@code /data} colour scheme (keys aqua, strings green, numbers &amp; arrays gold,
 * punctuation grey).
 *
 * <ul>
 *   <li>{@link #pretty} builds a single multi-line text (newlines render in chat).</li>
 *   <li>{@link #lines} builds one text per visual line (for the HUD / GUI, whose text renderer
 *       does not split on {@code \n}).</li>
 * </ul>
 * Traversal and text creation go through the {@link Nbt}/{@link Txt} facades, so no per-version or
 * per-mapping code is required.
 */
public final class NbtText {

    private NbtText() {
    }

    // ------------------------------------------------------------------ single multi-line text

    public static Object pretty(Object el, boolean sortKeys, boolean colorize) {
        Object out = Txt.empty();
        appendMultiline(out, el, 0, sortKeys, colorize);
        return out;
    }

    private static void appendMultiline(Object out, Object el, int indent, boolean sortKeys, boolean color) {
        if (Nbt.isCompound(el)) {
            if (Nbt.compoundEmpty(el)) {
                Txt.append(out, punct("{}", color));
                return;
            }
            Txt.append(out, punct("{", color));
            List<String> keys = sortedKeys(el, sortKeys);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                Txt.append(out, Txt.literal("\n" + "  ".repeat(indent + 1)));
                Txt.append(out, keyText(key, color));
                Txt.append(out, punct(": ", color));
                appendMultiline(out, Nbt.get(el, key), indent + 1, sortKeys, color);
                if (i < keys.size() - 1) Txt.append(out, punct(",", color));
            }
            Txt.append(out, Txt.literal("\n" + "  ".repeat(indent)));
            Txt.append(out, punct("}", color));
        } else if (Nbt.isList(el)) {
            int size = Nbt.listSize(el);
            if (size == 0) {
                Txt.append(out, punct("[]", color));
                return;
            }
            Txt.append(out, punct("[", color));
            for (int i = 0; i < size; i++) {
                Txt.append(out, Txt.literal("\n" + "  ".repeat(indent + 1)));
                appendMultiline(out, Nbt.listGet(el, i), indent + 1, sortKeys, color);
                if (i < size - 1) Txt.append(out, punct(",", color));
            }
            Txt.append(out, Txt.literal("\n" + "  ".repeat(indent)));
            Txt.append(out, punct("]", color));
        } else {
            Txt.append(out, leafText(el, color, Integer.MAX_VALUE));
        }
    }

    // ------------------------------------------------------------------ one text per line

    public static List<Object> lines(Object el, boolean sortKeys, boolean color, int maxValueLen) {
        LineBuilder lb = new LineBuilder();
        appendLines(lb, el, 0, sortKeys, color, maxValueLen);
        return lb.finish();
    }

    private static void appendLines(LineBuilder lb, Object el, int indent, boolean sortKeys, boolean color, int maxValueLen) {
        if (Nbt.isCompound(el)) {
            if (Nbt.compoundEmpty(el)) {
                lb.seg(punct("{}", color));
                return;
            }
            lb.seg(punct("{", color));
            List<String> keys = sortedKeys(el, sortKeys);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                lb.newLine(indent + 1);
                lb.seg(keyText(key, color));
                lb.seg(punct(": ", color));
                appendLines(lb, Nbt.get(el, key), indent + 1, sortKeys, color, maxValueLen);
                if (i < keys.size() - 1) lb.seg(punct(",", color));
            }
            lb.newLine(indent);
            lb.seg(punct("}", color));
        } else if (Nbt.isList(el)) {
            int size = Nbt.listSize(el);
            if (size == 0) {
                lb.seg(punct("[]", color));
                return;
            }
            lb.seg(punct("[", color));
            for (int i = 0; i < size; i++) {
                lb.newLine(indent + 1);
                appendLines(lb, Nbt.listGet(el, i), indent + 1, sortKeys, color, maxValueLen);
                if (i < size - 1) lb.seg(punct(",", color));
            }
            lb.newLine(indent);
            lb.seg(punct("]", color));
        } else {
            lb.seg(leafText(el, color, maxValueLen));
        }
    }

    // ------------------------------------------------------------------ leaf/key colouring

    public static Object leafText(Object el, boolean color, int maxLen) {
        String s = Nbt.leafString(el);
        if (s.length() > maxLen && maxLen > 1) {
            int hidden = s.length() - maxLen;
            s = s.substring(0, maxLen) + "…(+" + hidden + ")";
        }
        if (!color) return Txt.literal(s);
        int rgb = Nbt.isString(el) ? Txt.GREEN : Txt.GOLD;
        return Txt.colored(Txt.literal(s), rgb);
    }

    public static Object keyText(String key, boolean color) {
        String s = NbtFormat.quoteKeyIfNeeded(key);
        return color ? Txt.colored(Txt.literal(s), Txt.AQUA) : Txt.literal(s);
    }

    private static Object punct(String s, boolean color) {
        return color ? Txt.colored(Txt.literal(s), Txt.GRAY) : Txt.literal(s);
    }

    private static List<String> sortedKeys(Object compound, boolean sortKeys) {
        List<String> keys = new ArrayList<>(Nbt.keys(compound));
        if (sortKeys) keys.sort(String.CASE_INSENSITIVE_ORDER);
        return keys;
    }

    private static final class LineBuilder {
        private final List<Object> lines = new ArrayList<>();
        private Object current = Txt.empty();

        void seg(Object segment) {
            Txt.append(current, segment);
        }

        void newLine(int indent) {
            lines.add(current);
            current = Txt.literal("  ".repeat(indent));
        }

        List<Object> finish() {
            lines.add(current);
            return lines;
        }
    }
}
