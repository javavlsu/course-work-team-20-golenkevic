CREATE DATABASE IF NOT EXISTS marketplacedb;
USE marketplacedb;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    bio VARCHAR(255),
    profile_pic LONGBLOB,
    registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    role ENUM('buyer','seller','moderator','admin') NOT NULL DEFAULT 'buyer',
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE categories (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    product_condition ENUM('NEW','USED','VINTAGE') NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED','REMOVED') NOT NULL DEFAULT 'PENDING',
    image_url VARCHAR(500),
    image_data LONGBLOB,
    fk_category INT,
    fk_seller INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fk_category) REFERENCES categories(id),
    FOREIGN KEY (fk_seller) REFERENCES users(id)
);

CREATE TABLE orders (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fk_buyer INT NOT NULL,
    status ENUM('NEW','CONFIRMED','IN_DELIVERY','DELIVERED','COMPLETED','CANCELLED') NOT NULL DEFAULT 'NEW',
    delivery_address VARCHAR(500),
    contact_name VARCHAR(100),
    contact_phone VARCHAR(20),
    total_amount DECIMAL(10,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fk_buyer) REFERENCES users(id)
);

CREATE TABLE order_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fk_order INT NOT NULL,
    fk_product INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (fk_order) REFERENCES orders(id),
    FOREIGN KEY (fk_product) REFERENCES products(id)
);

CREATE TABLE cart_items (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fk_user INT NOT NULL,
    fk_product INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    UNIQUE KEY uk_cart (fk_user, fk_product),
    FOREIGN KEY (fk_user) REFERENCES users(id),
    FOREIGN KEY (fk_product) REFERENCES products(id)
);

CREATE TABLE favorites (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fk_user INT NOT NULL,
    fk_product INT NOT NULL,
    UNIQUE KEY uk_fav (fk_user, fk_product),
    FOREIGN KEY (fk_user) REFERENCES users(id),
    FOREIGN KEY (fk_product) REFERENCES products(id)
);

CREATE TABLE reviews (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fk_product INT NOT NULL,
    fk_user INT NOT NULL,
    rating TINYINT UNSIGNED NOT NULL,
    text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_review (fk_user, fk_product),
    FOREIGN KEY (fk_product) REFERENCES products(id),
    FOREIGN KEY (fk_user) REFERENCES users(id)
);

-- Начальные данные
INSERT INTO categories (name) VALUES ('Электроника'), ('Одежда'), ('Обувь'), ('Аксессуары'), ('Книги'), ('Мебель'), ('Спорт'), ('Другое');

INSERT INTO users (username, email, password_hash, role) VALUES
('admin', 'admin@marketplace.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin');
-- пароль: password123
