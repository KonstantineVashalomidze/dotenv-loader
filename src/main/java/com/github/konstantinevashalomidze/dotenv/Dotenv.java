package com.github.konstantinevashalomidze.dotenv;

import java.util.Map;

/**
 * Immutable, loaded environment configuration produced by {@link DotenvBuilder#load()}.
 *
 * <p>Instances are created via {@link Dotenv#configure()}, which returns a
 * {@link DotenvBuilder} for configuring how the .env file should be located and parsed.</p>
 *
 * @author <a href="https://portfolio.kosta-server.org/">Konstantine Vashalomidze</a>
 */
public class Dotenv {
    private final Map<String, String> values;

    Dotenv(Map<String, String> values) {
        this.values = values;
    }

    /**
     * Retrieves value by specified key
     * @param key for value retrieval
     * @return value associated with key
     * @throws DotenvException if key is not present in the loaded configuration
     */
    public String get(String key) {
        if (values.containsKey(key)) {
            return values.get(key);
        } else {
            throw new DotenvException("Key was not found: " + key);
        }
    }

    public static DotenvBuilder configure() {
        return new DotenvBuilder();
    }

    public String get(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    /**
     * Retrieves int value by specified key
     * @param key for value retrieval
     * @return int value associated with key
     * @throws DotenvException if key is not present in the loaded configuration, or is in invalid format
     */
    public int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new DotenvException("Type error: " + e.getMessage());
        }
    }

    /**
     * Retrieves boolean value by specified key
     * @param key for value retrieval
     * @return boolean value associated with key
     * @throws DotenvException if key is not specified, or is in invalid format
     */
    public boolean getBoolean(String key) {
        String value = get(key);
        if ("true".equals(value)) {
            return true;
        } else if ("false".equals(value)) {
            return false;
        } else {
            throw new DotenvException("Expected to be true/false was " + value);
        }
    }

    /**
     * Retrieves long value by specified key
     * @param key for value retrieval
     * @return long value associated with key
     * @throws DotenvException if key is not present in the loaded configuration, or is in invalid format
     */
    public long getLong(String key) {
        String value = get(key);
        try {
            return Long.parseLong(value);
        }  catch (NumberFormatException e) {
            throw new DotenvException("Type error: " + e.getMessage());
        }
    }

    /**
     * Retrieves double value by specified key
     * @param key for value retrieval
     * @return double value associated with key
     * @throws DotenvException if key is not present in the loaded configuration, or is in invalid format
     */
    public double getDouble(String key) {
        String value = get(key);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new DotenvException("Type error: " + e.getMessage());
        }
    }

    /**
     * Retrieves float value by specified key
     * @param key for value retrieval
     * @return float value associated with key
     * @throws DotenvException if key is not present in the loaded configuration, or is in invalid format
     */
    public float getFloat(String key) {
        String value = get(key);
        try {
            return Float.parseFloat(value);
        }  catch (NumberFormatException e) {
            throw new DotenvException("Type error: " + e.getMessage());
        }
    }

    /**
     * Retrieves short value by specified key
     * @param key for value retrieval
     * @return short value associated with key
     * @throws DotenvException if key is not present in the loaded configuration, or is in invalid format
     */
    public short getShort(String key) {
        String value = get(key);
        try {
            return Short.parseShort(value);
        }  catch (NumberFormatException e) {
            throw new DotenvException("Type error: " + e.getMessage());
        }
    }

    /**
     * Retrieves byte value by specified key
     * @param key for value retrieval
     * @return byte value associated with key
     * @throws DotenvException if key is not present in the loaded configuration, or is in invalid format
     */
    public byte getByte(String key) {
        String value = get(key);
        try {
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
          throw new DotenvException("Type error: " + e.getMessage());
        }
    }
}
