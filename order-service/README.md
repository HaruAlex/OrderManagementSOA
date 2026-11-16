# Order Management Service - Dịch Vụ Quản Lý Đơn Hàng

## Tổng quan

Dịch vụ quản lý đơn hàng là một phần của hệ thống SOA (Service-Oriented Architecture) để xử lý các nghiệp vụ liên quan đến đơn hàng. Dịch vụ này hoạt động độc lập với database riêng và cung cấp các API RESTful để tạo, xem, cập nhật và xóa đơn hàng.

## Tính năng chính

✅ **Quản lý đơn hàng (CRUD)**
- Tạo đơn hàng mới với kiểm tra tồn kho tự động
- Xem danh sách đơn hàng và chi tiết đơn hàng
- Cập nhật thông tin đơn hàng và trạng thái
- Xóa đơn hàng với khôi phục tồn kho tự động

✅ **Tích hợp với Product Service**
- Kiểm tra thông tin sản phẩm
- Kiểm tra và cập nhật tồn kho
- Đồng bộ dữ liệu giữa các service

✅ **Giao diện web hiện đại**
- Màu sắc: Xanh rêu đậm (#2d5016, #3d6b1f, #4a7c26)
- Responsive design
- Thống kê trực quan
- Modal form và toast notifications

✅ **Kiến trúc SOA**
- Service độc lập với database riêng
- REST API communication
- Loosely coupled architecture

## Công nghệ sử dụng

- **Framework**: Play Framework 2.8+ (Java)
- **Build Tool**: SBT (Scala Build Tool)
- **Database**: Microsoft SQL Server
- **JDBC Driver**: mssql-jdbc 12.4.2
- **JSON Processing**: Jackson Databind
- **HTTP Client**: Java 11 HttpClient
- **Frontend**: HTML5, CSS3, JavaScript (Vanilla)
- **Template Engine**: Scala Templates

## Cấu trúc dự án

```
order-service/
├── app/
│   ├── controllers/
│   │   ├── HomeController.java          # Controller trang chủ
│   │   └── OrderController.java         # Controller xử lý API đơn hàng
│   ├── models/
│   │   ├── Order.java                   # Model đơn hàng
│   │   ├── OrderItem.java               # Model chi tiết sản phẩm trong đơn
│   │   ├── Product.java                 # Model sản phẩm từ Product Service
│   │   ├── CreateOrderRequest.java      # Request DTO tạo đơn hàng
│   │   └── UpdateOrderRequest.java      # Request DTO cập nhật đơn hàng
│   ├── repositories/
│   │   └── OrderRepository.java         # Data access layer
│   ├── services/
│   │   ├── OrderService.java            # Business logic layer
│   │   └── ProductServiceClient.java    # Client gọi Product Service
│   ├── utils/
│   │   └── DatabaseUtil.java            # Utility quản lý database connection
│   └── views/
│       └── orders.scala.html            # Template giao diện quản lý đơn hàng
├── conf/
│   ├── application.conf                 # Cấu hình ứng dụng
│   └── routes                          # Định tuyến API
├── public/
│   ├── javascripts/
│   │   └── orders.js                   # Frontend JavaScript
│   └── stylesheets/
│       └── orders.css                  # Frontend CSS
├── build.sbt                           # Build configuration
└── README.md                           # Tài liệu này
```

## Yêu cầu hệ thống

- **Java**: JDK 11 trở lên
- **SBT**: Version 1.5.0 trở lên
- **SQL Server**: 2016 trở lên
- **RAM**: Tối thiểu 2GB
- **Port**: 9005 (Order Service), 9006 (Product Service)

## Cài đặt và chạy

### 1. Chuẩn bị Database

Chạy script SQL để tạo database và seed data:

```bash
# Mở SQL Server Management Studio và chạy file:
docs/database/OrderDB.sql
```

Hoặc sử dụng sqlcmd:

```bash
sqlcmd -S localhost -U sa -P 123456 -i docs/database/OrderDB.sql
```

### 2. Cấu hình Database Connection

File `conf/application.conf` đã được cấu hình sẵn:

```conf
db.default.driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver"
db.default.url = "jdbc:sqlserver://localhost:1433;databaseName=OrderDB;encrypt=false;trustServerCertificate=true"
db.default.username = "sa"
db.default.password = "123456"
```

**Lưu ý**: Thay đổi username và password nếu cần.

### 3. Cấu hình Product Service URL

Đảm bảo Product Service đang chạy trên port 9006:

```conf
product.service.url = "http://localhost:9006"
```

### 4. Build và chạy ứng dụng

```bash
# Di chuyển vào thư mục order-service
cd order-service

# Clean và compile
sbt clean compile

# Chạy ứng dụng
sbt run
```

Ứng dụng sẽ chạy tại: **http://localhost:9005**

### 5. Truy cập giao diện

Mở trình duyệt và truy cập:

- **Trang quản lý đơn hàng**: http://localhost:9005/orders/manage
- **API Health Check**: http://localhost:9005/api/orders/health

## API Endpoints

### REST API Documentation

#### 1. Health Check
```http
GET /api/orders/health
```
**Response:**
```json
{
  "status": "UP",
  "service": "Order Service",
  "productServiceConnected": true
}
```

#### 2. Lấy tất cả đơn hàng
```http
GET /api/orders
```
**Response:** Array of Order objects

#### 3. Lấy đơn hàng theo ID
```http
GET /api/orders/{id}
```
**Response:** Order object with items

#### 4. Lấy đơn hàng theo trạng thái
```http
GET /api/orders/status/{status}
```
**Trạng thái hợp lệ:** PENDING, CONFIRMED, SHIPPED, COMPLETED, CANCELLED

#### 5. Tạo đơn hàng mới
```http
POST /api/orders
Content-Type: application/json

{
  "customerName": "Nguyễn Văn A",
  "customerPhone": "0901234567",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 3,
      "quantity": 1
    }
  ]
}
```

**Response:**
```json
{
  "orderId": 9,
  "customerName": "Nguyễn Văn A",
  "customerPhone": "0901234567",
  "totalAmount": 32700000,
  "status": "PENDING",
  "createdAt": "2024-11-16T10:30:00",
  "items": [...]
}
```

#### 6. Cập nhật đơn hàng
```http
PUT /api/orders/{id}
Content-Type: application/json

{
  "customerName": "Nguyễn Văn B",
  "customerPhone": "0912345678",
  "status": "CONFIRMED"
}
```

#### 7. Cập nhật trạng thái
```http
PATCH /api/orders/{id}/status
Content-Type: application/json

{
  "status": "SHIPPED"
}
```

#### 8. Xóa đơn hàng
```http
DELETE /api/orders/{id}
```

## Business Logic

### Tạo đơn hàng
1. Validate thông tin khách hàng và danh sách sản phẩm
2. Gọi Product Service để lấy thông tin sản phẩm
3. Kiểm tra sản phẩm có active không
4. Kiểm tra tồn kho đủ không
5. Tính toán tổng tiền
6. Lưu đơn hàng vào database (transaction)
7. Giảm tồn kho tại Product Service

### Cập nhật trạng thái
- Khi chuyển sang trạng thái **CANCELLED**: Tự động khôi phục tồn kho

### Xóa đơn hàng
- Kiểm tra trạng thái đơn hàng
- Nếu chưa CANCELLED: Khôi phục tồn kho
- Xóa OrderItems trước (foreign key)
- Xóa Order

## Database Schema

### Bảng Orders
| Column | Type | Description |
|--------|------|-------------|
| OrderID | BIGINT | Primary key (Identity) |
| CustomerName | NVARCHAR(150) | Tên khách hàng |
| CustomerPhone | VARCHAR(20) | Số điện thoại |
| TotalAmount | DECIMAL(18,2) | Tổng tiền |
| Status | VARCHAR(30) | Trạng thái đơn hàng |
| CreatedAt | DATETIME2 | Ngày tạo |
| UpdatedAt | DATETIME2 | Ngày cập nhật |

### Bảng OrderItems
| Column | Type | Description |
|--------|------|-------------|
| OrderItemID | BIGINT | Primary key (Identity) |
| OrderID | BIGINT | Foreign key → Orders |
| ProductID | INT | ID sản phẩm |
| ProductName | NVARCHAR(150) | Tên sản phẩm |
| Quantity | INT | Số lượng |
| UnitPrice | DECIMAL(18,2) | Đơn giá |
| Subtotal | DECIMAL(18,2) | Thành tiền |

## Testing

### Test với cURL

```bash
# Health check
curl http://localhost:9005/api/orders/health

# Get all orders
curl http://localhost:9005/api/orders

# Get order by ID
curl http://localhost:9005/api/orders/1

# Create order
curl -X POST http://localhost:9005/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Test User",
    "customerPhone": "0901234567",
    "items": [
      {"productId": 1, "quantity": 1}
    ]
  }'

# Update order
curl -X PUT http://localhost:9005/api/orders/1 \
  -H "Content-Type: application/json" \
  -d '{
    "status": "CONFIRMED"
  }'

# Delete order
curl -X DELETE http://localhost:9005/api/orders/1
```

### Test với Postman

Import collection với các endpoint trên vào Postman để test dễ dàng hơn.

## Troubleshooting

### Lỗi kết nối Database

**Triệu chứng:** "Failed to establish database connection"

**Giải pháp:**
1. Kiểm tra SQL Server đang chạy
2. Kiểm tra username/password trong `application.conf`
3. Kiểm tra firewall cho port 1433
4. Kiểm tra SQL Server Authentication Mode (phải bật SQL Server and Windows Authentication)

### Lỗi kết nối Product Service

**Triệu chứng:** "Product Service is not available"

**Giải pháp:**
1. Đảm bảo Product Service đang chạy trên port 9006
2. Kiểm tra URL trong `application.conf`
3. Test Product Service độc lập trước

### Port đã được sử dụng

**Triệu chứng:** "Address already in use"

**Giải pháp:**
```bash
# Windows: Tìm process đang dùng port 9005
netstat -ano | findstr :9005

# Kill process
taskkill /PID <PID> /F

# Hoặc thay đổi port trong application.conf
play.server.http.port = 9007
```

## Best Practices

### Security
- ✅ Sử dụng PreparedStatement để tránh SQL Injection
- ✅ Validate input từ client
- ✅ Escape HTML output để tránh XSS
- ⚠️ Nên thêm authentication/authorization trong production

### Performance
- ✅ Sử dụng connection pooling
- ✅ Index trên các column thường query (Status, ProductID)
- ✅ Transaction cho các operation phức tạp
- ⚠️ Nên implement caching cho data ít thay đổi

### Error Handling
- ✅ Try-catch cho tất cả database operations
- ✅ Logging chi tiết với Play Logger
- ✅ Meaningful error messages cho client
- ✅ Rollback transaction khi có lỗi

## Mở rộng trong tương lai

### 1. Authentication & Authorization
- Implement JWT token
- Role-based access control (Admin, User)

### 2. Pagination
- Thêm pagination cho danh sách đơn hàng
- Page size và page number parameters

### 3. Search & Filter
- Tìm kiếm theo tên khách hàng
- Lọc theo khoảng thời gian
- Lọc theo khoảng giá

### 4. Notification Service
- Email notification khi đơn hàng được tạo
- SMS notification khi đơn hàng thay đổi trạng thái

### 5. Order History
- Lưu lịch sử thay đổi trạng thái
- Audit trail cho các thao tác

### 6. Payment Integration
- Tích hợp với payment gateway
- Tracking payment status

## Đóng góp

Nếu bạn muốn đóng góp cho dự án:

1. Fork repository
2. Tạo branch mới: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Tạo Pull Request

## Tác giả

Order Management Service - SOA Project

## License

Dự án này được phát triển cho mục đích học tập và nghiên cứu.

---

**Lưu ý quan trọng:**
- Đảm bảo Product Service đang chạy trước khi test tính năng tạo đơn hàng
- Luôn backup database trước khi thực hiện các thay đổi lớn
- Đọc kỹ log files khi gặp lỗi (logs/application.log)

**Liên hệ hỗ trợ:**
- Email: support@example.com
- GitHub Issues: [Link to repository]

---

Chúc bạn thành công! 🚀
