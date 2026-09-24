package com.github.konstantinevashalomidze.dotenv;

public enum ConflictPolicy {
    STRICT, // throw on conflict (default)
    OVERRIDE, // .env value wins, overwrites system property
    PRESERVE, // system property wins, .env value skipped for this key
}
