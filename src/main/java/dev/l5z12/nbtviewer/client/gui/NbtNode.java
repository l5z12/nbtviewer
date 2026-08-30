// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import dev.l5z12.nbtviewer.client.nbt.NbtFormat;
import dev.l5z12.nbtviewer.facade.Nbt;

/** One row in the collapsible NBT tree. Children are built lazily. Tags are held as {@code Object}
 * and traversed through the {@link Nbt} facade, so this is mapping-agnostic. */
public final class NbtNode {

    public final NbtNode parent;
    public final String key;          // compound key, or list index as a string, or "" for the root
    public final boolean listIndex;   // true if this node is an element of a list
    public final Object value;
    public final int depth;
    public boolean expanded;

    private List<NbtNode> children;

    public NbtNode(NbtNode parent, String key, boolean listIndex, Object value, int depth, boolean expanded) {
        this.parent = parent;
        this.key = key;
        this.listIndex = listIndex;
        this.value = value;
        this.depth = depth;
        this.expanded = expanded;
    }

    public static NbtNode root(Object value, int autoExpandDepth, boolean sortKeys) {
        NbtNode root = new NbtNode(null, "", false, value, 0, true);
        root.autoExpand(autoExpandDepth, sortKeys);
        return root;
    }

    public boolean isContainer() {
        return Nbt.isCompound(value) || Nbt.isList(value);
    }

    public int size() {
        if (Nbt.isCompound(value)) return Nbt.compoundSize(value);
        if (Nbt.isList(value)) return Nbt.listSize(value);
        return 0;
    }

    public boolean hasChildren() {
        return isContainer() && size() > 0;
    }

    public List<NbtNode> children(boolean sortKeys) {
        if (children == null) {
            children = new ArrayList<>();
            if (Nbt.isCompound(value)) {
                List<String> keys = new ArrayList<>(Nbt.keys(value));
                if (sortKeys) keys.sort(String.CASE_INSENSITIVE_ORDER);
                for (String k : keys) {
                    children.add(new NbtNode(this, k, false, Nbt.get(value, k), depth + 1, false));
                }
            } else if (Nbt.isList(value)) {
                int n = Nbt.listSize(value);
                for (int i = 0; i < n; i++) {
                    children.add(new NbtNode(this, Integer.toString(i), true, Nbt.listGet(value, i), depth + 1, false));
                }
            }
        }
        return children;
    }

    public void autoExpand(int remainingDepth, boolean sortKeys) {
        if (remainingDepth <= 0 || !hasChildren()) return;
        expanded = true;
        for (NbtNode child : children(sortKeys)) {
            child.autoExpand(remainingDepth - 1, sortKeys);
        }
    }

    public void setExpandedRecursive(boolean value, boolean sortKeys) {
        if (!isContainer()) return;
        expanded = value;
        for (NbtNode child : children(sortKeys)) {
            child.setExpandedRecursive(value, sortKeys);
        }
    }

    /** Depth-first list of the currently-visible nodes (root itself is not shown). */
    public void collectVisible(List<NbtNode> out, boolean sortKeys) {
        for (NbtNode child : children(sortKeys)) {
            out.add(child);
            if (child.isContainer() && child.expanded) {
                child.collectVisible(out, sortKeys);
            }
        }
    }

    /** SNBT-style access path from the root to this node, e.g. {@code blockEntity.Items[0].id}. */
    public String path() {
        if (parent == null) return "";
        StringBuilder sb = new StringBuilder(parent.path());
        if (listIndex) {
            sb.append('[').append(key).append(']');
        } else {
            if (sb.length() > 0) sb.append('.');
            sb.append(NbtFormat.keyNeedsQuote(key) ? NbtFormat.quote(key) : key);
        }
        return sb.toString();
    }

    /** Whether this node's own key, or (for a leaf) its value, satisfies the search {@code tester}.
     * The tester encapsulates the mode — plain substring or compiled regex — so the tree stays
     * ignorant of how the query was entered. */
    public boolean matches(Predicate<String> tester) {
        if (tester.test(key)) return true;
        if (!isContainer()) {
            return tester.test(Nbt.leafString(value));
        }
        return false;
    }
}
