-- =============================================================================
-- OnlineOrder — V1__initial_schema.sql
-- 初始数据库结构
--
-- 执行时机：仅首次运行 Flyway 时执行一次
-- 注意：此脚本不会重复执行，请通过新的迁移脚本修改表结构
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. customers 表：注册用户信息
-- -----------------------------------------------------------------------------
CREATE TABLE customers (
    id         BIGSERIAL PRIMARY KEY NOT NULL,
    email      VARCHAR(255) UNIQUE   NOT NULL,
    enabled    BOOLEAN DEFAULT TRUE  NOT NULL,
    password   VARCHAR(255)          NOT NULL,
    first_name VARCHAR(50),
    last_name  VARCHAR(50)
);

-- -----------------------------------------------------------------------------
-- 2. authorities 表：用户角色（RBAC）
-- -----------------------------------------------------------------------------
CREATE TABLE authorities (
    id        BIGSERIAL PRIMARY KEY NOT NULL,
    email     VARCHAR(255)           NOT NULL,
    authority VARCHAR(50)            NOT NULL,
    CONSTRAINT fk_auth_customer FOREIGN KEY (email) REFERENCES customers (email) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 3. carts 表：购物车（每个用户一个购物车）
-- -----------------------------------------------------------------------------
CREATE TABLE carts (
    id          BIGSERIAL PRIMARY KEY NOT NULL,
    customer_id INTEGER UNIQUE        NOT NULL,
    total_price NUMERIC(19,4)       NOT NULL DEFAULT 0,
    CONSTRAINT fk_cart_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 4. restaurants 表：餐厅/商家
-- -----------------------------------------------------------------------------
CREATE TABLE restaurants (
    id        BIGSERIAL PRIMARY KEY NOT NULL,
    name      VARCHAR(255)          NOT NULL,
    address   VARCHAR(500),
    phone     VARCHAR(50),
    image_url TEXT
);

-- -----------------------------------------------------------------------------
-- 5. menu_items 表：菜品
-- -----------------------------------------------------------------------------
CREATE TABLE menu_items (
    id            BIGSERIAL PRIMARY KEY NOT NULL,
    restaurant_id INTEGER              NOT NULL,
    name          VARCHAR(255)          NOT NULL,
    price         NUMERIC(19,4)       NOT NULL,
    description   TEXT,
    image_url     TEXT,
    CONSTRAINT fk_menu_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurants (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 6. cart_items 表：购物车中的商品行
-- -----------------------------------------------------------------------------
CREATE TABLE cart_items (
    id           BIGSERIAL PRIMARY KEY NOT NULL,
    menu_item_id INTEGER              NOT NULL,
    cart_id      INTEGER              NOT NULL,
    price        NUMERIC(19,4)       NOT NULL,
    quantity     INTEGER              NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_ci_cart FOREIGN KEY (cart_id)      REFERENCES carts (id)       ON DELETE CASCADE,
    CONSTRAINT fk_ci_menu FOREIGN KEY (menu_item_id) REFERENCES menu_items (id) ON DELETE CASCADE,
    -- 同一购物车中同一菜品只允许一行，通过 quantity 表示数量
    CONSTRAINT uq_cart_menu UNIQUE (cart_id, menu_item_id)
);

-- -----------------------------------------------------------------------------
-- 7. orders 表：订单记录
-- -----------------------------------------------------------------------------
CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY NOT NULL,
    customer_id  INTEGER              NOT NULL,
    status       VARCHAR(50)         NOT NULL DEFAULT 'PENDING',
    total_price  NUMERIC(19,4)      NOT NULL,
    created_at   TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_order_customer FOREIGN KEY (customer_id) REFERENCES customers (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 8. order_details 表：订单明细快照
-- -----------------------------------------------------------------------------
CREATE TABLE order_details (
    id                    BIGSERIAL PRIMARY KEY NOT NULL,
    order_id              INTEGER              NOT NULL,
    menu_item_id          INTEGER,
    menu_item_name        VARCHAR(255)         NOT NULL,
    menu_item_description TEXT,
    menu_item_image_url   TEXT,
    price                 NUMERIC(19,4)       NOT NULL,
    quantity              INTEGER              NOT NULL CHECK (quantity > 0),
    CONSTRAINT fk_od_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE
);

-- -----------------------------------------------------------------------------
-- 索引：优化查询性能
-- -----------------------------------------------------------------------------
CREATE INDEX idx_authorities_email        ON authorities(email);
CREATE INDEX idx_orders_customer_id       ON orders(customer_id);
CREATE INDEX idx_orders_created_at        ON orders(created_at DESC);
CREATE INDEX idx_orders_status            ON orders(status);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_order_details_order_id   ON order_details(order_id);
CREATE INDEX idx_menu_items_restaurant_id ON menu_items(restaurant_id);

-- -----------------------------------------------------------------------------
-- 示例数据：Burger King
-- -----------------------------------------------------------------------------
INSERT INTO restaurants (name, address, image_url, phone) VALUES
('Burger King', '773 N Mathilda Ave, Sunnyvale, CA 94085',
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/store%2Fheader%2F10171.png',
 '(408) 736-0101');

INSERT INTO menu_items (restaurant_id, name, description, price, image_url) VALUES
(1, 'Chicken Fries - 9 Pc',
 'Made with white meat chicken, our Chicken Fries are coated in a light crispy breading seasoned with savory spices and herbs.',
 4.89,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f439436f-c5ab-47af-bac4-7b73ab60a24b-retina-large.jpg'),
(1, 'Whopper Meal',
 'Our Whopper Sandwich is a 1/4 lb* of savory flame-grilled beef topped with juicy tomatoes, fresh lettuce, creamy mayonnaise, ketchup, crunchy pickles, and sliced white onions on a soft sesame seed bun.',
 10.59,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=300,format=auto,quality=50/https://cdn.doordash.com/media/photos/f878a689-618b-4c70-a00f-e7b1f320adc9-retina-large.jpg'),
(1, 'Impossible Whopper',
 'Our Impossible Whopper Sandwich features a savory flame-grilled patty made from plants topped with juicy tomatoes, fresh lettuce, creamy mayonnaise, ketchup, crunchy pickles, and sliced white onions on a soft sesame seed bun.',
 7.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/5c306a5f-fdd2-41d2-a660-9762aaa8eee8-retina-large.jpg'),
(1, 'HERSHEYS Sundae Pie',
 'Say hello to our HERSHEYS Sundae Pie. One part crunchy chocolate crust and one part chocolate creme filling, garnished with a delicious topping and real HERSHEYS Chocolate Chips.',
 3.09,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/80b1670d-e9c0-4886-5b7-1ad48edd24ca-retina-large.jpg'),
(1, 'Whopper',
 'Our Whopper Sandwich is a 1/4 lb* of savory flame-grilled beef topped with juicy tomatoes, fresh lettuce, creamy mayonnaise, ketchup, crunchy pickles, and sliced white onions on a soft sesame seed bun.',
 6.39,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/9b3d7985-e457-43b3-938d-5184f48c2687-retina-large-jpeg'),
(1, 'Double Whopper Meal',
 'Our Double Whopper Sandwich is a pairing of two 1/4 lb* savory flame-grilled beef patties topped with juicy tomatoes, fresh lettuce, creamy mayonnaise, ketchup, crunchy pickles, and sliced white onions on a soft sesame seed bun.',
 11.69,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/45addf4a-e8a8-47cb-a705-cce1d10ce86d-retina-large.jpg'),
(1, 'Spicy Crispy Chicken Sandwich',
 'Our Spicy Crispy Chicken Sandwich features a crispy chicken patty with spicy sauce, lettuce, and pickles on a toasted bun.',
 6.09,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/31dd68c2-06ec-42ad-bcd4-da7bd3425437-retina-large-jpeg'),
(1, 'Original Chicken Sandwich',
 'Our Original Chicken Sandwich is lightly breaded and topped with a simple combination of shredded lettuce and creamy mayonnaise on a sesame seed bun.',
 6.09,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/3e437f54-fa4e-4e9d-bf80-8a1e5b120f32-retina-large-jpeg'),
(1, 'Bacon King Sandwich Meal',
 'Our Bacon King Sandwich features two 1/4 lb* savory flame-grilled beef patties, topped with a hearty portion of thick-cut smoked bacon, melted American cheese and topped with ketchup and creamy mayonnaise all on a soft sesame seed bun.',
 12.19,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/adb96c32-3c5b-4375-ba92-b30767d2513d-retina-large.jpg'),
(1, 'Classic OREO Shake',
 'Cool down with our creamy hand spun OREO Shake.',
 3.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/c3ad483f-bad7-44f1-96af-4c3dcfc63c6d-retina-large.jpg');

-- -----------------------------------------------------------------------------
-- 示例数据：SGD Tofu House
-- -----------------------------------------------------------------------------
INSERT INTO restaurants (name, address, image_url, phone) VALUES
('SGD Tofu House', '955 W El Camino Real, Sunnyvale, CA 94087',
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/b7055ca9-3caf-4d9d-9c99-04be1e36dbbf-retina-large-jpeg',
 '(408) 720-0820');

INSERT INTO menu_items (restaurant_id, name, description, price, image_url) VALUES
(2, 'Original Soft Tofu',
 'Tofu boiled with your choice of meat and mushrooms. Served with your choice of side and an assortment of kimchi dishes.',
 17.06,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/b7055ca9-3caf-4d9d-9c99-04be1e36dbbf-retina-large-jpeg'),
(2, 'Combination Soft Tofu',
 'Tofu boiled with beef, shrimp, and clams. Served with your choice of side and an assortment of kimchi dishes.',
 17.06,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/37ad1974-1395-4e5c-86ff-fdf120cf8c58-retina-large-jpeg'),
(2, 'Seafood Soft Tofu',
 'Tofu boiled with mussels, shrimp, and clam. Served with your choice of side and an assortment of kimchi dishes.',
 17.06,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/96bc8289-1950-4b4f-823d-12f33349a5fe-retina-large-jpeg'),
(2, 'Seafood Pancake',
 'Squid, clam, imitation crab, and grilled onions fried in batter.',
 20.27,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/0a94b7e9-903d-49b7-937a-7940c8b56ad5-retina-large-jpeg'),
(2, 'Kimchi Soft Tofu',
 'Tofu boiled with kimchi and your choice of meat. Served with your choice of side and an assortment of kimchi dishes.',
 17.06,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/0c062cff-1868-40e1-946d-29d3e46f1541-retina-large-jpeg'),
(2, 'Beef Short Ribs',
 'Beef short ribs served with rice and an assortment of kimchi dishes.',
 29.36,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/6340c369-2485-4d60-afcf-ca9068448d84-retina-large.jpg'),
(2, 'Dumpling Soft Tofu',
 'Tofu boiled with dumplings, rice cake, and beef. Served with your choice of side and an assortment of kimchi dishes.',
 17.06,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/b7055ca9-3caf-4d9d-9c99-04be1e36dbbf-retina-large-jpeg'),
(2, 'Assorted Mushroom Tofu',
 'Tofu boiled with assorted mushrooms. Served with your choice of side and an assortment of kimchi dishes.',
 17.06,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/b7055ca9-3caf-4d9d-9c99-04be1e36dbbf-retina-large-jpeg'),
(2, 'BBQ Beef & Vegetables in Stoneware',
 'Rice, BBQ beef, and vegetables served in stoneware with an assortment of kimchi dishes.',
 20.27,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/9844dd4e-3c74-4942-8f90-2b3f4be25049-retina-large-jpeg'),
(2, 'Ham & Cheese Soft Tofu',
 'Tofu boiled with ham and cheese. Served with your choice of side and an assortment of kimchi dishes.',
 17.06,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/9c6b2a1c-1e2c-4d80-a111-2bebbcadd64c-retina-large.jpg');

-- -----------------------------------------------------------------------------
-- 示例数据：Fashion Wok
-- -----------------------------------------------------------------------------
INSERT INTO restaurants (name, address, image_url, phone) VALUES
('Fashion Wok', '1287 W El Camino Real, Sunnyvale, CA 94087',
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/5b34852e-d253-461c-8be8-1bb0bc5e39be-retina-large.jpg',
 '(408) 737-3388');

INSERT INTO menu_items (restaurant_id, name, description, price, image_url) VALUES
(3, 'Stir Fried Pork with Pepper',
 'Medium spicy.',
 13.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/5b34852e-d253-461c-8be8-1bb0bc5e39be-retina-large.jpg'),
(3, 'Eggplant with Minced Pork, Garlic, Cilantro',
 'A classic Chinese dish with tender eggplant and savory minced pork.',
 14.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/bf70f262-0c55-41e1-89bc-84c061ae485f-retina-large.jpg'),
(3, 'Stir Fried Cauliflower with Pork',
 'Mild spicy.',
 14.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/cb870c77-ace1-49ec-aa2f-9e18de102242-retina-large.jpg'),
(3, 'Poached Fish Fillets in Sour Soup',
 'Mild spicy.',
 17.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/1acf9c6b-189d-4583-a151-7ef522c283d9-retina-large.jpg'),
(3, 'Stir Fried Beef with Pepper',
 'Very spicy.',
 16.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/7f05859d-5e83-476d-a45a-73a3eb8a94e0-retina-large.jpg'),
(3, 'Stir Fried Shredded Tripe with Wugang Tofu',
 'Medium spicy.',
 19.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/8b2ca9fc-2c1d-4bf2-96ff-d0bd3c415e8d-retina-large.jpg'),
(3, 'Poached Sliced Beef in Hot Chili Oil',
 'Very spicy.',
 17.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/89ad8679-346e-41d8-b98f-3501fff4b277-retina-large.jpg'),
(3, 'Fried Rice',
 'With chopped broccoli, peas, carrots, bok choy, egg.',
 9.50,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/ec06c431-9426-4971-a129-920440e1c9ce-retina-large.jpg'),
(3, 'Smashed Green Pepper, Chinese Eggplant & Preserved Egg',
 'Very spicy.',
 11.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/2fe1b87f-d41f-4fa4-8cae-5f2ee5bb97e4-retina-large.jpg'),
(3, 'Stir Fried A-Choy with Minced Garlic',
 'A light and aromatic stir-fried green vegetable.',
 10.99,
 'https://img.cdn4dd.com/cdn-cgi/image/fit=contain,width=1920,format=auto,quality=50/https://cdn.doordash.com/media/photos/a307e73d-dd12-4841-be14-6f5825a64c59-retina-large.jpg');
