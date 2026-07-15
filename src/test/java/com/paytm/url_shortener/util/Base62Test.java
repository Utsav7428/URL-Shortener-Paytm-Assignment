package com.paytm.url_shortener.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class Base62Test {

    @Test
    @DisplayName("Should correctly encode zero")
    void encodeZero() {
        assertEquals("0", Base62.encode(0));
    }

    @Test
    @DisplayName("Should correctly encode a standard database auto-increment ID")
    void encodeStandardId() {
        // ID 125 should yield something predictable
        long id = 125;
        String encoded = Base62.encode(id);

        assertNotNull(encoded);
        assertFalse(encoded.isBlank());
        assertEquals(id, Base62.decode(encoded));
    }

    @ParameterizedTest
    @ValueSource(longs = {1, 62, 1000, 500000, 9999999999L})
    @DisplayName("Should maintain bi-directional integrity for various IDs")
    void bidirectionalIntegrity(long originalId) {
        String encoded = Base62.encode(originalId);
        long decoded = Base62.decode(encoded);

        assertEquals(originalId, decoded, "The decoded ID should match the original input");
    }

    @Test
    @DisplayName("Should throw exception when decoding invalid characters")
    void decodeInvalidCharacters() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("short_code!"));
    }
}