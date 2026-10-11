# Developer 5: Frontend Developer, Manager Screens and QA Lead

**Project:** Tasty Bite (Java Swing desktop app, JDBC, MySQL)
**Duration:** 12 weeks · **Team:** 5 developers · **Reports to:** Project Manager · **Merged by:** Developer 1
**My backup:** Developer 4

---

## 1. Role summary

I build the manager screens (dashboard F5, history F6, Update Menu F7) and I am the QA lead: test strategy, test cases, bug tracking, test rounds and user acceptance testing for the whole app.

## 2. What I own

Only I change these files. If someone else needs a change in one of them, they ask me.

| Area | Files |
|---|---|
| ui | DashboardPanel, HeaderBar, HistoryPanel, ItemDialog, MenuEditorPanel, OrderDetailDialog, OrderFormat, RoundIconButton, RowLayout, SectionDialog, StatusBadge |
| Documents | docs/qa/ (test documents) and docs/dev5/ (weekly reports) |

## 3. Responsibilities

- F5 dashboard: Latest Orders list with View Order, start preparing, cancel and complete buttons, a Status column and a refresh every 10 seconds.
- F6 history: all orders with a status badge, a View button and filter buttons (All, Pending, Preparing, Completed, Cancelled).
- F7 Update Menu: sections, item list, Add Item and Edit popups with an optional photo, Delete with confirmation, Add Section and Rename.
- QA lead: write the test strategy and test-case template, keep the bug board, run test round 1 (Week 9) and round 2 (Week 10), coordinate user acceptance testing (Week 11) and sign off the release (Week 12).
- Manager user guide and the manager demo.

## 4. Rules I follow

1. Swing only. No SQL and no direct database access in any screen. Database work runs in the background.
2. Use the shared theme. Do not invent new colours in a screen.
3. Screens that need a new navigation method ask Developer 1. I never edit MainFrame.
4. As QA lead I report bugs to the file owner, I do not fix other people's files. The owner fixes it on their own branch.
5. A bug is only closed after I have re-tested the fix.
6. I never edit a file I do not own. A bug in someone else's file becomes a GitHub Issue for its owner.
7. Definition of done: it compiles, I tested it against the real database, a reviewer approved it, it is merged, and it was shown in the Friday demo.

## 5. 12-week plan

| Week | Focus and tasks | Deliverable |
|---|---|---|
| 1 | Test strategy and test-case template; bug board; review manager screen flow | Test strategy, bug board |
| 2 | Shared header; agree the navigation methods with Developer 1; write and run test cases for login and registration | Shared header, F1 test results |
| 3 | F7 Update Menu: sections, item list, Add Item popup with name, price and photo | F7 first version |
| 4 | F7 edit, delete and confirmation, photo chooser; start F5 layout (Update Menu card, orders table) | F7 done, F5 layout |
| 5 | F5 dashboard: Latest Orders rows, View Order popup, cancel and complete buttons | F5 first version |
| 6 | F6 history list with status; write and run full order-cycle test cases | F6 done, cycle test results |
| 7 | F5 start-preparing button and Status column; F7 Add Section and Rename popups | Preparing UI, section UI |
| 8 | F5 refresh every 10 seconds; F6 filter buttons; feature-complete test pass | Feature-complete test report |
| 9 | QA lead: run the full test plan, log bugs, triage with the Project Manager, verify fixes | Test round 1 report |
| 10 | Regression round 2; exploratory testing; fix manager-screen defects | Test round 2 report |
| 11 | Coordinate UAT with sample restaurant users; collect feedback; final regression; manager user guide | UAT report, manager guide |
| 12 | Release sign-off testing; test summary report; give the manager demo | Test summary, demo |

## 6. Milestones I contribute to

| Milestone | End of week | My part |
|---|---|---|
| M0 Design approved | 1 | Test strategy ready |
| M2 Browse and add to cart | 4 | Update Menu screen working |
| M3 Full order cycle (MVP) | 6 | Dashboard and history working, order-cycle tests passed |
| M4 Feature complete | 8 | Feature-complete test report delivered |
| M5 Code complete | 10 | Two test rounds finished, no open Critical or Major bug |
| M6 Release | 12 | UAT passed, test summary and manager demo delivered |

## 7. Review checklist (my screen pull requests)

- [ ] No SQL or database access in the screen
- [ ] Database work runs in the background
- [ ] Shared theme used, no new colours
- [ ] Works at 800 x 560 and when maximised
- [ ] Long names, many orders and empty lists look right
- [ ] Clear message when a status change is refused
- [ ] Refresh does not jump the scroll position or lose what is on screen
- [ ] Tested with real data, including two managers acting on the same order

## 8. QA lead toolkit

**Bug severity**

| Severity | Meaning |
|---|---|
| Critical | Data loss, or a person cannot log in or order |
| Major | A feature does not work |
| Minor | Wrong text or a small layout issue |

**Bug report:** title, steps to repeat, expected result, actual result, screenshot, severity, module and owner.

**Who gets the bug**

| Area | Owner |
|---|---|
| Core, database script, login backend (B1), navigation | Developer 1 |
| B2, B3, B4, B8 (customer backend) | Developer 2 |
| B5, B6, B7 (manager backend) | Developer 3 |
| F1, F2, F3, F4, F8 and the shared theme | Developer 4 |
| F5, F6, F7 | Developer 5 (me) |

**Test rounds:** round 1 in Week 9, round 2 in Week 10, user acceptance in Week 11, final sign-off in Week 12.

**Release criteria:** all test cases pass, no Critical or Major bug is open, user acceptance is done with sample customers and a sample manager, documents are delivered, the demo is given and the Project Manager signs off.

## 9. Weekly routine

- **Monday:** planning with the team.
- **Daily:** 15-minute stand-up (done, next, blocked).
- **Friday:** the app is built from main, working demo, short status note to the Project Manager (done, next, blockers, bugs by severity).
- **Day 7:** my weekly report in the documents folder and my pull request link.

## 10. Week 12 handover checklist

- [ ] Test strategy, test cases, both test round reports, UAT report and test summary are in docs/qa
- [ ] Manager user guide is finished
- [ ] Demo script for the manager flow is ready and rehearsed
- [ ] All Critical and Major bugs are closed and re-tested
- [ ] All my branches merged, no open pull request

## 11. Who I work with

| Person | Why |
|---|---|
| Developer 3 | their services feed my screens F5 to F7 |
| Developer 4 | owns the theme and shared parts I use; My Orders uses my status badge |
| Developer 1 | owns MainFrame; I ask for new navigation methods |
| Developer 2 | I report backend bugs to Developer 2 and Developer 3 as GitHub Issues |
| Project Manager | Weekly status, change requests, milestone sign-off |
