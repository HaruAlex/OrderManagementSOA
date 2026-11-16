
-- =============================================
-- DATABASE: ProductDB
-- =============================================

IF DB_ID(N'ProductDB') IS NULL
    CREATE DATABASE ProductDB;
GO

USE ProductDB;
GO

-- =============================================
-- TABLE: Products
-- =============================================

CREATE TABLE Products (
    ProductID INT IDENTITY(1,1) PRIMARY KEY,
    ProductName NVARCHAR(150) NOT NULL,
    Description NVARCHAR(500) NULL,
    Price DECIMAL(18,2) NOT NULL,
    StockQuantity INT NOT NULL DEFAULT 0,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NULL,

    CONSTRAINT CK_Products_Price
        CHECK (Price >= 0),

    CONSTRAINT CK_Products_Stock
        CHECK (StockQuantity >= 0)
);
GO

-- =============================================
-- SAMPLE DATA
-- =============================================

INSERT INTO Products
    (ProductName, Description, Price, StockQuantity)
VALUES
    (N'Laptop Dell Inspiron', N'Laptop văn phòng', 15000000, 20),
    (N'Chuột Logitech M331', N'Chuột không dây', 350000, 50),
    (N'Bàn phím cơ Keychron', N'Bàn phím cơ', 1800000, 15),
    (N'Màn hình Samsung 24 inch', N'Màn hình máy tính', 3200000, 10),
    (N'Tai nghe Sony', N'Tai nghe không dây', 2500000, 30);
GO

SELECT * FROM Products;
GO
