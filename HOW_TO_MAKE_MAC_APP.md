# How to make TastyBite.app on a Mac (Apple Silicon / M1)

The result is a normal Mac app: it lives in Applications, shows in Launchpad and opens with a double-click.
It contains its own Java, so it does not need Java installed to run.

## What you need (once)

1. **MySQL running**, with the `tasty_bite` database created (you already have this).
2. **A full JDK 17 or newer** that includes `jpackage`. Eclipse's own built-in Java usually does not.
   Check in Terminal:
   ```
   javac -version
   jpackage --version
   ```
   If either fails: install **Temurin 21** (choose macOS, aarch64, JDK, .pkg) from adoptium.net,
   or run `brew install --cask temurin@21`. If Terminal still shows an older version, run:
   ```
   export JAVA_HOME=$(/usr/libexec/java_home -v 21)
   export PATH="$JAVA_HOME/bin:$PATH"
   ```
3. **The MySQL Connector/J jar inside the project's `lib` folder** (the real folder on disk, not only in Eclipse's build path).

## Build the app

1. Open Terminal and go to the project folder (type `cd ` then drag the TastyBite folder into Terminal).
2. Run:
   ```
   chmod +x build_mac_app.sh
   ./build_mac_app.sh
   ```
3. After about a minute you get `release/TastyBite.app`.
4. Drag `TastyBite.app` into **Applications**.

Optional, before step 2:
- Put your background photo at `resources/images/background.jpg` (it is packed into the app).
- Put an icon named `TastyBite.icns` in the project folder to give the app its own icon.

## First start

The app keeps its settings and food photos in a folder called **TastyBite inside your home folder**
(`~/TastyBite`), because a Mac app does not start inside the project folder.

Easiest: copy your settings once, before the first start:
```
mkdir -p ~/TastyBite
cp db.properties ~/TastyBite/
```
(Or just open the app. If it cannot connect, the message shows the file's location. Open that file in
TextEdit, set `db.password=your_mysql_password`, save, and open the app again.)

If you already added food photos while running from Eclipse, copy them too:
```
cp -R menu_images ~/TastyBite/
```

## If macOS blocks the app

- Right-click the app, choose **Open**, then **Open** again.
- Or: System Settings, Privacy & Security, scroll down, **Open Anyway**.
- Or in Terminal: `xattr -cr /Applications/TastyBite.app`

This mostly happens when the app was downloaded or sent to another Mac, because it is not signed by an Apple developer account.

## Before the demonstration

1. MySQL is running.
2. Run `reset_data.sql` to empty the data (tables stay).
3. Empty the folder `~/TastyBite/menu_images`.
4. Open TastyBite from Applications. Order for a fresh demo: register a manager (with a restaurant name),
   add menu items, then register a customer and place an order.

## Good to know

- Built on an M1, the app runs on Apple Silicon Macs. For an Intel Mac, build it with an Intel (x64) JDK.
- After you change code, run `./build_mac_app.sh` again, delete the old app from Applications, and drag in the new one.
- Running from Eclipse still works as before and uses the project folder's own `db.properties` and `menu_images`.
