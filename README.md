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

