package com.example.demo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HmacUtilTest {

    private final String secret = "test-secret";

    @Test
    void shouldValidateCorrectSignature() {

        String payload = "{\"action\":\"opened\"}";

        String signature =
                "sha256=" + HmacUtil.calculateHmac(payload, secret);

        boolean result =
                HmacUtil.isValid(
                        payload,
                        secret,
                        signature
                );

        assertTrue(result);
    }


    @Test
    void shouldRejectIncorrectSignature() {

        String payload = "{\"action\":\"opened\"}";

        String wrongSignature =
                "sha256=wrong-signature";

        boolean result =
                HmacUtil.isValid(
                        payload,
                        secret,
                        wrongSignature
                );

        assertFalse(result);
    }


    @Test
    void shouldRejectModifiedPayload() {

        String originalPayload =
                "{\"action\":\"opened\"}";

        String modifiedPayload =
                "{\"action\":\"closed\"}";

        String signature =
                "sha256=" +
                HmacUtil.calculateHmac(
                        originalPayload,
                        secret
                );

        boolean result =
                HmacUtil.isValid(
                        modifiedPayload,
                        secret,
                        signature
                );

        assertFalse(result);
    }
}