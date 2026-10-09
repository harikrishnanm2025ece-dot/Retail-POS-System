package app;

import dao.CustomerDAO;
import dao.EmployeeDAO;
import dao.PaymentDAO;
import dao.ProductDAO;
import dao.SaleDAO;
import dao.SaleItemDAO;
import model.Customer;
import model.Employee;
import model.Payment;
import model.Product;
import model.Sale;
import model.SaleItem;
import util.DatabaseSetup;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based Entry Point for Retail Point of Sale (POS) Management System.
 * Provides intuitive, menu-driven navigation for Products, Customers, Employees,
 * Billing & Sales, Payments, and Inventory & Sales Reporting.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final ProductDAO productDAO = new ProductDAO();
    private static final CustomerDAO customerDAO = new CustomerDAO();
    private static final EmployeeDAO employeeDAO = new EmployeeDAO();
    private static final SaleDAO saleDAO = new SaleDAO();
    private static final SaleItemDAO saleItemDAO = new SaleItemDAO();
    private static final PaymentDAO paymentDAO = new PaymentDAO();

    public static void main(String[] args) {
        // Automatically check if database tables exist on startup
        if (!DatabaseSetup.checkTablesExist()) {
            System.out.println("First-time setup detected. Initializing database schema...");
            DatabaseSetup.initializeDatabase(true);
        }

        boolean exit = false;
        while (!exit) {
            printMainMenu();
            int choice = readInt("Select an option (1-7): ", 1, 7);

            switch (choice) {
                case 1:
                    handleProductManagement();
                    break;
                case 2:
                    handleCustomerManagement();
                    break;
                case 3:
                    handleEmployeeManagement();
                    break;
                case 4:
                    handleSalesAndBilling();
                    break;
                case 5:
                    handlePaymentManagement();
                    break;
                case 6:
                    handleReportsAndInventory();
                    break;
                case 7:
                    exit = true;
                    System.out.println("\n=======================================================");
                    System.out.println("  Thank you for using Retail POS System. Goodbye!     ");
                    System.out.println("=======================================================\n");
                    break;
            }
        }
    }

    // =========================================================================
    // MAIN MENU
    // =========================================================================

    private static void printMainMenu() {
        System.out.println("\n=======================================================");
        System.out.println("        RETAIL POINT OF SALE (POS) SYSTEM              ");
        System.out.println("=======================================================");
        System.out.println("  1. Product Management");
        System.out.println("  2. Customer Management");
        System.out.println("  3. Employee Management");
        System.out.println("  4. Sales and Billing");
        System.out.println("  5. Payment Management");
        System.out.println("  6. Sales Reports and Inventory");
        System.out.println("  7. Exit");
        System.out.println("=======================================================");
    }

    // =========================================================================
    // FEATURE 1: PRODUCT MANAGEMENT
    // =========================================================================

    private static void handleProductManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-------------------------------------------------------");
            System.out.println("              PRODUCT MANAGEMENT                       ");
            System.out.println("-------------------------------------------------------");
            System.out.println("  1. Add New Product");
            System.out.println("  2. View All Products");
            System.out.println("  3. Search Product by ID");
            System.out.println("  4. Search Product by Name");
            System.out.println("  5. Search Product by Barcode");
            System.out.println("  6. Update Product Details");
            System.out.println("  7. Delete Product");
            System.out.println("  8. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            int choice = readInt("Select option (1-8): ", 1, 8);
            switch (choice) {
                case 1:
                    addProduct();
                    break;
                case 2:
                    viewAllProducts();
                    break;
                case 3:
                    searchProductById();
                    break;
                case 4:
                    searchProductByName();
                    break;
                case 5:
                    searchProductByBarcode();
                    break;
                case 6:
                    updateProduct();
                    break;
                case 7:
                    deleteProduct();
                    break;
                case 8:
                    back = true;
                    break;
            }
        }
    }

    private static void addProduct() {
        System.out.println("\n--- Add New Product ---");
        String name = readNonEmptyString("Enter Product Name: ");
        String category = readNonEmptyString("Enter Category: ");
        BigDecimal price = readPositiveBigDecimal("Enter Price (> 0): ₹");
        int quantity = readNonNegativeInt("Enter Initial Quantity (>= 0): ");
        String barcode = readNonEmptyString("Enter Unique Barcode: ");

        // Check if barcode already exists
        if (productDAO.getProductByBarcode(barcode) != null) {
            System.err.println("[Error] A product with barcode '" + barcode + "' already exists!");
            return;
        }

        Product p = new Product(name, category, price, quantity, barcode);
        boolean success = productDAO.addProduct(p);
        if (success) {
            System.out.println("[SUCCESS] Product added successfully with ID: " + p.getProductId());
        } else {
            System.err.println("[FAILED] Unable to add product. Please check your input.");
        }
    }

    private static void viewAllProducts() {
        System.out.println("\n--- All Products ---");
        List<Product> products = productDAO.getAllProducts();
        printProductTable(products);
    }

    private static void searchProductById() {
        int id = readPositiveInt("Enter Product ID to search: ");
        Product p = productDAO.getProductById(id);
        if (p != null) {
            List<Product> list = new ArrayList<>();
            list.add(p);
            printProductTable(list);
        } else {
            System.out.println("No product found with ID: " + id);
        }
    }

    private static void searchProductByName() {
        String keyword = readNonEmptyString("Enter Product Name or Keyword: ");
        List<Product> list = productDAO.searchProductsByName(keyword);
        if (list.isEmpty()) {
            System.out.println("No products found matching: " + keyword);
        } else {
            printProductTable(list);
        }
    }

    private static void searchProductByBarcode() {
        String barcode = readNonEmptyString("Enter Barcode to search: ");
        Product p = productDAO.getProductByBarcode(barcode);
        if (p != null) {
            List<Product> list = new ArrayList<>();
            list.add(p);
            printProductTable(list);
        } else {
            System.out.println("No product found with barcode: " + barcode);
        }
    }

    private static void updateProduct() {
        int id = readPositiveInt("Enter Product ID to update: ");
        Product existing = productDAO.getProductById(id);
        if (existing == null) {
            System.err.println("[Error] Product not found with ID: " + id);
            return;
        }

        System.out.println("Current details: " + existing);
        System.out.println("(Press Enter to keep current value)");

        System.out.print("New Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) existing.setName(name);

        System.out.print("New Category [" + existing.getCategory() + "]: ");
        String cat = scanner.nextLine().trim();
        if (!cat.isEmpty()) existing.setCategory(cat);

        System.out.print("New Price [₹" + existing.getPrice() + "]: ");
        String priceStr = scanner.nextLine().trim();
        if (!priceStr.isEmpty()) {
            try {
                BigDecimal p = new BigDecimal(priceStr);
                if (p.compareTo(BigDecimal.ZERO) > 0) {
                    existing.setPrice(p);
                } else {
                    System.out.println("[Warning] Price must be > 0. Retaining previous price.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[Warning] Invalid number format. Retaining previous price.");
            }
        }

        System.out.print("New Quantity [" + existing.getQuantity() + "]: ");
        String qtyStr = scanner.nextLine().trim();
        if (!qtyStr.isEmpty()) {
            try {
                int q = Integer.parseInt(qtyStr);
                if (q >= 0) {
                    existing.setQuantity(q);
                } else {
                    System.out.println("[Warning] Quantity must be >= 0. Retaining previous quantity.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[Warning] Invalid number format. Retaining previous quantity.");
            }
        }

        System.out.print("New Barcode [" + existing.getBarcode() + "]: ");
        String barcode = scanner.nextLine().trim();
        if (!barcode.isEmpty()) existing.setBarcode(barcode);

        boolean updated = productDAO.updateProduct(existing);
        if (updated) {
            System.out.println("[SUCCESS] Product details updated successfully!");
        } else {
            System.err.println("[FAILED] Product update failed.");
        }
    }

    private static void deleteProduct() {
        int id = readPositiveInt("Enter Product ID to delete: ");
        Product existing = productDAO.getProductById(id);
        if (existing == null) {
            System.err.println("[Error] Product not found with ID: " + id);
            return;
        }

        System.out.print("Are you sure you want to delete '" + existing.getName() + "'? (Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("Y")) {
            boolean deleted = productDAO.deleteProduct(id);
            if (deleted) {
                System.out.println("[SUCCESS] Product deleted successfully.");
            } else {
                System.err.println("[FAILED] Could not delete product. It may be linked to previous sales records.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void printProductTable(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.println("+------+--------------------------------+-----------------+------------+-------+-----------------+");
        System.out.println("| ID   | Name                           | Category        | Price (₹)  | Stock | Barcode         |");
        System.out.println("+------+--------------------------------+-----------------+------------+-------+-----------------+");
        for (Product p : products) {
            System.out.printf("| %-4d | %-30s | %-15s | %10.2f | %5d | %-15s |\n",
                    p.getProductId(),
                    truncate(p.getName(), 30),
                    truncate(p.getCategory(), 15),
                    p.getPrice(),
                    p.getQuantity(),
                    truncate(p.getBarcode(), 15));
        }
        System.out.println("+------+--------------------------------+-----------------+------------+-------+-----------------+");
    }

    // =========================================================================
    // FEATURE 2: CUSTOMER MANAGEMENT
    // =========================================================================

    private static void handleCustomerManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-------------------------------------------------------");
            System.out.println("              CUSTOMER MANAGEMENT                      ");
            System.out.println("-------------------------------------------------------");
            System.out.println("  1. Register New Customer");
            System.out.println("  2. View All Customers");
            System.out.println("  3. Search Customer by ID");
            System.out.println("  4. Search Customer by Phone");
            System.out.println("  5. Search Customer by Name");
            System.out.println("  6. Update Customer Details");
            System.out.println("  7. Delete Customer");
            System.out.println("  8. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            int choice = readInt("Select option (1-8): ", 1, 8);
            switch (choice) {
                case 1:
                    registerCustomer();
                    break;
                case 2:
                    viewAllCustomers();
                    break;
                case 3:
                    searchCustomerById();
                    break;
                case 4:
                    searchCustomerByPhone();
                    break;
                case 5:
                    searchCustomerByName();
                    break;
                case 6:
                    updateCustomer();
                    break;
                case 7:
                    deleteCustomer();
                    break;
                case 8:
                    back = true;
                    break;
            }
        }
    }

    private static void registerCustomer() {
        System.out.println("\n--- Register New Customer ---");
        String name = readNonEmptyString("Enter Customer Name: ");
        String phone = readNonEmptyString("Enter Phone Number: ");
        System.out.print("Enter Email Address (optional): ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) email = null;

        if (customerDAO.getCustomerByPhone(phone) != null) {
            System.err.println("[Error] A customer with phone number '" + phone + "' already exists!");
            return;
        }

        Customer c = new Customer(name, phone, email);
        boolean success = customerDAO.registerCustomer(c);
        if (success) {
            System.out.println("[SUCCESS] Customer registered successfully with ID: " + c.getCustomerId());
        } else {
            System.err.println("[FAILED] Unable to register customer.");
        }
    }

    private static void viewAllCustomers() {
        System.out.println("\n--- All Customers ---");
        List<Customer> customers = customerDAO.getAllCustomers();
        printCustomerTable(customers);
    }

    private static void searchCustomerById() {
        int id = readPositiveInt("Enter Customer ID: ");
        Customer c = customerDAO.getCustomerById(id);
        if (c != null) {
            List<Customer> list = new ArrayList<>();
            list.add(c);
            printCustomerTable(list);
        } else {
            System.out.println("No customer found with ID: " + id);
        }
    }

    private static void searchCustomerByPhone() {
        String phone = readNonEmptyString("Enter Phone Number: ");
        Customer c = customerDAO.getCustomerByPhone(phone);
        if (c != null) {
            List<Customer> list = new ArrayList<>();
            list.add(c);
            printCustomerTable(list);
        } else {
            System.out.println("No customer found with phone: " + phone);
        }
    }

    private static void searchCustomerByName() {
        String name = readNonEmptyString("Enter Customer Name or Keyword: ");
        List<Customer> list = customerDAO.searchCustomersByName(name);
        if (list.isEmpty()) {
            System.out.println("No customers found matching: " + name);
        } else {
            printCustomerTable(list);
        }
    }

    private static void updateCustomer() {
        int id = readPositiveInt("Enter Customer ID to update: ");
        Customer existing = customerDAO.getCustomerById(id);
        if (existing == null) {
            System.err.println("[Error] Customer not found with ID: " + id);
            return;
        }

        System.out.println("Current details: " + existing);
        System.out.println("(Press Enter to keep current value)");

        System.out.print("New Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) existing.setName(name);

        System.out.print("New Phone [" + existing.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) existing.setPhone(phone);

        System.out.print("New Email [" + (existing.getEmail() != null ? existing.getEmail() : "") + "]: ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty()) existing.setEmail(email);

        boolean updated = customerDAO.updateCustomer(existing);
        if (updated) {
            System.out.println("[SUCCESS] Customer updated successfully!");
        } else {
            System.err.println("[FAILED] Customer update failed.");
        }
    }

    private static void deleteCustomer() {
        int id = readPositiveInt("Enter Customer ID to delete: ");
        Customer existing = customerDAO.getCustomerById(id);
        if (existing == null) {
            System.err.println("[Error] Customer not found with ID: " + id);
            return;
        }

        System.out.print("Are you sure you want to delete customer '" + existing.getName() + "'? (Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("Y")) {
            boolean deleted = customerDAO.deleteCustomer(id);
            if (deleted) {
                System.out.println("[SUCCESS] Customer deleted successfully.");
            } else {
                System.err.println("[FAILED] Could not delete customer.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void printCustomerTable(List<Customer> customers) {
        if (customers.isEmpty()) {
            System.out.println("No customers registered.");
            return;
        }
        System.out.println("+------+---------------------------+---------------+--------------------------------+");
        System.out.println("| ID   | Name                      | Phone         | Email                          |");
        System.out.println("+------+---------------------------+---------------+--------------------------------+");
        for (Customer c : customers) {
            System.out.printf("| %-4d | %-25s | %-13s | %-30s |\n",
                    c.getCustomerId(),
                    truncate(c.getName(), 25),
                    truncate(c.getPhone(), 13),
                    truncate(c.getEmail() != null ? c.getEmail() : "N/A", 30));
        }
        System.out.println("+------+---------------------------+---------------+--------------------------------+");
    }

    // =========================================================================
    // FEATURE 3: EMPLOYEE MANAGEMENT
    // =========================================================================

    private static void handleEmployeeManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-------------------------------------------------------");
            System.out.println("              EMPLOYEE MANAGEMENT                      ");
            System.out.println("-------------------------------------------------------");
            System.out.println("  1. Add Employee");
            System.out.println("  2. View All Employees");
            System.out.println("  3. Search Employee by ID");
            System.out.println("  4. Search Employee by Name");
            System.out.println("  5. Update Employee Details");
            System.out.println("  6. Delete Employee");
            System.out.println("  7. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            int choice = readInt("Select option (1-7): ", 1, 7);
            switch (choice) {
                case 1:
                    addEmployee();
                    break;
                case 2:
                    viewAllEmployees();
                    break;
                case 3:
                    searchEmployeeById();
                    break;
                case 4:
                    searchEmployeeByName();
                    break;
                case 5:
                    updateEmployee();
                    break;
                case 6:
                    deleteEmployee();
                    break;
                case 7:
                    back = true;
                    break;
            }
        }
    }

    private static void addEmployee() {
        System.out.println("\n--- Add Employee ---");
        String name = readNonEmptyString("Enter Employee Name: ");
        String role = readNonEmptyString("Enter Role (e.g. Cashier, Store Manager, Billing Associate): ");
        String phone = readNonEmptyString("Enter Phone Number: ");

        Employee emp = new Employee(name, role, phone);
        boolean success = employeeDAO.addEmployee(emp);
        if (success) {
            System.out.println("[SUCCESS] Employee added successfully with ID: " + emp.getEmployeeId());
        } else {
            System.err.println("[FAILED] Unable to add employee.");
        }
    }

    private static void viewAllEmployees() {
        System.out.println("\n--- All Employees ---");
        List<Employee> employees = employeeDAO.getAllEmployees();
        printEmployeeTable(employees);
    }

    private static void searchEmployeeById() {
        int id = readPositiveInt("Enter Employee ID: ");
        Employee e = employeeDAO.getEmployeeById(id);
        if (e != null) {
            List<Employee> list = new ArrayList<>();
            list.add(e);
            printEmployeeTable(list);
        } else {
            System.out.println("No employee found with ID: " + id);
        }
    }

    private static void searchEmployeeByName() {
        String name = readNonEmptyString("Enter Employee Name or Keyword: ");
        List<Employee> list = employeeDAO.searchEmployeesByName(name);
        if (list.isEmpty()) {
            System.out.println("No employees found matching: " + name);
        } else {
            printEmployeeTable(list);
        }
    }

    private static void updateEmployee() {
        int id = readPositiveInt("Enter Employee ID to update: ");
        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) {
            System.err.println("[Error] Employee not found with ID: " + id);
            return;
        }

        System.out.println("Current details: " + existing);
        System.out.println("(Press Enter to keep current value)");

        System.out.print("New Name [" + existing.getName() + "]: ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) existing.setName(name);

        System.out.print("New Role [" + existing.getRole() + "]: ");
        String role = scanner.nextLine().trim();
        if (!role.isEmpty()) existing.setRole(role);

        System.out.print("New Phone [" + existing.getPhone() + "]: ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty()) existing.setPhone(phone);

        boolean updated = employeeDAO.updateEmployee(existing);
        if (updated) {
            System.out.println("[SUCCESS] Employee updated successfully!");
        } else {
            System.err.println("[FAILED] Employee update failed.");
        }
    }

    private static void deleteEmployee() {
        int id = readPositiveInt("Enter Employee ID to delete: ");
        Employee existing = employeeDAO.getEmployeeById(id);
        if (existing == null) {
            System.err.println("[Error] Employee not found with ID: " + id);
            return;
        }

        System.out.print("Are you sure you want to delete employee '" + existing.getName() + "'? (Y/N): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("Y")) {
            boolean deleted = employeeDAO.deleteEmployee(id);
            if (deleted) {
                System.out.println("[SUCCESS] Employee deleted successfully.");
            } else {
                System.err.println("[FAILED] Could not delete employee. They may have processed sales transactions.");
            }
        } else {
            System.out.println("Deletion cancelled.");
        }
    }

    private static void printEmployeeTable(List<Employee> employees) {
        if (employees.isEmpty()) {
            System.out.println("No employees found.");
            return;
        }
        System.out.println("+------+---------------------------+----------------------+---------------+");
        System.out.println("| ID   | Name                      | Role                 | Phone         |");
        System.out.println("+------+---------------------------+----------------------+---------------+");
        for (Employee e : employees) {
            System.out.printf("| %-4d | %-25s | %-20s | %-13s |\n",
                    e.getEmployeeId(),
                    truncate(e.getName(), 25),
                    truncate(e.getRole(), 20),
                    truncate(e.getPhone(), 13));
        }
        System.out.println("+------+---------------------------+----------------------+---------------+");
    }

    // =========================================================================
    // FEATURE 4: SALES AND BILLING
    // =========================================================================

    private static void handleSalesAndBilling() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-------------------------------------------------------");
            System.out.println("              SALES AND BILLING                        ");
            System.out.println("-------------------------------------------------------");
            System.out.println("  1. Create New Sale (Billing / Checkout)");
            System.out.println("  2. View Recent Sales");
            System.out.println("  3. View Invoice by Invoice Number");
            System.out.println("  4. View Invoice by Sale ID");
            System.out.println("  5. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            int choice = readInt("Select option (1-5): ", 1, 5);
            switch (choice) {
                case 1:
                    createNewSale();
                    break;
                case 2:
                    viewRecentSales();
                    break;
                case 3:
                    viewInvoiceByNumber();
                    break;
                case 4:
                    viewInvoiceById();
                    break;
                case 5:
                    back = true;
                    break;
            }
        }
    }

    private static void createNewSale() {
        System.out.println("\n=======================================================");
        System.out.println("             NEW SALES TRANSACTION                     ");
        System.out.println("=======================================================");

        // Step 1: Select Employee
        List<Employee> employees = employeeDAO.getAllEmployees();
        if (employees.isEmpty()) {
            System.err.println("[Error] No employees registered. Please add an employee first before billing.");
            return;
        }

        System.out.println("\nAvailable Cashiers/Employees:");
        for (Employee emp : employees) {
            System.out.println("  [" + emp.getEmployeeId() + "] " + emp.getName() + " (" + emp.getRole() + ")");
        }
        int employeeId = readPositiveInt("Enter Cashier/Employee ID: ");
        Employee cashier = employeeDAO.getEmployeeById(employeeId);
        if (cashier == null) {
            System.err.println("[Error] Invalid Employee ID.");
            return;
        }

        // Step 2: Customer Selection (Registered vs Walk-in)
        Integer customerId = null;
        Customer customer = null;
        System.out.println("\nCustomer Selection:");
        System.out.println("  1. Walk-in Customer (No registration)");
        System.out.println("  2. Existing Registered Customer");
        System.out.println("  3. Register New Customer Now");
        int custChoice = readInt("Select option (1-3): ", 1, 3);

        if (custChoice == 2) {
            System.out.print("Enter Customer Phone or Customer ID: ");
            String query = scanner.nextLine().trim();
            try {
                int cid = Integer.parseInt(query);
                customer = customerDAO.getCustomerById(cid);
            } catch (NumberFormatException e) {
                customer = customerDAO.getCustomerByPhone(query);
            }
            if (customer != null) {
                customerId = customer.getCustomerId();
                System.out.println("[Selected Customer] " + customer.getName() + " (Phone: " + customer.getPhone() + ")");
            } else {
                System.out.println("[Notice] Customer not found. Proceeding as Walk-in Customer.");
            }
        } else if (custChoice == 3) {
            String cName = readNonEmptyString("Enter Customer Name: ");
            String cPhone = readNonEmptyString("Enter Phone Number: ");
            System.out.print("Enter Email Address (optional): ");
            String cEmail = scanner.nextLine().trim();
            if (cEmail.isEmpty()) cEmail = null;

            Customer newCust = new Customer(cName, cPhone, cEmail);
            if (customerDAO.registerCustomer(newCust)) {
                customerId = newCust.getCustomerId();
                customer = newCust;
                System.out.println("[SUCCESS] Registered and selected customer: " + newCust.getName());
            } else {
                System.out.println("[Warning] Registration failed. Proceeding as Walk-in Customer.");
            }
        } else {
            System.out.println("[Selected] Walk-in Customer.");
        }

        // Step 3: Add Items to Cart
        List<SaleItem> cartItems = new ArrayList<>();
        BigDecimal runningTotal = BigDecimal.ZERO;

        boolean addingItems = true;
        while (addingItems) {
            System.out.println("\n--- Add Product to Bill ---");
            System.out.println("Search by: 1. Product ID | 2. Barcode | 3. Name Search");
            int searchMode = readInt("Select search mode (1-3): ", 1, 3);

            Product product = null;
            if (searchMode == 1) {
                int pid = readPositiveInt("Enter Product ID: ");
                product = productDAO.getProductById(pid);
            } else if (searchMode == 2) {
                String barcode = readNonEmptyString("Enter Barcode: ");
                product = productDAO.getProductByBarcode(barcode);
            } else {
                String nameSearch = readNonEmptyString("Enter Product Name: ");
                List<Product> matches = productDAO.searchProductsByName(nameSearch);
                if (matches.isEmpty()) {
                    System.out.println("No matching products found.");
                    continue;
                } else if (matches.size() == 1) {
                    product = matches.get(0);
                } else {
                    printProductTable(matches);
                    int pid = readPositiveInt("Select Product ID from list above: ");
                    product = productDAO.getProductById(pid);
                }
            }

            if (product == null) {
                System.err.println("[Error] Product not found.");
            } else {
                System.out.printf("Selected: %s | Price: ₹%.2f | Available Stock: %d\n",
                        product.getName(), product.getPrice(), product.getQuantity());

                // Calculate already carted quantity for this product
                int alreadyInCart = 0;
                for (SaleItem item : cartItems) {
                    if (item.getProductId() == product.getProductId()) {
                        alreadyInCart += item.getQuantity();
                    }
                }

                int maxAvailable = product.getQuantity() - alreadyInCart;
                if (maxAvailable <= 0) {
                    System.err.println("[Error] Insufficient stock! This item is currently out of stock or already in cart.");
                } else {
                    int qty = readPositiveInt("Enter Quantity (Max available " + maxAvailable + "): ");
                    if (qty > maxAvailable) {
                        System.err.println("[Error] Requested quantity exceeds available stock (" + maxAvailable + ").");
                    } else {
                        BigDecimal subtotal = product.getPrice().multiply(new BigDecimal(qty)).setScale(2, RoundingMode.HALF_UP);
                        SaleItem lineItem = new SaleItem(product.getProductId(), qty, product.getPrice(), subtotal);
                        lineItem.setProductName(product.getName());
                        lineItem.setBarcode(product.getBarcode());

                        // If already in cart, merge quantity
                        boolean merged = false;
                        for (SaleItem existingItem : cartItems) {
                            if (existingItem.getProductId() == product.getProductId()) {
                                existingItem.setQuantity(existingItem.getQuantity() + qty);
                                existingItem.setSubtotal(existingItem.getSubtotal().add(subtotal));
                                merged = true;
                                break;
                            }
                        }
                        if (!merged) {
                            cartItems.add(lineItem);
                        }

                        runningTotal = runningTotal.add(subtotal);
                        System.out.printf("Added %d x %s. Current Bill Total: ₹%.2f\n", qty, product.getName(), runningTotal);
                    }
                }
            }

            if (cartItems.isEmpty()) {
                System.out.print("No items added yet. Try again? (Y/N): ");
                String retry = scanner.nextLine().trim();
                if (!retry.equalsIgnoreCase("Y")) {
                    System.out.println("Transaction cancelled.");
                    return;
                }
            } else {
                System.out.print("Add another product? (Y/N): ");
                String more = scanner.nextLine().trim();
                if (!more.equalsIgnoreCase("Y")) {
                    addingItems = false;
                }
            }
        }

        if (cartItems.isEmpty()) {
            System.out.println("Cart is empty. Transaction aborted.");
            return;
        }

        // Step 4: Display Cart Summary
        System.out.println("\n-------------------------------------------------------");
        System.out.println("                   BILL SUMMARY                        ");
        System.out.println("-------------------------------------------------------");
        System.out.println("+----+--------------------------------+-------+------------+------------+");
        System.out.println("| #  | Item Name                      | Qty   | Unit Price | Line Total |");
        System.out.println("+----+--------------------------------+-------+------------+------------+");
        int index = 1;
        for (SaleItem item : cartItems) {
            System.out.printf("| %-2d | %-30s | %-5d | %10.2f | %10.2f |\n",
                    index++,
                    truncate(item.getProductName(), 30),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal());
        }
        System.out.println("+----+--------------------------------+-------+------------+------------+");
        System.out.printf("| GRAND TOTAL:                                         ₹%10.2f |\n", runningTotal);
        System.out.println("+-----------------------------------------------------------------------+");

        // Step 5: Payment Processing
        System.out.println("\n--- Select Payment Method ---");
        System.out.println("  1. CASH");
        System.out.println("  2. UPI");
        System.out.println("  3. CARD");
        int payChoice = readInt("Select payment option (1-3): ", 1, 3);
        String paymentMethod = "CASH";
        BigDecimal amountPaid = runningTotal;
        BigDecimal changeReturned = BigDecimal.ZERO;

        if (payChoice == 1) {
            paymentMethod = "CASH";
            while (true) {
                BigDecimal tendered = readPositiveBigDecimal("Enter Cash Tendered by Customer: ₹");
                if (tendered.compareTo(runningTotal) < 0) {
                    System.err.printf("[Error] Tendered amount (₹%.2f) is less than Bill Total (₹%.2f). Please enter full amount.\n",
                            tendered, runningTotal);
                } else {
                    changeReturned = tendered.subtract(runningTotal);
                    amountPaid = runningTotal;
                    System.out.printf("[Cash] Received: ₹%.2f | Change to Return: ₹%.2f\n", tendered, changeReturned);
                    break;
                }
            }
        } else if (payChoice == 2) {
            paymentMethod = "UPI";
            System.out.print("Scan UPI QR / Enter UPI Reference ID (e.g. UPI12345678): ");
            String ref = scanner.nextLine().trim();
            System.out.printf("[UPI] Payment of ₹%.2f confirmed (Ref: %s)\n", runningTotal, (ref.isEmpty() ? "AUTO-VERIFIED" : ref));
        } else {
            paymentMethod = "CARD";
            System.out.print("Enter Card Last 4 Digits / Auth Code: ");
            String auth = scanner.nextLine().trim();
            System.out.printf("[CARD] Authorization successful for ₹%.2f (Card: %s)\n", runningTotal, (auth.isEmpty() ? "APPROVED" : auth));
        }

        // Step 6: Finalize Transaction using SaleDAO Transaction Management
        String invoiceNo = saleDAO.generateInvoiceNumber();
        Sale sale = new Sale(invoiceNo, customerId, employeeId, runningTotal, "COMPLETED");
        Payment payment = new Payment(0, paymentMethod, amountPaid, "SUCCESS");

        System.out.println("\n[Processing] Finalizing sale and updating inventory stock...");
        boolean success = saleDAO.createSale(sale, cartItems, payment);

        if (success) {
            sale.setCustomerName(customer != null ? customer.getName() : "Walk-in Customer");
            sale.setEmployeeName(cashier.getName());
            sale.setItems(cartItems);
            sale.setPayment(payment);

            System.out.println("\n[SUCCESS] Transaction completed and committed successfully!");
            printInvoice(sale, changeReturned);
        } else {
            System.err.println("\n[FAILED] Transaction could not be completed! All database changes have been rolled back.");
        }
    }

    private static void viewRecentSales() {
        System.out.println("\n--- Recent Sales ---");
        List<Sale> sales = saleDAO.getAllSales();
        if (sales.isEmpty()) {
            System.out.println("No sales transactions found.");
            return;
        }

        System.out.println("+------+---------------------+---------------------------+---------------------+---------------------+------------+-----------+");
        System.out.println("| ID   | Invoice No          | Customer                  | Cashier             | Date & Time         | Total (₹)  | Status    |");
        System.out.println("+------+---------------------+---------------------------+---------------------+---------------------+------------+-----------+");
        int count = 0;
        for (Sale s : sales) {
            System.out.printf("| %-4d | %-19s | %-25s | %-19s | %-19s | %10.2f | %-9s |\n",
                    s.getSaleId(),
                    s.getInvoiceNo(),
                    truncate(s.getCustomerName(), 25),
                    truncate(s.getEmployeeName(), 19),
                    s.getSaleDate().toString().substring(0, 19),
                    s.getTotalAmount(),
                    s.getStatus());
            count++;
            if (count >= 15) break; // Display top 15 recent
        }
        System.out.println("+------+---------------------+---------------------------+---------------------+---------------------+------------+-----------+");
    }

    private static void viewInvoiceByNumber() {
        String inv = readNonEmptyString("Enter Invoice Number (e.g. INV-...): ");
        Sale sale = saleDAO.getSaleByInvoiceNo(inv);
        if (sale != null) {
            printInvoice(sale, BigDecimal.ZERO);
        } else {
            System.err.println("Invoice not found with number: " + inv);
        }
    }

    private static void viewInvoiceById() {
        int id = readPositiveInt("Enter Sale ID: ");
        Sale sale = saleDAO.getSaleById(id);
        if (sale != null) {
            printInvoice(sale, BigDecimal.ZERO);
        } else {
            System.err.println("Sale transaction not found with ID: " + id);
        }
    }

    /**
     * Formats and prints a professional retail invoice.
     */
    private static void printInvoice(Sale sale, BigDecimal changeReturned) {
        System.out.println("\n==========================================================================");
        System.out.println("                         RETAIL STORE INVOICE                             ");
        System.out.println("                     Retail POS Management System                         ");
        System.out.println("==========================================================================");
        System.out.printf(" Invoice No: %-25s Date: %s\n", sale.getInvoiceNo(), sale.getSaleDate() != null ? sale.getSaleDate() : "Just Now");
        System.out.printf(" Cashier   : %-25s Status: %s\n", sale.getEmployeeName(), sale.getStatus());
        System.out.printf(" Customer  : %-25s\n", sale.getCustomerName());
        System.out.println("--------------------------------------------------------------------------");
        System.out.println(" Item Name                       Barcode         Qty   Price(₹)   Total(₹)");
        System.out.println("--------------------------------------------------------------------------");

        for (SaleItem item : sale.getItems()) {
            System.out.printf(" %-30s  %-14s  %-4d  %9.2f  %9.2f\n",
                    truncate(item.getProductName(), 30),
                    truncate(item.getBarcode() != null ? item.getBarcode() : "-", 14),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getSubtotal());
        }

        System.out.println("--------------------------------------------------------------------------");
        System.out.printf(" TOTAL AMOUNT:                                                 ₹%10.2f\n", sale.getTotalAmount());
        if (sale.getPayment() != null) {
            System.out.printf(" Payment Method: %-20s Paid Amount:  ₹%10.2f\n",
                    sale.getPayment().getPaymentMethod(), sale.getPayment().getAmount());
            if (changeReturned != null && changeReturned.compareTo(BigDecimal.ZERO) > 0) {
                System.out.printf(" Change Returned:                                              ₹%10.2f\n", changeReturned);
            }
        }
        System.out.println("==========================================================================");
        System.out.println("                  Thank you for shopping with us!                         ");
        System.out.println("==========================================================================\n");
    }

    // =========================================================================
    // FEATURE 5: PAYMENT MANAGEMENT
    // =========================================================================

    private static void handlePaymentManagement() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-------------------------------------------------------");
            System.out.println("              PAYMENT MANAGEMENT                       ");
            System.out.println("-------------------------------------------------------");
            System.out.println("  1. View All Payments");
            System.out.println("  2. Search Payment by Payment ID");
            System.out.println("  3. Search Payment by Sale ID");
            System.out.println("  4. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            int choice = readInt("Select option (1-4): ", 1, 4);
            switch (choice) {
                case 1:
                    viewAllPayments();
                    break;
                case 2:
                    searchPaymentById();
                    break;
                case 3:
                    searchPaymentBySaleId();
                    break;
                case 4:
                    back = true;
                    break;
            }
        }
    }

    private static void viewAllPayments() {
        System.out.println("\n--- All Payments ---");
        List<Payment> payments = paymentDAO.getAllPayments();
        printPaymentTable(payments);
    }

    private static void searchPaymentById() {
        int id = readPositiveInt("Enter Payment ID: ");
        Payment p = paymentDAO.getPaymentById(id);
        if (p != null) {
            List<Payment> list = new ArrayList<>();
            list.add(p);
            printPaymentTable(list);
        } else {
            System.out.println("No payment found with ID: " + id);
        }
    }

    private static void searchPaymentBySaleId() {
        int saleId = readPositiveInt("Enter Sale ID: ");
        Payment p = paymentDAO.getPaymentBySaleId(saleId);
        if (p != null) {
            List<Payment> list = new ArrayList<>();
            list.add(p);
            printPaymentTable(list);
        } else {
            System.out.println("No payment found for Sale ID: " + saleId);
        }
    }

    private static void printPaymentTable(List<Payment> payments) {
        if (payments.isEmpty()) {
            System.out.println("No payment records found.");
            return;
        }
        System.out.println("+------+---------+---------------------+--------+------------+---------------------+---------+");
        System.out.println("| ID   | Sale ID | Invoice No          | Method | Amount (₹) | Payment Date        | Status  |");
        System.out.println("+------+---------+---------------------+--------+------------+---------------------+---------+");
        for (Payment p : payments) {
            System.out.printf("| %-4d | %-7d | %-19s | %-6s | %10.2f | %-19s | %-7s |\n",
                    p.getPaymentId(),
                    p.getSaleId(),
                    truncate(p.getInvoiceNo() != null ? p.getInvoiceNo() : "N/A", 19),
                    p.getPaymentMethod(),
                    p.getAmount(),
                    p.getPaymentDate().toString().substring(0, 19),
                    p.getPaymentStatus());
        }
        System.out.println("+------+---------+---------------------+--------+------------+---------------------+---------+");
    }

    // =========================================================================
    // FEATURE 6: SALES REPORTS AND INVENTORY
    // =========================================================================

    private static void handleReportsAndInventory() {
        boolean back = false;
        while (!back) {
            System.out.println("\n-------------------------------------------------------");
            System.out.println("          SALES REPORTS AND INVENTORY                  ");
            System.out.println("-------------------------------------------------------");
            System.out.println("  1. Daily Sales Report (Today or Custom Date)");
            System.out.println("  2. Total Sales Revenue Summary");
            System.out.println("  3. View Inventory Stock Overview (All Products)");
            System.out.println("  4. View Low-Stock Products");
            System.out.println("  5. View Out-of-Stock Products");
            System.out.println("  6. View Full Invoice Details by Invoice No");
            System.out.println("  7. Back to Main Menu");
            System.out.println("-------------------------------------------------------");

            int choice = readInt("Select option (1-7): ", 1, 7);
            switch (choice) {
                case 1:
                    displayDailySalesReport();
                    break;
                case 2:
                    displayTotalRevenueSummary();
                    break;
                case 3:
                    viewInventoryStockOverview();
                    break;
                case 4:
                    viewLowStockProducts();
                    break;
                case 5:
                    viewOutOfStockProducts();
                    break;
                case 6:
                    viewInvoiceByNumber();
                    break;
                case 7:
                    back = true;
                    break;
            }
        }
    }

    private static void displayDailySalesReport() {
        System.out.println("\n--- Daily Sales Report ---");
        System.out.print("Enter Date (YYYY-MM-DD) or press Enter for Today [" + LocalDate.now() + "]: ");
        String dateStr = scanner.nextLine().trim();
        LocalDate targetDate;
        if (dateStr.isEmpty()) {
            targetDate = LocalDate.now();
        } else {
            try {
                targetDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                System.err.println("[Error] Invalid date format. Please use YYYY-MM-DD.");
                return;
            }
        }

        List<Sale> dailySales = saleDAO.getDailySales(targetDate);
        System.out.println("\n=================================================================");
        System.out.println("           DAILY SALES REPORT FOR: " + targetDate);
        System.out.println("=================================================================");
        if (dailySales.isEmpty()) {
            System.out.println("No completed sales transactions found for " + targetDate);
        } else {
            System.out.println("+------+---------------------+---------------------------+---------------------+------------+");
            System.out.println("| ID   | Invoice No          | Customer                  | Cashier             | Total (₹)  |");
            System.out.println("+------+---------------------+---------------------------+---------------------+------------+");
            BigDecimal dayTotal = BigDecimal.ZERO;
            for (Sale s : dailySales) {
                System.out.printf("| %-4d | %-19s | %-25s | %-19s | %10.2f |\n",
                        s.getSaleId(),
                        s.getInvoiceNo(),
                        truncate(s.getCustomerName(), 25),
                        truncate(s.getEmployeeName(), 19),
                        s.getTotalAmount());
                dayTotal = dayTotal.add(s.getTotalAmount());
            }
            System.out.println("+------+---------------------+---------------------------+---------------------+------------+");
            System.out.printf("| Total Sales Count: %-4d                     Day Revenue: ₹%10.2f |\n",
                    dailySales.size(), dayTotal);
            System.out.println("+-------------------------------------------------------------------+");
        }
    }

    private static void displayTotalRevenueSummary() {
        System.out.println("\n=======================================================");
        System.out.println("             SALES REVENUE SUMMARY                     ");
        System.out.println("=======================================================");
        BigDecimal totalRev = saleDAO.getTotalSalesRevenue();
        List<Sale> allSales = saleDAO.getAllSales();
        int completedCount = 0;
        int cancelledCount = 0;
        for (Sale s : allSales) {
            if ("COMPLETED".equalsIgnoreCase(s.getStatus())) {
                completedCount++;
            } else {
                cancelledCount++;
            }
        }

        System.out.println("  Total Completed Transactions : " + completedCount);
        System.out.println("  Cancelled / Void Transactions: " + cancelledCount);
        System.out.printf("  Total Net Sales Revenue      : ₹%.2f\n", totalRev);
        if (completedCount > 0) {
            BigDecimal avgTicket = totalRev.divide(new BigDecimal(completedCount), 2, RoundingMode.HALF_UP);
            System.out.printf("  Average Transaction Value    : ₹%.2f\n", avgTicket);
        }
        System.out.println("=======================================================");
    }

    private static void viewInventoryStockOverview() {
        System.out.println("\n--- Available Stock for All Products ---");
        List<Product> products = productDAO.getAllProducts();
        printProductTable(products);
    }

    private static void viewLowStockProducts() {
        int threshold = readIntWithDefault("Enter Low-Stock Threshold [default 5]: ", 5);
        List<Product> lowStock = productDAO.getLowStockProducts(threshold);
        System.out.println("\n--- Low-Stock Alert (Quantity <= " + threshold + ") ---");
        if (lowStock.isEmpty()) {
            System.out.println("All products have sufficient stock above " + threshold + " units.");
        } else {
            printProductTable(lowStock);
        }
    }

    private static void viewOutOfStockProducts() {
        System.out.println("\n--- Out-of-Stock Products (Quantity = 0) ---");
        List<Product> outOfStock = productDAO.getOutOfStockProducts();
        if (outOfStock.isEmpty()) {
            System.out.println("No products are currently out of stock!");
        } else {
            printProductTable(outOfStock);
        }
    }

    // =========================================================================
    // INPUT VALIDATION AND UTILITIES
    // =========================================================================

    private static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("Invalid input. Please enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }

    private static int readPositiveInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val > 0) {
                    return val;
                }
                System.out.println("Please enter a positive integer (> 0).");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }

    private static int readNonNegativeInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(line);
                if (val >= 0) {
                    return val;
                }
                System.out.println("Please enter a non-negative integer (>= 0).");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }

    private static int readIntWithDefault(String prompt, int defaultVal) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                return defaultVal;
            }
            try {
                int val = Integer.parseInt(line);
                if (val >= 0) {
                    return val;
                }
                System.out.println("Please enter a non-negative integer.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter an integer.");
            }
        }
    }

    private static BigDecimal readPositiveBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                BigDecimal val = new BigDecimal(line);
                if (val.compareTo(BigDecimal.ZERO) > 0) {
                    return val.setScale(2, RoundingMode.HALF_UP);
                }
                System.out.println("Value must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount. Please enter a valid decimal number (e.g. 50.00).");
            }
        }
    }

    private static String readNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("Input cannot be empty. Please enter a value.");
        }
    }

    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 3) + "...";
    }
}
