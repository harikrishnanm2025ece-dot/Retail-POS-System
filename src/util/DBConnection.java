package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Utility class for establishing and managing MySQL JDBC database connections.
 * Reads configuration parameters from db.properties.
 */
public class DBConnection {

    private static String url;
    private static String username;
    private static String password;
    private static String driver;
    private static boolean initialized = false;

    static {
        loadProperties();
    }

    private static synchronized void loadProperties() {
        if (initialized) {
            return;
        }

        Properties props = new Properties();
        InputStream is = null;

        try {
            // First check root working directory
            File file = new File("db.properties");
            if (file.exists()) {
                is = new FileInputStream(file);
            } else {
                // Check classpath
                is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
                if (is == null) {
                    // Try looking in parent directory (in case running from bin/ or src/)
                    File parentFile = new File("../db.properties");
                    if (parentFile.exists()) {
                        is = new FileInputStream(parentFile);
                    }
                }
            }

            if (is != null) {
                props.load(is);
                driver = props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver");
                url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/retail_pos?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
                username = props.getProperty("db.username", "root");
                password = props.getProperty("db.password", "");
            } else {
                System.err.println("[WARN] db.properties file not found. Falling back to default settings.");
                driver = "com.mysql.cj.jdbc.Driver";
                url = "jdbc:mysql://localhost:3306/retail_pos?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
                username = "root";
                password = "";
            }

            // Register JDBC driver
            Class.forName(driver);
            initialized = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[ERROR] MySQL JDBC Driver not found in classpath: " + e.getMessage());
            System.err.println("Please ensure mysql-connector-j-*.jar is added to the project build path.");
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to load db.properties: " + e.getMessage());
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Obtains a new database connection using properties from db.properties.
     *
     * @return Connection active JDBC connection
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!initialized) {
            loadProperties();
        }
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Tests whether the database connection can be established successfully.
     *
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("[DBConnection Test Failed] " + e.getMessage());
            return false;
        }
    }

    public static String getUrl() {
        return url;
    }

    public static String getUsername() {
        return username;
    }
}
