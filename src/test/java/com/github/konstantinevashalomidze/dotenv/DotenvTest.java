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

        Dotenv dotenv = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .load();

        assertThrows(DotenvException.class, () -> dotenv.get("THERE_IS_SUCH_KEY"));
    }

    @Test
    void ignoreIfMissingReturnsEmptyConfig(@TempDir Path tempDir) {
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .directory(tempDir.toString())
                .filename(".env")
                .load();

        assertThrows(DotenvException.class, () -> dotenv.get("ANYTHING"));
    }

    @Test
    void missingFileThrowsWhenNotIgnored(@TempDir Path tempDir) {
        assertThrows(DotenvException.class, () -> Dotenv.configure().directory(tempDir.toString()).filename(".env").load());
    }

    @Test
    void typedGettersReturnCorrectTypes(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "PORT=5432\nDEBUG=true\n");

        Dotenv dotenv = Dotenv.configure()
                        .directory(tempDir.toString())
                                .filename(".env")
                                        .load();

        assertEquals(5432, dotenv.getInt("PORT"));
        assertTrue(dotenv.getBoolean("DEBUG"));
    }

    @Test
    void getIntThrowsOnInvalidNumber(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "PORT=notanumber\n");

        Dotenv dotenv = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .load();

        assertThrows(DotenvException.class, () -> dotenv.getInt("PORT"));
    }

    @Test
    void missingRequiredKeyThrows(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=localhost\n");

        assertThrows(DotenvException.class, () -> {
            Dotenv dotenv = Dotenv.configure()
                    .directory(tempDir.toString())
                    .filename(".env")
                    .required("DB_PASSWORD")
                    .load();
        });
    }


    @Test
    void strictPolicyThrowsOnConflict(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=fromenv\n");

        System.setProperty("DB_HOST", "fromsystem");

        assertThrows(DotenvException.class, () -> {
            Dotenv dotenv = Dotenv.configure()
                    .directory(tempDir.toString())
                    .filename(".env")
                    .systemProperties()
                    .load();
        });
    }

    @Test
    void overridePolicyOverwritesSystemProperty(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=fromenv\n");

        System.setProperty("DB_HOST", "fromsystem");

        Dotenv dotenv = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .systemProperties()
                .onConflict("DB_HOST", ConflictPolicy.OVERRIDE)
                .load();

        assertEquals("fromenv",  dotenv.get("DB_HOST"));
        assertEquals(System.getProperty("DB_HOST"), dotenv.get("DB_HOST"));
    }

    @Test
    void preservePolicyKeepsSystemProperty(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=fromenv\n");

        System.setProperty("DB_HOST", "fromsystem");

        Dotenv dotenv = Dotenv.configure()
                .directory(tempDir.toString())
                .filename(".env")
                .systemProperties()
                .onConflict("DB_HOST", ConflictPolicy.PRESERVE)
                .load();

        assertEquals("fromsystem", System.getProperty("DB_HOST"));
        assertEquals("fromenv", dotenv.get("DB_HOST"));
    }


}
