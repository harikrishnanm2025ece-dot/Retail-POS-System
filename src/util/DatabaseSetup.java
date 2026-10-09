package util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility to initialize database tables and seed sample data for Retail POS.
 * Can be run standalone or invoked during application setup.
 */
public class DatabaseSetup {

    private static final String CREATE_PRODUCTS_TABLE =
            "CREATE TABLE IF NOT EXISTS products (" +
            "    product_id INT AUTO_INCREMENT PRIMARY KEY," +
            "    name VARCHAR(100) NOT NULL," +
            "    category VARCHAR(50) NOT NULL," +
            "    price DECIMAL(10, 2) NOT NULL CHECK (price > 0)," +
            "    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0)," +
            "    barcode VARCHAR(50) NOT NULL UNIQUE," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ");";

    private static final String CREATE_CUSTOMERS_TABLE =
            "CREATE TABLE IF NOT EXISTS customers (" +
            "    customer_id INT AUTO_INCREMENT PRIMARY KEY," +
            "    name VARCHAR(100) NOT NULL," +
            "    phone VARCHAR(20) NOT NULL UNIQUE," +
            "    email VARCHAR(100)," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ");";

    private static final String CREATE_EMPLOYEES_TABLE =
            "CREATE TABLE IF NOT EXISTS employees (" +
            "    employee_id INT AUTO_INCREMENT PRIMARY KEY," +
            "    name VARCHAR(100) NOT NULL," +
            "    role VARCHAR(50) NOT NULL," +
            "    phone VARCHAR(20) NOT NULL," +
            "    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ");";

    private static final String CREATE_SALES_TABLE =
            "CREATE TABLE IF NOT EXISTS sales (" +
            "    sale_id INT AUTO_INCREMENT PRIMARY KEY," +
            "    invoice_no VARCHAR(50) NOT NULL UNIQUE," +
            "    customer_id INT NULL," +
            "    employee_id INT NOT NULL," +
            "    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0)," +
            "    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED'," +
            "    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL," +
            "    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)" +
            ");";

    private static final String CREATE_SALE_ITEMS_TABLE =
            "CREATE TABLE IF NOT EXISTS sale_items (" +
            "    item_id INT AUTO_INCREMENT PRIMARY KEY," +
            "    sale_id INT NOT NULL," +
            "    product_id INT NOT NULL," +
            "    quantity INT NOT NULL CHECK (quantity > 0)," +
            "    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0)," +
            "    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0)," +
            "    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE," +
            "    FOREIGN KEY (product_id) REFERENCES products(product_id)" +
            ");";

    private static final String CREATE_PAYMENTS_TABLE =
            "CREATE TABLE IF NOT EXISTS payments (" +
            "    payment_id INT AUTO_INCREMENT PRIMARY KEY," +
            "    sale_id INT NOT NULL UNIQUE," +
            "    payment_method VARCHAR(20) NOT NULL," +
            "    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0)," +
            "    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    payment_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS'," +
            "    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE" +
            ");";

    private static final String[] SAMPLE_PRODUCTS = {
            "INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES (1, 'Organic Whole Milk 1L', 'Dairy', 58.00, 45, '890123400001');",
            "INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES (2, 'Whole Wheat Bread 400g', 'Bakery', 42.50, 30, '890123400002');",
            "INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES (3, 'Dark Roast Coffee 200g', 'Beverages', 290.00, 20, '890123400003');",
            "INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES (4, 'Basmati Rice 5kg', 'Grains', 450.00, 15, '890123400004');",
            "INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES (5, 'Pure Olive Oil 1L', 'Oils', 680.00, 8, '890123400005');",
            "INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES (6, 'Green Tea 100g', 'Beverages', 185.00, 25, '890123400006');",
            "INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES (7, 'Rolled Oats 1kg', 'Breakfast', 220.00, 4, '890123400007');",
            "INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES (8, 'Roasted Almonds 500g', 'Snacks', 420.00, 0, '890123400008');"
    };

    private static final String[] SAMPLE_CUSTOMERS = {
            "INSERT IGNORE INTO customers (customer_id, name, phone, email) VALUES (1, 'Rahul Sharma', '9876543210', 'rahul.sharma@example.com');",
            "INSERT IGNORE INTO customers (customer_id, name, phone, email) VALUES (2, 'Priya Patel', '9812345678', 'priya.patel@example.com');",
            "INSERT IGNORE INTO customers (customer_id, name, phone, email) VALUES (3, 'Anand Kumar', '9701122334', 'anand.kumar@example.com');"
    };

    private static final String[] SAMPLE_EMPLOYEES = {
            "INSERT IGNORE INTO employees (employee_id, name, role, phone) VALUES (1, 'Vikram Singh', 'Store Manager', '9888112233');",
            "INSERT IGNORE INTO employees (employee_id, name, role, phone) VALUES (2, 'Sneha Roy', 'Cashier', '9888445566');",
            "INSERT IGNORE INTO employees (employee_id, name, role, phone) VALUES (3, 'Arun Das', 'Billing Associate', '9888778899');"
    };

    /**
     * Initializes all required tables and optionally seeds default sample records.
     *
     * @param seedData true if sample products, customers, and employees should be inserted
     * @return true if initialization completed without errors
     */
    public static boolean initializeDatabase(boolean seedData) {
        System.out.println("=================================================");
        System.out.println("  Initializing Retail POS Database (retail_pos)  ");
        System.out.println("=================================================");

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            System.out.println("[INFO] Creating table: products...");
            stmt.executeUpdate(CREATE_PRODUCTS_TABLE);

            System.out.println("[INFO] Creating table: customers...");
            stmt.executeUpdate(CREATE_CUSTOMERS_TABLE);

            System.out.println("[INFO] Creating table: employees...");
            stmt.executeUpdate(CREATE_EMPLOYEES_TABLE);

            System.out.println("[INFO] Creating table: sales...");
            stmt.executeUpdate(CREATE_SALES_TABLE);

            System.out.println("[INFO] Creating table: sale_items...");
            stmt.executeUpdate(CREATE_SALE_ITEMS_TABLE);

            System.out.println("[INFO] Creating table: payments...");
            stmt.executeUpdate(CREATE_PAYMENTS_TABLE);

            if (seedData) {
                System.out.println("[INFO] Seeding initial sample data...");
                for (String sql : SAMPLE_PRODUCTS) {
                    stmt.executeUpdate(sql);
                }
                for (String sql : SAMPLE_CUSTOMERS) {
                    stmt.executeUpdate(sql);
                }
                for (String sql : SAMPLE_EMPLOYEES) {
                    stmt.executeUpdate(sql);
                }
            }

            System.out.println("[SUCCESS] Database initialization completed successfully!");
            return true;

        } catch (SQLException e) {
            System.err.println("[ERROR] Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if all required tables exist in the database.
     *
     * @return true if all 6 tables exist
     */
    public static boolean checkTablesExist() {
        String[] tables = {"products", "customers", "employees", "sales", "sale_items", "payments"};
        try (Connection conn = DBConnection.getConnection()) {
            for (String tbl : tables) {
                try (ResultSet rs = conn.getMetaData().getTables(null, null, tbl, null)) {
                    if (!rs.next()) {
                        return false;
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Running DatabaseSetup...");
        boolean success = initializeDatabase(true);
        if (success) {
            System.out.println("Database tables and sample data are ready.");
        } else {
            System.err.println("Database setup encountered errors.");
        }
    }
}
