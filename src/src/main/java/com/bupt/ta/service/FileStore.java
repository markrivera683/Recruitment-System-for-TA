package com.bupt.ta.service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Minimal JSON file store — no external libraries.
 * Supports reading/writing a JSON array of flat objects whose fields are all Strings.
 * Format: [{"key":"value",...}, ...]
 */
public class FileStore {
    private final Path baseDir;

    public FileStore(Path baseDir) {
        this.baseDir = baseDir;
    }

    // ------------------------------------------------------------------ read

    /**
     * Reads a JSON array file and returns a list of raw string maps.
     * Each map represents one JSON object with string values.
     */
    public List<Map<String, String>> readMaps(String fileName) throws IOException {
        Path p = baseDir.resolve(fileName);
        if (!Files.exists(p)) return new ArrayList<>();
        String json = new String(Files.readAllBytes(p), StandardCharsets.UTF_8).trim();
        if (json.isEmpty() || json.equals("[]")) return new ArrayList<>();
        return parseJsonArray(json);
    }

    // ----------------------------------------------------------------- write

    /**
     * Writes a list of string maps as a JSON array to the given file.
     */
    public void writeMaps(String fileName, List<Map<String, String>> items) throws IOException {
        Path p = baseDir.resolve(fileName);
        Files.createDirectories(p.getParent());
        try (Writer w = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(p.toFile()), StandardCharsets.UTF_8))) {
            w.write(toJsonArray(items));
        }
    }

    // ------------------------------------------------------- JSON serialiser

    private static String toJsonArray(List<Map<String, String>> items) {
        return toJsonArrayOfObjects(items);
    }

    /**
     * Serialises a JSON array of objects with string values (used for nested profile payloads).
     */
    public static String toJsonArrayOfObjects(List<Map<String, String>> items) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append('\n').append(toJsonObject(items.get(i)));
        }
        if (!items.isEmpty()) sb.append('\n');
        sb.append(']');
        return sb.toString();
    }

    private static String toJsonObject(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append('\"').append(escapeJson(e.getKey())).append('\"')
              .append(':').append('\"').append(escapeJson(e.getValue())).append('\"');
        }
        sb.append('}');
        return sb.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // ------------------------------------------------------- JSON parser

    /**
     * Minimal hand-rolled parser for a JSON array of flat string-valued objects.
     * Handles escaped characters inside string values.
     */
    static List<Map<String, String>> parseJsonArray(String json) {
        List<Map<String, String>> result = new ArrayList<>();
        int i = 0;
        int len = json.length();
        // skip leading '['
        while (i < len && json.charAt(i) != '[') i++;
        i++; // consume '['

        while (i < len) {
            // skip whitespace
            while (i < len && Character.isWhitespace(json.charAt(i))) i++;
            if (i >= len) break;
            char c = json.charAt(i);
            if (c == ']') break;
            if (c == '{') {
                // parse object
                int[] pos = {i + 1}; // pos[0] is current index inside object
                Map<String, String> obj = new LinkedHashMap<>();
                while (pos[0] < len) {
                    skipWs(json, pos);
                    if (json.charAt(pos[0]) == '}') { pos[0]++; break; }
                    if (json.charAt(pos[0]) == ',') { pos[0]++; continue; }
                    if (json.charAt(pos[0]) == '\"') {
                        String key = readString(json, pos);
                        skipWs(json, pos);
                        if (pos[0] < len && json.charAt(pos[0]) == ':') pos[0]++;
                        skipWs(json, pos);
                        String value = readValue(json, pos);
                        obj.put(key, value);
                    } else {
                        pos[0]++; // unexpected char, skip
                    }
                }
                result.add(obj);
                i = pos[0];
            } else if (c == ',') {
                i++;
            } else {
                i++;
            }
        }
        return result;
    }

    private static void skipWs(String s, int[] pos) {
        while (pos[0] < s.length() && Character.isWhitespace(s.charAt(pos[0]))) pos[0]++;
    }

    private static String readString(String s, int[] pos) {
        pos[0]++; // consume opening quote
        StringBuilder sb = new StringBuilder();
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == '\\') {
                pos[0]++;
                if (pos[0] < s.length()) {
                    char esc = s.charAt(pos[0]);
                    switch (esc) {
                        case '\"': sb.append('\"'); break;
                        case '\\': sb.append('\\'); break;
                        case 'n':  sb.append('\n'); break;
                        case 'r':  sb.append('\r'); break;
                        case 't':  sb.append('\t'); break;
                        default:   sb.append(esc); break;
                    }
                    pos[0]++;
                }
            } else if (c == '\"') {
                pos[0]++; // consume closing quote
                break;
            } else {
                sb.append(c);
                pos[0]++;
            }
        }
        return sb.toString();
    }

    private static String readValue(String s, int[] pos) {
        if (pos[0] < s.length() && s.charAt(pos[0]) == '\"') {
            return readString(s, pos);
        }
        // bare value (null, number, boolean) — read until delimiter
        StringBuilder sb = new StringBuilder();
        while (pos[0] < s.length()) {
            char c = s.charAt(pos[0]);
            if (c == ',' || c == '}' || Character.isWhitespace(c)) break;
            sb.append(c);
            pos[0]++;
        }
        String v = sb.toString();
        return v.equals("null") ? null : v;
    }
}
