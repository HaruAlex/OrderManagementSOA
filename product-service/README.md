# Product Management Service - Dịch Vụ Quản Lý Sản Phẩm

## Tổng quan

Dịch vụ quản lý sản phẩm là một phần của hệ thống SOA để xử lý các nghiệp vụ liên quan đến sản phẩm. Service này hoạt động độc lập với database riêng và cung cấp các API RESTful cho Order Service và giao diện web.

## Tính năng chính

**Quản lý sản phẩm (CRUD)**
- Tạo sản phẩm mới
- Xem danh sách và chi tiết sản phẩm
- Cập nhật thông tin sản phẩm
- Xóa sản phẩm
- Active/Inactive products

**Quản lý tồn kho**
- Nhập kho (increase stock)
- Xuất kho (decrease stock)
- Kiểm tra tồn kho
- Cảnh báo sắp hết hàng

**Giao diện web**
- Màu xanh rêu đậm
- Responsive design
- Statistics dashboard
- Stock adjustment modal

## Công nghệ sử dụng

- **Framework**: Play Framework 2.8+ (Java)
- **Database**: Microsoft SQL Server
- **JDBC Driver**: mssql-jdbc 12.4.2
- **JSON**: Jackson Databind
- **Frontend**: HTML5, CSS3, Vanilla JavaScript

## Cấu trúc dự án

```
product-service/
├── app/
│   ├── controllers/
│   │   └── ProductController.java
│   ├── models/
│   │   └── Product.java
│   ├── repositories/
│   │   └── ProductRepository.java
│   ├── services/
│   │   └── ProductService.java
│   ├── utils/
│   │   └── DatabaseUtil.java
│   └── views/
│       └── products.scala.html
├── conf/
│   ├── application.conf
│   └── routes
├── public/
│   ├── javascripts/
│   │   └── products.js
│   └── stylesheets/
│       └── products.css
├── build.sbt
└── README.md
```

## Cài đặt và chạy

### 1. Chuẩn bị Database

```bash
sqlcmd -S localhost -U sa -P 123456 -i docs/database/ProductDB.sql
```

### 2. Cấu hình

File `conf/application.conf`:

```conf
play.server.http.port = 9006
db.default.url = "jdbc:sqlserver://localhost:1433;databaseName=ProductDB"
db.default.username = "sa"
db.default.password = "123456"
```

### 3. Chạy service

```bash
cd product-service
sbt clean compile
sbt run
```

Service chạy tại: **http://localhost:9006**

### 4. Truy cập

- **Giao diện**: http://localhost:9006/products/manage
- **API Health**: http://localhost:9006/api/products/health

## API Endpoints

### 1. Health Check
```http
GET /api/products/health
```

### 2. Lấy tất cả sản phẩm
```http
GET /api/products
```

### 3. Lấy sản phẩm theo ID
```http
GET /api/products/{id}
```

### 4. Tạo sản phẩm mới
```http
POST /api/products
Content-Type: application/json

{
  "productName": "Laptop Dell Inspiron",
  "description": "Laptop văn phòng",
  "price": 15000000,
  "stockQuantity": 20,
  "isActive": true
}
```

### 5. Cập nhật sản phẩm
```http
PUT /api/products/{id}
Content-Type: application/json

{
  "productName": "Laptop Dell Inspiron Updated",
  "description": "Laptop cao cấp",
  "price": 16000000,
  "stockQuantity": 15,
  "isActive": true
}
```

### 6. Xóa sản phẩm
```http
DELETE /api/products/{id}
```

### 7. Giảm tồn kho
```http
POST /api/products/{id}/decrease-stock
Content-Type: application/json

{
  "quantity": 5
}
```

**Business Logic:**
- Chỉ giảm nếu tồn kho đủ
- Cập nhật UpdatedAt timestamp
- Return error nếu không đủ hàng

### 8. Tăng tồn kho
```http
POST /api/products/{id}/increase-stock
Content-Type: application/json

{
  "quantity": 10
}
```

## Database Schema

### Products Table

```sql
CREATE TABLE Products (
    ProductID INT IDENTITY(1,1) PRIMARY KEY,
    ProductName NVARCHAR(150) NOT NULL,
    Description NVARCHAR(500) NULL,
    Price DECIMAL(18,2) NOT NULL,
    StockQuantity INT NOT NULL DEFAULT 0,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
    UpdatedAt DATETIME2 NULL,
    
    CONSTRAINT CK_Products_Price CHECK (Price >= 0),
    CONSTRAINT CK_Products_Stock CHECK (StockQuantity >= 0)
);
```

## Business Logic

### Decrease Stock
1. Kiểm tra product tồn tại
2. Kiểm tra quantity > 0
3. Kiểm tra tồn kho đủ không (StockQuantity >= quantity)
4. Nếu OK: UPDATE StockQuantity = StockQuantity - quantity
5. Cập nhật UpdatedAt
6. Return success/error

### Increase Stock
1. Kiểm tra product tồn tại
2. Kiểm tra quantity > 0
3. UPDATE StockQuantity = StockQuantity + quantity
4. Cập nhật UpdatedAt
5. Return success

### Create Product
1. Validate productName (required)
2. Validate price >= 0
3. Validate stockQuantity >= 0
4. Insert vào database
5. Return product với ID

## Giao diện người dùng

### Dashboard
- **Tổng sản phẩm**: Hiển thị số lượng sản phẩm
- **Đang bán**: Số sản phẩm active
- **Tổng tồn kho**: Tổng số lượng tất cả sản phẩm
- **Sắp hết hàng**: Sản phẩm có stock < 10

### Bảng sản phẩm
- Hiển thị: ID, Tên, Mô tả, Giá, Tồn kho, Trạng thái
- Filter theo Active/Inactive
- Actions: Edit, Adjust Stock, Delete

### Modal Forms
1. **Create/Edit Product**: Form nhập thông tin sản phẩm
2. **Stock Adjustment**: Form điều chỉnh tồn kho (Nhập/Xuất)

## Testing

### Test với cURL

```bash
# Health check
curl http://localhost:9006/api/products/health

# Get all products
curl http://localhost:9006/api/products

# Get product by ID
curl http://localhost:9006/api/products/1

# Create product
curl -X POST http://localhost:9006/api/products \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Test Product",
    "price": 100000,
    "stockQuantity": 50,
    "isActive": true
  }'

# Update product
curl -X PUT http://localhost:9006/api/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "productName": "Updated Product",
    "price": 120000,
    "stockQuantity": 40,
    "isActive": true
  }'

# Decrease stock
curl -X POST http://localhost:9006/api/products/1/decrease-stock \
  -H "Content-Type: application/json" \
  -d '{"quantity": 5}'

# Increase stock
curl -X POST http://localhost:9006/api/products/1/increase-stock \
  -H "Content-Type: application/json" \
  -d '{"quantity": 10}'

# Delete product
curl -X DELETE http://localhost:9006/api/products/1
```

## Integration với Order Service

Order Service gọi Product Service API để:

1. **Lấy thông tin sản phẩm** khi tạo đơn hàng
   ```
   GET /api/products/{id}
   ```

2. **Kiểm tra tồn kho** trước khi tạo đơn
   - Đọc stockQuantity từ product
   - Kiểm tra có đủ không

3. **Giảm tồn kho** sau khi tạo đơn thành công
   ```
   POST /api/products/{id}/decrease-stock
   ```

4. **Tăng tồn kho** khi hủy đơn
   ```
   POST /api/products/{id}/increase-stock
   ```

## Error Handling

### Common Errors

**404 Not Found**
```json
{
  "success": false,
  "error": "Product not found with ID: 123"
}
```

**400 Bad Request**
```json
{
  "success": false,
  "error": "Not enough stock or product not found"
}
```

**500 Internal Server Error**
```json
{
  "success": false,
  "error": "Failed to create product"
}
```

## Logging

Service sử dụng Play Logger:

```java
logger.info("Created product with ID: {}", productId);
logger.error("Error decreasing stock for product: {}", productId, e);
```

Logs được lưu tại: `logs/application.log`

## Performance

### Database Optimization
- ✅ Connection pooling (HikariCP)
- ✅ PreparedStatement để tránh SQL injection
- ✅ Indexes trên ProductID
- ✅ Transaction management

### Caching
- ⚠️ Chưa implement (có thể thêm Redis)

## Security

### Implemented
- ✅ SQL Injection prevention
- ✅ Input validation
- ✅ Error logging

### To Do
- ⚠️ Authentication/Authorization
- ⚠️ Rate limiting
- ⚠️ API key validation

## Troubleshooting

### Port 9006 đã được sử dụng

```powershell
netstat -ano | findstr :9006
taskkill /PID <PID> /F
```

### Database connection error

1. Kiểm tra SQL Server running
2. Kiểm tra username/password
3. Test: `sqlcmd -S localhost -U sa -P 123456`

### Order Service không gọi được Product Service

1. Kiểm tra Product Service đang chạy
2. Test health: `curl http://localhost:9006/api/products/health`
3. Kiểm tra URL trong Order Service config

## Deployment

### Development
```bash
sbt run
```

### Production
```bash
sbt dist
unzip target/universal/product-service-1.0-SNAPSHOT.zip
cd product-service-1.0-SNAPSHOT
bin/product-service -Dconfig.file=/path/to/production.conf
```

## Monitoring

### Health Check
```bash
curl http://localhost:9006/api/products/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "Product Service"
}
```

## Dependencies

```sbt
libraryDependencies ++= Seq(
  guice,
  javaJdbc,
  "com.microsoft.sqlserver" % "mssql-jdbc" % "12.4.2.jre11",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.15.2",
  "com.fasterxml.jackson.datatype" % "jackson-datatype-jsr310" % "2.15.2"
)
```

## Liên hệ

- System README: [../README.md](../README.md)
- Order Service: [../order-service/README.md](../order-service/README.md)

---

Chúc bạn thành công!
