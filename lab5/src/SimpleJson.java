import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilitar minimalist și complet autonom pentru parsarea răspunsurilor JSON
 * primite de la serviciile REST prin HttpURLConnection.
 * Nu necesită nicio bibliotecă externă (fără Jackson, Gson etc.).
 */
public class SimpleJson {

    private final String src;
    private int pos = 0;

    private SimpleJson(String src) {
        this.src = src != null ? src.trim() : "";
    }

    /**
     * Parsează un șir JSON și returnează un Map<String, Object>.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseObject(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new LinkedHashMap<>();
        }
        SimpleJson parser = new SimpleJson(json);
        Object obj = parser.parseValue();
        if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return new LinkedHashMap<>();
    }

    private Object parseValue() {
        skipWhitespace();
        if (pos >= src.length()) {
            return null;
        }
        char ch = src.charAt(pos);
        if (ch == '{') {
            return parseJsonObject();
        } else if (ch == '[') {
            return parseJsonArray();
        } else if (ch == '"') {
            return parseJsonString();
        } else if (ch == 't' || ch == 'f') {
            return parseJsonBoolean();
        } else if (ch == 'n') {
            return parseJsonNull();
        } else if (ch == '-' || (ch >= '0' && ch <= '9')) {
            return parseJsonNumber();
        }
        pos++;
        return null;
    }

    private Map<String, Object> parseJsonObject() {
        Map<String, Object> map = new LinkedHashMap<>();
        pos++; // sari peste '{'
        skipWhitespace();

        if (pos < src.length() && src.charAt(pos) == '}') {
            pos++;
            return map;
        }

        while (pos < src.length()) {
            skipWhitespace();
            if (pos >= src.length()) break;
            if (src.charAt(pos) == '}') {
                pos++;
                break;
            }

            // Cheia trebuie să fie un string
            String key = "";
            if (src.charAt(pos) == '"') {
                key = parseJsonString();
            } else {
                // Cheie fără ghilimele (fallback)
                int start = pos;
                while (pos < src.length() && src.charAt(pos) != ':' && !Character.isWhitespace(src.charAt(pos))) {
                    pos++;
                }
                key = src.substring(start, pos);
            }

            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ':') {
                pos++; // sari peste ':'
            }

            Object value = parseValue();
            map.put(key, value);

            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ',') {
                pos++; // următorul membru
            } else if (pos < src.length() && src.charAt(pos) == '}') {
                pos++;
                break;
            }
        }
        return map;
    }

    private List<Object> parseJsonArray() {
        List<Object> list = new ArrayList<>();
        pos++; // sari peste '['
        skipWhitespace();

        if (pos < src.length() && src.charAt(pos) == ']') {
            pos++;
            return list;
        }

        while (pos < src.length()) {
            skipWhitespace();
            if (pos >= src.length()) break;
            if (src.charAt(pos) == ']') {
                pos++;
                break;
            }

            list.add(parseValue());

            skipWhitespace();
            if (pos < src.length() && src.charAt(pos) == ',') {
                pos++;
            } else if (pos < src.length() && src.charAt(pos) == ']') {
                pos++;
                break;
            }
        }
        return list;
    }

    private String parseJsonString() {
        pos++; // sari peste '"'
        StringBuilder sb = new StringBuilder();
        while (pos < src.length()) {
            char ch = src.charAt(pos++);
            if (ch == '"') {
                break;
            }
            if (ch == '\\' && pos < src.length()) {
                char esc = src.charAt(pos++);
                switch (esc) {
                    case '"': sb.append('"'); break;
                    case '\\': sb.append('\\'); break;
                    case '/': sb.append('/'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        if (pos + 4 <= src.length()) {
                            String hex = src.substring(pos, pos + 4);
                            pos += 4;
                            try {
                                sb.append((char) Integer.parseInt(hex, 16));
                            } catch (NumberFormatException ignored) {}
                        }
                        break;
                    default:
                        sb.append(esc);
                        break;
                }
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private Number parseJsonNumber() {
        int start = pos;
        if (pos < src.length() && (src.charAt(pos) == '-' || src.charAt(pos) == '+')) {
            pos++;
        }
        boolean isDouble = false;
        while (pos < src.length()) {
            char ch = src.charAt(pos);
            if (ch >= '0' && ch <= '9') {
                pos++;
            } else if (ch == '.' || ch == 'e' || ch == 'E') {
                isDouble = true;
                pos++;
            } else {
                break;
            }
        }
        String numStr = src.substring(start, pos);
        try {
            if (isDouble) {
                return Double.parseDouble(numStr);
            } else {
                long val = Long.parseLong(numStr);
                if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                    return (int) val;
                }
                return val;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private Boolean parseJsonBoolean() {
        if (src.startsWith("true", pos)) {
            pos += 4;
            return Boolean.TRUE;
        } else if (src.startsWith("false", pos)) {
            pos += 5;
            return Boolean.FALSE;
        }
        pos++;
        return null;
    }

    private Object parseJsonNull() {
        if (src.startsWith("null", pos)) {
            pos += 4;
        }
        return null;
    }

    private void skipWhitespace() {
        while (pos < src.length()) {
            char ch = src.charAt(pos);
            if (ch == ' ' || ch == '\t' || ch == '\n' || ch == '\r') {
                pos++;
            } else {
                break;
            }
        }
    }

    // Metode helper sigure de extragere date:

    public static Double getDouble(Map<String, Object> map, String key, Double defaultVal) {
        if (map == null || !map.containsKey(key)) return defaultVal;
        Object v = map.get(key);
        if (v instanceof Number) {
            return ((Number) v).doubleValue();
        }
        if (v instanceof String) {
            try { return Double.parseDouble((String) v); } catch (Exception ignored) {}
        }
        return defaultVal;
    }

    public static Integer getInt(Map<String, Object> map, String key, Integer defaultVal) {
        if (map == null || !map.containsKey(key)) return defaultVal;
        Object v = map.get(key);
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        if (v instanceof String) {
            try { return Integer.parseInt((String) v); } catch (Exception ignored) {}
        }
        return defaultVal;
    }

    public static String getString(Map<String, Object> map, String key, String defaultVal) {
        if (map == null || !map.containsKey(key)) return defaultVal;
        Object v = map.get(key);
        return v != null ? v.toString() : defaultVal;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) return null;
        Object v = map.get(key);
        if (v instanceof Map) {
            return (Map<String, Object>) v;
        }
        return null;
    }
}
