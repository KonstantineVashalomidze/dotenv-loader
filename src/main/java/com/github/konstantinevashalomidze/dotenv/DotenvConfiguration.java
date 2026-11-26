package com.github.konstantinevashalomidze.dotenv;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.*;

public record DotenvConfiguration(
        String directory,
        String filename,
        boolean ignoreIfMissing,
        boolean systemProperties,
        @NonNull Map<String, ConflictPolicy> conflictPolicies,
        @NonNull Set<String> requiredKeys
) {

    @Override
    public @NonNull Map<String, ConflictPolicy> conflictPolicies() {
        return Map.copyOf(conflictPolicies);
    }

    @Override
    public @NonNull Set<String> requiredKeys() {
        return Set.copyOf(requiredKeys);
    }

    public static @NonNull Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public final static class Builder {

        private String directory = "./";
        private String filename = ".env";
        private boolean ignoreIfMissing = false;
        private boolean systemProperties = false;
        private final Map<String, ConflictPolicy> conflictPolicies = new HashMap<>();
        private final Set<String> requiredKeys = new LinkedHashSet<>();

        /**
         * Registers a key that must be present in the .env file. If not present when
         * {@link #load()} is called loading fails with a {@link DotenvException} listin
         * all missing required keys.
         *
         * @param key the key that must be present
         * @return this builder for chaining
         */
        public @NonNull Builder required(String key) {
            requiredKeys.add(key);
            return this;
        }

        /**
         * Registers a keys that must be present in the .env file. If not present when
         * {@link #load()} is called loading fails with a {@link DotenvException} listin
         * all missing required keys.
         *
         * @param keys the keys that must be present
         * @return this builder for chaining
         */
        public @NonNull Builder required(String... keys) {
            requiredKeys.addAll(List.of(keys));
            return this;
        }

        /**
         * For each key specify strategy of resolving conflicts.
         * <p>
         * Conflicts arise when {@link #systemProperties()} flag is set and there are same keys presented both in
         * system properties and .env file. Use {@link ConflictPolicy} to specify what should happen with such keys.
         *
         * @param key    to specify strategy in case there are conflicts between system properties and .env
         * @param policy how conflict should be resolved
         * @return this builder for chaining
         * @throws DotenvException if duplicate conflict policy is being set on builder
         */
        public @NonNull Builder onConflict(String key, ConflictPolicy policy) {
            if (conflictPolicies.containsKey(key)) {
                throw new DotenvException("Duplicate conflict policy key: " + key);
            }
            conflictPolicies.put(key, policy);
            return this;
        }

        /**
         * Set this flag if .env entries should be registered in JVM properties.
         *
         * @return this builder for chaining
         */
        public @NonNull Builder systemProperties() {
            this.systemProperties = true;
            return this;
        }

        /**
         * Sets the directory in which .env file should be located.
         *
         * @param directory the directory to search for the .env file in
         * @return this builder for chaining
         */
        public @NonNull Builder directory(String directory) {
            this.directory = directory;
            return this;
        }

        /**
         * Set the name of .env file
         *
         * @param filename name of .env file
         * @return this builder for chaining
         */
        public @NonNull Builder filename(String filename) {
            this.filename = filename;
            return this;
        }

        /**
         * Set this flag in case you need no exception if .env file is missing
         *
         * @return this builder for chaining
         */
        public @NonNull Builder ignoreIfMissing() {
            this.ignoreIfMissing = true;
            return this;
        }

        public @NonNull DotenvConfiguration build() {
            return new DotenvConfiguration(
                    directory,
                    filename,
                    ignoreIfMissing,
                    systemProperties,
                    conflictPolicies,
                    requiredKeys
            );
        }
    }
}
