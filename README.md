# Order Management SOA System

## Tổng quan hệ thống

Hệ thống quản lý đơn hàng và sản phẩm được xây dựng theo kiến trúc **SOA (Service-Oriented Architecture)**. Hệ thống bao gồm 2 services độc lập, mỗi service có database riêng và giao tiếp với nhau thông qua REST API.

### Mục tiêu

- Xây dựng hệ thống microservices theo kiến trúc SOA
- Mỗi service hoạt động độc lập với database riêng
- Giao tiếp giữa các services qua REST API
- Giao diện web hiện đại với màu xanh rêu đậm
- Đầy đủ chức năng CRUD cho cả Product và Order

## Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────┐
│                        User Interface                        │
│         (Web Browser - Responsive Design)                    │
└─────────────────────────────────────────────────────────────┘
                            │
                ┌───────────┴───────────┐
                │                       │
┌───────────────▼──────────┐  ┌────────▼──────────────┐
│   Product Service        │  │   Order Service        │
│   Port: 9006             │  │   Port: 9005           │
│   ─────────────          │  │   ─────────────        │
│   • CRUD Products        │◄─┤   • CRUD Orders        │
│   • Stock Management     │  │   • Check Stock        │
│   • Price Management     │  │   • Create Orders      │
│                          │  │   • Update Status      │
└──────────┬───────────────┘  └────────┬───────────────┘
           │                           │
           │                           │
┌──────────▼───────────────┐  ┌───────▼────────────────┐
│   ProductDB              │  │   OrderDB               │
│   (SQL Server)           │  │   (SQL Server)          │
│   ─────────────          │  │   ─────────────         │
│   • Products Table       │  │   • Orders Table        │
│                          │  │   • OrderItems Table    │
└──────────────────────────┘  └─────────────────────────┘
```

## Các Services

### 1. Product Service (Port 9006)

**Chức năng:**
-  Quản lý sản phẩm (CRUD)
-  Quản lý giá và mô tả
-  Quản lý tồn kho
-  Increase/Decrease stock API
-  Active/Inactive products

**Database:** ProductDB
- Table: Products

**Endpoints:**
- `GET /api/products` - Lấy danh sách sản phẩm
- `GET /api/products/:id` - Lấy chi tiết sản phẩm
- `POST /api/products` - Tạo sản phẩm mới
- `PUT /api/products/:id` - Cập nhật sản phẩm
- `DELETE /api/products/:id` - Xóa sản phẩm
- `POST /api/products/:id/increase-stock` - Tăng tồn kho
- `POST /api/products/:id/decrease-stock` - Giảm tồn kho

### 2. Order Service (Port 9005)

**Chức năng:**
-  Quản lý đơn hàng (CRUD)
-  Tích hợp với Product Service
-  Kiểm tra tồn kho tự động
-  Cập nhật tồn kho khi tạo/hủy đơn
-  Quản lý trạng thái đơn hàng

**Database:** OrderDB
- Tables: Orders, OrderItems

**Endpoints:**
- `GET /api/orders` - Lấy danh sách đơn hàng
- `GET /api/orders/:id` - Lấy chi tiết đơn hàng
- `GET /api/orders/status/:status` - Lọc theo trạng thái
- `POST /api/orders` - Tạo đơn hàng mới
- `PUT /api/orders/:id` - Cập nhật đơn hàng
- `PATCH /api/orders/:id/status` - Cập nhật trạng thái
- `DELETE /api/orders/:id` - Xóa đơn hàng

## Giao diện

### Màu sắc chủ đạo: Xanh rêu đậm
- Primary: `#2d5016`
- Secondary: `#3d6b1f`
- Accent: `#4a7c26`

### Tính năng giao diện
-  Responsive design
-  Navigation menu giữa 2 services
-  Statistics cards
-  Filter và search
-  Modal forms
-  Toast notifications
-  Real-time updates

## Cài đặt và chạy

### Yêu cầu hệ thống

- **Java**: JDK 11+
- **SBT**: 1.5.0+
- **SQL Server**: 2016+
- **RAM**: 4GB+
- **Ports**: 9005, 9006

### 1. Chuẩn bị Database

Chạy 2 scripts SQL để tạo databases:

```bash
# Tạo ProductDB
sqlcmd -S localhost -U sa -P 123456 -i docs/database/ProductDB.sql

# Tạo OrderDB
sqlcmd -S localhost -U sa -P 123456 -i docs/database/OrderDB.sql
```

### 2. Khởi chạy Product Service

```bash
cd product-service
sbt clean compile
sbt run
```

Service sẽ chạy tại: **http://localhost:9006**

### 3. Khởi chạy Order Service

```bash
cd order-service
sbt clean compile
sbt run
```

Service sẽ chạy tại: **http://localhost:9005**

### 4. Truy cập ứng dụng

- **Product Management**: http://localhost:9006/products/manage
- **Order Management**: http://localhost:9005/orders/manage

## Luồng hoạt động

### Tạo đơn hàng mới

1. User truy cập Order Management
2. Click "Tạo Đơn Hàng Mới"
3. Nhập thông tin khách hàng và sản phẩm
4. **Order Service** gọi API đến **Product Service** để:
   - Lấy thông tin sản phẩm (tên, giá)
   - Kiểm tra sản phẩm có active không
   - Kiểm tra tồn kho đủ không
5. Nếu OK, Order Service tạo đơn hàng
6. Order Service gọi API giảm tồn kho tại Product Service
7. Hiển thị thông báo thành công

### Hủy đơn hàng

1. User chọn đơn hàng và cập nhật trạng thái "CANCELLED"
2. Order Service tự động khôi phục tồn kho
3. Gọi API tăng tồn kho tại Product Service
4. Cập nhật trạng thái đơn hàng

## Database Schema

### ProductDB

**Products Table:**
```sql
ProductID        INT PRIMARY KEY IDENTITY
ProductName      NVARCHAR(150) NOT NULL
Description      NVARCHAR(500)
Price            DECIMAL(18,2) NOT NULL
StockQuantity    INT NOT NULL DEFAULT 0
IsActive         BIT NOT NULL DEFAULT 1
CreatedAt        DATETIME2 DEFAULT SYSDATETIME()
UpdatedAt        DATETIME2
```

### OrderDB

**Orders Table:**
```sql
OrderID          BIGINT PRIMARY KEY IDENTITY
CustomerName     NVARCHAR(150) NOT NULL
CustomerPhone    VARCHAR(20)
TotalAmount      DECIMAL(18,2) NOT NULL
Status           VARCHAR(30) NOT NULL
CreatedAt        DATETIME2 DEFAULT SYSDATETIME()
UpdatedAt        DATETIME2
```

**OrderItems Table:**
```sql
OrderItemID      BIGINT PRIMARY KEY IDENTITY
OrderID          BIGINT FOREIGN KEY
ProductID        INT NOT NULL
ProductName      NVARCHAR(150) NOT NULL
Quantity         INT NOT NULL
UnitPrice        DECIMAL(18,2) NOT NULL
Subtotal         DECIMAL(18,2) NOT NULL
```

## Testing

### Test với cURL

**Product Service:**
```bash
# Get all products
curl http://localhost:9006/api/products

# Create product
curl -X POST http://localhost:9006/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Laptop Dell",
    "description": "Laptop cao cấp",
    "price": 15000000,
    "stockQuantity": 10,
    "isActive": true
  }'

# Decrease stock
curl -X POST http://localhost:9006/api/products/1/decrease-stock \
  -H "Content-Type: application/json" \
  -d '{"quantity": 2}'
```

**Order Service:**
```bash
# Get all orders
curl http://localhost:9005/api/orders

# Create order
curl -X POST http://localhost:9005/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Nguyễn Văn A",
    "customerPhone": "0901234567",
    "items": [
      {"productId": 1, "quantity": 2}
    ]
  }'
```

## Trạng thái đơn hàng

- **PENDING** - Đang chờ xử lý
- **CONFIRMED** - Đã xác nhận
- **SHIPPED** - Đang giao hàng
- **COMPLETED** - Hoàn thành
- **CANCELLED** - Đã hủy

## Configuration

### Product Service (application.conf)

```conf
play.server.http.port = 9006
db.default.url = "jdbc:sqlserver://localhost:1433;databaseName=ProductDB"
db.default.username = "sa"
db.default.password = "123456"
```

### Order Service (application.conf)

```conf
play.server.http.port = 9005
db.default.url = "jdbc:sqlserver://localhost:1433;databaseName=OrderDB"
db.default.username = "sa"
db.default.password = "123456"
product.service.url = "http://localhost:9006"
```

## Troubleshooting

### Lỗi kết nối giữa services

**Triệu chứng:** Order Service không thể tạo đơn hàng

**Giải pháp:**
1. Đảm bảo Product Service đang chạy trên port 9006
2. Kiểm tra URL trong Order Service config
3. Test Product Service health: `curl http://localhost:9006/api/products/health`

### Port đã được sử dụng

**Windows:**
```powershell
# Tìm process đang dùng port
netstat -ano | findstr :9005
netstat -ano | findstr :9006

# Kill process
taskkill /PID <PID> /F
```

### Lỗi database connection

1. Kiểm tra SQL Server đang chạy
2. Kiểm tra SQL Server Authentication mode
3. Kiểm tra firewall cho port 1433
4. Test connection: `sqlcmd -S localhost -U sa -P 123456`

## Tài liệu chi tiết

- [Product Service README](product-service/README.md)
- [Order Service README](order-service/README.md)

## Best Practices được áp dụng

### Architecture
-  Loosely coupled services
-  Single Responsibility Principle
-  RESTful API design
-  Service independence

### Code Quality
-  Document comments cho tất cả classes
-  Error handling đầy đủ
-  Logging chi tiết
-  Input validation

### Database
-  Transaction management
-  Foreign key constraints
-  Indexes cho performance
-  Connection pooling

### Security
-  SQL Injection prevention (PreparedStatement)
-  XSS prevention (HTML escaping)
-  Input validation
## License

Dự án này được phát triển cho mục đích học tập và nghiên cứu.

---

## Kiến thức áp dụng

- **Play Framework**: Web framework cho Java/Scala
- **SOA Architecture**: Service-Oriented Architecture
- **REST API**: RESTful web services
- **SQL Server**: Relational database
- **JDBC**: Java Database Connectivity
- **JSON**: Data interchange format
- **HTML/CSS/JavaScript**: Frontend development
- **Scala Templates**: Server-side templating
- **SBT**: Build tool


**Lưu ý quan trọng:**
- Đảm bảo chạy Product Service trước Order Service
- Luôn backup database trước khi thực hiện thay đổi lớn
- Kiểm tra logs khi gặp lỗi: `logs/application.log`

