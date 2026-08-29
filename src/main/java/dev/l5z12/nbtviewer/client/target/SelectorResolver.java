// SPDX-FileCopyrightText: 2026 l5z12
//
// SPDX-License-Identifier: GPL-3.0-or-later

package dev.l5z12.nbtviewer.client.target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import dev.l5z12.nbtviewer.facade.Mc;
import dev.l5z12.nbtviewer.facade.Txt;

/**
 * Client-side resolution of vanilla-style target selectors. The vanilla {@code EntityArgument}
 * resolves against a {@code ServerCommandSource}, which a client command doesn't have, so this
 * re-implements the client-meaningful subset against the loaded client entities:
 *
 * <ul>
 *   <li>bases {@code @s @p @a @r @e @n};</li>
 *   <li>options {@code type} (with {@code !} negation, bare names get the {@code minecraft:}
 *       namespace), {@code name} (with {@code !}, quotes allowed), {@code distance} (ranges
 *       {@code a..b}, {@code ..b}, {@code a..}, or {@code n} as a max), {@code limit},
 *       {@code sort} = {@code nearest|furthest|random|arbitrary}.</li>
 * </ul>
 *
 * Options needing server state (scores, team, gamemode, advancements, nbt, tags) are ignored.
 * Distance/sort are measured from the client player.
 */
public final class SelectorResolver {

    private SelectorResolver() {
    }

    public static boolean isSelector(String s) {
        return s != null && s.length() >= 2 && s.charAt(0) == '@';
    }

    public static List<Object> resolve(Object client, String selector) {
        if (!isSelector(selector) || !Mc.hasWorld(client)) return List.of();

        Object self = Mc.player(client);
        char base = selector.charAt(1);
        List<Object> set;
        String sort;
        int limit;
        switch (base) {
            case 's' -> {
                return self == null ? List.of() : new ArrayList<>(List.of(self));
            }
            case 'p' -> { set = Mc.allPlayers(client); sort = "nearest"; limit = 1; }
            case 'r' -> { set = Mc.allPlayers(client); sort = "random"; limit = 1; }
            case 'a' -> { set = Mc.allPlayers(client); sort = "arbitrary"; limit = Integer.MAX_VALUE; }
            case 'n' -> { set = Mc.allLoadedEntities(client); sort = "nearest"; limit = 1; }
            case 'e' -> { set = Mc.allLoadedEntities(client); sort = "arbitrary"; limit = Integer.MAX_VALUE; }
            default -> {
                return List.of();
            }
        }

        List<String> typeFilters = new ArrayList<>();
        List<String> nameFilters = new ArrayList<>();
        String distance = null;

        for (String[] kv : parseOptions(bracketBody(selector))) {
            switch (kv[0]) {
                case "type" -> typeFilters.add(kv[1]);
                case "name" -> nameFilters.add(kv[1]);
                case "distance", "r" -> distance = kv[1];
                case "sort" -> sort = kv[1];
                case "limit", "c" -> limit = parseIntOr(kv[1], limit);
                default -> { /* unsupported option: ignore */ }
            }
        }

        List<Object> result = new ArrayList<>(set);
        String dist = distance;
        result.removeIf(e -> !matchesType(e, typeFilters)
                || !matchesName(e, nameFilters)
                || !matchesDistance(self, e, dist));

        sort(result, self, sort);
        if (limit >= 0 && result.size() > limit) {
            result = new ArrayList<>(result.subList(0, limit));
        }
        return result;
    }

    // ------------------------------------------------------------------ option parsing

    private static String bracketBody(String selector) {
        int lb = selector.indexOf('[');
        if (lb >= 0 && selector.endsWith("]")) {
            return selector.substring(lb + 1, selector.length() - 1);
        }
        return "";
    }

    private static List<String[]> parseOptions(String body) {
        List<String[]> out = new ArrayList<>();
        if (body.isBlank()) return out;
        for (String part : splitTopLevel(body)) {
            int eq = part.indexOf('=');
            if (eq < 0) continue;
            String key = part.substring(0, eq).trim().toLowerCase(Locale.ROOT);
            String value = part.substring(eq + 1).trim();
            if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            out.add(new String[] {key, value});
        }
        return out;
    }

    /** Split on commas that are not inside double quotes or nested brackets. */
    private static List<String> splitTopLevel(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean quoted = false;
        int depth = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '"') quoted = !quoted;
            else if (!quoted && (c == '[' || c == '{')) depth++;
            else if (!quoted && (c == ']' || c == '}')) depth--;
            if (c == ',' && !quoted && depth == 0) {
                parts.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(c);
            }
        }
        if (cur.length() > 0) parts.add(cur.toString());
        return parts;
    }

    // ------------------------------------------------------------------ filters

    private static boolean matchesType(Object entity, List<String> filters) {
        if (filters.isEmpty()) return true;
        String id = Mc.entityId(entity);
        for (String raw : filters) {
            boolean negate = raw.startsWith("!");
            String want = normalizeId(negate ? raw.substring(1) : raw);
            boolean equal = id.equals(want);
            if (negate == equal) return false; // positive must match; negative must not
        }
        return true;
    }

    private static boolean matchesName(Object entity, List<String> filters) {
        if (filters.isEmpty()) return true;
        String name = Txt.str(Mc.entityName(entity));
        for (String raw : filters) {
            boolean negate = raw.startsWith("!");
            String want = negate ? raw.substring(1) : raw;
            boolean equal = name.equals(want);
            if (negate == equal) return false;
        }
        return true;
    }

    private static boolean matchesDistance(Object self, Object entity, String range) {
        if (range == null || self == null) return true;
        double d = distance(self, entity);
        double min = 0;
        double max = Double.MAX_VALUE;
        int dots = range.indexOf("..");
        if (dots >= 0) {
            String lo = range.substring(0, dots).trim();
            String hi = range.substring(dots + 2).trim();
            if (!lo.isEmpty()) min = parseDoubleOr(lo, 0);
            if (!hi.isEmpty()) max = parseDoubleOr(hi, Double.MAX_VALUE);
        } else {
            max = parseDoubleOr(range.trim(), Double.MAX_VALUE); // bare value treated as a maximum
        }
        return d >= min - 1.0e-6 && d <= max + 1.0e-6;
    }

    // ------------------------------------------------------------------ sorting

    private static void sort(List<Object> entities, Object self, String sort) {
        switch (sort == null ? "" : sort.toLowerCase(Locale.ROOT)) {
            case "nearest" -> {
                if (self != null) entities.sort((a, b) -> Double.compare(distanceSq(self, a), distanceSq(self, b)));
            }
            case "furthest" -> {
                if (self != null) entities.sort((a, b) -> Double.compare(distanceSq(self, b), distanceSq(self, a)));
            }
            case "random" -> Collections.shuffle(entities);
            default -> { /* arbitrary: keep insertion order */ }
        }
    }

    // ------------------------------------------------------------------ helpers

    private static String normalizeId(String v) {
        return v.indexOf(':') >= 0 ? v : "minecraft:" + v;
    }

    private static double distance(Object self, Object e) {
        return Math.sqrt(distanceSq(self, e));
    }

    private static double distanceSq(Object self, Object e) {
        double dx = Mc.entityX(e) - Mc.entityX(self);
        double dy = Mc.entityY(e) - Mc.entityY(self);
        double dz = Mc.entityZ(e) - Mc.entityZ(self);
        return dx * dx + dy * dy + dz * dz;
    }

    private static int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static double parseDoubleOr(String s, double fallback) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
