// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.nbt;

import java.util.ArrayList;
import java.util.List;

import dev.l5z12.nbtviewer.facade.Nbt;

/**
 * Plain-text (uncoloured) SNBT rendering plus small statistics helpers.
 * Traversal goes through the {@link Nbt} facade ({@code keys}/{@code get}/{@code listGet}/
 * {@code leafString}), so this needs no per-version or per-mapping code — SNBT leaf form comes from
 * each tag's own {@code toString()}, which is stable across generations.
 */
public final class NbtFormat {

    private static final String INDENT = "  ";

    private NbtFormat() {
    }

    public static String toSnbt(Object el, boolean pretty, boolean sortKeys) {
        if (el == null) return "";
        if (!pretty) return Nbt.leafString(el);
        StringBuilder sb = new StringBuilder();
        appendPretty(sb, el, 0, sortKeys);
        return sb.toString();
    }

    private static void appendPretty(StringBuilder sb, Object el, int indent, boolean sortKeys) {
        if (Nbt.isCompound(el)) {
            if (Nbt.compoundEmpty(el)) {
                sb.append("{}");
                return;
            }
            sb.append("{\n");
            List<String> keys = new ArrayList<>(Nbt.keys(el));
            if (sortKeys) keys.sort(String.CASE_INSENSITIVE_ORDER);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                indent(sb, indent + 1).append(quoteKeyIfNeeded(key)).append(": ");
                appendPretty(sb, Nbt.get(el, key), indent + 1, sortKeys);
                if (i < keys.size() - 1) sb.append(',');
                sb.append('\n');
            }
            indent(sb, indent).append('}');
        } else if (Nbt.isList(el)) {
            appendList(sb, el, indent, sortKeys);
        } else {
            // Primitives + arrays (byte/int/long arrays): SNBT leaf form is version-stable.
            sb.append(Nbt.leafString(el));
        }
    }

    private static void appendList(StringBuilder sb, Object list, int indent, boolean sortKeys) {
        int size = Nbt.listSize(list);
        if (size == 0) {
            sb.append("[]");
            return;
        }
        sb.append("[\n");
        for (int i = 0; i < size; i++) {
            indent(sb, indent + 1);
            appendPretty(sb, Nbt.listGet(list, i), indent + 1, sortKeys);
            if (i < size - 1) sb.append(',');
            sb.append('\n');
        }
        indent(sb, indent).append(']');
    }

    private static StringBuilder indent(StringBuilder sb, int depth) {
        for (int i = 0; i < depth; i++) sb.append(INDENT);
        return sb;
    }

    // ---------------------------------------------------------------- key quoting

    public static boolean keyNeedsQuote(String key) {
        if (key.isEmpty()) return true;
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            boolean simple = (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '+';
            if (!simple) return true;
        }
        return false;
    }

    public static String quoteKeyIfNeeded(String key) {
        return keyNeedsQuote(key) ? quote(key) : key;
    }

    /**
     * Inverse of {@link #quote}: strip the surrounding quotes and un-escape an SNBT string literal.
     * Minecraft only ever escapes the backslash and the chosen quote character, so a single-pass
     * un-escape is exact. A value that is not a quoted literal is returned unchanged.
     */
    public static String unquote(String s) {
        if (s.length() >= 2) {
            char q = s.charAt(0);
            if ((q == '"' || q == '\'') && s.charAt(s.length() - 1) == q) {
                StringBuilder sb = new StringBuilder(s.length() - 2);
                for (int i = 1; i < s.length() - 1; i++) {
                    char c = s.charAt(i);
                    if (c == '\\' && i + 1 < s.length() - 1) {
                        sb.append(s.charAt(++i));
                    } else {
                        sb.append(c);
                    }
                }
                return sb.toString();
            }
        }
        return s;
    }

    public static String quote(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 2);
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"' || c == '\\') sb.append('\\');
            sb.append(c);
        }
        return sb.append('"').toString();
    }

    // ---------------------------------------------------------------- statistics

    /** Total number of tags in the tree (compounds/lists count themselves plus their children). */
    public static int countTags(Object el) {
        return Nbt.countTags(el);
    }
}
