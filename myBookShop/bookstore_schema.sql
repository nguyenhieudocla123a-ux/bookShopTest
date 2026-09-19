IF DB_ID('bookstore') IS NULL
BEGIN
    CREATE DATABASE bookstore;
END;

USE bookstore;

DROP TABLE IF EXISTS dbo.borrow_records;
DROP TABLE IF EXISTS dbo.user_roles;
DROP TABLE IF EXISTS dbo.refresh_tokens;
DROP TABLE IF EXISTS dbo.books;
DROP TABLE IF EXISTS dbo.authors;
DROP TABLE IF EXISTS dbo.roles;
DROP TABLE IF EXISTS dbo.users;

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
    name NVARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE dbo.authors (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,
    country NVARCHAR(255) NULL
);

CREATE TABLE dbo.books (
    id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
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
    expires_at DATETIME2 NOT NULL,
    revoked BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    user_id INT NOT NULL,
    CONSTRAINT FK_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES dbo.users(id)
);

CREATE TABLE dbo.borrow_records (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATE NOT NULL,
    return_date DATE NOT NULL,
    status NVARCHAR(50) NOT NULL,
    CONSTRAINT FK_borrow_records_user FOREIGN KEY (user_id) REFERENCES dbo.users(id),
    CONSTRAINT FK_borrow_records_book FOREIGN KEY (book_id) REFERENCES dbo.books(id)
);

INSERT INTO dbo.roles(name) VALUES ('USER'), ('ADMIN'), ('STAFF');
