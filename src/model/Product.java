package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model representing a retail Product.
 */
public class Product {
    private int productId;
    private String name;
    private String category;
    private BigDecimal price;
    private int quantity;
    private String barcode;
    private Timestamp createdAt;

    public Product() {
    }

    public Product(String name, String category, BigDecimal price, int quantity, String barcode) {
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.barcode = barcode;
    }

    public Product(int productId, String name, String category, BigDecimal price, int quantity, String barcode, Timestamp createdAt) {
        this.productId = productId;
        this.name = name;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.barcode = barcode;
        this.createdAt = createdAt;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Product [ID=%d, Name='%s', Category='%s', Price=₹%.2f, Stock=%d, Barcode='%s']",
                productId, name, category, price, quantity, barcode);
    }
}
