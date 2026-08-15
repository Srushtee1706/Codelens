# CodeLens

### Developer Productivity Analytics Platform

CodeLens is a backend-focused developer productivity analytics platform that collects GitHub repository events, securely processes them, stores the data in MongoDB, and provides developer-level analytics.

The project is built using Spring Boot and is designed to evolve into a complete DORA and developer productivity analytics platform.

---

##  Features

### GitHub Webhook Integration

CodeLens receives GitHub webhook events through a Spring Boot REST endpoint.

Currently supported:

- Pull Request events
- Push events

Endpoint:

```text
POST /api/webhooks/github
