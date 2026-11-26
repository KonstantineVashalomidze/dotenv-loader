package com.github.konstantinevashalomidze.dotenv;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class DotenvTest {

    @AfterEach
    void cleanup() {
        System.clearProperty("DB_HOST");
    }

    @Test
    void getThrowsWhenKeyMissing(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "THERE_IS_NO_SUCH_KEY=null\n");

        DotenvConfiguration configuration = DotenvConfiguration.builder()
                .directory(tempDir.toString())
                .filename(".env")
                .build();

        Dotenv dotenv = DotenvLoader.load(configuration);

        assertThrows(DotenvException.class, () -> dotenv.get("THERE_IS_SUCH_KEY"));
    }

    @Test
    void ignoreIfMissingReturnsEmptyConfig(@TempDir Path tempDir) {
        DotenvConfiguration configuration = DotenvConfiguration.builder()
                .ignoreIfMissing()
                .directory(tempDir.toString())
                .filename(".env")
                .build();

        Dotenv dotenv = DotenvLoader.load(configuration);

        assertThrows(DotenvException.class, () -> dotenv.get("ANYTHING"));
    }

    @Test
    void missingFileThrowsWhenNotIgnored(@TempDir Path tempDir) {
        assertThrows(
                DotenvException.class,
                () -> {
                    final DotenvConfiguration configuration = DotenvConfiguration.builder()
                            .directory(tempDir.toString())
                            .filename(".env")
                            .build();
                    DotenvLoader.load(configuration);
                }
        );
    }

    @Test
    void typedGettersReturnCorrectTypes(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "PORT=5432\nDEBUG=true\n");

        DotenvConfiguration configuration = DotenvConfiguration.builder()
                .directory(tempDir.toString())
                .filename(".env")
                .build();

        Dotenv dotenv = DotenvLoader.load(configuration);

        assertEquals(5432, dotenv.getInt("PORT"));
        assertTrue(dotenv.getBoolean("DEBUG"));
    }

    @Test
    void getIntThrowsOnInvalidNumber(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "PORT=notanumber\n");

        DotenvConfiguration configuration = DotenvConfiguration.builder()
                .directory(tempDir.toString())
                .filename(".env")
                .build();

        Dotenv dotenv = DotenvLoader.load(configuration);

        assertThrows(DotenvException.class, () -> dotenv.getInt("PORT"));
    }

    @Test
    void missingRequiredKeyThrows(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=localhost\n");

        assertThrows(
                DotenvException.class, () -> {
                    DotenvConfiguration configuration = DotenvConfiguration.builder()
                            .directory(tempDir.toString())
                            .filename(".env")
                            .required("DB_PASSWORD")
                            .build();

                    DotenvLoader.load(configuration);
                }
        );
    }


    @Test
    void strictPolicyThrowsOnConflict(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=fromenv\n");

        System.setProperty("DB_HOST", "fromsystem");

        assertThrows(
                DotenvException.class, () -> {
                    DotenvConfiguration configuration = DotenvConfiguration.builder()
                            .directory(tempDir.toString())
                            .filename(".env")
                            .systemProperties()
                            .build();

                    DotenvLoader.load(configuration);
                }
        );
    }

    @Test
    void overridePolicyOverwritesSystemProperty(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=fromenv\n");

        System.setProperty("DB_HOST", "fromsystem");

        DotenvConfiguration configuration = DotenvConfiguration.builder()
                .directory(tempDir.toString())
                .filename(".env")
                .systemProperties()
                .onConflict("DB_HOST", ConflictPolicy.OVERRIDE)
                .build();

        Dotenv dotenv = DotenvLoader.load(configuration);

        assertEquals("fromenv", dotenv.get("DB_HOST"));
        assertEquals(System.getProperty("DB_HOST"), dotenv.get("DB_HOST"));
    }

    @Test
    void preservePolicyKeepsSystemProperty(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=fromenv\n");

        System.setProperty("DB_HOST", "fromsystem");

        DotenvConfiguration configuration = DotenvConfiguration.builder()
                .directory(tempDir.toString())
                .filename(".env")
                .systemProperties()
                .onConflict("DB_HOST", ConflictPolicy.PRESERVE)
                .build();

        Dotenv dotenv = DotenvLoader.load(configuration);

        assertEquals("fromsystem", System.getProperty("DB_HOST"));
        assertEquals("fromenv", dotenv.get("DB_HOST"));
    }
}
