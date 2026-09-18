package com.digitaldetective.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Computes SHA-256 hashes for files using a streaming approach so that
 * large files never need to be loaded fully into memory.
 */
public final class HashCalculator {

    private static final int BUFFER_SIZE = 8192;

    private HashCalculator() {
    }

    /**
     * Computes the SHA-256 hash of the file at the given path, returned as a
     * lower-case hexadecimal string.
     *
     * @throws IOException if the file cannot be read.
     */
    public static String sha256(Path path) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed to be available on every standard JVM.
            throw new IllegalStateException("SHA-256 algorithm not available on this JVM", e);
        }

        try (InputStream in = Files.newInputStream(path)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        return toHex(digest.digest());
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                sb.append('0');
            }
            sb.append(hex);
        }
        return sb.toString();
    }
}
