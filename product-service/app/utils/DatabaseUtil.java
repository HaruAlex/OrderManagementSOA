package utils;

import com.typesafe.config.Config;
import play.Logger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utility class để quản lý kết nối database SQL Server
 */
@Singleton
public class DatabaseUtil {
    
    private static final Logger.ALogger logger = Logger.of(DatabaseUtil.class);
    
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    private final String dbDriver;
    
    @Inject
    public DatabaseUtil(Config config) {
        this.dbDriver = config.getString("db.default.driver");
        this.dbUrl = config.getString("db.default.url");
        this.dbUsername = config.getString("db.default.username");
        this.dbPassword = config.getString("db.default.password");
        
        try {
            Class.forName(dbDriver);
            logger.info("SQL Server JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            logger.error("SQL Server JDBC Driver not found", e);
            throw new RuntimeException("Failed to load database driver", e);
        }
    }
    
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }
    
    public void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                logger.error("Error closing database connection", e);
            }
        }
    }
}
