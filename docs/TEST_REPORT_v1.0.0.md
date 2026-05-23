# Test Report — v1.0.0

**Date:** 2026-05-23  
**Command:** `mvn clean test`  
**Environment:** JDK 11+, H2 in-memory (unit/integration), Mockito 5.11

## Summary

| Metric | Result |
|--------|--------|
| Total automated tests | 281 |
| Failures | 0 |
| Errors | 0 |
| CI | GitHub Actions `mvn clean verify` |

## Coverage by layer

| Layer | Test classes | Focus |
|-------|--------------|-------|
| Model | 6 | Entities, stats, display DTOs |
| Service | 8 + 6 AI | Business rules, persistence |
| Util | 5 | Validation, config, HTTP client |
| AI / LM | 10 + 6 feature | Mock/HTTP clients, prompts |
| Servlet | 12+ | Auth, MO scope, applications, admin |
| Security | 2 | BCrypt, CSRF, role guards |
| Integration | 2 | Register→apply→approve lifecycle |

## UAT checklist (Must US)

| US | Scenario | Status |
|----|----------|--------|
| US-01 | TA registration | Pass |
| US-02 | Login / logout | Pass |
| US-04 | Protected routes | Pass |
| US-07–11 | Profile + CV | Pass |
| US-15–17 | Browse/search jobs | Pass |
| US-21–22 | Apply + status | Pass |
| US-26–28 | MO post + review | Pass |
| US-32 | Admin user mgmt | Pass |

Full matrix: [TEST_PLAN.md](TEST_PLAN.md) and [TRACEABILITY.md](TRACEABILITY.md).

## Known gaps (automated)

- No browser E2E (Selenium); servlet mocks used instead.
- SMTP delivery verified manually via MailHog in Docker demo.

## Sign-off

Automated regression suitable for v1.0.0 release gate. Manual UAT per DEMO_SCRIPT recommended before production deploy.
