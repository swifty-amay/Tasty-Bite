-- =====================================================================
--  Tasty Bite - empty all data, keep the tables
--
--  Use before final submission. Removes every user, manager, restaurant,
--  menu item and order. The tables, the default-section trigger and the
--  database itself stay, so the app works straight away: a new manager
--  registers, and Snacks and Beverages are created automatically.
--  Ids start again from 1.
--
--  Also delete the photos in the menu_images folder next to the app.
--  Take a backup first if you may need the data again:
--     mysqldump -u root -p tasty_bite > tasty_bite_backup.sql
--
--  Run:  mysql -u root -p < reset_data.sql
-- =====================================================================

USE tasty_bite;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE order_items;
TRUNCATE TABLE orders;
TRUNCATE TABLE menu_items;
TRUNCATE TABLE categories;
TRUNCATE TABLE restaurants;
TRUNCATE TABLE users;
TRUNCATE TABLE managers;
SET FOREIGN_KEY_CHECKS = 1;
