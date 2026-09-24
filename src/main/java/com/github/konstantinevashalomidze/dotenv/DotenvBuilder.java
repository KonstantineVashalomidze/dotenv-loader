package com.github.konstantinevashalomidze.dotenv;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Fluent builder for configuring and loading a {@link Dotenv} instance.
 *
 * <p>Obtained via {@link Dotenv#configure()}. Configuration methods (e.g.
 * {@link #directory(String)}, {@link #filename(String)}, {@link #required(String)}
 * return this builder to allow method chaining. Calling {@link #load()} reads and
 * parses the configured .env file and returns an immutable {@link Dotenv}.</p>
 *
 * @author Konstantine Vashalomidze
 */
public class DotenvBuilder {
    private String directory = "./";
    private String filename = ".env";
    private boolean ignoreIfMissing = false;
    private boolean systemProperties = false;
    private final Map<String, ConflictPolicy> conflictPolicies = new HashMap<>();
    private final Set<String> requiredKeys = new LinkedHashSet<>();

    DotenvBuilder() {

    }

    /**
     * Registers a key that must be present in the .env file. If not present when
     * {@link #load()} is called loading fails with a {@link DotenvException} listin
     * all missing required keys.
     *
     * @param key the key that must be present
     * @return this builder for chaining
     */
    public DotenvBuilder required(String key) {
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
    public DotenvBuilder required(String... keys) {
        requiredKeys.addAll(List.of(keys));
        return this;
    }

    /**
     * For each key specify strategy of resolving conflicts.
     *
     * Conflicts arise when {@link #systemProperties()} flag is set and there are same keys presented both in
     * system properties and .env file. Use {@link ConflictPolicy} to specify what should happen with such keys.
     *
     * @param key to specify strategy in case there are conflicts between system properties and .env
     * @param policy how conflict should be resolved
     * @return this builder for chaining
     * @throws DotenvException if duplicate conflict policy is being set on builder
     */
    public DotenvBuilder onConflict(String key, ConflictPolicy policy) {
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
    public DotenvBuilder systemProperties() {
        this.systemProperties = true;
        return this;
    }

    /**
     * Sets the directory in which .env file should be located.
     *
     * @param directory the directory to search for the .env file in
     * @return this builder for chaining
     */
    public DotenvBuilder directory(String directory) {
        this.directory = directory;
        return this;
    }

    /**
     * Set the name of .env file
     *
     * @param filename name of .env file
     * @return this builder for chaining
     */
    public DotenvBuilder filename(String filename) {
        this.filename = filename;
        return this;
    }

    /**
     * Set this flag in case you need no exception if .env file is missing
     *
     * @return this builder for chaining
     */
    public DotenvBuilder ignoreIfMissing() {
        this.ignoreIfMissing = true;
        return this;
    }

    /**
     * Reads and parses the configured .env file, applying any configured validation
     * (required keys) and optional side effects (exporting values to system properties),
     * and returns the result as an immutable {@link Dotenv}.
     *
     * <p>The file is looked up at {@code directory}/{@code filename} as configured via
     * {@link #directory(String)} and {@link #filename(String)} (defaults: {@code "./"}
     * and {@code ".env"}).</p>
     *
     * @return an immutable {@link Dotenv} holding the parsed key-value pairs
     * @throws DotenvException if the file does not exist and {@link #ignoreIfMissing()}
     *         was not set; if the file exists but cannot be read or parsed; if any key
     *         registered via {@link #required(String)}/{@link #required(String...)} is
     *         missing from the file; or if {@link #systemProperties()} is enabled and a
     *         parsed key conflicts with an existing system property under a
     *         {@link ConflictPolicy#STRICT} (or unregistered) policy
     */
    public Dotenv load() {
        Path path = Path.of(directory, filename);
        if (Files.notExists(path)) {
            if (ignoreIfMissing) {
                return new Dotenv(new LinkedHashMap<>());
            } else {
                throw new DotenvException(".env file not found");
            }
        } else {
            DotenvParser dotenvParser = new DotenvParser();
            try {
                Map<String, String> parsed = dotenvParser.parse(path);
                Dotenv dotenv =  new Dotenv(parsed);
                if (systemProperties) {
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
                }

                List<String> expectedKeys = new ArrayList<>();
                requiredKeys.forEach(k -> {
                    if (!parsed.containsKey(k)) {
                        expectedKeys.add(k);
                    }
                });

                if (!expectedKeys.isEmpty()) {
                    throw new DotenvException("Missing required keys in .env file: " + String.join(", ", expectedKeys));
                }


                if (systemProperties) {
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


                return dotenv;
            } catch (IOException e) {
                throw new DotenvException("Parsing error: " + e.getMessage());
            }
        }
    }

}
