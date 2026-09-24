package com.github.konstantinevashalomidze.dotenv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DotenvParserTest {
    @Test
    void simpleKeyValuePairIsParsed(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DB_HOST=localhost\n");

        DotenvParser parser = new DotenvParser();
        Map<String, String> result = parser.parse(envFile);

        assertEquals("localhost", result.get("DB_HOST"));
    }

    @Test
    void commentsAndBlankLinesAreSkipped(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "# this is a comment\n\nDB_HOST=localhost\n");

        DotenvParser parser = new DotenvParser();
        Map<String, String> result = parser.parse(envFile);

        assertEquals(1, result.size());
        assertEquals("localhost", result.get("DB_HOST"));
    }

    @Test
    void quotedValuesAreStripped(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "DOUBLE=\"hello world\"\nSINGLE='hello world'\n");

        DotenvParser parser = new DotenvParser();
        Map<String, String> result = parser.parse(envFile);

        assertEquals(2, result.size());
        assertEquals("hello world", result.get("DOUBLE"));
        assertEquals("hello world", result.get("SINGLE"));
    }

    @Test
    void escapeSequencesAreProcessed(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "MSG=\"line1\\nline2\"\n");

        DotenvParser parser = new DotenvParser();
        Map<String, String> result = parser.parse(envFile);

        assertEquals(1, result.size());
        assertEquals("line1\nline2", result.get("MSG"));
    }

    @Test
    void singleQuotedValuesAreLiteral(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "HOST=localhost\nLITERAL='no ${HOST} expansion and no \\n escape'\n");

        DotenvParser parser = new DotenvParser();
        Map<String, String> result = parser.parse(envFile);

        assertEquals(2, result.size());
        assertEquals("localhost", result.get("HOST"));
        assertEquals("no ${HOST} expansion and no \\n escape", result.get("LITERAL"));
    }

    @Test
    void variableExpansionWorks(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "HOST=localhost\nPORT=5432\nURL=http://${HOST}:${PORT}/api\n");

        DotenvParser parser = new DotenvParser();
        Map<String, String> result = parser.parse(envFile);

        assertEquals(3, result.size());
        assertEquals("localhost", result.get("HOST"));
        assertEquals("5432", result.get("PORT"));
        assertEquals("http://localhost:5432/api", result.get("URL"));
    }

    @Test
    void missingVariableReferenceThrows(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "URL=http://${MISSING_VAR}/path\n");

        DotenvParser parser = new DotenvParser();

        assertThrows(DotenvException.class, () -> {
            parser.parse(envFile);
        });
    }

    @Test
    void duplicateKeyThrows(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "HOST=first\nHOST=second\n");

        DotenvParser parser = new DotenvParser();

        assertThrows(DotenvException.class, () -> {
            parser.parse(envFile);
        });
    }


    @Test
    void keyStartingWithDigitThrows(@TempDir Path tempDir) throws IOException {
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "123KEY=value");

        DotenvParser parser = new DotenvParser();

        assertThrows(DotenvException.class, () -> {
            parser.parse(envFile);
        });
    }

}
