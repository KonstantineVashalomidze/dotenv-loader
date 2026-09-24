package com.github.konstantinevashalomidze.dotenv;

/**
 * STRICT - throw on conflict (default)
 * OVERRIDE - .env value wins, overwrites system property
 * PRESERVE - system property wins, .env value skpped for this key
 */
public enum ConflictPolicy {
    /// throw on conflict (default)
    STRICT,
    /// .env value wins, overwrites system property
    OVERRIDE,
    /// system property wins, .env value skipped for this key
    PRESERVE,
}
