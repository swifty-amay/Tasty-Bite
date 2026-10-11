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
| 5 | Verify B4 order saving: transaction and rollback tests, constraints, indexes | Order saving verified |
| 6 | Cross-role integration testing (place, see, complete, history); fix integration bugs; check order query speed | MVP integration report |
| 7 | Write and test the migration for the Preparing status on a database that already holds data; review status rules | Migration script |
| 8 | Code review sweep of every module; feature freeze; tag build 0.9 | Tagged build 0.9 |
| 9 | Security review: password storage, prepared statements, role checks, session handling; fix findings | Security review report |
| 10 | Performance and data checks (indexes, list limits); clean up technical debt | Cleaned code base |
| 11 | Technical documentation: architecture, schema, setup guide; packaging with run instructions | Technical documents, package |
| 12 | Release build, backend regression sign-off, setup walkthrough, handover | Version 1.0 build |

## 6. Milestones I am accountable for

| Milestone | End of week | My part |
|---|---|---|
| M0 Design approved | 1 | Architecture and database design ready for sign-off |
| M1 Login works | 2 | Database live, B1 working, passwords hashed |
| M3 Full order cycle (MVP) | 6 | Integration testing complete, no blocking bugs |
| M4 Feature complete | 8 | Review sweep done, build 0.9 tagged |
| M5 Code complete | 10 | Security and performance findings closed |
| M6 Release | 12 | Version 1.0 build and handover |

## 7. Review checklist (database and security pull requests)

- [ ] Uses prepared statements only
- [ ] Query is limited by `restaurant_id` or `user_id` where it should be
- [ ] Multi-step writes use a transaction with rollback
- [ ] No password or secret in code, logs or screens
- [ ] Role checked in the service layer, not only hidden on screen
- [ ] New table or column comes with a numbered migration script
- [ ] Error messages shown to people do not reveal SQL or internals
- [ ] Database calls do not run on the screen thread

## 8. Weekly routine

- **Monday:** planning with the team.
- **Daily:** 15-minute stand-up (done, next, blocked).
- **Friday:** integration build from `main`, working demo, short status note to the Project Manager (done, next, blockers, bugs by severity).

## 9. Week 12 handover checklist

- [ ] `tasty_bite.sql` creates a clean database from scratch
- [ ] `reset_data.sql` empties all data and keeps the tables
- [ ] Setup guide: install MySQL, run the script, set `db.properties`, run the app
- [ ] `db.properties` contains a placeholder password, not a real one
- [ ] Runnable JAR and `.exe` tested on a clean computer
- [ ] Architecture, schema and migration notes up to date
- [ ] Open issues and known limits listed for the Project Manager

## 10. Who I work with

| Person | Why |
|---|---|
| Developer 2 and Developer 3 | Review their backend pull requests; agree on interfaces and database changes |
| Developer 4 and Developer 5 | Integration builds; help when a screen needs a new backend call |
| Project Manager | Weekly status, change requests, milestone sign-off |
