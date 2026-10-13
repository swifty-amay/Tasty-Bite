# Tasty Bite

A Java desktop application for restaurant ordering. Customers pick a restaurant, browse its menu, fill a cart and place an order. Managers build their restaurant's menu, receive orders on a dashboard, move each order through its stages, and review the history.

Built by a team of five developers with **Java (Swing only)**, **JDBC** and **MySQL**.


## Google Drive Link
All the required documents regarding this project have been uploaded on the google drive. Kindly click on below link to view it - [Click Here](https://drive.google.com/drive/folders/1HPuF6Kmo_9hgiIoNs94X0RUUwZ9CiDjV?usp=sharing)



## Quick start: download and run (Windows)

No installation is needed. The download already points at an online database.

1. Download **[TastyBite-for-Windows.zip](https://drive.google.com/file/d/1zN7zMi1gyS13N9jbAgzHx47jgnpFWN3Q/view?usp=sharing)** from Google Drive (shared with the course teacher).
2. Unzip it and double-click **TastyBite.exe** inside the `TastyBite` folder. An internet connection is required.
3. If Windows says it protected your PC, click **More info**, then **Run anyway** (the app is not code-signed).
4. Log in with the accounts in `DEMO-ACCOUNTS.txt`, or register a new manager (with a restaurant name) and a customer.

The online database is a free cloud MySQL service. If the app cannot connect, the database may have been switched off after a period without use. Contact the team to switch it on again.

## Contents

1. [Features](#1-features)
2. [Modules](#2-modules)
3. [Requirements](#3-requirements)
4. [Getting started: how to run the project](#4-getting-started-how-to-run-the-project)
5. [First-time walkthrough](#5-first-time-walkthrough)
6. [Building an app you can double-click](#6-building-an-app-you-can-double-click)
7. [Where the app keeps its files](#7-where-the-app-keeps-its-files)
8. [Resetting and upgrading the database](#8-resetting-and-upgrading-the-database)
9. [Project structure](#9-project-structure)
10. [Database](#10-database)
11. [Order life cycle](#11-order-life-cycle)
12. [Team and contributions](#12-team-and-contributions)
13. [Troubleshooting](#13-troubleshooting)
14. [Known limits and future work](#14-known-limits-and-future-work)

---

## 1. Features

**Everyone**
- Welcome screen with a User and a Manager button, registration and login for both roles
- Passwords stored only as salted hashes
- Profile: change username and password (current password required), logout

**Customer**
- Choose a restaurant and browse its menu in sections, each a sideways-scrolling row of item cards (photo, name, price, plus button)
- Cart with minus and plus buttons, subtotal, 5 percent tax and total
- Place an order. If the manager changed a price meanwhile, the cart is updated and the customer reviews it first
- **My Orders:** all past and current orders with live status, refreshed every 10 seconds

**Manager**
- **Update Menu:** add, edit and delete items with optional photos, add and rename sections
- **Dashboard:** latest orders with View Order, Start Preparing, Cancel and Complete, refreshed every 10 seconds
- **History:** every order with a status label and filter buttons (All, Pending, Preparing, Completed, Cancelled)

## 2. Modules

Every screen (F) has a matching backend module (B).

| Module | Screen | Backend |
|---|---|---|
| 1 | F1 Welcome, login, register | B1 Authentication and sessions |
| 2 | F2 Restaurant picker and menu | B2 Restaurant and menu queries, cart |
| 3 | F3 Profile | B3 Username and password changes |
| 4 | F4 Cart and Place Order | B4 Bill, tax, saving the order |
| 5 | F5 Manager dashboard | B5 Active orders and status changes |
| 6 | F6 Order history | B6 All orders with status |
| 7 | F7 Update Menu | B7 Items, sections, photos |
| 8 | F8 My Orders (customer) | B8 The customer's own orders |

The code is layered: **screens** (`ui`) call **services** (`service`), which call **data access classes** (`dao`), which are the only code that talks to MySQL. Screens never contain SQL.

## 3. Requirements

| Needed | Version |
|---|---|
| JDK (full JDK, not only a runtime) | 17 or newer |
| MySQL Server | 8.x |
| MySQL Connector/J (the JDBC driver jar) | 8.x, placed in the `lib` folder |
| Eclipse (optional) | Any recent version |

## 4. Getting started: how to run the project

### Step 1: Get the code

```
git clone https://github.com/<user>/<repo>.git
cd <repo>
```

### Step 2: Create the database

Start MySQL, then run the database script. It creates the `tasty_bite` database, all tables, indexes and the trigger that creates the default Snacks and Beverages sections.

```
mysql -u root -p < tasty_bite.sql
```

(Or open `tasty_bite.sql` in MySQL Workbench and run it.) Running it again later drops and recreates everything, so it also works as a full reset.

### Step 3: Add the settings file

The file `db.properties` holds your MySQL address and password. It is not stored in Git. Create it in the project folder from the template:

```
cp db.properties.example db.properties
```

Then edit it:

```
db.url=jdbc:mysql://localhost:3306/tasty_bite?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=your_mysql_password
```

### Step 4: Add the JDBC driver

Download **MySQL Connector/J** (the `mysql-connector-j-8.x.x.jar` file) and copy it into the `lib` folder.

### Step 5: Run

**Option A: Eclipse**
1. File, Import, Existing Projects into Workspace, and choose this folder (or File, New, Java Project, untick the default location and choose the folder).
2. Right-click the jar in `lib`, Build Path, Add to Build Path.
3. Right-click the `resources` folder, Build Path, Use as Source Folder (this makes the background image available).
4. If Eclipse created a `module-info.java` file, delete it.
5. Open `src/com/tastybite/Main.java`, right-click, Run As, Java Application. Eclipse starts in the project folder, so `db.properties` is found.

**Option B: Command line, macOS and Linux**

```
mkdir -p out
javac -d out -cp "lib/*" $(find src -name "*.java")
java -cp "out:lib/*:resources" com.tastybite.Main
```

**Option C: Command line, Windows**

```
mkdir out
javac -d out -cp "lib/*" src\com\tastybite\*.java src\com\tastybite\util\*.java src\com\tastybite\model\*.java src\com\tastybite\dao\*.java src\com\tastybite\service\*.java src\com\tastybite\ui\*.java
java -cp "out;lib/*;resources" com.tastybite.Main
```

Run these from the project folder so `db.properties` is found.

If the database cannot be reached, the app shows a message that includes the path of the settings file it read.

### Using an online database (optional)

The app works with any MySQL 8 database it can reach, including a free cloud one. Create the database, run `tasty_bite.sql` on it, and use `db.properties.cloud.example` as your `db.properties`. Cloud databases need an encrypted connection, so the address ends with `?sslMode=REQUIRED`:

```
db.url=jdbc:mysql://HOST:PORT/tasty_bite?sslMode=REQUIRED
db.user=USER
db.password=PASSWORD
```

Step-by-step instructions, including building the Windows `.exe` on GitHub and sharing it, are in `HOW_TO_PUBLISH_EXE_AND_DATABASE.md`.

## 5. First-time walkthrough

The database starts empty, so create the data in this order:

1. On the welcome screen choose **Manager**, click **New manager? Register**, and enter a username, a password and a **restaurant name**.
2. Log in as that manager. On the dashboard click **Update**, then **Add Item** in a section (Snacks or Beverages) to create a few items. A photo is optional. Use **Add Section** for extra sections such as Desserts.
3. Click **Home**, open the profile icon and **Logout**.
4. Choose **User**, register a customer, and log in.
5. Pick the restaurant, add items with the plus buttons, open **Cart** and **Place Order**.
6. Log back in as the manager. The order appears on the dashboard. Press the orange play button (Start Preparing), then the green tick (Complete).
7. Log in as the customer again and open **My Orders**. The status updates by itself.

## 6. Building an app you can double-click

**macOS (Apple Silicon or Intel):** with a full JDK 17 or newer and the driver jar in `lib`:

```
chmod +x build_mac_app.sh
./build_mac_app.sh
```

This creates `release/TastyBite.app`. Drag it into Applications. Details, including first start and what to do if macOS blocks the app, are in `HOW_TO_MAKE_MAC_APP.md`.

**Windows:** build the JAR as above (or export a Runnable JAR from Eclipse with *Extract required libraries into generated JAR*), put it in a folder called `dist`, and on Windows with JDK 17 or newer run:

```
jpackage --type app-image --name TastyBite --input dist --main-jar TastyBite.jar --main-class com.tastybite.Main --dest release
```

This creates `release\TastyBite\TastyBite.exe`. Zip the whole `TastyBite` folder.

The `.exe` can also be built automatically on GitHub: the workflow `.github/workflows/build-windows.yml` builds it on a Windows machine (Actions tab, Run workflow). See `HOW_TO_PUBLISH_EXE_AND_DATABASE.md`.

The packaged app contains the program only. **The database is not included**: the computer that runs it needs a MySQL database it can reach, either installed locally or online.

## 7. Where the app keeps its files

The app needs two things: the settings file `db.properties` and a folder `menu_images` where manager photos are copied. It uses the first of these places that contains a `db.properties`:

1. The folder the app was started from (Eclipse, command line).
2. The folder of the app itself, next to `TastyBite.exe`. A settings file shipped inside the download is found this way, however the app is started.
3. The folder `TastyBite` inside your home folder (`~/TastyBite`). On the very first start the app creates a `db.properties` template there. Put your database details in it and open the app again.

Photos are stored as relative paths (`menu_images/...`), so a database backup stays portable as long as the photo folder travels with it.

## 8. Resetting and upgrading the database

| Task | How |
|---|---|
| Empty all data, keep the tables | `mysql -u root -p < reset_data.sql`, then delete the files in `menu_images` |
| Full clean start | Run `tasty_bite.sql` again |
| Upgrade a database created before the Preparing status existed | Run `migration_001_add_preparing.sql` once. It keeps all data |

Database structure changes are always delivered as numbered migration scripts that keep existing data.

## 9. Project structure

```
src/com/tastybite/
  Main.java                  application entry point
  util/                      DBConnection, AppPaths, PasswordUtil, Session, DbTime, Money, ImageLoader
  model/                     Role, Account, Restaurant, MenuItem, MenuSection, Cart, CartItem, Bill,
                             OrderStatus, OrderSummary, OrderLine, OrderDetail, MyOrder
  dao/                       AuthDAO, MenuDAO, ProfileDAO, OrderDAO, UserOrderDAO,
                             MenuEditDAO, OrderQueryDAO        (all SQL lives here)
  service/                   AuthService, MenuService, ProfileService, OrderService, UserOrderService,
                             MenuEditService, ManagerOrderService, ServiceException, AuthException
  ui/                        MainFrame (navigator), the screens (Welcome, Login, Register, RestaurantPicker,
                             Menu, Profile, Cart, MyOrders, Dashboard, History, MenuEditor), popups
                             (ItemDialog, SectionDialog, OrderDetailDialog) and shared parts (UiTheme,
                             HeaderBar, StatusBadge, ...)
lib/                         MySQL Connector/J jar
resources/images/            background.jpg (optional; a gradient is used if missing)
tasty_bite.sql               database script (new installs)
migration_001_add_preparing.sql   upgrade script
reset_data.sql               empties the data, keeps the tables
build_mac_app.sh             builds TastyBite.app
HOW_TO_MAKE_MAC_APP.md       Mac packaging guide
docs/                        each developer's weekly reports, notes and test reports
```

## 10. Database

Seven tables:

| Table | Purpose |
|---|---|
| `users` | Customer accounts |
| `managers` | Manager accounts |
| `restaurants` | One restaurant per manager |
| `categories` | Menu sections of a restaurant (Snacks and Beverages are created automatically) |
| `menu_items` | Items on a menu, with price, optional photo path and a deleted flag |
| `orders` | One order of one customer at one restaurant, with subtotal, tax, total, status and times |
| `order_items` | The lines of an order. Name and price are copied at order time |

One manager has one restaurant, a restaurant has many sections and a section has many items. A customer places many orders, a restaurant receives many orders, and an order has many lines.

Design choices worth knowing:
- Deleting a menu item only hides it, so past orders stay correct.
- Orders are never deleted. They only change status.
- An order and its lines are saved in one transaction, all or nothing.
- Order times are stamped by the database and shown in the local time of the computer.
- Prices are in Indian rupees. The 5 percent tax rate is one constant in `model/Bill.java`.

## 11. Order life cycle

```
Pending  --Start Preparing-->  Preparing  --Complete-->  Completed
   |                               |
   +------------Complete-----------+  (a pending order can also be completed directly)
   +--Cancel--> Cancelled  <--Cancel--+
```

Completed and Cancelled orders leave the dashboard but stay in History. Any other change is refused with a clear message, for example when two managers act on the same order.

## 12. Team and contributions

Five developers built the project. Each owned a separate set of files, so the work merged into `main` without conflicts. Developer 1 merged all pull requests.

| Module | Frontend | Backend |
|---|---|---|
| 1 Login and registration | Developer 4 | Developer 1 |
| 2 Restaurant and menu | Developer 4 | Developer 2 |
| 3 Profile | Developer 4 | Developer 2 |
| 4 Cart and ordering | Developer 4 | Developer 2 |
| 5 Manager dashboard | Developer 5 | Developer 3 |
| 6 Order history | Developer 5 | Developer 3 |
| 7 Update Menu | Developer 5 | Developer 3 |
| 8 My Orders | Developer 4 | Developer 2 |

### Developer 1: Tech Lead, Database and Core Architect (AMAY RAJ SRIVASTAV [24EARCS018])

- Designed the layered architecture and the seven-table database, with keys, indexes and the trigger that creates default menu sections.
- Wrote the core of the application: the database connection with connection reuse (fast against an online database), the settings and file locations, salted password hashing, the session, and the helper that shows order times correctly in any time zone.
- Built authentication (module 1 backend): registration, login, and creating a manager together with their restaurant in one transaction.
- Built the application entry point and the screen navigator (`Main`, `MainFrame`).
- Reviewed every change touching the database or security, ran the weekly integration builds, merged all pull requests and tagged the releases.
- Wrote the Preparing-status migration, the data reset script, the Mac packaging script and the setup and technical documentation, and carried out the security review.

### Developer 2: Backend Developer, Customer Flow (ANJALI SINGH [24EARCS021])

- Built the menu backend: restaurant list and menu by section, hiding empty sections and deleted items (module 2).
- Built the cart classes and the bill calculation with tax and correct rounding.
- Built the profile backend: view and change username and password with a current-password check (module 3).
- Built order placing (module 4): the order and its lines saved in one transaction, names and prices copied into the order, and a check against the live menu so a customer never pays a price they did not see.
- Built the backend for My Orders (module 8): the customer's own orders across restaurants with their items.

### Developer 3: Backend Developer, Manager Flow (ANUSHKA RAI [24EARCS027])

- Built menu editing (module 7 backend): adding, editing and soft-deleting items, adding and renaming sections, every query limited to the manager's own restaurant.
- Built photo handling: only real JPG, PNG or GIF images under 5 MB are accepted, copied into the app's photo folder.
- Built the manager order backend (module 5): active orders, order details, and guarded status changes (Start Preparing, Complete, Cancel).
- Built the history backend (module 6): all orders, newest first, capped list.
- Defined the order status rules and the ownership checks that keep one restaurant's data private from another.

### Developer 4: Frontend Developer, Customer Screens and UI System (ABHISHEK GOYAL [24EARCS005])

- Created the look of the whole application: the theme (colours, fonts, buttons, cards), the profile icon, the background and the shared helpers for money and images.
- Built the welcome, login and registration screens (module 1).
- Built the restaurant picker and the menu screen with sideways-scrolling section rows inside a vertical page, and the item cards (module 2).
- Built the profile screen with view and edit modes (module 3) and the cart screen with quantity buttons, bill and order confirmation (module 4).
- Built My Orders (module 8): orders grouped as In progress and Past orders, status labels, plain-language status lines and live refresh.
- Wrote the customer user guide and gave the customer demo.

### Developer 5: Frontend Developer, Manager Screens and QA Lead (AAKRITI GUPTA [24EARCS001])

- Built the manager dashboard (module 5): latest orders, View Order popup, start, cancel and complete buttons, a status column and refresh every 10 seconds.
- Built the history screen (module 6) with status labels and filter buttons.
- Built Update Menu (module 7): section blocks, item list with thumbnails, the Add Item and Edit popup with photo choice, delete with confirmation, and Add Section and Rename.
- Built the shared header, status label, round action buttons and table row layout used across the manager screens.
- Acted as QA lead: test strategy and test cases, bug tracking, two test rounds, user acceptance testing and the final release sign-off.
- Wrote the manager user guide and gave the manager demo.

## 13. Troubleshooting

| Problem | What to do |
|---|---|
| Popup "Could not connect to the database" | Check MySQL is running, the user and password in `db.properties` (the popup shows its location), and that the driver jar is in `lib` |
| "No suitable driver" | The Connector/J jar is not on the classpath. In Eclipse add it to the Build Path. For a runnable JAR choose *Extract required libraries into generated JAR* |
| Eclipse: "The type javax.swing... is not accessible" | Delete the `module-info.java` file Eclipse created |
| Eclipse complains about `record` | Set the compiler compliance to 17 or higher in Project, Properties, Java Compiler |
| Database error when pressing Start Preparing | Run `migration_001_add_preparing.sql` once |
| Photos do not show | They are looked for in `menu_images` next to `db.properties` (or in `~/TastyBite`). Only JPG, PNG and GIF under 5 MB are accepted |
| macOS blocks the app | Right-click the app, Open. Or run `xattr -cr /Applications/TastyBite.app` |
| Background is a plain orange gradient | Add your image as `resources/images/background.jpg` (about 1920 x 1080) and mark `resources` as a source folder |
| `Access denied for user` | Wrong MySQL password in `db.properties` |
| "Communications link failure" with an online database | The database service is switched off, there is no internet, or the network blocks its port. Try another network such as a phone hotspot |
| Errors about SSL with an online database | The address must end with `?sslMode=REQUIRED`. Do not use `useSSL=false` |

## 14. Known limits and future work

- The dashboard shows the latest 500 orders in History and a customer sees their latest 100 orders.
- Only the restaurant can cancel an order. A customer cannot.
- Photos are stored on the computer where they were uploaded, not in the database. If managers and customers use different computers with a shared database, photos are only seen on the manager's computer.
- Future ideas: online payment, notifications when an order changes, ratings and reviews, customers cancelling their own orders, a web or mobile version.
