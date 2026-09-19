-- ============================================
-- 1. ROLES
-- ============================================
INSERT INTO dbo.roles(name)
VALUES
    ('USER'),
    ('ADMIN'),
    ('STAFF');


-- ============================================
-- 2. AUTHORS
-- ============================================
INSERT INTO dbo.authors(name, country)
VALUES
    ('Nguyễn Nhật Ánh', 'Vietnam'),
    ('Nam Cao', 'Vietnam'),
    ('J.K. Rowling', 'United Kingdom'),
    ('George Orwell', 'United Kingdom'),
    ('Haruki Murakami', 'Japan');


-- ============================================
-- 3. BOOKS
-- ============================================

INSERT INTO dbo.books(title, price, quantity, author_id)
SELECT
    'Mắt Biếc',
    85000,
    20,
    id
FROM dbo.authors
WHERE name = 'Nguyễn Nhật Ánh';

INSERT INTO dbo.books(title, price, quantity, author_id)
SELECT
    'Tôi Thấy Hoa Vàng Trên Cỏ Xanh',
    95000,
    15,
    id
FROM dbo.authors
WHERE name = 'Nguyễn Nhật Ánh';

INSERT INTO dbo.books(title, price, quantity, author_id)
SELECT
    'Chí Phèo',
    60000,
    30,
    id
FROM dbo.authors
WHERE name = 'Nam Cao';

INSERT INTO dbo.books(title, price, quantity, author_id)
SELECT
    'Harry Potter and the Philosopher''s Stone',
    180000,
    12,
    id
FROM dbo.authors
WHERE name = 'J.K. Rowling';

INSERT INTO dbo.books(title, price, quantity, author_id)
SELECT
    '1984',
    120000,
    18,
    id
FROM dbo.authors
WHERE name = 'George Orwell';

INSERT INTO dbo.books(title, price, quantity, author_id)
SELECT
    'Norwegian Wood',
    135000,
    10,
    id
FROM dbo.authors
WHERE name = 'Haruki Murakami';