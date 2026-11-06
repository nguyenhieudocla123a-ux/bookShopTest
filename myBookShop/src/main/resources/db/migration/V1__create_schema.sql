-- Tao toan bo bang khi chay tren database trong.
-- Database da co du lieu duoc Flyway baseline o version 1 nen se bo qua file nay.
-- roles.name chua UNIQUE vi V2 va V4 chen trung mot gia tri; V6 se loai ban trung roi them UNIQUE.
-- refresh_tokens.created_at chua co DEFAULT vi V5 can ALTER cot nay truoc khi them DEFAULT.

CREATE TABLE dbo.users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    username NVARCHAR(255) NOT NULL,
    email NVARCHAR(255) NOT NULL,
    password NVARCHAR(255) NOT NULL,
    phone NVARCHAR(255) NULL,
    enabled BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE dbo.roles (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL
);

CREATE TABLE dbo.authors (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    country NVARCHAR(255) NULL
);

CREATE TABLE dbo.books (
    id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    price NUMERIC(38,2) NULL,
    quantity INT NOT NULL,
    author_id INT NOT NULL,
    CONSTRAINT FK_books_author FOREIGN KEY (author_id) REFERENCES dbo.authors(id)
);

CREATE TABLE dbo.user_roles (
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT FK_user_roles_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_user_roles_role FOREIGN KEY (role_id) REFERENCES dbo.roles(id)
);

CREATE TABLE dbo.refresh_tokens (
    id INT IDENTITY(1,1) PRIMARY KEY,
    token NVARCHAR(255) NOT NULL UNIQUE,
    expires_at DATETIMEOFFSET(6) NULL,
    revoked BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NULL,
    user_id INT NOT NULL,
    CONSTRAINT FK_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
);

CREATE TABLE dbo.borrow_records (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE NOT NULL,
    status VARCHAR(255) NULL,
    CONSTRAINT FK_borrow_records_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_borrow_records_book FOREIGN KEY (book_id) REFERENCES dbo.books(id)
);
