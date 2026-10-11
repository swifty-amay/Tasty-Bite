# Developer 1: Tech Lead, Database and Core Architect

**Project:** Tasty Bite (Java Swing desktop app, JDBC, MySQL)
**Duration:** 12 weeks · **Team:** 5 developers · **Reports to:** Project Manager
**Backup owner for:** Developer 2 (customer-flow backend)

---

## 1. Role summary

I am responsible for the foundation everything else stands on: the database, the shared core classes, authentication, and the quality of what gets merged. I review every change that touches the database or security, run the Friday integration build, and make the final release build.

## 2. What I own

| Area | Files |
|---|---|
| Database | `tasty_bite.sql`, all `migration_00X_*.sql` files, the test-data script |
| Core classes | `util/DBConnection.java`, `util/PasswordUtil.java`, `util/Session.java` |
| Module 1 backend (B1) | `dao/AuthDAO.java`, `service/AuthService.java`, `service/AuthException.java` |
| Shared models | `model/Role.java`, `model/Account.java`, `model/Restaurant.java` |
| Process | Repository, branch rules, coding standards, integration builds, release build |

## 3. Responsibilities

- Architecture: keep the layers clean (screen, service, data access, MySQL). Screens never run SQL.
- Database: design the 7 tables, keys, indexes and the default Snacks and Beverages trigger.
- Authentication: registration, login, hashed passwords, manager and restaurant created in one transaction.
- Code review: review all pull requests that touch the database, passwords, roles or sessions.
- Integration: build the app from `main` every Friday and click through the main flow.
- Security review in Week 9, performance and data checks in Week 10.
- Technical documentation and the release package in Weeks 11 and 12.

## 4. Rules I enforce

1. No direct pushes to `main`. One branch per task, for example `feature/B1-auth`.
2. Database changes only through numbered migration scripts, tested on a database that already holds data.
3. Every SQL statement uses prepared statements. No string-built queries.
4. Passwords are never stored or logged in plain text. Hashing only (PBKDF2 with a salt).
5. Every query for a manager's data is limited to that manager's `restaurant_id`. Every customer query is limited to that customer's `user_id`.
6. Order and order-line saving happens in one transaction (all or nothing).
7. Definition of done: compiles, tested against the real database, reviewed, merged, shown in the Friday demo.

## 5. 12-week plan

| Week | Focus and tasks | Deliverable |
|---|---|---|
| 1 | Review sketches, define layers (screen, service, data access, MySQL) and coding standards; set up repository and branch rules; draw the table design | Architecture note, ER diagram, repository |
| 2 | Write the database script (tables, keys, indexes, default sections); connection class, session, password hashing; start B1 | Database script, core classes |
| 3 | Finish B1: duplicate checks, manager and restaurant created together in one transaction; review B2 and B7 pull requests; shared test-data script | B1 done, test data |
| 4 | Define order and order-line saving rules; review F2 and F3; first Friday integration build | Order rules document |
