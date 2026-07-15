package com.paytm.url_shortener.util;

public final class Base62 {

    private static final String ALLOWED_CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = ALLOWED_CHARACTERS.length();

    private Base62() {
    }

    /**
     * Encodes a database ID (Long) into a unique Base62 string.
     */
    public static String encode(long input) {
        if (input == 0) {
            return String.valueOf(ALLOWED_CHARACTERS.charAt(0));
        }

        StringBuilder sb = new StringBuilder();
        long value = input;

        while (value > 0) {
            int remainder = (int) (value % BASE);
            sb.append(ALLOWED_CHARACTERS.charAt(remainder));
            value /= BASE;
        }

        return sb.reverse().toString();
    }

    /**
     * Decodes a Base62 string back into its original database ID (Long).
     */
    public static long decode(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Input string cannot be null or empty");
        }

        long decoded = 0;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int index = ALLOWED_CHARACTERS.indexOf(c);

            if (index == -1) {
                throw new IllegalArgumentException("Invalid character found in Base62 string: " + c);
            }

            decoded = decoded * BASE + index;
        }

        return decoded;
    }
}