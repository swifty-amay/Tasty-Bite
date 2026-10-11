# Tasty Bite

Java Swing desktop app with a MySQL backend (JDBC). Built module by module.

## Structure

```
TastyBite/
  src/com/tastybite/
    Main.java
    util/      DBConnection, PasswordUtil, Session          (shared foundation)
    util/      ... plus Money, ImageLoader
    model/     Role, Account, Restaurant, MenuItem, MenuSection, Cart, CartItem, Bill
    dao/       AuthDAO (B1), MenuDAO (B2), ProfileDAO (B3), OrderDAO (B4), MenuEditDAO (B7)
    service/   AuthService, AuthException (B1), MenuService, ServiceException (B2), ProfileService (B3), OrderService (B4), MenuEditService (B7)
    ui/        MainFrame (navigator), WelcomePanel, LoginPanel, RegisterPanel,
               HomePlaceholderPanel, BackgroundPanel, UiTheme, AvatarIcon   (F1)
               RestaurantPickerPanel, MenuPanel, ItemCard, VerticalPage     (F2)
               ProfilePanel                                                 (F3)
               HeaderBar (shared), MenuEditorPanel, ItemDialog              (F7)
               CartPanel, QtyButton                                         (F4)
  resources/images/    put background.jpg here (a gradient is used if missing)
  lib/                 put the MySQL Connector/J jar here
  menu_images/         created automatically; manager photos are copied here
  db.properties        MySQL url, user and password
  tasty_bite.sql       database script
```

## Setup

1. Run `tasty_bite.sql` in MySQL (already done on your machine).
2. Download **MySQL Connector/J** (the `mysql-connector-j-x.x.x.jar` file) and copy it into `lib/`.
3. Edit `db.properties` and set your MySQL password.
4. (Optional) Copy a food photo to `resources/images/background.jpg`.

## Run from the command line (JDK 17 or newer), from the project root

Windows:
```
mkdir out
javac -d out -cp "lib/*" src\com\tastybite\*.java src\com\tastybite\util\*.java src\com\tastybite\model\*.java src\com\tastybite\dao\*.java src\com\tastybite\service\*.java src\com\tastybite\ui\*.java
java -cp "out;lib/*;resources" com.tastybite.Main
```

Linux / macOS:
```
mkdir -p out
javac -d out -cp "lib/*" $(find src -name "*.java")
java -cp "out:lib/*:resources" com.tastybite.Main
```

## Run from an IDE (IntelliJ / Eclipse / NetBeans)

- Add the jar in `lib/` to the project libraries.
- Mark `src` as sources and `resources` as resources.
- Set the working directory to the project root so `db.properties` is found.
- Run `com.tastybite.Main`.

## Modules done

- F1 / B1: Welcome, Login, Register (user and manager), password hashing, session
- F2 / B2: Restaurant picker, menu with scrolling item cards, in-memory cart
- F4 / B4: Cart screen, bill (5% tax, set in model/Bill.java), Place Order saved to the database
- F3 / B3: Profile (view, Change Details, Save), same flow for user and manager
- F7 / B7: Update Menu (add, edit, delete items with optional photo), manager only
