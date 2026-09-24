# dotenv-loader

A lightweight Java library for loading configuration from `.env` files at startup,
with variable expansion, typed access, and optional integration with JVM system properties.


## Features

* Parses `.env` files
* Supports quoted values escape sequences are treated as literals inside single-quoted values and as an escape sequence in double-quoted values
* Supports variable expansion
* Supports typed access `getInt`, `getBoolean`, `getLong` and so on
* Supports integration with JVM system properties
* Fails fast on conflicts, duplicate keys, invalid key names, missing variable references
* Supports multiple conflict resolution strategies
* Recognizes and strips the `export` prefix so bash-style exported vars work
* Supports defining required variables
* Supports `ignoreIfMissing` flag if presence of `.env` file is optional

## Installation

Add the __JitPack__ repositroy and following  dependency to your `pom.xml`

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.KonstantineVashalomidze</groupId>
        <artifactId>dotenv-loader</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
```

## Quickstart

Create `.env` file with following content at project root level
```
DB_HOST=localhost
DB_PORT=5432

DB_URL=http://${DB_HOST}:${DB_PORT}/api
```

```java
import com.github.konstantinevashalomidze.dotenv.Dotenv;

public class Main {
    public static void main(String... args) {
        Dotenv dotenv = Dotenv.configure()
                .load();
        String dbHost = dotenv.get("DB_HOST");
        int dbPort = dotenv.getInt("DB_PORT");
        String dbUrl = dotenv.get("DB_URL");
        ...
        ...
        ...
    }
}
```

## API Reference

### Builder options

```java
Dotenv dotenv = Dotenv.configure()
        .directory("./config")           // where to look for the file (default: "./")
        .filename("example.env")         // the file name (default: ".env")
        .ignoreIfMissing()               // don't throw if the file doesn't exist; loads an empty config instead
        .required("DB_HOST", "API_KEY")  // fail fast at load() if any of these keys are missing
        .load();
```

### Reading values

```java
String host = dotenv.get("DB_HOST");                 // throws DotenvException if missing
String region = dotenv.get("REGION", "us-east-1");    // returns default if missing, never throws

int port = dotenv.getInt("DB_PORT");
long timeout = dotenv.getLong("TIMEOUT_MS");
double ratio = dotenv.getDouble("SAMPLE_RATE");
boolean debug = dotenv.getBoolean("DEBUG");           // must be exactly "true" or "false" (case-sensitve)
```

### Quoted values and escaping

```
UNQUOTED=hello world
DOUBLE="hello world"
SINGLE='hello world'
ESCAPED="line1\nline2"        # \n becomes an actual newline
LITERAL='no ${expansion} and no \n escaping here'   # single quotes are always literal
```

### Variable expansion

```
HOST=localhost
PORT=5432
URL=http://${HOST}:${PORT}/api    # -> http://localhost:5432/api
```

Expansion looks up already-parsed keys first, then falls back to real system environment variables (`System.getenv()`). If a referenced variable isn't found in either, loading fails with a `DotenvException`.

### System properties integration

```java
Dotenv dotenv = Dotenv.configure()
        .systemProperties()  // export every parsed key to System.setProperty
        .onConflict("LOG_LEVEL", ConflictPolicy.PRESERVE)  // keep the existing system property
        .onConflict("DB_HOST", ConflictPolicy.OVERRIDE)    // let the .env value win
        .load();
```

By default (`ConflictPolicy.STRICT`, or if no policy is registered for a key), loading fails if a key already exists as a system property at all. This can be relaxed per-key with `onConflict`.

## License

MIT - see [LICENSE](LICENSE) for details.