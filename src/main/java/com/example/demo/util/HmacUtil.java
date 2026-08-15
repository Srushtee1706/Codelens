package com.example.demo.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HmacUtil {

    /**
     * Generates HMAC-SHA256 for the given payload using the shared secret.
     */
    public static String calculateHmac(
            String payload,
            String secret
    ) {

        try {

            // Create HMAC-SHA256 algorithm
            Mac mac = Mac.getInstance("HmacSHA256");

            // Convert secret into a cryptographic key
            SecretKeySpec secretKey =
                    new SecretKeySpec(
                            secret.getBytes(StandardCharsets.UTF_8),
                            "HmacSHA256"
                    );

            // Initialize HMAC with secret
            mac.init(secretKey);

            // Calculate HMAC
            byte[] hash =
                    mac.doFinal(
                            payload.getBytes(StandardCharsets.UTF_8)
                    );

            // Convert bytes to hexadecimal
            StringBuilder result = new StringBuilder();

            for (byte b : hash) {

                result.append(
                        String.format("%02x", b)
                );
            }

            return result.toString();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Failed to calculate HMAC",
                    e
            );
        }
    }


    /**
     * Verifies GitHub's X-Hub-Signature-256 header.
     */
    public static boolean isValid(
            String payload,
            String secret,
            String githubSignature
    ) {

        // Calculate our own HMAC
        String calculatedSignature =
                "sha256=" +
                calculateHmac(
                        payload,
                        secret
                );


        // Debugging
        System.out.println("--------------------------------");
        System.out.println("HMAC Verification");

        System.out.println(
                "Calculated Signature: "
                        + calculatedSignature
        );

        System.out.println(
                "GitHub Signature: "
                        + githubSignature
        );


        // Make sure GitHub actually sent a signature
        if (githubSignature == null) {

            System.out.println(
                    "GitHub signature is NULL"
            );

            return false;
        }


        // Compare signatures securely
        boolean valid =
                MessageDigest.isEqual(

                        calculatedSignature.getBytes(
                                StandardCharsets.UTF_8
                        ),

                        githubSignature.getBytes(
                                StandardCharsets.UTF_8
                        )
                );


        System.out.println(
                "Signature Valid: " + valid
        );

        System.out.println("--------------------------------");


        return valid;
    }
}