# Publishing Tasty Bite: online database, Windows .exe and the Drive link

**The plan in three lines**
1. The database lives online (a free cloud MySQL), so your teacher needs to install nothing.
2. GitHub builds `TastyBite.exe` for you. You add the database settings and upload the zip to Google Drive.
3. The Drive link goes into the GitHub `README.md`.

> **What was tested and what was not.** The app changes, the settings lookup, the connection reuse, the restricted database user and the workflow's build steps were tested on a Linux computer with a MySQL-compatible database. I could **not** test with a real Aiven account, on Windows, or with Google Drive. Those steps follow the providers' documentation. If one fails, send me the exact error text.

---

## Part 1: Create the online database (Aiven, free)

Aiven's free MySQL plan needs no credit card and has no time limit. It gives 1 GB of storage, and one free MySQL service per account. Aiven may switch a free service off if it is unused for some time (it sends a notice first, and you can switch it back on in the console), so **check it is running before you send anything**.
Source: https://aiven.io/docs/products/mysql/concepts/mysql-free-tier

1. Create an account at aiven.io.
2. Click **Create service**, choose **MySQL**, choose the **Free** plan, and pick the region closest to you if it lets you choose.
3. Wait until the service shows **Running**.
4. Open the service **Overview** page. Note these four values: **Host**, **Port**, **User** (normally `avnadmin`) and **Password**. The port is not 3306.

## Part 2: Create the tables

Run this in the project folder on your Mac (it uses the `mysql` program that came with your MySQL install). Replace HOST and PORT:

```
mysql -h HOST -P PORT -u avnadmin -p --ssl-mode=REQUIRED < tasty_bite.sql
```

Type the password when asked. Cloud databases only accept encrypted connections, so `--ssl-mode=REQUIRED` is needed.

- **Error at the very end about the trigger?** That is fine. Some hosts do not allow triggers. The tables are already created, and the app creates the default sections (Snacks and Beverages) itself.
- **"Access denied" when creating the database?** Create a database called `tasty_bite` in the Aiven console (Databases tab) and run the command again. Or use the host's own database name instead (usually `defaultdb`):
  ```
  sed '/^CREATE DATABASE/,/^USE tasty_bite;/d' tasty_bite.sql | mysql -h HOST -P PORT -u avnadmin -p --ssl-mode=REQUIRED defaultdb
  ```
  In that case use `defaultdb` instead of `tasty_bite` in the address in Part 3.

Check it worked:
```
mysql -h HOST -P PORT -u avnadmin -p --ssl-mode=REQUIRED -e "USE tasty_bite; SHOW TABLES;"
```
You should see 7 tables.

## Part 3: Point the app at it and create the demo data

1. Copy `db.properties.cloud.example` to `db.properties` in your project folder and fill it in:
   ```
   db.url=jdbc:mysql://HOST:PORT/tasty_bite?sslMode=REQUIRED
   db.user=avnadmin
   db.password=YOUR_PASSWORD
   ```
   Build this address by hand. Do not paste the "Service URI" from the console (it is not a JDBC address), and write `sslMode` exactly like that.
2. Run the app from Eclipse. If the Welcome screen opens, it is connected.
3. Create the demo data the teacher will see: register a manager called `demo_manager` with a restaurant name, add a few menu items, register a customer called `demo_customer`, and place a couple of orders.
4. Write the logins in a small text file called `DEMO-ACCOUNTS.txt`.

**Photos:** photos are saved on the computer where they were added, not in the database. If you added photos, copy your `menu_images` folder into the delivery package (Part 5) so your teacher sees them.

## Part 4: Create a separate login for your teacher (recommended)

The delivery zip will contain a database password, so do not put the admin password in it. Create a restricted user. Run this as the admin:

```
mysql -h HOST -P PORT -u avnadmin -p --ssl-mode=REQUIRED -e "CREATE USER 'teacher_app'@'%' IDENTIFIED BY 'choose-a-long-random-password'; GRANT SELECT, INSERT, UPDATE ON tasty_bite.* TO 'teacher_app'@'%';"
```

The app only reads, adds and updates rows, so these three rights are enough. I ran the app's checks as a user with exactly these rights and they all passed, and that user could not drop a table. Use `teacher_app` and its password in the teacher's `db.properties`.

If Aiven refuses the command, create the user in the console instead, or skip this part and delete the whole service after grading.

## Part 5: Build the .exe on GitHub

1. Make sure these are in your repository (they are in the new project zip): `src/`, `resources/`, `tasty_bite.sql`, `reset_data.sql`, and the file `.github/workflows/build-windows.yml`. Developer 1 commits the workflow file.
2. On GitHub open **Actions**, choose **Build Windows app**, and click **Run workflow**. It takes a few minutes.
3. Open the finished run and download **TastyBite-windows** from the Artifacts section. Unzip it. Inside is the `TastyBite` folder with `TastyBite.exe`.

If a step fails, open the failed run, copy the red error text and send it to me.

## Part 6: Assemble the package for your teacher

Put these into the unzipped `TastyBite` folder, **next to `TastyBite.exe`**:

1. `db.properties` with the teacher's settings:
   ```
   db.url=jdbc:mysql://HOST:PORT/tasty_bite?sslMode=REQUIRED
   db.user=teacher_app
   db.password=THE_TEACHER_PASSWORD
   ```
2. `DEMO-ACCOUNTS.txt`.
3. Your `menu_images` folder, if you added photos.

Zip the whole `TastyBite` folder as `TastyBite-for-Windows.zip`.

The app finds `db.properties` next to the `.exe` however it is started. **If you can, test the zip on a Windows PC** (or ask a friend) before sending it. Check that it opens, you can log in with a demo account, and an order can be placed.

## Part 7: Upload to Google Drive and share it

1. Upload `TastyBite-for-Windows.zip` to Google Drive.
2. Click **Share**, add your teacher's email address, and set the role to **Viewer**. Keep the general access as **Restricted**. The zip contains a database password, so it should not be open to everyone with the link.
3. Click **Copy link**.

## Part 8: Put the link in GitHub

In `README.md`, replace `DRIVE-LINK-HERE` in the "Quick start" section with your Drive link. **Never** commit `db.properties` or any password to GitHub. The `.gitignore` already keeps `db.properties` out.

## Before you send it: checklist

- [ ] The Aiven service shows **Running**
- [ ] The zip opens and works on a computer that is not yours
- [ ] The teacher's database user is the restricted one, not the admin
- [ ] The Drive file is shared with your teacher's email
- [ ] The link in the GitHub `README.md` works
- [ ] You know your teacher's network may block the unusual database port. If the app cannot connect there, try a phone hotspot
- [ ] After grading: delete the Aiven service or change the password

## If something goes wrong

| Problem | What to do |
|---|---|
| Popup "Could not connect to the database" | Aiven service switched off (switch it on), no internet, port blocked by the network, or a wrong value in `db.properties` |
| "Communications link failure" | Same as above: check the service is Running and the host and port are right |
| "Access denied for user" | Wrong user or password in `db.properties` |
| Errors mentioning SSL | The address must end with `?sslMode=REQUIRED`. Do not use `useSSL=false` |
| "Unknown database" | The tables were not created, or the database name in the address is wrong. Check Part 2 |
| The app opens but data is missing | `db.properties` points at a different database than the one you filled with demo data |
| Photos show as coloured letters | The `menu_images` folder was not copied next to `TastyBite.exe` |
| Windows says it protected your PC | Click **More info**, then **Run anyway** (the app is not code-signed) |
| Actions run fails | Copy the red error text and send it to me |

## What changed in the app for this

- **Settings lookup:** the app now also looks for `db.properties` next to `TastyBite.exe`, so a settings file shipped in the download is found.
- **Faster on an online database:** the app reuses database connections instead of opening a new one for every action, which matters when the database is far away.
- **No freezing:** logging in and registering now run in the background, so the window does not freeze while waiting for the database.
- **Default sections:** the app creates Snacks and Beverages itself, so it works even on a host that does not allow database triggers.
