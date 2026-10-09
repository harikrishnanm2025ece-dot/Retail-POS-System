package model;

import java.math.BigDecimal;

/**
 * Model representing an item line in a sales transaction.
 */
public class SaleItem {
    private int itemId;
    private int saleId;
    private int productId;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    // Display fields
    private String productName;
    private String barcode;

    public SaleItem() {
    }

    public SaleItem(int productId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public SaleItem(int saleId, int productId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public SaleItem(int itemId, int saleId, int productId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.itemId = itemId;
        this.saleId = saleId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    @Override
    public String toString() {
        return String.format("SaleItem [ItemID=%d, ProductID=%d, Name='%s', Qty=%d, UnitPrice=₹%.2f, Subtotal=₹%.2f]",
                itemId, productId, (productName != null ? productName : ""), quantity, unitPrice, subtotal);
    }
}
