-- Ngay thuc te tra sach
ALTER TABLE dbo.borrow_records ADD returned_date DATE NULL;
GO

-- Chong trung username ngay tu database, ke ca khi hai request den cung luc
CREATE UNIQUE INDEX UX_users_username ON dbo.users(username);

-- Moi nguoi chi co mot phieu dang muon cho moi cuon sach
CREATE UNIQUE INDEX UX_borrow_active ON dbo.borrow_records(user_id, book_id)
    WHERE status IN ('BORROWING', 'OVERDUE');

-- Tang toc cac truy van thuong dung
CREATE INDEX IX_borrow_user ON dbo.borrow_records(user_id);
CREATE INDEX IX_borrow_status_date ON dbo.borrow_records(status, return_date);
CREATE INDEX IX_books_author ON dbo.books(author_id);
CREATE INDEX IX_refresh_tokens_user ON dbo.refresh_tokens(user_id);

-- Database moi tao tu V1 chua co UNIQUE tren roles.name. Database cu da co thi bo qua.
IF NOT EXISTS (
    SELECT 1
    FROM sys.indexes i
    JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id
    JOIN sys.columns c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
    WHERE i.object_id = OBJECT_ID('dbo.roles') AND i.is_unique = 1 AND c.name = 'name'
)
BEGIN
    DELETE r
    FROM dbo.roles r
    WHERE r.id NOT IN (SELECT MIN(id) FROM dbo.roles GROUP BY name)
      AND r.id NOT IN (SELECT role_id FROM dbo.user_roles);

    CREATE UNIQUE INDEX UX_roles_name ON dbo.roles(name);
END;
