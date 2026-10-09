package dao;

import model.SaleItem;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for SaleItem entity.
 * Handles item creation within sales transactions and item retrievals.
 */
public class SaleItemDAO {

    /**
     * Inserts a sale item line within an active database transaction.
     *
     * @param conn active transactional Connection
     * @param item SaleItem to persist
     * @return true if successful
     * @throws SQLException on database error
     */
    public boolean addItem(Connection conn, SaleItem item) throws SQLException {
        String sql = "INSERT INTO sale_items (sale_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, item.getSaleId());
            ps.setInt(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.setBigDecimal(5, item.getSubtotal());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setItemId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves all items belonging to a specific sale, joining with product details for display.
     *
     * @param saleId ID of the sale
     * @return List of SaleItems
     */
    public List<SaleItem> getItemsBySaleId(int saleId) {
        List<SaleItem> list = new ArrayList<>();
        String sql = "SELECT si.item_id, si.sale_id, si.product_id, si.quantity, si.unit_price, si.subtotal, " +
                     "       p.name AS product_name, p.barcode " +
                     "FROM sale_items si " +
                     "JOIN products p ON si.product_id = p.product_id " +
                     "WHERE si.sale_id = ? " +
                     "ORDER BY si.item_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SaleItem item = new SaleItem(
                            rs.getInt("item_id"),
                            rs.getInt("sale_id"),
                            rs.getInt("product_id"),
                            rs.getInt("quantity"),
                            rs.getBigDecimal("unit_price"),
                            rs.getBigDecimal("subtotal")
                    );
                    item.setProductName(rs.getString("product_name"));
                    item.setBarcode(rs.getString("barcode"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("[Error in getItemsBySaleId] " + e.getMessage());
        }
        return list;
    }
}
