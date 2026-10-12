-- =====================================================================
--  Tasty Bite - migration 001: add the "Preparing" order status
--
--  Run this ONCE on a database that was created with the earlier
--  tasty_bite.sql. It keeps all existing data (orders, menus, users).
--  New installs do not need it: tasty_bite.sql already includes it.
--
--  mysql -u root -p < migration_001_add_preparing.sql
-- =====================================================================

USE tasty_bite;

ALTER TABLE orders
    MODIFY status ENUM('PLACED','PREPARING','COMPLETED','CANCELLED')
    NOT NULL DEFAULT 'PLACED';
