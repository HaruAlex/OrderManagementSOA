package repositories;

import models.Order;
import models.OrderItem;
import play.Logger;
import utils.DatabaseUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository class để thao tác với database cho Order và OrderItem.
 * Thực hiện các thao tác CRUD và các query phức tạp.
 */
@Singleton
public class OrderRepository {
    
    private static final Logger.ALogger logger = Logger.of(OrderRepository.class);
    
    private final DatabaseUtil databaseUtil;
    
    /**
     * Constructor với dependency injection
     * @param databaseUtil Database utility để quản lý connection
     */
    @Inject
    public OrderRepository(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }
    
    /**
     * Lấy danh sách tất cả đơn hàng
     * @return List các đơn hàng
     */
    public List<Order> findAll() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT OrderID, CustomerName, CustomerPhone, TotalAmount, Status, CreatedAt, UpdatedAt " +
                    "FROM Orders ORDER BY CreatedAt DESC";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Load items for each order
                order.setItems(findItemsByOrderId(order.getOrderId()));
                orders.add(order);
            }
            
            logger.info("Found {} orders", orders.size());
        } catch (SQLException e) {
            logger.error("Error finding all orders", e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return orders;
    }
    
    /**
     * Tìm đơn hàng theo ID
     * @param orderId ID của đơn hàng
     * @return Optional chứa Order nếu tìm thấy
     */
    public Optional<Order> findById(Long orderId) {
        String sql = "SELECT OrderID, CustomerName, CustomerPhone, TotalAmount, Status, CreatedAt, UpdatedAt " +
                    "FROM Orders WHERE OrderID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, orderId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                // Load items for this order
                order.setItems(findItemsByOrderId(order.getOrderId()));
                logger.info("Found order with ID: {}", orderId);
                return Optional.of(order);
            }
        } catch (SQLException e) {
            logger.error("Error finding order by ID: {}", orderId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return Optional.empty();
    }
    
    /**
     * Lấy danh sách sản phẩm trong đơn hàng
     * @param orderId ID của đơn hàng
     * @return List các OrderItem
     */
    public List<OrderItem> findItemsByOrderId(Long orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT OrderItemID, OrderID, ProductID, ProductName, Quantity, UnitPrice, Subtotal " +
                    "FROM OrderItems WHERE OrderID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, orderId);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                OrderItem item = mapResultSetToOrderItem(rs);
                items.add(item);
            }
        } catch (SQLException e) {
            logger.error("Error finding items for order: {}", orderId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return items;
    }
    
    /**
     * Tạo đơn hàng mới
     * @param order Đơn hàng cần tạo
     * @return Order đã được tạo với ID
     */
    public Order create(Order order) {
        String orderSql = "INSERT INTO Orders (CustomerName, CustomerPhone, TotalAmount, Status) " +
                         "VALUES (?, ?, ?, ?)";
        String itemSql = "INSERT INTO OrderItems (OrderID, ProductID, ProductName, Quantity, UnitPrice, Subtotal) " +
                        "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Insert Order
            PreparedStatement orderStmt = conn.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            orderStmt.setString(1, order.getCustomerName());
            orderStmt.setString(2, order.getCustomerPhone());
            orderStmt.setBigDecimal(3, order.getTotalAmount());
            orderStmt.setString(4, order.getStatus());
            
            int affectedRows = orderStmt.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            // Get generated OrderID
            ResultSet generatedKeys = orderStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                order.setOrderId(generatedKeys.getLong(1));
            } else {
                throw new SQLException("Creating order failed, no ID obtained.");
            }
            
            // Insert OrderItems
            PreparedStatement itemStmt = conn.prepareStatement(itemSql);
            for (OrderItem item : order.getItems()) {
                itemStmt.setLong(1, order.getOrderId());
                itemStmt.setInt(2, item.getProductId());
                itemStmt.setString(3, item.getProductName());
                itemStmt.setInt(4, item.getQuantity());
                itemStmt.setBigDecimal(5, item.getUnitPrice());
                itemStmt.setBigDecimal(6, item.getSubtotal());
                itemStmt.addBatch();
            }
            itemStmt.executeBatch();
            
            conn.commit(); // Commit transaction
            logger.info("Created order with ID: {}", order.getOrderId());
            
            return findById(order.getOrderId()).orElse(order);
            
        } catch (SQLException e) {
            logger.error("Error creating order", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
            throw new RuntimeException("Failed to create order", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit", e);
                }
            }
            databaseUtil.closeConnection(conn);
        }
    }
    
    /**
     * Cập nhật thông tin đơn hàng
     * @param orderId ID của đơn hàng cần cập nhật
     * @param order Thông tin cập nhật
     * @return true nếu cập nhật thành công
     */
    public boolean update(Long orderId, Order order) {
        String sql = "UPDATE Orders SET CustomerName = ?, CustomerPhone = ?, Status = ?, UpdatedAt = SYSDATETIME() " +
                    "WHERE OrderID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, order.getCustomerName());
            pstmt.setString(2, order.getCustomerPhone());
            pstmt.setString(3, order.getStatus());
            pstmt.setLong(4, orderId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Updated order with ID: {}", orderId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating order: {}", orderId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Cập nhật trạng thái đơn hàng
     * @param orderId ID của đơn hàng
     * @param status Trạng thái mới
     * @return true nếu cập nhật thành công
     */
    public boolean updateStatus(Long orderId, String status) {
        String sql = "UPDATE Orders SET Status = ?, UpdatedAt = SYSDATETIME() WHERE OrderID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setLong(2, orderId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Updated status for order {}: {}", orderId, status);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating order status: {}", orderId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Xóa đơn hàng
     * @param orderId ID của đơn hàng cần xóa
     * @return true nếu xóa thành công
     */
    public boolean delete(Long orderId) {
        String deleteItemsSql = "DELETE FROM OrderItems WHERE OrderID = ?";
        String deleteOrderSql = "DELETE FROM Orders WHERE OrderID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Delete OrderItems first (foreign key constraint)
            PreparedStatement deleteItemsStmt = conn.prepareStatement(deleteItemsSql);
            deleteItemsStmt.setLong(1, orderId);
            deleteItemsStmt.executeUpdate();
            
            // Delete Order
            PreparedStatement deleteOrderStmt = conn.prepareStatement(deleteOrderSql);
            deleteOrderStmt.setLong(1, orderId);
            int affectedRows = deleteOrderStmt.executeUpdate();
            
            conn.commit(); // Commit transaction
            
            if (affectedRows > 0) {
                logger.info("Deleted order with ID: {}", orderId);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error deleting order: {}", orderId, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error rolling back transaction", ex);
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("Error resetting auto-commit", e);
                }
            }
            databaseUtil.closeConnection(conn);
        }
        
        return false;
    }
    
    /**
     * Lấy danh sách đơn hàng theo trạng thái
     * @param status Trạng thái cần tìm
     * @return List các đơn hàng
     */
    public List<Order> findByStatus(String status) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT OrderID, CustomerName, CustomerPhone, TotalAmount, Status, CreatedAt, UpdatedAt " +
                    "FROM Orders WHERE Status = ? ORDER BY CreatedAt DESC";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Order order = mapResultSetToOrder(rs);
                order.setItems(findItemsByOrderId(order.getOrderId()));
                orders.add(order);
            }
            
            logger.info("Found {} orders with status: {}", orders.size(), status);
        } catch (SQLException e) {
            logger.error("Error finding orders by status: {}", status, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return orders;
    }
    
    /**
     * Map ResultSet sang Order object
     * @param rs ResultSet từ query
     * @return Order object
     * @throws SQLException nếu có lỗi đọc dữ liệu
     */
    private Order mapResultSetToOrder(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setOrderId(rs.getLong("OrderID"));
        order.setCustomerName(rs.getString("CustomerName"));
        order.setCustomerPhone(rs.getString("CustomerPhone"));
        order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
        order.setStatus(rs.getString("Status"));
        
        Timestamp createdAt = rs.getTimestamp("CreatedAt");
        if (createdAt != null) {
            order.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("UpdatedAt");
        if (updatedAt != null) {
            order.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return order;
    }
    
    /**
     * Map ResultSet sang OrderItem object
     * @param rs ResultSet từ query
     * @return OrderItem object
     * @throws SQLException nếu có lỗi đọc dữ liệu
     */
    private OrderItem mapResultSetToOrderItem(ResultSet rs) throws SQLException {
        OrderItem item = new OrderItem();
        item.setOrderItemId(rs.getLong("OrderItemID"));
        item.setOrderId(rs.getLong("OrderID"));
        item.setProductId(rs.getInt("ProductID"));
        item.setProductName(rs.getString("ProductName"));
        item.setQuantity(rs.getInt("Quantity"));
        item.setUnitPrice(rs.getBigDecimal("UnitPrice"));
        item.setSubtotal(rs.getBigDecimal("Subtotal"));
        return item;
    }
}
