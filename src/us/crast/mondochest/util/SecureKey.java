package us.crast.mondochest.util;

import java.security.SecureRandom;

public final class SecureKey {
	private static final char[] KEY_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private final SecureRandom rng;

    public SecureKey(int entropyBytes) {
        this.rng = new SecureRandom();
    }

    public String makeKey(int length) {
        synchronized (this.rng) {
            char[] output = new char[length];
            for (int i = 0; i < length; i++) {
            	output[i] = KEY_CHARS[rng.nextInt(36)];
            }
            return new String(output);
        }
    }

}