-- Скрипт заполнения базы маркетплейса тестовыми данными.
-- Запускать ПОСЛЕ старта приложения (бренды/категории/админ создаются DataInitializer'ом).
-- Пользователей (~100) создаём через Postman runner на /auth/register.

DELIMITER $$

DROP PROCEDURE IF EXISTS GenerateMockData$$

CREATE PROCEDURE GenerateMockData()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE max_user INT;
    DECLARE max_product INT;
    DECLARE rand_cat INT;
    DECLARE rand_brand INT;
    DECLARE rand_seller INT;

    SELECT COUNT(*) INTO max_user FROM users;
    SELECT COUNT(*) INTO max_product FROM products;

    -- 1. Создаём 200 товаров со случайной категорией / брендом / продавцом
    SET i = 1;
    WHILE i <= 200 DO
        SELECT id INTO rand_cat    FROM categories ORDER BY RAND() LIMIT 1;
        SELECT id INTO rand_brand  FROM brands     ORDER BY RAND() LIMIT 1;
        SELECT id INTO rand_seller FROM users      ORDER BY RAND() LIMIT 1;
        INSERT INTO products (title, description, price, product_condition, status,
                              fk_category, fk_brand, fk_seller,
                              gender, season, color, material, size, created_at)
        VALUES (
            CONCAT('Товар №', i),
            CONCAT('Автоматически сгенерированное описание товара ', i),
            ROUND(500 + RAND() * 30000, 2),
            ELT(FLOOR(1 + RAND() * 3), 'NEW', 'USED', 'VINTAGE'),
            'APPROVED',
            rand_cat,
            rand_brand,
            rand_seller,
            ELT(FLOOR(1 + RAND() * 3), 'MALE', 'FEMALE', 'UNISEX'),
            ELT(FLOOR(1 + RAND() * 4), 'SUMMER', 'WINTER', 'DEMI', 'UNIVERSAL'),
            ELT(FLOOR(1 + RAND() * 5), 'Чёрный', 'Белый', 'Серый', 'Бежевый', 'Коричневый'),
            ELT(FLOOR(1 + RAND() * 4), 'Кожа', 'Хлопок', 'Деним', 'Текстиль'),
            ELT(FLOOR(1 + RAND() * 5), 'S', 'M', 'L', 'XL', 'F'),
            NOW() - INTERVAL FLOOR(RAND() * 90) DAY
        );
        SET i = i + 1;
    END WHILE;

    SELECT COUNT(*) INTO max_product FROM products;

    -- 2. ~1500 отзывов (UNIQUE(fk_user, fk_product) — берём с запасом)
    SET i = 1;
    WHILE i <= 2500 DO
        INSERT IGNORE INTO reviews (fk_product, fk_user, rating, text, created_at)
        SELECT p.id, u.id,
               FLOOR(1 + RAND() * 5),
               CONCAT('Тестовый отзыв номер ', i, ', автоматически сгенерирован.'),
               NOW() - INTERVAL FLOOR(RAND() * 60) DAY
        FROM products p, users u
        ORDER BY RAND() LIMIT 1;
        SET i = i + 1;
    END WHILE;

    -- 3. ~1500 записей в избранном (UNIQUE(fk_user, fk_product) — INSERT IGNORE)
    SET i = 1;
    WHILE i <= 2500 DO
        INSERT IGNORE INTO favorites (fk_user, fk_product)
        SELECT u.id, p.id FROM users u, products p ORDER BY RAND() LIMIT 1;
        SET i = i + 1;
    END WHILE;

    -- 4. ~300 заказов
    SET i = 1;
    WHILE i <= 300 DO
        INSERT INTO orders (fk_buyer, status, delivery_address, contact_name, contact_phone,
                            total_amount, created_at)
        SELECT u.id,
               ELT(FLOOR(1 + RAND() * 6), 'NEW', 'CONFIRMED', 'IN_DELIVERY',
                                           'DELIVERED', 'COMPLETED', 'CANCELLED'),
               CONCAT('г. Владимир, ул. Тестовая, д. ', FLOOR(1 + RAND() * 100)),
               CONCAT('Покупатель ', i),
               CONCAT('+7900', LPAD(FLOOR(RAND() * 9999999), 7, '0')),
               0,
               NOW() - INTERVAL FLOOR(RAND() * 60) DAY
        FROM users u ORDER BY RAND() LIMIT 1;
        SET i = i + 1;
    END WHILE;

    -- 5. ~700 позиций в заказах + пересчёт total_amount
    SET i = 1;
    WHILE i <= 700 DO
        INSERT INTO order_items (fk_order, fk_product, price, quantity)
        SELECT o.id, p.id, p.price, FLOOR(1 + RAND() * 3)
        FROM orders o, products p
        ORDER BY RAND()
        LIMIT 1;
        SET i = i + 1;
    END WHILE;

    UPDATE orders o
    SET total_amount = COALESCE(
        (SELECT SUM(oi.price * oi.quantity) FROM order_items oi WHERE oi.fk_order = o.id),
        0
    );
END$$

DELIMITER ;

CALL GenerateMockData();
