-- V3 viet chuoi tieng Viet khong co tien to N'...' nen SQL Server doi ky tu ngoai bang ma thanh dau ?.
-- Chi sua dung cac gia tri bi hong, khong dong vao du lieu nguoi dung da doi ten.
UPDATE dbo.authors SET name = N'Nguyễn Nhật Ánh' WHERE name = N'Nguy?n Nh?t Ánh';
UPDATE dbo.books SET title = N'Mắt Biếc' WHERE title = N'M?t Bi?c';
UPDATE dbo.books SET title = N'Tôi Thấy Hoa Vàng Trên Cỏ Xanh' WHERE title = N'Tôi Th?y Hoa Vàng Trên C? Xanh';
