// PulseIndex — generates and verifies pulse-index.json, the machine-readable retrieval
// layer for Pulse's component kit (see AGENTS.md / ARCHITECTURE.md).
//
// STRUCTURE (component names, params, types, whether-defaulted) is parsed from the Kotlin
// source so the index cannot lie about the public API. SEMANTICS (role, perfTier, since,
// agent-adaptation guidance) come from the hand-curated sidecar pulse-meta.json.
//
//   Generate:  java tools/PulseIndex.java generate   [repoRoot]
//   Verify:    java tools/PulseIndex.java verify      [repoRoot]   # exit 1 on drift
//
// Single-file, JDK 17+ (no external dependencies, no Android SDK, no Gradle needed) so the
// CI drift-check runs in seconds without the whole Android toolchain.
//
// This file is intentionally dependency-free, including a tiny JSON reader/writer, because
// the JDK ships no JSON API and adding one would mean a build step for a text-parsing tool.

import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class PulseIndex {

    static final String COMP_DIR = "pulse-ui/src/main/java/design/pulse/ui/components";
    static final String THEME_DIR = "pulse-ui/src/main/java/design/pulse/ui/theme";
    static final String META_FILE = "pulse-meta.json";
    static final String OUT_FILE = "pulse-index.json";
    static final String COMPONENT_PKG = "design.pulse.ui.components";

    public static void main(String[] args) throws Exception {
        String mode = args.length > 0 ? args[0] : "generate";
        String root = args.length > 1 ? args[1] : ".";

        Built built = build(root);
        String json = built.json;

        if (mode.equals("verify")) {
            boolean ok = true;
            if (!built.problems.isEmpty()) {
                ok = false;
                System.err.println("pulse-index.json / pulse-meta.json are out of sync with the source:");
                for (String p : built.problems) System.err.println("  - " + p);
            }
            Path out = Paths.get(root, OUT_FILE);
            // Read as UTF-8 explicitly: the index carries em-dashes, and the JVM default charset is
            // not UTF-8 on Java 17/Windows — decoding with it made verify falsely report "stale"
            // locally while CI (UTF-8 default on Linux) stayed green.
            String current = Files.exists(out)
                    ? new String(Files.readAllBytes(out), java.nio.charset.StandardCharsets.UTF_8) : "";
            if (!current.trim().equals(json.trim())) {
                ok = false;
                System.err.println("pulse-index.json is stale. Regenerate and commit:");
                System.err.println("  java tools/PulseIndex.java generate");
            }
            if (!ok) System.exit(1);
            System.out.println("pulse-index.json is up to date (" + built.count + " components).");
        } else {
            Files.write(Paths.get(root, OUT_FILE), json.getBytes("UTF-8"));
            System.out.println("Wrote " + OUT_FILE + " (" + built.count + " components).");
            for (String p : built.problems) System.out.println("  WARNING: " + p);
        }
    }

    static final class Built {
        final String json;
        final int count;
        final List<String> problems;
        Built(String json, int count, List<String> problems) {
            this.json = json; this.count = count; this.problems = problems;
        }
    }

    // ---- build the index ---------------------------------------------------------------

    @SuppressWarnings("unchecked")
    static Built build(String root) throws Exception {
        List<String> problems = new ArrayList<>();

        // 1. Curated sidecar.
        String metaText = readFile(Paths.get(root, META_FILE));
        Map<String, Object> meta = (Map<String, Object>) Json.parse(metaText);
        Map<String, Object> metaComponents = asMap(get(meta, "components"));
        Map<String, Object> defaultAgentMeta = asMap(get(asMap(get(meta, "defaults")), "agentMeta"));

        // 2. Facts parsed from source.
        String pulseVersion = parseVersion(readFile(Paths.get(root, "pulse-ui/build.gradle.kts")));
        List<String> accents = parseAccents(readFile(Paths.get(root, THEME_DIR, "Structure.kt")));

        // Every *.kt in the components dir is scanned (sorted for deterministic output) — adding a
        // new component file needs no change here; just give each public component a pulse-meta entry.
        List<String> componentFiles = new ArrayList<>();
        try (java.util.stream.Stream<Path> s = Files.list(Paths.get(root, COMP_DIR))) {
            s.filter(p -> p.getFileName().toString().endsWith(".kt"))
             .map(p -> p.getFileName().toString())
             .sorted()
             .forEach(componentFiles::add);
        }

        List<Map<String, Object>> components = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (String file : componentFiles) {
            String src = readFile(Paths.get(root, COMP_DIR, file));
            for (Decl d : parseDecls(src)) {
                seen.add(d.name);
                Map<String, Object> metaEntry = metaComponents == null ? null
                        : asMap(metaComponents.get(d.name));
                if (metaEntry == null) {
                    problems.add("component '" + d.name + "' (" + file
                            + ") has no entry in pulse-meta.json - add role/perfTier/since.");
                }
                components.add(assemble(d, file, metaEntry, defaultAgentMeta));
            }
        }

        // 3. Orphan sidecar entries (component renamed/removed but meta left behind).
        if (metaComponents != null) {
            for (String k : metaComponents.keySet()) {
                if (!seen.contains(k)) {
                    problems.add("pulse-meta.json has an entry for '" + k
                            + "' but no such component exists in source.");
                }
            }
        }

        // 4. Emit.
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("schemaVersion", 1);
        out.put("generator", "tools/PulseIndex.java");
        out.put("pulseVersion", pulseVersion);
        out.put("accents", accents);
        out.put("componentCount", components.size());
        out.put("components", components);

        String json = JsonWriter.write(out) + "\n";
        return new Built(json, components.size(), problems);
    }

    @SuppressWarnings("unchecked")
    static Map<String, Object> assemble(Decl d, String file, Map<String, Object> metaEntry,
                                        Map<String, Object> defaultAgentMeta) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("name", d.name);
        c.put("kind", d.isModifier ? "modifier" : "composable");
        if (d.isModifier) c.put("receiver", "Modifier");
        c.put("package", COMPONENT_PKG);
        c.put("file", file);
        c.put("role", metaEntry == null ? null : metaEntry.get("role"));
        c.put("since", metaEntry == null ? null : metaEntry.get("since"));
        c.put("perfTier", metaEntry == null ? null : metaEntry.get("perfTier"));
        if (d.summary != null) c.put("summary", d.summary);

        // params (structure from source; per-param `since` from sidecar)
        Map<String, Object> metaParams = metaEntry == null ? null : asMap(metaEntry.get("params"));
        List<Map<String, Object>> params = new ArrayList<>();
        for (Param p : d.params) {
            Map<String, Object> pm = new LinkedHashMap<>();
            pm.put("name", p.name);
            pm.put("type", p.type);
            pm.put("hasDefault", p.hasDefault);
            pm.put("required", !p.hasDefault);
            Map<String, Object> mp = metaParams == null ? null : asMap(metaParams.get(p.name));
            if (mp != null && mp.get("since") != null) pm.put("since", mp.get("since"));
            params.add(pm);
        }
        c.put("params", params);

        // agentMeta = defaults, with per-component overrides + appended neverDoExtra
        Map<String, Object> compAgentMeta = metaEntry == null ? null : asMap(metaEntry.get("agentMeta"));
        Map<String, Object> agentMeta = new LinkedHashMap<>();
        agentMeta.put("extend", pick(compAgentMeta, "extend", defaultAgentMeta));
        List<Object> neverDo = new ArrayList<>();
        List<Object> baseNeverDo = asList(defaultAgentMeta == null ? null : defaultAgentMeta.get("neverDo"));
        if (baseNeverDo != null) neverDo.addAll(baseNeverDo);
        List<Object> extra = asList(compAgentMeta == null ? null : compAgentMeta.get("neverDoExtra"));
        if (extra != null) neverDo.addAll(extra);
        agentMeta.put("neverDo", neverDo);
        agentMeta.put("meaningVia", pick(compAgentMeta, "meaningVia", defaultAgentMeta));
        c.put("agentMeta", agentMeta);
        return c;
    }

    static Object pick(Map<String, Object> override, String key, Map<String, Object> fallback) {
        if (override != null && override.get(key) != null) return override.get(key);
        return fallback == null ? null : fallback.get(key);
    }

    // ---- Kotlin signature parsing ------------------------------------------------------

    static final class Decl {
        String name;
        boolean isModifier;
        String summary;
        List<Param> params = new ArrayList<>();
    }
    static final class Param {
        String name;
        String type;
        boolean hasDefault;
    }

    // Top-level `fun` declarations only (column 0), so nested local funs (indented) are ignored.
    static final Pattern FUN = Pattern.compile("(?m)^fun\\s+(Modifier\\.)?(\\w+)\\s*\\(");

    static List<Decl> parseDecls(String src) {
        List<Decl> out = new ArrayList<>();
        Matcher m = FUN.matcher(src);
        while (m.find()) {
            Decl d = new Decl();
            d.isModifier = m.group(1) != null;
            d.name = m.group(2);
            int open = m.end() - 1; // index of '('
            String block = stripComments(extractParenBlock(src, open));
            for (String part : splitTopLevel(block)) {
                String t = part.trim();
                if (t.isEmpty()) continue;
                d.params.add(parseParam(t));
            }
            d.summary = precedingSummary(src, m.start());
            out.add(d);
        }
        return out;
    }

    // Strip Kotlin // line comments and /* */ block comments (respecting string literals), so a
    // KDoc-style aside inside a parameter list can't leak commas/brackets into the param split.
    static String stripComments(String s) {
        StringBuilder sb = new StringBuilder();
        boolean inStr = false;
        char q = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                sb.append(c);
                if (c == '\\') { if (i + 1 < s.length()) sb.append(s.charAt(++i)); continue; }
                if (c == q) inStr = false;
                continue;
            }
            if (c == '"' || c == '\'') { inStr = true; q = c; sb.append(c); continue; }
            if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/') {
                i += 2;
                while (i < s.length() && s.charAt(i) != '\n') i++;
                if (i < s.length()) sb.append('\n');
                continue;
            }
            if (c == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < s.length() && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/')) i++;
                i++; // land on the closing '/', the for-loop's i++ steps past it
                continue;
            }
            sb.append(c);
        }
        return sb.toString();
    }

    // Returns the content between the outermost parentheses starting at index `open` ('(').
    static String extractParenBlock(String s, int open) {
        int depth = 0;
        boolean inStr = false;
        char q = 0;
        StringBuilder sb = new StringBuilder();
        for (int k = open; k < s.length(); k++) {
            char c = s.charAt(k);
            if (inStr) {
                sb.append(c);
                if (c == '\\') { if (k + 1 < s.length()) { sb.append(s.charAt(++k)); } continue; }
                if (c == q) inStr = false;
                continue;
            }
            if (c == '"') { inStr = true; q = c; sb.append(c); continue; }
            if (c == '(') { depth++; if (depth == 1) continue; }
            else if (c == ')') { depth--; if (depth == 0) return sb.toString(); }
            sb.append(c);
        }
        return sb.toString();
    }

    // Split a parameter block on commas that sit at bracket-depth 0 (outside (), {}, [], <>, strings).
    static List<String> splitTopLevel(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        int round = 0, curly = 0, square = 0, angle = 0;
        boolean inStr = false;
        char q = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                sb.append(c);
                if (c == '\\') { if (i + 1 < s.length()) sb.append(s.charAt(++i)); continue; }
                if (c == q) inStr = false;
                continue;
            }
            switch (c) {
                case '"': case '\'': inStr = true; q = c; sb.append(c); break;
                case '(': round++; sb.append(c); break;
                case ')': round--; sb.append(c); break;
                case '{': curly++; sb.append(c); break;
                case '}': curly--; sb.append(c); break;
                case '[': square++; sb.append(c); break;
                case ']': square--; sb.append(c); break;
                case '<': angle++; sb.append(c); break;
                case '>':
                    if (i > 0 && s.charAt(i - 1) == '-') { sb.append(c); } // part of "->", not a generic close
                    else { if (angle > 0) angle--; sb.append(c); }
                    break;
                case ',':
                    if (round == 0 && curly == 0 && square == 0 && angle == 0) {
                        parts.add(sb.toString()); sb.setLength(0);
                    } else sb.append(c);
                    break;
                default: sb.append(c);
            }
        }
        if (sb.toString().trim().length() > 0) parts.add(sb.toString());
        return parts;
    }

    static Param parseParam(String p) {
        Param out = new Param();
        int colon = topLevelIndexOf(p, ':');
        String beforeColon = colon >= 0 ? p.substring(0, colon) : p;
        // name = last whitespace-separated token before ':' (handles `vararg x`, annotations)
        String[] toks = beforeColon.trim().split("\\s+");
        out.name = toks[toks.length - 1];
        String rest = colon >= 0 ? p.substring(colon + 1) : "";
        int eq = topLevelEquals(rest);
        out.hasDefault = eq >= 0;
        String type = (eq >= 0 ? rest.substring(0, eq) : rest);
        out.type = type.replaceAll("\\s+", " ").trim();
        return out;
    }

    // First occurrence of `target` at bracket-depth 0 (outside brackets/strings).
    static int topLevelIndexOf(String s, char target) {
        int round = 0, curly = 0, square = 0, angle = 0;
        boolean inStr = false;
        char q = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                if (c == '\\') { i++; continue; }
                if (c == q) inStr = false;
                continue;
            }
            switch (c) {
                case '"': case '\'': inStr = true; q = c; break;
                case '(': round++; break;   case ')': round--; break;
                case '{': curly++; break;   case '}': curly--; break;
                case '[': square++; break;  case ']': square--; break;
                case '<': angle++; break;
                case '>': if (!(i > 0 && s.charAt(i - 1) == '-') && angle > 0) angle--; break;
                default:
                    if (c == target && round == 0 && curly == 0 && square == 0 && angle == 0) return i;
            }
        }
        return -1;
    }

    // Index of the default-assignment '=' at depth 0 (not ==, <=, >=, !=, =>).
    static int topLevelEquals(String s) {
        int round = 0, curly = 0, square = 0, angle = 0;
        boolean inStr = false;
        char q = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (inStr) {
                if (c == '\\') { i++; continue; }
                if (c == q) inStr = false;
                continue;
            }
            switch (c) {
                case '"': case '\'': inStr = true; q = c; break;
                case '(': round++; break;   case ')': round--; break;
                case '{': curly++; break;   case '}': curly--; break;
                case '[': square++; break;  case ']': square--; break;
                case '<': angle++; break;
                case '>': if (!(i > 0 && s.charAt(i - 1) == '-') && angle > 0) angle--; break;
                case '=':
                    if (round == 0 && curly == 0 && square == 0 && angle == 0) {
                        char prev = i > 0 ? s.charAt(i - 1) : ' ';
                        char next = i + 1 < s.length() ? s.charAt(i + 1) : ' ';
                        if (next != '=' && prev != '=' && prev != '!' && prev != '<'
                                && prev != '>' && prev != '-') return i;
                    }
                    break;
                default: /* skip */
            }
        }
        return -1;
    }

    // The first sentence of the KDoc immediately above a declaration (only whitespace and
    // annotation lines may sit between the `*/` and the `fun`), cleaned of `*`, links and newlines.
    static String precedingSummary(String s, int funStart) {
        int close = s.lastIndexOf("*/", funStart);
        if (close < 0) return null;
        String gap = s.substring(close + 2, funStart);
        for (String line : gap.split("\n", -1)) {
            String t = line.trim();
            if (!t.isEmpty() && !t.startsWith("@")) return null; // not an adjacent doc comment
        }
        int open = s.lastIndexOf("/**", close);
        if (open < 0) return null;
        String body = s.substring(open + 3, close);
        StringBuilder sb = new StringBuilder();
        for (String line : body.split("\n")) {
            String t = line.trim();
            if (t.startsWith("*")) t = t.substring(1).trim();
            if (!t.isEmpty()) { if (sb.length() > 0) sb.append(' '); sb.append(t); }
        }
        String text = sb.toString()
                .replaceAll("\\[([^\\]]+)\\]", "$1")   // KDoc [Link] -> Link
                .replaceAll("\\s+", " ").trim();
        // first sentence
        Matcher dot = Pattern.compile("\\.(\\s|$)").matcher(text);
        if (dot.find()) text = text.substring(0, dot.start() + 1);
        return text.isEmpty() ? null : text;
    }

    static String parseVersion(String buildGradle) {
        Matcher m = Pattern.compile("(?m)^\\s*version\\s*=\\s*\"([^\"]+)\"").matcher(buildGradle);
        return m.find() ? m.group(1) : "unknown";
    }

    static List<String> parseAccents(String structureKt) {
        Matcher m = Pattern.compile("enum\\s+class\\s+PulseAccent\\s*\\{([^}]*)\\}").matcher(structureKt);
        List<String> out = new ArrayList<>();
        if (m.find()) {
            for (String raw : m.group(1).split(",")) {
                String t = raw.trim();
                if (!t.isEmpty()) out.add(t);
            }
        }
        return out;
    }

    // ---- small helpers -----------------------------------------------------------------

    static String readFile(Path p) throws Exception {
        return new String(Files.readAllBytes(p), "UTF-8");
    }
    @SuppressWarnings("unchecked")
    static Map<String, Object> asMap(Object o) { return o instanceof Map ? (Map<String, Object>) o : null; }
    @SuppressWarnings("unchecked")
    static List<Object> asList(Object o) { return o instanceof List ? (List<Object>) o : null; }
    static Object get(Map<String, Object> m, String k) { return m == null ? null : m.get(k); }

    // ---- tiny JSON reader --------------------------------------------------------------

    static final class Json {
        private final String s;
        private int i;
        private Json(String s) { this.s = s; }
        static Object parse(String s) { Json j = new Json(s); j.ws(); return j.value(); }
        private void ws() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }
        private Object value() {
            char c = s.charAt(i);
            switch (c) {
                case '{': return obj();
                case '[': return arr();
                case '"': return str();
                case 't': i += 4; return Boolean.TRUE;
                case 'f': i += 5; return Boolean.FALSE;
                case 'n': i += 4; return null;
                default: return num();
            }
        }
        private Map<String, Object> obj() {
            Map<String, Object> m = new LinkedHashMap<>();
            i++; ws();
            if (s.charAt(i) == '}') { i++; return m; }
            while (true) {
                ws(); String k = str(); ws(); i++; /* : */ ws();
                m.put(k, value()); ws();
                char c = s.charAt(i++);
                if (c == '}') break; // else ','
            }
            return m;
        }
        private List<Object> arr() {
            List<Object> a = new ArrayList<>();
            i++; ws();
            if (s.charAt(i) == ']') { i++; return a; }
            while (true) {
                ws(); a.add(value()); ws();
                char c = s.charAt(i++);
                if (c == ']') break; // else ','
            }
            return a;
        }
        private String str() {
            StringBuilder sb = new StringBuilder();
            i++; // opening quote
            while (true) {
                char c = s.charAt(i++);
                if (c == '"') break;
                if (c == '\\') {
                    char e = s.charAt(i++);
                    switch (e) {
                        case 'n': sb.append('\n'); break;
                        case 't': sb.append('\t'); break;
                        case 'r': sb.append('\r'); break;
                        case 'b': sb.append('\b'); break;
                        case 'f': sb.append('\f'); break;
                        case '/': sb.append('/'); break;
                        case '"': sb.append('"'); break;
                        case '\\': sb.append('\\'); break;
                        case 'u': sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                        default: sb.append(e);
                    }
                } else sb.append(c);
            }
            return sb.toString();
        }
        private Object num() {
            int st = i;
            while (i < s.length() && "-+.eE0123456789".indexOf(s.charAt(i)) >= 0) i++;
            return Double.parseDouble(s.substring(st, i));
        }
    }

    // ---- tiny JSON writer (stable order, 2-space indent) -------------------------------

    static final class JsonWriter {
        static String write(Object v) {
            StringBuilder sb = new StringBuilder();
            write(v, sb, 0);
            return sb.toString();
        }
        @SuppressWarnings("unchecked")
        private static void write(Object v, StringBuilder sb, int indent) {
            if (v == null) { sb.append("null"); return; }
            if (v instanceof String) { quote((String) v, sb); return; }
            if (v instanceof Boolean) { sb.append(v.toString()); return; }
            if (v instanceof Integer || v instanceof Long) { sb.append(v.toString()); return; }
            if (v instanceof Double) {
                double d = (Double) v;
                if (d == Math.rint(d) && !Double.isInfinite(d)) sb.append(Long.toString((long) d));
                else sb.append(Double.toString(d));
                return;
            }
            if (v instanceof Map) {
                Map<String, Object> m = (Map<String, Object>) v;
                if (m.isEmpty()) { sb.append("{}"); return; }
                sb.append("{\n");
                int n = 0;
                for (Map.Entry<String, Object> e : m.entrySet()) {
                    pad(sb, indent + 1);
                    quote(e.getKey(), sb);
                    sb.append(": ");
                    write(e.getValue(), sb, indent + 1);
                    if (++n < m.size()) sb.append(',');
                    sb.append('\n');
                }
                pad(sb, indent); sb.append('}');
                return;
            }
            if (v instanceof List) {
                List<Object> l = (List<Object>) v;
                if (l.isEmpty()) { sb.append("[]"); return; }
                sb.append("[\n");
                for (int k = 0; k < l.size(); k++) {
                    pad(sb, indent + 1);
                    write(l.get(k), sb, indent + 1);
                    if (k + 1 < l.size()) sb.append(',');
                    sb.append('\n');
                }
                pad(sb, indent); sb.append(']');
                return;
            }
            quote(v.toString(), sb);
        }
        private static void pad(StringBuilder sb, int indent) {
            for (int k = 0; k < indent; k++) sb.append("  ");
        }
        private static void quote(String s, StringBuilder sb) {
            sb.append('"');
            for (int k = 0; k < s.length(); k++) {
                char c = s.charAt(k);
                switch (c) {
                    case '"': sb.append("\\\""); break;
                    case '\\': sb.append("\\\\"); break;
                    case '\n': sb.append("\\n"); break;
                    case '\r': sb.append("\\r"); break;
                    case '\t': sb.append("\\t"); break;
                    default:
                        if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                        else sb.append(c);
                }
            }
            sb.append('"');
        }
    }
}
