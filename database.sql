-- ============================================================
-- RETAIL POINT OF SALE (POS) MANAGEMENT SYSTEM DATABASE SCRIPT
-- Database: retail_pos
-- ============================================================

CREATE DATABASE IF NOT EXISTS retail_pos;
USE retail_pos;

-- ------------------------------------------------------------
-- 1. Table: products
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10, 2) NOT NULL CHECK (price > 0),
    quantity INT NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    barcode VARCHAR(50) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- 2. Table: customers
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    customer_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- 3. Table: employees
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS employees (
    employee_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- 4. Table: sales
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    invoice_no VARCHAR(50) NOT NULL UNIQUE,
    customer_id INT NULL,
    employee_id INT NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL CHECK (total_amount >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
    FOREIGN KEY (customer_id) REFERENCES customers(customer_id) ON DELETE SET NULL,
    FOREIGN KEY (employee_id) REFERENCES employees(employee_id)
);

-- ------------------------------------------------------------
-- 5. Table: sale_items
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sale_items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10, 2) NOT NULL CHECK (unit_price >= 0),
    subtotal DECIMAL(10, 2) NOT NULL CHECK (subtotal >= 0),
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id)
);

-- ------------------------------------------------------------
-- 6. Table: payments
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NOT NULL UNIQUE,
    payment_method VARCHAR(20) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL CHECK (amount > 0),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'SUCCESS',
    FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE
);

-- ============================================================
-- SAMPLE DATA (Initial records for testing)
-- ============================================================

-- Sample Products
INSERT IGNORE INTO products (product_id, name, category, price, quantity, barcode) VALUES
(1, ' Milk 1L', 'Dairy', 58.00, 45, '890123400001'),
(2, ' Bread 400g', 'Bakery', 42.50, 30, '890123400002'),
(3, ' Coffee 200g', 'Beverages', 290.00, 20, '890123400003'),
(4, 'Rice 5kg', 'Grains', 450.00, 15, '890123400004'),
(5, ' Oil 1L', 'Oils', 680.00, 8, '890123400005'),
(6, 'Tea 100g', 'Beverages', 185.00, 25, '890123400006'),
(7, 'Oats 1kg', 'Breakfast', 220.00, 4, '890123400007'),
(8, ' Almonds 500g', 'Snacks', 420.00, 0, '890123400008');

-- Sample Customers
INSERT IGNORE INTO customers (customer_id, name, phone, email) VALUES
(1, 'Rahul ', '9876543210', 'rahul.sharma@example.com'),
(2, 'Priya ', '9812345678', 'priya.patel@example.com'),
(3, 'Anand Kumar', '9701122334', 'anand.kumar@example.com');

-- Sample Employees
INSERT IGNORE INTO employees (employee_id, name, role, phone) VALUES
(1, 'Vikram', 'Store Manager', '9888112233'),
(2, 'chiranjeevi', 'Cashier', '9888445566'),
(3, 'Arjun Das', 'Billing Associate', '9888778899');
