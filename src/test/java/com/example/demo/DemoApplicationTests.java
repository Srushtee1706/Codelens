package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "github.webhook.secret=test-secret"
})
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

}