# Developer 2: Backend Developer, Customer Flow

**Project:** Tasty Bite (Java Swing desktop app, JDBC, MySQL)
**Duration:** 12 weeks · **Team:** 5 developers · **Reports to:** Project Manager · **Merged by:** Developer 1
**My backup:** Developer 3

---

## 1. Role summary

I build the customer side of the backend: restaurant and menu queries (B2), the profile (B3), the cart, bill and placing orders (B4), and the customer's own orders (B8). The customer screens call my services. I never write screen code.

## 2. What I own

Only I change these files. If someone else needs a change in one of them, they ask me.

| Area | Files |
|---|---|
| model | Bill, Cart, CartItem, MenuItem, MenuSection, MyOrder |
| dao | MenuDAO, OrderDAO, ProfileDAO, UserOrderDAO |
| service | MenuService, OrderService, ProfileService, UserOrderService |
| Documents | docs/dev2/ |

## 3. Responsibilities

- B2: list restaurants and fetch a restaurant's menu by section. Hide sections with no items and items that were deleted.
- Cart and bill: the cart classes (add, remove, subtotal) and the bill (5 percent tax, rounded half up, the same rule on screen and in the saved order).
- B3: view and change username and password. The current password is always required and passwords are hashed again.
- B4: save the order and all its lines in one transaction. Copy item names and prices into the order. Check the cart against the live menu first, and refuse the order if a price changed or an item was removed.
- B8: the customer's own orders from every restaurant with their lines, newest first, at most 100.
- Write backend test notes for every module and keep my module documents up to date.

## 4. Rules I follow

1. Services check the role (only customers order) and every customer query is limited to the logged-in user_id.
2. Prepared statements only. No SQL built from text typed by a person.
3. Multi-step writes (the order and its lines) use one transaction with rollback.
4. Error messages shown to people never reveal SQL or internals.
5. A table or column change goes through Developer 1 as a numbered migration script. I never change the database script myself.
6. I never edit a file I do not own. A bug in someone else's file becomes a GitHub Issue for its owner.
7. Definition of done: it compiles, I tested it against the real database, a reviewer approved it, it is merged, and it was shown in the Friday demo.

## 5. 12-week plan

| Week | Focus and tasks | Deliverable |
|---|---|---|
| 1 | Study screens F2 to F4; define interfaces for menu, cart and orders; list the data models | Backend design for B2 to B4 |
| 2 | Models and first queries: restaurant list, menu by section; test with sample data | Menu queries |
| 3 | Finish B2: hide empty sections, skip deleted items; test the cart classes (add, remove, subtotal) | B2 done, cart classes |
| 4 | B3 profile: view and change username and password with current-password check; bill and tax calculation with correct rounding | B3 done, bill logic |
| 5 | B4 place order: save order and lines in one transaction, copy names and prices, check for changed prices or removed items | B4 first version |
| 6 | B4 edge cases: empty cart, role checks, cart cleared on restaurant change; fix integration bugs; tests | B4 done |
| 7 | B8 My Orders: customer's orders across restaurants with their lines | B8 first version |
| 8 | Finish B8 with status text data; support live refresh; tests | B8 done |
| 9 | Run backend test cases; fix bugs; automated checks for B2, B3, B4, B8 | Test results, fixes |
| 10 | Edge cases: simultaneous orders, large carts, database-down messages; cleanup | Hardened customer backend |
| 11 | Regression tests; document B2, B3, B4, B8; support UAT | Module documents |
| 12 | Final fixes, knowledge handover, release support | Handover notes |

## 6. Milestones I contribute to

| Milestone | End of week | My part |
|---|---|---|
| M2 Browse and add to cart | 4 | Menu service, cart classes and bill logic ready |
| M3 Full order cycle (MVP) | 6 | Order saving works and survives the edge-case tests |
| M4 Feature complete | 8 | B8 My Orders finished |
| M5 Code complete | 10 | Edge cases closed, no open Critical or Major bug in my modules |
| M6 Release | 12 | Module documents and handover notes delivered |

## 7. Review checklist (my backend pull requests)

- [ ] Prepared statements only
- [ ] Customer queries limited by user_id
- [ ] Role checked in the service, not only hidden on screen
- [ ] Order and its lines saved in one transaction with rollback
- [ ] Prices and names copied into the order
- [ ] Cart checked against the live menu before ordering
- [ ] Errors shown to people do not reveal SQL
- [ ] Tested against the real database, including a failure case

## 8. Weekly routine

- **Monday:** planning with the team.
- **Daily:** 15-minute stand-up (done, next, blocked).
- **Friday:** the app is built from main, working demo, short status note to the Project Manager (done, next, blockers, bugs by severity).
- **Day 7:** my weekly report in the documents folder and my pull request link.

## 9. Week 12 handover checklist

- [ ] Module documents for B2, B3, B4 and B8 are up to date
- [ ] Backend test results and edge-case results are in docs/dev2
- [ ] Known limits listed (for example the 100-order limit on My Orders)
- [ ] All my branches merged, no open pull request
- [ ] Open issues handed to the Project Manager

## 10. Who I work with

| Person | Why |
|---|---|
| Developer 4 | screens F2 to F4 and F8 call my services; agree each service method before they use it |
| Developer 3 | shared order models (OrderStatus, OrderLine) are theirs; ask them if I need a change |
| Developer 1 | reviews my database and security changes; owns the database script and shared classes |
| Developer 5 | tests my modules in the order cycle; bugs come to me as GitHub Issues |
| Project Manager | Weekly status, change requests, milestone sign-off |
