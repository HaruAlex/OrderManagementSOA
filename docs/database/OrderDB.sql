
-- =============================================
-- DATABASE: OrderDB
-- =============================================

IF DB_ID(N'OrderDB') IS NULL
    CREATE DATABASE OrderDB;
GO

USE OrderDB;
GO

-- =============================================
-- TABLE: Orders
-- =============================================

CREATE TABLE Orders (
    OrderID BIGINT IDENTITY(1,1) PRIMARY KEY,
    CustomerName NVARCHAR(150) NOT NULL,
    CustomerPhone VARCHAR(20) NULL,
    TotalAmount DECIMAL(18,2) NOT NULL DEFAULT 0,
    Status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NULL,

    CONSTRAINT CK_Orders_TotalAmount
        CHECK (TotalAmount >= 0),

    CONSTRAINT CK_Orders_Status
        CHECK (Status IN (
            'PENDING',
            'CONFIRMED',
            'SHIPPED',
            'COMPLETED',
            'CANCELLED'
        ))
);
GO

-- =============================================
-- TABLE: OrderItems
-- =============================================

CREATE TABLE OrderItems (
    OrderItemID BIGINT IDENTITY(1,1) PRIMARY KEY,
    OrderID BIGINT NOT NULL,
    ProductID INT NOT NULL,
    ProductName NVARCHAR(150) NOT NULL,
    Quantity INT NOT NULL,
    UnitPrice DECIMAL(18,2) NOT NULL,
    Subtotal DECIMAL(18,2) NOT NULL,

    CONSTRAINT FK_OrderItems_Orders
        FOREIGN KEY (OrderID)
        REFERENCES Orders(OrderID),

    CONSTRAINT CK_OrderItems_Quantity
        CHECK (Quantity > 0),

    CONSTRAINT CK_OrderItems_UnitPrice
        CHECK (UnitPrice >= 0),

    CONSTRAINT CK_OrderItems_Subtotal
        CHECK (Subtotal >= 0)
);
GO

-- =============================================
-- INDEXES
-- =============================================

CREATE INDEX IX_Orders_Status
ON Orders(Status);

CREATE INDEX IX_OrderItems_OrderID
ON OrderItems(OrderID);

CREATE INDEX IX_OrderItems_ProductID
ON OrderItems(ProductID);
GO

-- =============================================
-- VERIFY DATABASE
-- =============================================

SELECT * FROM Orders;
SELECT * FROM OrderItems;
GO

-- =============================================
-- SEED DATA: Orders
-- =============================================

INSERT INTO Orders (
    CustomerName,
    CustomerPhone,
    TotalAmount,
    Status
)
VALUES
(N'Nguyễn Văn An', '0901234567', 15700000, 'PENDING'),
(N'Trần Thị Bình', '0912345678', 2150000, 'CONFIRMED'),
(N'Lê Văn Cường', '0923456789', 6400000, 'SHIPPED'),
(N'Phạm Thị Dung', '0934567890', 2500000, 'COMPLETED'),
(N'Hoàng Văn Em', '0945678901', 350000, 'CANCELLED'),
(N'Đỗ Thị Hoa', '0956789012', 3300000, 'PENDING'),
(N'Bùi Văn Hùng', '0967890123', 1800000, 'CONFIRMED'),
(N'Võ Thị Lan', '0978901234', 3200000, 'COMPLETED');
GO

-- =============================================
-- SEED DATA: OrderItems
-- ProductID tham chiếu đến ProductDB
-- =============================================

INSERT INTO OrderItems (
    OrderID,
    ProductID,
    ProductName,
    Quantity,
    UnitPrice,
    Subtotal
)
VALUES
-- Order 1: Nguyễn Văn An
(1, 1, N'Laptop Dell Inspiron', 1, 15000000, 15000000),
(1, 2, N'Chuột Logitech M331', 2, 350000, 700000),

-- Order 2: Trần Thị Bình
(2, 3, N'Bàn phím cơ Keychron', 1, 1800000, 1800000),
(2, 2, N'Chuột Logitech M331', 1, 350000, 350000),

-- Order 3: Lê Văn Cường
(3, 4, N'Màn hình Samsung 24 inch', 2, 3200000, 6400000),

-- Order 4: Phạm Thị Dung
(4, 5, N'Tai nghe Sony', 1, 2500000, 2500000),

-- Order 5: Hoàng Văn Em
(5, 2, N'Chuột Logitech M331', 1, 350000, 350000),

-- Order 6: Đỗ Thị Hoa
(6, 4, N'Màn hình Samsung 24 inch', 1, 3200000, 3200000),
(6, 2, N'Chuột Logitech M331', 1, 350000, 350000),

-- Order 7: Bùi Văn Hùng
(7, 3, N'Bàn phím cơ Keychron', 1, 1800000, 1800000),

-- Order 8: Võ Thị Lan
(8, 4, N'Màn hình Samsung 24 inch', 1, 3200000, 3200000);
GO

-- Danh sách đơn hàng
SELECT * FROM Orders
ORDER BY OrderID;

-- Danh sách chi tiết đơn hàng
SELECT * FROM OrderItems
ORDER BY OrderID, OrderItemID;

-- Xem đơn hàng kèm sản phẩm
SELECT
    o.OrderID,
    o.CustomerName,
    o.CustomerPhone,
    o.TotalAmount,
    o.Status,
    oi.ProductID,
    oi.ProductName,
    oi.Quantity,
    oi.UnitPrice,
    oi.Subtotal
FROM Orders o
INNER JOIN OrderItems oi
    ON o.OrderID = oi.OrderID
ORDER BY o.OrderID, oi.OrderItemID;

-- Thống kê số lượng đơn theo trạng thái
SELECT
    Status,
    COUNT(*) AS TotalOrders
FROM Orders
GROUP BY Status;
GO