package com.mopl.jpa.support;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UuidBinaryConverter {

    public static byte[] toBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long mostSignificantBits = uuid.getMostSignificantBits();
        long leastSignificantBits = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (mostSignificantBits >>> (8 * (7 - i)));
            bytes[i + 8] = (byte) (leastSignificantBits >>> (8 * (7 - i)));
        }
        return bytes;
    }
}
