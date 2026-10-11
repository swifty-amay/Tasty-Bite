-- =====================================================================
--  Tasty Bite - MySQL schema
--  Tables: managers, users, restaurants, categories, menu_items,
--          orders, order_items
--  Run in MySQL 8.x (Workbench or CLI):  mysql -u root -p < tasty_bite.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS tasty_bite
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE tasty_bite;

-- Drop in reverse dependency order so the script can be re-run safely
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS menu_items;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS restaurants;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS managers;

-- ---------------------------------------------------------------------
-- 1. managers
-- ---------------------------------------------------------------------
CREATE TABLE managers (
    manager_id     INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_managers_username UNIQUE (username)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 2. users (customers)
--    phone is optional and not used by any screen yet
-- ---------------------------------------------------------------------
CREATE TABLE users (
    user_id        INT AUTO_INCREMENT PRIMARY KEY,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    phone          VARCHAR(15)  NULL,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_users_username UNIQUE (username)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 3. restaurants (one manager <-> one restaurant)
-- ---------------------------------------------------------------------
CREATE TABLE restaurants (
    restaurant_id  INT AUTO_INCREMENT PRIMARY KEY,
    manager_id     INT          NOT NULL,
    name           VARCHAR(100) NOT NULL,
    CONSTRAINT uq_restaurants_manager UNIQUE (manager_id),
    CONSTRAINT uq_restaurants_name    UNIQUE (name),
    CONSTRAINT fk_restaurants_manager
        FOREIGN KEY (manager_id) REFERENCES managers (manager_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 4. categories (Snacks, Beverages, ... per restaurant)
-- ---------------------------------------------------------------------
CREATE TABLE categories (
    category_id    INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id  INT         NOT NULL,
    name           VARCHAR(50) NOT NULL,
    CONSTRAINT uq_categories_restaurant_name UNIQUE (restaurant_id, name),
    CONSTRAINT fk_categories_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 5. menu_items (soft delete via is_deleted)
-- ---------------------------------------------------------------------
CREATE TABLE menu_items (
    item_id        INT AUTO_INCREMENT PRIMARY KEY,
    restaurant_id  INT           NOT NULL,
    category_id    INT           NOT NULL,
    name           VARCHAR(100)  NOT NULL,
    price          DECIMAL(8,2)  NOT NULL,
    image_path     VARCHAR(255)  NULL,
    is_deleted     BOOLEAN       NOT NULL DEFAULT FALSE,
    CONSTRAINT chk_menu_items_price CHECK (price >= 0),
    CONSTRAINT fk_menu_items_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_menu_items_category
        FOREIGN KEY (category_id) REFERENCES categories (category_id)
        ON DELETE RESTRICT,
    INDEX idx_menu_restaurant_category (restaurant_id, category_id, is_deleted)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 6. orders
-- ---------------------------------------------------------------------
CREATE TABLE orders (
    order_id       INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT           NOT NULL,
    restaurant_id  INT           NOT NULL,
    subtotal       DECIMAL(10,2) NOT NULL,
    tax            DECIMAL(10,2) NOT NULL,
    total          DECIMAL(10,2) NOT NULL,
    status         ENUM('PLACED','PREPARING','COMPLETED','CANCELLED') NOT NULL DEFAULT 'PLACED',
    placed_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_orders_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurants (restaurant_id)
        ON DELETE RESTRICT,
    INDEX idx_orders_restaurant_status (restaurant_id, status),
    INDEX idx_orders_user (user_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- 7. order_items (name and price copied at order time)
-- ---------------------------------------------------------------------
CREATE TABLE order_items (
    order_item_id  INT AUTO_INCREMENT PRIMARY KEY,
    order_id       INT          NOT NULL,
    item_id        INT          NOT NULL,
    item_name      VARCHAR(100) NOT NULL,
    unit_price     DECIMAL(8,2) NOT NULL,
    quantity       INT          NOT NULL,
    CONSTRAINT chk_order_items_qty CHECK (quantity > 0),
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES orders (order_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_order_items_item
        FOREIGN KEY (item_id) REFERENCES menu_items (item_id)
        ON DELETE RESTRICT,
    INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB;

-- ---------------------------------------------------------------------
-- Default categories: every new restaurant automatically gets
-- "Snacks" and "Beverages" (the default headings from the sketch).
-- ---------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_restaurants_default_categories;

DELIMITER $$
CREATE TRIGGER trg_restaurants_default_categories
AFTER INSERT ON restaurants
FOR EACH ROW
BEGIN
    INSERT INTO categories (restaurant_id, name) VALUES
        (NEW.restaurant_id, 'Snacks'),
        (NEW.restaurant_id, 'Beverages');
END$$
DELIMITER ;

-- =====================================================================
-- Optional quick check after running the script:
--   SHOW TABLES;
--   INSERT INTO managers (username, password_hash) VALUES ('demo', 'x');
--   INSERT INTO restaurants (manager_id, name) VALUES (LAST_INSERT_ID(), 'Demo Cafe');
--   SELECT * FROM categories;   -- should show Snacks and Beverages
-- =====================================================================
