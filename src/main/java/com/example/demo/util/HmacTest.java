package com.example.demo.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class HmacTest {

    public static void main(String[] args) throws Exception {

        String payload = "{\"action\":\"opened\",\"repository\":{\"name\":\"codelens\"},\"pull_request\":{\"number\":25,\"user\":{\"login\":\"rahul\"}}}";

        String secret = "my-secret-123";

        Mac mac = Mac.getInstance("HmacSHA256");

        SecretKeySpec secretKey = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        mac.init(secretKey);

        byte[] hash = mac.doFinal(
                payload.getBytes(StandardCharsets.UTF_8)
        );

        StringBuilder signature = new StringBuilder();

        for (byte b : hash) {
            signature.append(String.format("%02x", b));
        }

        System.out.println("Signature:");
        System.out.println("sha256=" + signature);
    }
}