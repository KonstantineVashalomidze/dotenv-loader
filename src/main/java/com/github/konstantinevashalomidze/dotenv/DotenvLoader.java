package com.github.konstantinevashalomidze.dotenv;

import lombok.NonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class DotenvLoader {

    public static @NonNull Dotenv load(DotenvConfiguration configuration) {
        Path path = Path.of(configuration.directory(), configuration.filename());
        if (Files.notExists(path)) {
            if (configuration.ignoreIfMissing()) {
                return new Dotenv(new LinkedHashMap<>());
            } else {
                throw new DotenvException(".env file not found");
            }
        } else {
            DotenvParser dotenvParser = new DotenvParser();
            try {
                Map<String, String> parsed = dotenvParser.parse(path);
                Dotenv dotenv = new Dotenv(parsed);
                if (configuration.systemProperties()) {
                    final Map<String, ConflictPolicy> conflictPolicies = configuration.conflictPolicies();

                    parsed.forEach((key, value) -> {
                        if (System.getProperties().containsKey(key)) {
                            if (conflictPolicies.containsKey(key)) {
                                ConflictPolicy conflictPolicy = conflictPolicies.get(key);
                                if (conflictPolicy == ConflictPolicy.STRICT) {
                                    throw new DotenvException("System property " + key + " already exists with value " + System.getProperty(key) + " but in .env value was " + value);
                                }
                            } else {
                                throw new DotenvException("System property " + key + " already exists with value " + System.getProperty(key) + " but in .env value was " + value);
                            }
                        }
                    });

                    parsed.forEach((key, value) -> {
                        if (conflictPolicies.containsKey(key)) {
                            ConflictPolicy conflictPolicy = conflictPolicies.get(key);
                            if (conflictPolicy != ConflictPolicy.PRESERVE) {
                                System.setProperty(key, value);
                            }
                        } else {
                            System.setProperty(key, value);
                        }
                    });
                }

                List<String> expectedKeys = new ArrayList<>();
                configuration.requiredKeys()
                        .forEach(k -> {
                            if (!parsed.containsKey(k)) {
                                expectedKeys.add(k);
                            }
                        });

                if (!expectedKeys.isEmpty()) {
                    throw new DotenvException("Missing required keys in .env file: " + String.join(", ", expectedKeys));
                }


                return dotenv;
            } catch (IOException e) {
                throw new DotenvException("Parsing error: " + e.getMessage());
            }
        }
    }
}
