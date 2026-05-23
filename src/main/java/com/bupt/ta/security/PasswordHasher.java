package com.bupt.ta.security;

import org.mindrot.jbcrypt.BCrypt;

/**
 * BCrypt password hashing with backward-compatible plaintext verification for legacy records.
 */
public final class PasswordHasher {

    private PasswordHasher() {}

    public static String hash(String plainPassword) {
        if (plainPassword == null) {
            throw new IllegalArgumentException("Password is required");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(10));
    }

    public static boolean verify(String plainPassword, String storedHash) {
        if (storedHash == null || storedHash.isEmpty()) {
            return false;
        }
        if (isBcryptHash(storedHash)) {
            return plainPassword != null && BCrypt.checkpw(plainPassword, storedHash);
        }
        return plainPassword != null && plainPassword.equals(storedHash);
    }

    public static boolean isBcryptHash(String stored) {
        return stored != null
                && (stored.startsWith("$2a$") || stored.startsWith("$2b$") || stored.startsWith("$2y$"));
    }
}
