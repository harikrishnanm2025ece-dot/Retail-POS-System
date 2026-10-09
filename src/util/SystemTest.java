package util;

import dao.CustomerDAO;
import dao.EmployeeDAO;
import dao.PaymentDAO;
import dao.ProductDAO;
import dao.SaleDAO;
import model.Customer;
import model.Employee;
import model.Payment;
import model.Product;
import model.Sale;
import model.SaleItem;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Diagnostic and Integration Test suite for Retail POS System.
 * Tests JDBC connection, database tables, and core DAO operations.
 */
public class SystemTest {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      RETAIL POS SYSTEM DIAGNOSTIC TESTS         ");
        System.out.println("=================================================");

        testDatabaseConnection();
        testDatabaseInitialization();
        testProductDAO();
        testCustomerDAO();
        testEmployeeDAO();
        testSaleTransactionAndRollback();
        testReportsQuery();

        System.out.println("\n=================================================");
        System.out.println(String.format("  TEST SUMMARY: %d PASSED, %d FAILED", testsPassed, testsFailed));
        System.out.println("=================================================");
    }

    private static void testDatabaseConnection() {
        System.out.print("[TEST 1] Testing JDBC Database Connection... ");
        try (Connection conn = DBConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("PASSED (Connected to " + DBConnection.getUrl() + ")");
                testsPassed++;
            } else {
                System.out.println("FAILED (Connection is null or closed)");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
            testsFailed++;
        }
    }

    private static void testDatabaseInitialization() {
        System.out.print("[TEST 2] Verifying Database Schema and Tables... ");
        try {
            boolean ready = DatabaseSetup.checkTablesExist();
            if (!ready) {
                System.out.println("Tables missing. Initializing now...");
                DatabaseSetup.initializeDatabase(true);
            }
            if (DatabaseSetup.checkTablesExist()) {
                System.out.println("PASSED (All 6 core tables verified)");
                testsPassed++;
            } else {
                System.out.println("FAILED (Could not verify all tables)");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
            testsFailed++;
        }
    }

    private static void testProductDAO() {
        System.out.print("[TEST 3] Testing ProductDAO (CRUD operations)... ");
        ProductDAO dao = new ProductDAO();
        String testBarcode = "TEST_BARCODE_" + System.currentTimeMillis();
        Product product = new Product("Test Biscuits", "Snacks", new BigDecimal("35.00"), 50, testBarcode);

        try {
            // Create
            boolean added = dao.addProduct(product);
            if (!added || product.getProductId() <= 0) {
                throw new RuntimeException("Failed to add product");
            }

            // Read
            Product fetched = dao.getProductByBarcode(testBarcode);
            if (fetched == null || !fetched.getName().equals("Test Biscuits")) {
                throw new RuntimeException("Failed to fetch product by barcode");
            }

            // Update
            fetched.setPrice(new BigDecimal("38.00"));
            boolean updated = dao.updateProduct(fetched);
            if (!updated) {
                throw new RuntimeException("Failed to update product");
            }

            // Delete
            boolean deleted = dao.deleteProduct(fetched.getProductId());
            if (!deleted) {
                throw new RuntimeException("Failed to delete test product");
            }

            System.out.println("PASSED");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
            testsFailed++;
        }
    }

    private static void testCustomerDAO() {
        System.out.print("[TEST 4] Testing CustomerDAO (CRUD operations)... ");
        CustomerDAO dao = new CustomerDAO();
        String testPhone = "999" + (System.currentTimeMillis() % 10000000);
        Customer customer = new Customer("Test Customer", testPhone, "test@pos.local");

        try {
            // Create
            boolean added = dao.registerCustomer(customer);
            if (!added || customer.getCustomerId() <= 0) {
                throw new RuntimeException("Failed to register customer");
            }

            // Read
            Customer fetched = dao.getCustomerByPhone(testPhone);
            if (fetched == null || !fetched.getName().equals("Test Customer")) {
                throw new RuntimeException("Failed to fetch customer by phone");
            }

            // Update
            fetched.setName("Updated Test Customer");
            boolean updated = dao.updateCustomer(fetched);
            if (!updated) {
                throw new RuntimeException("Failed to update customer");
            }

            // Delete
            boolean deleted = dao.deleteCustomer(fetched.getCustomerId());
            if (!deleted) {
                throw new RuntimeException("Failed to delete test customer");
            }

            System.out.println("PASSED");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
            testsFailed++;
        }
    }

    private static void testEmployeeDAO() {
        System.out.print("[TEST 5] Testing EmployeeDAO (CRUD operations)... ");
        EmployeeDAO dao = new EmployeeDAO();
        String testPhone = "988" + (System.currentTimeMillis() % 10000000);
        Employee employee = new Employee("Test Staff", "Cashier", testPhone);

        try {
            // Create
            boolean added = dao.addEmployee(employee);
            if (!added || employee.getEmployeeId() <= 0) {
                throw new RuntimeException("Failed to add employee");
            }

            // Read
            Employee fetched = dao.getEmployeeById(employee.getEmployeeId());
            if (fetched == null || !fetched.getName().equals("Test Staff")) {
                throw new RuntimeException("Failed to fetch employee by ID");
            }

            // Delete
            boolean deleted = dao.deleteEmployee(employee.getEmployeeId());
            if (!deleted) {
                throw new RuntimeException("Failed to delete test employee");
            }

            System.out.println("PASSED");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
            testsFailed++;
        }
    }

    private static void testSaleTransactionAndRollback() {
        System.out.print("[TEST 6] Testing Sale Transaction & Atomic Rollback... ");
        ProductDAO productDAO = new ProductDAO();
        EmployeeDAO employeeDAO = new EmployeeDAO();
        SaleDAO saleDAO = new SaleDAO();

        try {
            // Ensure at least one product and one employee exist
            List<Employee> employees = employeeDAO.getAllEmployees();
            if (employees.isEmpty()) {
                Employee tempEmp = new Employee("Default Cashier", "Cashier", "9000000000");
                employeeDAO.addEmployee(tempEmp);
                employees = employeeDAO.getAllEmployees();
            }
            int empId = employees.get(0).getEmployeeId();

            // Create temporary test product with 10 units
            String barcode = "TX_PROD_" + System.currentTimeMillis();
            Product p = new Product("Tx Test Product", "General", new BigDecimal("100.00"), 10, barcode);
            productDAO.addProduct(p);

            // 1. Test successful sale
            Sale sale = new Sale(saleDAO.generateInvoiceNumber(), null, empId, new BigDecimal("200.00"), "COMPLETED");
            List<SaleItem> items = new ArrayList<>();
            items.add(new SaleItem(p.getProductId(), 2, new BigDecimal("100.00"), new BigDecimal("200.00")));
            Payment payment = new Payment(0, "CASH", new BigDecimal("200.00"), "SUCCESS");

            boolean committed = saleDAO.createSale(sale, items, payment);
            if (!committed) {
                throw new RuntimeException("Normal sale creation failed to commit");
            }

            // Verify stock reduced from 10 to 8
            Product updatedProd = productDAO.getProductById(p.getProductId());
            if (updatedProd.getQuantity() != 8) {
                throw new RuntimeException("Stock was not properly decremented; expected 8, got " + updatedProd.getQuantity());
            }

            // 2. Test rollback scenario: Attempt to buy 15 units when only 8 are left
            Sale invalidSale = new Sale(saleDAO.generateInvoiceNumber(), null, empId, new BigDecimal("1500.00"), "COMPLETED");
            List<SaleItem> excessItems = new ArrayList<>();
            excessItems.add(new SaleItem(p.getProductId(), 15, new BigDecimal("100.00"), new BigDecimal("1500.00")));
            Payment invalidPayment = new Payment(0, "UPI", new BigDecimal("1500.00"), "SUCCESS");

            boolean rolledBack = !saleDAO.createSale(invalidSale, excessItems, invalidPayment);
            if (!rolledBack) {
                throw new RuntimeException("Transaction should have failed and rolled back when stock was insufficient");
            }

            // Verify stock remains intact at 8
            Product afterRollbackProd = productDAO.getProductById(p.getProductId());
            if (afterRollbackProd.getQuantity() != 8) {
                throw new RuntimeException("Rollback failed to preserve stock integrity");
            }

            // Cleanup test product
            productDAO.deleteProduct(p.getProductId());

            System.out.println("PASSED (Commit & Rollback verified)");
            testsPassed++;
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
            testsFailed++;
        }
    }

    private static void testReportsQuery() {
        System.out.print("[TEST 7] Testing Sales & Inventory Reports Query... ");
        try {
            SaleDAO saleDAO = new SaleDAO();
            ProductDAO productDAO = new ProductDAO();

            BigDecimal totalRevenue = saleDAO.getTotalSalesRevenue();
            List<Sale> todaySales = saleDAO.getDailySales(LocalDate.now());
            List<Product> lowStock = productDAO.getLowStockProducts(5);
            List<Product> outOfStock = productDAO.getOutOfStockProducts();

            if (totalRevenue != null && todaySales != null && lowStock != null && outOfStock != null) {
                System.out.println(String.format("PASSED (Revenue=₹%.2f, TodaySales=%d, LowStock=%d, OutOfStock=%d)",
                        totalRevenue, todaySales.size(), lowStock.size(), outOfStock.size()));
                testsPassed++;
            } else {
                System.out.println("FAILED (Null report results)");
                testsFailed++;
            }
        } catch (Exception e) {
            System.out.println("FAILED (" + e.getMessage() + ")");
            testsFailed++;
        }
    }
}
