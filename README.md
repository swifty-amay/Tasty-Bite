# Developer 4: Frontend Developer, Customer Screens and UI System

**Project:** Tasty Bite (Java Swing desktop app, JDBC, MySQL)
**Duration:** 12 weeks · **Team:** 5 developers · **Reports to:** Project Manager · **Merged by:** Developer 1
**My backup:** Developer 5

---

## 1. Role summary

I build the customer screens and the shared look of the whole app: welcome, login and register (F1), restaurant picker and menu (F2), profile (F3), cart (F4) and My Orders (F8). I own the theme and reusable parts, and I keep every screen consistent.

## 2. What I own

Only I change these files. If someone else needs a change in one of them, they ask me.

| Area | Files |
|---|---|
| util | ImageLoader, Money |
| ui | AvatarIcon, BackgroundPanel, CartPanel, ItemCard, LoginPanel, MenuPanel, MyOrdersPanel, ProfilePanel, QtyButton, RegisterPanel, RestaurantPickerPanel, UiTheme, VerticalPage, WelcomePanel |
| Other | background.jpg |
| Documents | docs/dev4/ |

## 3. Responsibilities

- Style guide and theme: colours, fonts, spacing, buttons, cards and the profile icon, so every screen looks the same.
- F1: welcome screen with the background image, login and register for both roles, and the success popup.
- F2: restaurant picker and menu with a horizontal scroll row per section inside a vertical scroll page; item cards with image, name, price and a plus button.
- F3 profile (view and edit mode), F4 cart (minus and plus, bill, Place Order popup, empty state), F8 My Orders (In progress and Past orders, status badges, live refresh).
- Keep all database work out of the screens. Screens call service classes, and slow calls run in the background so the window never freezes.
- Customer user guide with screenshots and the customer demo.

## 4. Rules I follow

1. Swing only. No other UI framework or toolkit.
2. No SQL and no direct database access in any screen. Screens call the service classes.
3. Anything that talks to the database runs in the background (SwingWorker), then updates the screen.
4. Use the shared theme (colours, fonts, buttons). Do not invent new colours in a screen.
5. The window opens at 1000 x 680, can be resized, and must work down to 800 x 560.
6. Screens that need a new navigation method ask Developer 1. I never edit MainFrame.
7. I never edit a file I do not own. A bug in someone else's file becomes a GitHub Issue for its owner.
8. Definition of done: it compiles, I tested it against the real database, a reviewer approved it, it is merged, and it was shown in the Friday demo.

## 5. 12-week plan

| Week | Focus and tasks | Deliverable |
|---|---|---|
| 1 | UI style guide (colours, fonts, spacing); plan reusable parts (buttons, cards, profile icon); screen flow from the sketches | Style guide |
| 2 | F1: welcome screen with background image, login, registration for both roles, success popup; agree the navigation methods with Developer 1 | F1 done |
| 3 | F2: restaurant picker; menu header; section rows with horizontal scrolling inside a vertical scroll page | F2 layout |
| 4 | F2 item cards (image, name, price, plus button) and image loading; F3 profile with view and edit modes | F2, F3 done |
| 5 | F4 cart: item rows with minus and plus, bill, Place Order popup, empty-cart state | F4 first version |
| 6 | Polish the customer flow; clear error messages; fix integration bugs | Customer flow polished |
| 7 | F8 My Orders: order cards grouped as In progress and Past orders, status badges | F8 first version |
| 8 | F8 live refresh and plain-language status lines; My Orders link in the menu header | F8 done |
| 9 | Test customer screens (window sizes, long names, many items); fix defects | Defect fixes |
| 10 | Consistency pass over all screens; tab order and readable contrast; fix defects | Polished UI |
| 11 | Customer user guide with screenshots; UAT support; UI regression | Customer guide |
| 12 | Final polish; prepare and give the customer demo; handover | Demo, handover |

## 6. Milestones I contribute to

| Milestone | End of week | My part |
|---|---|---|
| M1 Login works | 2 | Welcome, login and register screens working with the backend |
| M2 Browse and add to cart | 4 | Restaurant picker, menu cards, profile working |
| M3 Full order cycle (MVP) | 6 | Cart and Place Order working, customer flow polished |
| M4 Feature complete | 8 | My Orders with live refresh working |
| M5 Code complete | 10 | Consistency pass done, no open Critical or Major UI bug |
| M6 Release | 12 | Customer guide and customer demo delivered |

## 7. Review checklist (my screen pull requests)

- [ ] No SQL or database access in the screen
- [ ] Database work runs in the background
- [ ] Shared theme used, no new colours
- [ ] Works at 800 x 560 and when maximised
- [ ] Long names, many items and empty lists look right
- [ ] Clear message for every error and empty state
- [ ] Tab order and Enter key behave sensibly
- [ ] Tested with real data, not only sample text

## 8. Weekly routine

- **Monday:** planning with the team.
- **Daily:** 15-minute stand-up (done, next, blocked).
- **Friday:** the app is built from main, working demo, short status note to the Project Manager (done, next, blockers, bugs by severity).
- **Day 7:** my weekly report in the documents folder and my pull request link.

## 9. Week 12 handover checklist

- [ ] Customer user guide with screenshots is finished
- [ ] UI test report and consistency notes are in docs/dev4
- [ ] Demo script for the customer flow is ready and rehearsed
- [ ] All my branches merged, no open pull request
- [ ] Open issues handed to the Project Manager

## 10. Who I work with

| Person | Why |
|---|---|
| Developer 2 | their services feed my screens F2 to F4 and F8 |
| Developer 3 | their order status values feed My Orders |
| Developer 1 | owns MainFrame; I ask for new navigation methods |
| Developer 5 | tests my screens; bugs come to me as GitHub Issues; we share the status badge and order formats |
| Project Manager | Weekly status, change requests, milestone sign-off |
