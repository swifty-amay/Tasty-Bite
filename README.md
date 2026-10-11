# Developer 3: Backend Developer, Manager Flow

**Project:** Tasty Bite (Java Swing desktop app, JDBC, MySQL)
**Duration:** 12 weeks · **Team:** 5 developers · **Reports to:** Project Manager · **Merged by:** Developer 1
**My backup:** Developer 2

---

## 1. Role summary

I build the manager side of the backend: menu editing with photos and sections (B7), the dashboard orders and their status changes (B5), and the order history (B6). The manager screens call my services. I never write screen code.

## 2. What I own

Only I change these files. If someone else needs a change in one of them, they ask me.

| Area | Files |
|---|---|
| model | OrderDetail, OrderLine, OrderStatus, OrderSummary |
| dao | MenuEditDAO, OrderQueryDAO |
| service | ManagerOrderService, MenuEditService |
| Documents | docs/dev3/ |

## 3. Responsibilities

- B7: add, edit and soft-delete menu items, and add or rename sections. Every query is limited to the manager's own restaurant.
- Photos: accept only JPG, PNG or GIF under 5 MB, check the file is a real image, copy it into the menu_images folder and store the relative path.
- B5: active orders, order details, and the status changes (start preparing, mark complete, cancel) as guarded updates.
- B6: the history of all orders, newest first, capped at 500.
- Status rules: pending to preparing to completed, pending to completed directly, and cancel from pending or preparing. Anything else is refused with a clear message.
- Write backend test notes for every module and keep my module documents up to date.

## 4. Rules I follow

1. Every query for a manager's data is limited to that manager's restaurant_id. Another manager must never be able to see or change it.
2. Status changes are guarded updates: the order must still be in the expected status, otherwise nothing changes and a clear message is returned.
3. Menu items are only soft-deleted, so old orders keep working. Completed and cancelled orders are never deleted.
4. Prepared statements only. Photos are checked before they are saved.
5. A table or column change goes through Developer 1 as a numbered migration script. I never change the database script myself.
6. I never edit a file I do not own. A bug in someone else's file becomes a GitHub Issue for its owner.
7. Definition of done: it compiles, I tested it against the real database, a reviewer approved it, it is merged, and it was shown in the Friday demo.

## 5. 12-week plan

| Week | Focus and tasks | Deliverable |
|---|---|---|
| 1 | Study screens F5 to F7; design order queries and statuses; decide how photos are stored | Backend design for B5 to B7 |
| 2 | Menu editing skeleton and input rules (name length, price format) | B7 skeleton |
| 3 | B7: add, edit and soft-delete items; every query limited to the manager's own restaurant; photo checks and copy into a menu_images folder | B7 first version |
| 4 | B7 hardening: duplicate names, tests that another manager cannot touch the menu; start B5 queries for active orders | B7 done, B5 started |
| 5 | B5: latest orders, order details, mark complete and cancel with guarded status changes | B5 first version |
| 6 | B6: history of all orders, newest first, capped list; tests; fix integration bugs | B6 done |
| 7 | Preparing status: Pending to Preparing to Completed, cancel from either; B7 add and rename sections | Status rules, sections |
| 8 | Efficient queries for live refresh; history filter data; tests | B5 and B6 final |
| 9 | Run backend test cases; fix bugs; tests for status changes and ownership | Test results, fixes |
| 10 | Edge cases: two managers acting on the same order, missing images, very long histories; cleanup | Hardened manager backend |
| 11 | Regression tests; document B5 to B7; support UAT | Module documents |
| 12 | Final fixes, handover, release support | Handover notes |

## 6. Milestones I contribute to

| Milestone | End of week | My part |
|---|---|---|
| M2 Browse and add to cart | 4 | Menu editing works so managers can create menus |
| M3 Full order cycle (MVP) | 6 | Dashboard queries, complete, cancel and history working |
| M4 Feature complete | 8 | Preparing status and sections working |
| M5 Code complete | 10 | Edge cases closed, no open Critical or Major bug in my modules |
| M6 Release | 12 | Module documents and handover notes delivered |

## 7. Review checklist (my backend pull requests)

- [ ] Prepared statements only
- [ ] Every query limited by restaurant_id
- [ ] Status change is a guarded update (expected status checked)
- [ ] Deleted items are soft-deleted, orders never deleted
- [ ] Photo checked: type, size, real image
- [ ] Role checked in the service, not only hidden on screen
- [ ] Errors shown to people do not reveal SQL
- [ ] Tested against the real database, including another manager trying to touch my data

## 8. Weekly routine

- **Monday:** planning with the team.
- **Daily:** 15-minute stand-up (done, next, blocked).
- **Friday:** the app is built from main, working demo, short status note to the Project Manager (done, next, blockers, bugs by severity).
- **Day 7:** my weekly report in the documents folder and my pull request link.

## 9. Week 12 handover checklist

- [ ] Module documents for B5, B6 and B7 are up to date
- [ ] Backend test results and edge-case results are in docs/dev3
- [ ] Known limits listed (for example the 500-order limit on History, photo rules)
- [ ] All my branches merged, no open pull request
- [ ] Open issues handed to the Project Manager

## 10. Who I work with

| Person | Why |
|---|---|
| Developer 5 | screens F5 to F7 call my services; agree each service method before they use it |
| Developer 2 | shared menu models (MenuItem, MenuSection) are theirs; ask them if I need a change |
| Developer 1 | reviews my database and security changes; owns the database script and shared classes |
| Developer 4 | My Orders reads the order status values I own |
| Project Manager | Weekly status, change requests, milestone sign-off |

