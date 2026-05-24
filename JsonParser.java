

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ============================================================
 *  JsonParser.java  –  Minimal JSON Parser
 * ------------------------------------------------------------
 *  A hand-written, zero-dependency JSON parser that supports
 *  the full JSON grammar needed by the Open-Meteo API:
 *
 *    • Objects  { "key": value, … }
 *    • Arrays   [ value, value, … ]
 *    • Strings  "…"
 *    • Numbers  (int & double)
 *    • Booleans true / false
 *    • null
 *
 *  Returns a plain Java object graph:
 *    JSON Object  →  Map<String, Object>
 *    JSON Array   →  List<Object>
 *    JSON String  →  String
 *    JSON Number  →  Double or Long
 *    JSON Boolean →  Boolean
 *    JSON null    →  null
 *
 *  Usage:
 *    Object root = new JsonParser(jsonString).parse();
 *    Map<String, Object> obj = JsonParser.asObject(root);
 *    double temp = JsonParser.asDouble(obj.get("temperature"));
 * ============================================================
 */
public class JsonParser {

    private final String json;
    private int pos = 0;

    public JsonParser(String json) {
        this.json = json;
    }

    // ── public entry point ────────────────────────────────

    /** Parses the JSON string and returns the root value. */
    public Object parse() {
        skipWhitespace();
        Object value = parseValue();
        skipWhitespace();
        return value;
    }

    // ── type-safe cast helpers (static convenience) ───────

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object o) {
        if (!(o instanceof Map)) throw new IllegalArgumentException(
                "Expected JSON object, got: " + (o == null ? "null" : o.getClass().getSimpleName()));
        return (Map<String, Object>) o;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object o) {
        if (!(o instanceof List)) throw new IllegalArgumentException(
                "Expected JSON array, got: " + (o == null ? "null" : o.getClass().getSimpleName()));
        return (List<Object>) o;
    }

    public static double asDouble(Object o) {
        if (o instanceof Number) return ((Number) o).doubleValue();
        if (o instanceof String) return Double.parseDouble((String) o);
        throw new IllegalArgumentException("Cannot convert to double: " + o);
    }

    public static int asInt(Object o) {
        if (o instanceof Number) return ((Number) o).intValue();
        if (o instanceof String) return Integer.parseInt((String) o);
        throw new IllegalArgumentException("Cannot convert to int: " + o);
    }

    public static boolean asBoolean(Object o) {
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number)  return ((Number) o).intValue() != 0;
        throw new IllegalArgumentException("Cannot convert to boolean: " + o);
    }

    public static String asString(Object o) {
        return o == null ? "" : o.toString();
    }

    /** Safely fetches a nested key from a Map, returning null if absent. */
    public static Object get(Map<String, Object> map, String key) {
        return map.getOrDefault(key, null);
    }

    // ── core parser ───────────────────────────────────────

    private Object parseValue() {
        skipWhitespace();
        if (pos >= json.length()) throw new ParseException("Unexpected end of input", pos);

        char c = json.charAt(pos);
        if (c == '{')  return parseObject();
        if (c == '[')  return parseArray();
        if (c == '"')  return parseString();
        if (c == 't' || c == 'f') return parseBoolean();
        if (c == 'n')  return parseNull();
        if (c == '-' || Character.isDigit(c)) return parseNumber();

        throw new ParseException("Unexpected character: '" + c + "'", pos);
    }

    private Map<String, Object> parseObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        expect('{');
        skipWhitespace();
        if (peek() == '}') { pos++; return map; }  // empty object

        while (true) {
            skipWhitespace();
            String key = parseString();
            skipWhitespace();
            expect(':');
            skipWhitespace();
            Object value = parseValue();
            map.put(key, value);
            skipWhitespace();
            if (peek() == '}') { pos++; break; }
            expect(',');
        }
        return map;
    }

    private List<Object> parseArray() {
        List<Object> list = new ArrayList<>();
        expect('[');
        skipWhitespace();
        if (peek() == ']') { pos++; return list; }  // empty array

        while (true) {
            skipWhitespace();
            list.add(parseValue());
            skipWhitespace();
            if (peek() == ']') { pos++; break; }
            expect(',');
        }
        return list;
    }

    private String parseString() {
        expect('"');
        StringBuilder sb = new StringBuilder();
        while (pos < json.length()) {
            char c = json.charAt(pos++);
            if (c == '"') return sb.toString();
            if (c == '\\') {
                char esc = json.charAt(pos++);
                switch (esc) {
                    case '"':  sb.append('"');  break;
                    case '\\': sb.append('\\'); break;
                    case '/':  sb.append('/');  break;
                    case 'n':  sb.append('\n'); break;
                    case 'r':  sb.append('\r'); break;
                    case 't':  sb.append('\t'); break;
                    case 'b':  sb.append('\b'); break;
                    case 'f':  sb.append('\f'); break;
                    case 'u': {
                        String hex = json.substring(pos, pos + 4);
                        sb.append((char) Integer.parseInt(hex, 16));
                        pos += 4;
                        break;
                    }
                    default: sb.append(esc);
                }
            } else {
                sb.append(c);
            }
        }
        throw new ParseException("Unterminated string", pos);
    }

    private Number parseNumber() {
        int start = pos;
        if (peek() == '-') pos++;
        while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
        boolean isDouble = false;
        if (pos < json.length() && json.charAt(pos) == '.') {
            isDouble = true; pos++;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
        }
        if (pos < json.length() && (json.charAt(pos) == 'e' || json.charAt(pos) == 'E')) {
            isDouble = true; pos++;
            if (peek() == '+' || peek() == '-') pos++;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
        }
        String num = json.substring(start, pos);
        return isDouble ? Double.parseDouble(num) : Long.parseLong(num);
    }

    private Boolean parseBoolean() {
        if (json.startsWith("true",  pos)) { pos += 4; return Boolean.TRUE; }
        if (json.startsWith("false", pos)) { pos += 5; return Boolean.FALSE; }
        throw new ParseException("Expected boolean", pos);
    }

    private Object parseNull() {
        if (json.startsWith("null", pos)) { pos += 4; return null; }
        throw new ParseException("Expected null", pos);
    }

    // ── internal helpers ──────────────────────────────────

    private void skipWhitespace() {
        while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
    }

    private char peek() {
        return pos < json.length() ? json.charAt(pos) : '\0';
    }

    private void expect(char c) {
        if (pos >= json.length() || json.charAt(pos) != c)
            throw new ParseException("Expected '" + c + "' but found '" + peek() + "'", pos);
        pos++;
    }

    // ── inner exception ───────────────────────────────────

    public static class ParseException extends RuntimeException {
        public ParseException(String msg, int position) {
            super(msg + " at position " + position);
        }
    }
}
