package com.example.demo.webhook;

import com.example.demo.service.GitHubEventProcessor;
import com.example.demo.util.HmacUtil;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class GitHubWebhookController {

    private final GitHubEventProcessor eventProcessor;

    @Value("${github.webhook.secret}")
    private String webhookSecret;

    public GitHubWebhookController(
            GitHubEventProcessor eventProcessor
    ) {
        this.eventProcessor = eventProcessor;
    }

    @PostMapping("/github")
    public ResponseEntity<String> receiveWebhook(

            @RequestHeader("X-GitHub-Event")
            String eventType,

            @RequestHeader("X-Hub-Signature-256")
            String githubSignature,

            @RequestBody String payload
    ) {

        // 1. Verify HMAC
        boolean valid = HmacUtil.isValid(
                payload,
                webhookSecret,
                githubSignature
        );

        if (!valid) {

            return ResponseEntity
                    .status(401)
                    .body("Invalid webhook signature");
        }

        // 2. Process event
        try {

            eventProcessor.process(
                    eventType,
                    payload
            );

            return ResponseEntity.ok(
                    "Webhook processed and saved"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body("Invalid webhook payload");
        }
    }
}