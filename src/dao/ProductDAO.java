package dao;

import model.Product;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Product entity.
 * Handles all database operations for products.
 */
public class ProductDAO {

    /**
     * Adds a new product to the database.
     *
     * @param product Product to insert
     * @return true if insertion succeeded, false otherwise
     */
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO products (name, category, price, quantity, barcode) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setString(5, product.getBarcode());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        product.setProductId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[Error in addProduct] " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves all products from the database.
     *
     * @return List of all products
     */
    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, barcode, created_at FROM products ORDER BY product_id ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            System.err.println("[Error in getAllProducts] " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves a product by its ID.
     *
     * @param productId ID of product
     * @return Product if found, null otherwise
     */
    public Product getProductById(int productId) {
        String sql = "SELECT product_id, name, category, price, quantity, barcode, created_at FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getProductById] " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves a product by its unique barcode.
     *
     * @param barcode Product barcode
     * @return Product if found, null otherwise
     */
    public Product getProductByBarcode(String barcode) {
        String sql = "SELECT product_id, name, category, price, quantity, barcode, created_at FROM products WHERE barcode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, barcode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getProductByBarcode] " + e.getMessage());
        }
        return null;
    }

    /**
     * Searches products whose names contain the given keyword.
     *
     * @param keyword search term
     * @return List of matching products
     */
    public List<Product> searchProductsByName(String keyword) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, barcode, created_at FROM products WHERE LOWER(name) LIKE ? ORDER BY name ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + keyword.toLowerCase().trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in searchProductsByName] " + e.getMessage());
        }
        return list;
    }

    /**
     * Updates an existing product's details.
     *
     * @param product Product with updated values
     * @return true if updated, false otherwise
     */
    public boolean updateProduct(Product product) {
        String sql = "UPDATE products SET name = ?, category = ?, price = ?, quantity = ?, barcode = ? WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, product.getName());
            ps.setString(2, product.getCategory());
            ps.setBigDecimal(3, product.getPrice());
            ps.setInt(4, product.getQuantity());
            ps.setString(5, product.getBarcode());
            ps.setInt(6, product.getProductId());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[Error in updateProduct] " + e.getMessage());
        }
        return false;
    }

    /**
     * Deletes a product by its ID.
     *
     * @param productId ID of product to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            System.err.println("[Integrity Notice] Cannot delete product ID " + productId + " as it is referenced in past sales records.");
        } catch (SQLException e) {
            System.err.println("[Error in deleteProduct] " + e.getMessage());
        }
        return false;
    }

    /**
     * Retrieves products with stock quantity less than or equal to a threshold.
     *
     * @param threshold stock limit
     * @return List of low stock products
     */
    public List<Product> getLowStockProducts(int threshold) {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT product_id, name, category, price, quantity, barcode, created_at FROM products WHERE quantity <= ? ORDER BY quantity ASC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToProduct(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getLowStockProducts] " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieves products that are out of stock (quantity = 0).
     *
     * @return List of out-of-stock products
     */
    public List<Product> getOutOfStockProducts() {
        return getLowStockProducts(0);
    }

    /**
     * Checks if requested quantity is available in stock.
     *
     * @param productId ID of product
     * @param requiredQty quantity to check
     * @return true if stock >= requiredQty
     */
    public boolean isStockAvailable(int productId, int requiredQty) {
        String sql = "SELECT quantity FROM products WHERE product_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("quantity") >= requiredQty;
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in isStockAvailable] " + e.getMessage());
        }
        return false;
    }

    /**
     * Decreases stock quantity for a product inside an active transactional connection.
     * Prevents stock from becoming negative using conditional WHERE clause.
     *
     * @param conn active transactional Connection
     * @param productId ID of product
     * @param qtyToDeduct quantity to subtract
     * @return true if stock was successfully reduced
     * @throws SQLException on error or insufficient stock
     */
    public boolean deductStock(Connection conn, int productId, int qtyToDeduct) throws SQLException {
        String sql = "UPDATE products SET quantity = quantity - ? WHERE product_id = ? AND quantity >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qtyToDeduct);
            ps.setInt(2, productId);
            ps.setInt(3, qtyToDeduct);
            int rows = ps.executeUpdate();
            return rows > 0;
        }
    }

    /**
     * Helper method to map a ResultSet row to a Product object.
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getBigDecimal("price"),
                rs.getInt("quantity"),
                rs.getString("barcode"),
                rs.getTimestamp("created_at")
        );
    }
}
