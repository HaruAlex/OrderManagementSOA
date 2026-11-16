package repositories;

import models.Product;
import play.Logger;
import utils.DatabaseUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository để thao tác với database cho Product
 */
@Singleton
public class ProductRepository {
    
    private static final Logger.ALogger logger = Logger.of(ProductRepository.class);
    private final DatabaseUtil databaseUtil;
    
    @Inject
    public ProductRepository(DatabaseUtil databaseUtil) {
        this.databaseUtil = databaseUtil;
    }
    
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM Products ORDER BY ProductID";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding all products", e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return products;
    }
    
    public Optional<Product> findById(Integer productId) {
        String sql = "SELECT * FROM Products WHERE ProductID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            logger.error("Error finding product by ID: {}", productId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return Optional.empty();
    }
    
    public Product create(Product product) {
        String sql = "INSERT INTO Products (ProductName, Description, Price, StockQuantity, IsActive) VALUES (?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getStockQuantity());
            pstmt.setBoolean(5, product.getIsActive());
            
            pstmt.executeUpdate();
            
            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                product.setProductId(generatedKeys.getInt(1));
            }
            
            logger.info("Created product with ID: {}", product.getProductId());
            return findById(product.getProductId()).orElse(product);
            
        } catch (SQLException e) {
            logger.error("Error creating product", e);
            throw new RuntimeException("Failed to create product", e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
    }
    
    public boolean update(Integer productId, Product product) {
        String sql = "UPDATE Products SET ProductName = ?, Description = ?, Price = ?, StockQuantity = ?, IsActive = ?, UpdatedAt = SYSDATETIME() WHERE ProductID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, product.getProductName());
            pstmt.setString(2, product.getDescription());
            pstmt.setBigDecimal(3, product.getPrice());
            pstmt.setInt(4, product.getStockQuantity());
            pstmt.setBoolean(5, product.getIsActive());
            pstmt.setInt(6, productId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Updated product with ID: {}", productId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error updating product: {}", productId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return false;
    }
    
    public boolean delete(Integer productId) {
        String sql = "DELETE FROM Products WHERE ProductID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, productId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Deleted product with ID: {}", productId);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error deleting product: {}", productId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return false;
    }
    
    public boolean decreaseStock(Integer productId, Integer quantity) {
        String sql = "UPDATE Products SET StockQuantity = StockQuantity - ?, UpdatedAt = SYSDATETIME() WHERE ProductID = ? AND StockQuantity >= ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            pstmt.setInt(3, quantity);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Decreased stock for product {}: {} units", productId, quantity);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error decreasing stock for product: {}", productId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return false;
    }
    
    public boolean increaseStock(Integer productId, Integer quantity) {
        String sql = "UPDATE Products SET StockQuantity = StockQuantity + ?, UpdatedAt = SYSDATETIME() WHERE ProductID = ?";
        
        Connection conn = null;
        try {
            conn = databaseUtil.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, quantity);
            pstmt.setInt(2, productId);
            
            int affectedRows = pstmt.executeUpdate();
            
            if (affectedRows > 0) {
                logger.info("Increased stock for product {}: {} units", productId, quantity);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error increasing stock for product: {}", productId, e);
        } finally {
            databaseUtil.closeConnection(conn);
        }
        
        return false;
    }
    
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("ProductID"));
        product.setProductName(rs.getString("ProductName"));
        product.setDescription(rs.getString("Description"));
        product.setPrice(rs.getBigDecimal("Price"));
        product.setStockQuantity(rs.getInt("StockQuantity"));
        product.setIsActive(rs.getBoolean("IsActive"));
        
        Timestamp createdAt = rs.getTimestamp("CreatedAt");
        if (createdAt != null) {
            product.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("UpdatedAt");
        if (updatedAt != null) {
            product.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return product;
    }
}
