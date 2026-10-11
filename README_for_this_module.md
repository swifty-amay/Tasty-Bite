# Tasty Bite

Java Swing desktop app with a MySQL backend (JDBC). Built module by module.

## Structure

```
TastyBite/
  src/com/tastybite/
    Main.java
    util/      DBConnection, PasswordUtil, Session          (shared foundation)
    util/      ... plus Money, ImageLoader
    model/     Role, Account, Restaurant, MenuItem, MenuSection, Cart, CartItem
    dao/       AuthDAO (B1), MenuDAO (B2)
    service/   AuthService, AuthException (B1), MenuService, ServiceException (B2)
    ui/        MainFrame (navigator), WelcomePanel, LoginPanel, RegisterPanel,
               HomePlaceholderPanel, BackgroundPanel, UiTheme, AvatarIcon   (F1)
               RestaurantPickerPanel, MenuPanel, ItemCard                   (F2)
  resources/images/    put background.jpg here (a gradient is used if missing)
  lib/                 put the MySQL Connector/J jar here
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
