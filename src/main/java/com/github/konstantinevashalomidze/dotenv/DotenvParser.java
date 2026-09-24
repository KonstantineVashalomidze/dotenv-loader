package com.github.konstantinevashalomidze.dotenv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Konstantine Vashalomidze
 */
public class DotenvParser {
    public Map<String, String> parse(Path filePah) throws IOException {
        List<String> lines = Files.readAllLines(filePah);
        Map<String, String> parsedResult = new LinkedHashMap<>();
        for (String line : lines) {
            if (line.startsWith("export ")) {
                line = line.substring(7);
            }
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            int eq = line.indexOf('=');
            String key = line.substring(0, eq).trim();
            validateKey(key, parsedResult);
            String value = processValue(line.substring(eq + 1).trim(), parsedResult);
            parsedResult.put(key, value);
        }

        return parsedResult;
    }

    private void validateKey(String key, Map<String, String> alreadyParsed) {
        if (key.isBlank()) {
            throw new DotenvException("Malformed key: " + key);
        }
        if (alreadyParsed.containsKey(key)) {
            throw new DotenvException("Duplicate key: " + key);
        }
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (i == 0 && Character.isDigit(c)) {
                throw new DotenvException("Malformed key: " + key);
            }
            if (!Character.isLetterOrDigit(c) && c != '_') {
                throw new DotenvException("Invalid key: " + key);
            }
        }
    }

    private String processValue(String rawValue, Map<String, String> parsedResult) {
        if (rawValue.startsWith("\"") && rawValue.endsWith("\"")) {
            return processDoubleQuotes(rawValue, parsedResult);
        } else if (rawValue.startsWith("'") && rawValue.endsWith("'")) {
            return processSingleQuotes(rawValue);
        } else {
            return expandVariables(rawValue, parsedResult);
        }
    }

    private String processDoubleQuotes(String rawValue, Map<String, String> parsedResult) {
        if (rawValue.length() < 2) {
            throw new DotenvException("Malformed value: " + rawValue);
        }
        String unquoted = rawValue.substring(1, rawValue.length() - 1);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < unquoted.length(); i++) {
            char c = unquoted.charAt(i);
            if (c == '\\') {
                if (i != unquoted.length() - 1) {
                    char c1 = unquoted.charAt(i + 1);
                    switch (c1) {
                        case 'n' -> sb.append("\n");
                        case 't' -> sb.append("\t");
                        case 'r' -> sb.append("\r");
                        case '"' -> sb.append("\"");
                        case '\\' -> sb.append("\\");
                        default -> throw new DotenvException("Invalid escape sequence '\\" + c1 + "' in value: " + rawValue);
                    }
                    i++;
                }
            } else {
                sb.append(c);
            }
        }

        return expandVariables(sb.toString(), parsedResult);
    }

    private String processSingleQuotes(String rawValue) {
        if (rawValue.length() < 2) {
            throw new DotenvException("Malformed value: " + rawValue);
        }
        return rawValue.substring(1, rawValue.length() - 1);
    }

    private String expandVariables(String value, Map<String, String> alreadyParsed) {
        StringBuilder valueBuilder = new StringBuilder();
        StringBuilder key = new StringBuilder();
        int readingKey = -1;
        boolean braced = false;
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (c == '$') {
                readingKey = i;
                braced = false;
            } else if (readingKey != -1) {
                if (c == '{') {
                    braced = true;
                    continue;
                }
                if (c == '}' || (!Character.isLetter(c) && !Character.isDigit(c) && c != '_')) {
                    String associatedValue = alreadyParsed.get(key.toString());
                    if (associatedValue == null) {
                        throw new DotenvException("Variable " + key + " is missing");
                    }
                    valueBuilder.append(associatedValue);

                    readingKey = -1;
                    key.setLength(0);
                    braced = false;
                    if (c != '}') {
                        valueBuilder.append(c);
                    }
                } else {
                    key.append(c);
                }
            } else {
                valueBuilder.append(c);
            }
        }
        if (readingKey != -1) {
            if (braced) {
                throw new DotenvException("Malformed variable " + key);
            } else {
                String associatedValue = alreadyParsed.get(key.toString());
                if (associatedValue == null) {
                    throw new DotenvException("Variable " + key + " is missing");
                }
                valueBuilder.append(associatedValue);
            }
        }
        return valueBuilder.toString();
    }
}