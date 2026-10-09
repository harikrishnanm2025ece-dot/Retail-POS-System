================================================================================
RETAIL POINT OF SALE (POS) MANAGEMENT SYSTEM
A Standalone Java + JDBC + MySQL Desktop Console Application
================================================================================

1. PROJECT OVERVIEW
--------------------------------------------------------------------------------
The Retail Point of Sale (POS) Management System is a robust, standalone console-
based application developed using Core Java, JDBC, and MySQL. It follows a clean
layered architecture (Model-DAO-Utility-Application) designed specifically for
academic, practical, and enterprise point-of-sale management without any heavy
frameworks (no Spring Boot, no Hibernate, no JPA).

Key Capabilities:
- Product Management (CRUD, low-stock & out-of-stock tracking, barcode support)
- Customer Management (registration, search, tracking purchase history)
- Employee Management (cashiers, managers, role tracking)
- Sales and Billing (multi-item shopping cart, real-time stock validation, 
  automatic subtotal/grand total calculation, professional receipt generation)
- Payment Management (Cash with change calculator, UPI, Card)
- Atomic Transaction Management (conn.setAutoCommit(false), commit(), rollback())
- Sales Reports & Inventory (daily sales, revenue analytics, low-stock alerts)


2. PROJECT DIRECTORY STRUCTURE
--------------------------------------------------------------------------------
Retail_POS/
│
├── .project                     (Eclipse project descriptor)
├── .classpath                   (Eclipse classpath with src and lib referenced)
├── database.sql                 (MySQL database schema and sample seed data)
├── db.properties                (Database connection URL, user, and password)
├── README.txt                   (Complete documentation and instructions)
│
├── lib/
│   └── mysql-connector-j-8.3.0.jar  (MySQL JDBC Driver library)
│
├── bin/                         (Compiled Java bytecode .class files)
│
└── src/
    ├── app/
    │   └── Main.java            (Menu-driven interactive console application)
    │
    ├── model/
    │   ├── Product.java         (Product entity model with price and stock)
    │   ├── Customer.java        (Customer entity model)
    │   ├── Employee.java        (Employee/Staff entity model)
    │   ├── Sale.java            (Sale header entity model)
    │   ├── SaleItem.java        (Sale item line model)
    │   └── Payment.java         (Payment transaction model)
    │
    ├── dao/
    │   ├── ProductDAO.java      (CRUD and inventory operations for products)
    │   ├── CustomerDAO.java     (CRUD and search operations for customers)
    │   ├── EmployeeDAO.java     (CRUD and search operations for employees)
    │   ├── SaleDAO.java         (Atomic sales transaction creation & reporting)
    │   ├── SaleItemDAO.java     (Line-item persistence and retrieval)
    │   └── PaymentDAO.java      (Payment logging and queries)
    │
    └── util/
        ├── DBConnection.java    (Centralized JDBC connection manager)
        ├── DatabaseSetup.java   (Automated schema migration & data seeder)
        └── SystemTest.java      (Automated diagnostic and integration tests)


3. DATABASE ARCHITECTURE (MySQL)
--------------------------------------------------------------------------------
Database Name: retail_pos

Tables:
1. `products`
   - `product_id`   : INT AUTO_INCREMENT PRIMARY KEY
   - `name`         : VARCHAR(100) NOT NULL
   - `category`     : VARCHAR(50) NOT NULL
   - `price`        : DECIMAL(10, 2) NOT NULL (CHECK price > 0)
   - `quantity`     : INT NOT NULL DEFAULT 0 (CHECK quantity >= 0)
   - `barcode`      : VARCHAR(50) NOT NULL UNIQUE
   - `created_at`   : TIMESTAMP DEFAULT CURRENT_TIMESTAMP

2. `customers`
   - `customer_id`  : INT AUTO_INCREMENT PRIMARY KEY
   - `name`         : VARCHAR(100) NOT NULL
   - `phone`        : VARCHAR(20) NOT NULL UNIQUE
   - `email`        : VARCHAR(100)
   - `created_at`   : TIMESTAMP DEFAULT CURRENT_TIMESTAMP

3. `employees`
   - `employee_id`  : INT AUTO_INCREMENT PRIMARY KEY
   - `name`         : VARCHAR(100) NOT NULL
   - `role`         : VARCHAR(50) NOT NULL
   - `phone`        : VARCHAR(20) NOT NULL
   - `created_at`   : TIMESTAMP DEFAULT CURRENT_TIMESTAMP

4. `sales`
   - `sale_id`      : INT AUTO_INCREMENT PRIMARY KEY
   - `invoice_no`   : VARCHAR(50) NOT NULL UNIQUE
   - `customer_id`  : INT NULL (FK -> customers.customer_id, ON DELETE SET NULL)
   - `employee_id`  : INT NOT NULL (FK -> employees.employee_id)
   - `sale_date`    : TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - `total_amount` : DECIMAL(10, 2) NOT NULL (CHECK total_amount >= 0)
   - `status`       : VARCHAR(20) NOT NULL DEFAULT 'COMPLETED'

5. `sale_items`
   - `item_id`      : INT AUTO_INCREMENT PRIMARY KEY
   - `sale_id`      : INT NOT NULL (FK -> sales.sale_id, ON DELETE CASCADE)
   - `product_id`   : INT NOT NULL (FK -> products.product_id)
   - `quantity`     : INT NOT NULL (CHECK quantity > 0)
   - `unit_price`   : DECIMAL(10, 2) NOT NULL
   - `subtotal`     : DECIMAL(10, 2) NOT NULL

6. `payments`
   - `payment_id`   : INT AUTO_INCREMENT PRIMARY KEY
   - `sale_id`      : INT NOT NULL UNIQUE (FK -> sales.sale_id, ON DELETE CASCADE)
   - `payment_method`: VARCHAR(20) NOT NULL ('CASH', 'UPI', 'CARD')
   - `amount`       : DECIMAL(10, 2) NOT NULL (CHECK amount > 0)
   - `payment_date` : TIMESTAMP DEFAULT CURRENT_TIMESTAMP
   - `payment_status`: VARCHAR(20) NOT NULL DEFAULT 'SUCCESS'


4. DATABASE PRIVILEGES & PREREQUISITES
--------------------------------------------------------------------------------
1. Ensure MySQL Server is installed and running (default port: 3306).
2. The configured MySQL user account (default: 'root') must possess:
   - CREATE, ALTER, DROP, SELECT, INSERT, UPDATE, DELETE privileges.
3. Does the database need to be created manually?
   - NO: The JDBC connection URL includes `createDatabaseIfNotExist=true`, and
     `DatabaseSetup.java` automatically runs on application startup to create 
     the database and all required tables if they are not already present.
   - Alternatively, you can run `database.sql` directly inside MySQL Workbench
     or MySQL Command Line Client:
     mysql -u root -p < database.sql


5. CONFIGURING `db.properties`
--------------------------------------------------------------------------------
Update the database connection settings in `db.properties` if needed:

db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/retail_pos?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
db.username=root
db.password=YourPasswordHere


6. ECLIPSE IDE SETUP INSTRUCTIONS
--------------------------------------------------------------------------------
Step 1: Open Eclipse IDE.
Step 2: Go to `File` -> `Open Projects from File System...` (or `Import...` -> 
        `Existing Projects into Workspace`).
Step 3: Click `Directory...` and browse to `D:\RETAIL POS` (or where the project 
        folder is located). Click `Finish`.
Step 4: Adding MySQL Connector/J JAR (Already configured in .classpath):
        If you need to configure the JAR manually:
        a. Right-click on the `Retail_POS` project in Project Explorer.
        b. Select `Build Path` -> `Configure Build Path...`.
        c. Go to the `Libraries` tab.
        d. Select `Classpath`, then click `Add JARs...` (or `Add External JARs...`).
        e. Select `lib/mysql-connector-j-8.3.0.jar` from inside the project folder.
        f. Click `Apply and Close`.
Step 5: Run the Project:
        - To run diagnostics & setup: Right-click `src/util/SystemTest.java` 
          -> `Run As` -> `Java Application`.
        - To run the POS application: Right-click `src/app/Main.java` 
          -> `Run As` -> `Java Application`.


7. RUNNING FROM COMMAND LINE / TERMINAL
--------------------------------------------------------------------------------
If running on Windows PowerShell or Command Prompt:

1. Compile the project:
   cmd /c "if not exist bin mkdir bin && javac -cp lib/mysql-connector-j-8.3.0.jar;src -d bin src/model/*.java src/dao/*.java src/util/*.java src/app/*.java"

2. Run Automated System Diagnostic Test:
   java -cp "bin;lib/mysql-connector-j-8.3.0.jar" util.SystemTest

3. Run the Retail POS Main Console Application:
   java -cp "bin;lib/mysql-connector-j-8.3.0.jar" app.Main


8. SUMMARY OF PACKAGES AND CLASSES
--------------------------------------------------------------------------------
Package: `app`
- `Main.java`:
  Interactive console application providing 7 top-level menus and rich submenus.
  Handles user input validation, terminal table rendering, billing workflow,
  and receipt printing.

Package: `model`
- `Product.java`  : Represents inventory items (ID, name, category, price, stock, barcode).
- `Customer.java` : Represents store customers (ID, name, phone, email).
- `Employee.java` : Represents store employees (ID, name, role, phone).
- `Sale.java`     : Represents sales transactions (sale ID, invoice no, customer ID, employee ID, total, status).
- `SaleItem.java` : Represents individual item lines inside a sale transaction.
- `Payment.java`  : Represents payments linked to sales transactions (method, amount, date, status).

Package: `dao`
- `ProductDAO.java` : CRUD for products, search by ID/name/barcode, low-stock & out-of-stock queries,
  atomic stock deduction helper.
- `CustomerDAO.java`: CRUD for customers, search by ID/phone/name.
- `EmployeeDAO.java`: CRUD for employees, search by ID/name.
- `SaleDAO.java`    : Creates atomic sales transactions with commit and rollback, daily sales report,
  total sales revenue, invoice generation.
- `SaleItemDAO.java`: Inserts line items inside transactions and fetches line items by sale ID.
- `PaymentDAO.java` : Records payments inside transactions and queries payment histories.

Package: `util`
- `DBConnection.java` : Thread-safe JDBC connection supplier loading `db.properties`.
- `DatabaseSetup.java`: DDL schema creator and seed data populator.
- `SystemTest.java`   : Full automated suite testing connection, tables, DAO CRUD, and rollback.


9. CRUD OPERATIONS IMPLEMENTED IN EACH DAO
--------------------------------------------------------------------------------
1. ProductDAO:
   - Create : addProduct(Product product)
   - Read   : getAllProducts(), getProductById(int id), getProductByBarcode(String barcode),
              searchProductsByName(String keyword), getLowStockProducts(int threshold),
              getOutOfStockProducts(), isStockAvailable(int productId, int quantity)
   - Update : updateProduct(Product product), deductStock(Connection conn, int productId, int qty)
   - Delete : deleteProduct(int productId)

2. CustomerDAO:
   - Create : registerCustomer(Customer customer)
   - Read   : getAllCustomers(), getCustomerById(int id), getCustomerByPhone(String phone),
              searchCustomersByName(String name)
   - Update : updateCustomer(Customer customer)
   - Delete : deleteCustomer(int customerId)

3. EmployeeDAO:
   - Create : addEmployee(Employee employee)
   - Read   : getAllEmployees(), getEmployeeById(int id), searchEmployeesByName(String name)
   - Update : updateEmployee(Employee employee)
   - Delete : deleteEmployee(int employeeId)

4. SaleDAO:
   - Create : createSale(Sale sale, List<SaleItem> items, Payment payment) [Atomic with commit/rollback]
   - Read   : getAllSales(), getSaleById(int saleId), getSaleByInvoiceNo(String invoiceNo),
              getDailySales(LocalDate date), getTotalSalesRevenue()
   - Update : Implicit status updates
   - Delete : Cascade deletion via MySQL relational constraints

5. SaleItemDAO:
   - Create : addItem(Connection conn, SaleItem item)
   - Read   : getItemsBySaleId(int saleId)

6. PaymentDAO:
   - Create : recordPayment(Connection conn, Payment payment), recordPayment(Payment payment)
   - Read   : getAllPayments(), getPaymentById(int paymentId), getPaymentBySaleId(int saleId)


10. TRANSACTION MANAGEMENT & ROLLBACK IMPLEMENTATION
--------------------------------------------------------------------------------
In `SaleDAO.java`:
1. Connection obtained via `DBConnection.getConnection()`.
2. Auto-commit disabled: `conn.setAutoCommit(false);`
3. Operations performed in order:
   a. Insert sale record into `sales` table -> retrieve generated `sale_id`.
   b. Iterate each `SaleItem` in cart:
      - Insert item into `sale_items`.
      - Deduct stock with validation:
        `UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?`
      - If affected rows is 0 (insufficient stock), throws `SQLException`.
   c. Insert payment record into `payments` table.
4. If all succeed:
   `conn.commit();`
5. If any step fails:
   `conn.rollback();`
   Stock remains unchanged, sale items are discarded, and payment is not recorded.
6. In `finally` block:
   `conn.setAutoCommit(true); conn.close();`


11. EXAMPLE CONSOLE OUTPUT
--------------------------------------------------------------------------------

Example A: Adding a New Product
-----------------------------------------------------------------------
=======================================================
        RETAIL POINT OF SALE (POS) SYSTEM              
=======================================================
  1. Product Management
  2. Customer Management
  3. Employee Management
  4. Sales and Billing
  5. Payment Management
  6. Sales Reports and Inventory
  7. Exit
=======================================================
Select an option (1-7): 1

-------------------------------------------------------
              PRODUCT MANAGEMENT                       
-------------------------------------------------------
  1. Add New Product
  2. View All Products
  3. Search Product by ID
  4. Search Product by Name
  5. Search Product by Barcode
  6. Update Product Details
  7. Delete Product
  8. Back to Main Menu
-------------------------------------------------------
Select option (1-8): 1

--- Add New Product ---
Enter Product Name: Dark Roast Coffee 200g
Enter Category: Beverages
Enter Price (> 0): ₹290.00
Enter Initial Quantity (>= 0): 25
Enter Unique Barcode: 890123400010
[SUCCESS] Product added successfully with ID: 9


Example B: Completing a Sales & Billing Transaction
-----------------------------------------------------------------------
Select an option (1-7): 4

-------------------------------------------------------
              SALES AND BILLING                        
-------------------------------------------------------
  1. Create New Sale (Billing / Checkout)
  2. View Recent Sales
  3. View Invoice by Invoice Number
  4. View Invoice by Sale ID
  5. Back to Main Menu
-------------------------------------------------------
Select option (1-5): 1

=======================================================
             NEW SALES TRANSACTION                     
=======================================================

Available Cashiers/Employees:
  [1] Vikram Singh (Store Manager)
  [2] Sneha Roy (Cashier)
  [3] Arun Das (Billing Associate)
Enter Cashier/Employee ID: 2

Customer Selection:
  1. Walk-in Customer (No registration)
  2. Existing Registered Customer
  3. Register New Customer Now
Select option (1-3): 1
[Selected] Walk-in Customer.

--- Add Product to Bill ---
Search by: 1. Product ID | 2. Barcode | 3. Name Search
Select search mode (1-3): 2
Enter Barcode: 890123400001
Selected: Organic Whole Milk 1L | Price: ₹58.00 | Available Stock: 45
Enter Quantity (Max available 45): 2
Added 2 x Organic Whole Milk 1L. Current Bill Total: ₹116.00
Add another product? (Y/N): Y

--- Add Product to Bill ---
Search by: 1. Product ID | 2. Barcode | 3. Name Search
Select search mode (1-3): 1
Enter Product ID: 2
Selected: Whole Wheat Bread 400g | Price: ₹42.50 | Available Stock: 30
Enter Quantity (Max available 30): 1
Added 1 x Whole Wheat Bread 400g. Current Bill Total: ₹158.50
Add another product? (Y/N): N

-------------------------------------------------------
                   BILL SUMMARY                        
-------------------------------------------------------
+----+--------------------------------+-------+------------+------------+
| #  | Item Name                      | Qty   | Unit Price | Line Total |
+----+--------------------------------+-------+------------+------------+
| 1  | Organic Whole Milk 1L          | 2     |      58.00 |     116.00 |
| 2  | Whole Wheat Bread 400g         | 1     |      42.50 |      42.50 |
+----+--------------------------------+-------+------------+------------+
| GRAND TOTAL:                                         ₹    158.50 |
+-----------------------------------------------------------------------+

--- Select Payment Method ---
  1. CASH
  2. UPI
  3. CARD
Select payment option (1-3): 1
Enter Cash Tendered by Customer: ₹200.00
[Cash] Received: ₹200.00 | Change to Return: ₹41.50

[Processing] Finalizing sale and updating inventory stock...

[SUCCESS] Transaction completed and committed successfully!

==========================================================================
                         RETAIL STORE INVOICE                             
                     Retail POS Management System                         
==========================================================================
 Invoice No: INV-20261009-084210       Date: 2026-10-09 12:12:01.0
 Cashier   : Sneha Roy                 Status: COMPLETED
 Customer  : Walk-in Customer           
--------------------------------------------------------------------------
 Item Name                       Barcode         Qty   Price(₹)   Total(₹)
--------------------------------------------------------------------------
 Organic Whole Milk 1L           890123400001    2        58.00      116.00
 Whole Wheat Bread 400g          890123400002    1        42.50       42.50
--------------------------------------------------------------------------
 TOTAL AMOUNT:                                                 ₹    158.50
 Payment Method: CASH                 Paid Amount:  ₹    158.50
 Change Returned:                                              ₹     41.50
==========================================================================
                  Thank you for shopping with us!                         
==========================================================================
================================================================================
