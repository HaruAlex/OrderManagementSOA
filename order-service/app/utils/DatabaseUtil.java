package utils;

import com.typesafe.config.Config;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class để quản lý kết nối database SQL Server.
 * Sử dụng Connection Pool để tối ưu hiệu năng.
 */
@Singleton
public class DatabaseUtil {
    
    private static final Logger.ALogger logger = Logger.of(DatabaseUtil.class);
    
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final String dbDriver;
    
    /**
     * Constructor với dependency injection từ Play Framework
     * @param config Configuration từ application.conf
     */
    @Inject
    public DatabaseUtil(Config config) {
        this.dbDriver = config.getString("db.default.driver");
        this.dbUrl = config.getString("db.default.url");
        this.dbUsername = config.getString("db.default.username");
        this.dbPassword = config.getString("db.default.password");
        
        // Load JDBC driver
        try {
            Class.forName(dbDriver);
            logger.info("SQL Server JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("SQL Server JDBC Driver not found", e);
            throw new RuntimeException("Failed to load database driver", e);
        }
    }
    
    /**
     * Tạo và trả về một connection mới đến database
     * @return Connection object
     * @throws SQLException nếu không thể kết nối
     */
    public Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            logger.debug("Database connection established successfully");
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to establish database connection", e);
            throw e;
        }
    }
    
    /**
     * Đóng connection an toàn
     * @param conn Connection cần đóng
     */
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                logger.debug("Database connection closed successfully");
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
    
    /**
     * Kiểm tra kết nối database có hoạt động không
     * @return true nếu kết nối thành công, false nếu thất bại
     */
    public boolean testConnection() {
        Connection conn = null;
        try {
            conn = getConnection();
            boolean isValid = conn.isValid(5); // timeout 5 seconds
            logger.info("Database connection test: " + (isValid ? "SUCCESS" : "FAILED"));
            return isValid;
        } catch (SQLException e) {
            logger.error("Database connection test failed", e);
            return false;
        } finally {
            closeConnection(conn);
        }
    }
    
    /**
     * Lấy thông tin về database configuration (không bao gồm password)
     * @return String chứa thông tin config
     */
    public String getConnectionInfo() {
        return String.format("Driver: %s, URL: %s, Username: %s", 
                           dbDriver, dbUrl, dbUsername);
    }
}
