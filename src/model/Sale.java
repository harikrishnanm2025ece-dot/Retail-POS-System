package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Model representing a Sales Transaction.
 */
public class Sale {
    private int saleId;
    private String invoiceNo;
    private Integer customerId; // Nullable for walk-in customers
    private int employeeId;
    private Timestamp saleDate;
    private BigDecimal totalAmount;
    private String status; // 'COMPLETED', 'CANCELLED'

    // Additional display & relational fields
    private String customerName;
    private String employeeName;
    private List<SaleItem> items = new ArrayList<>();
    private Payment payment;

    public Sale() {
        this.status = "COMPLETED";
    }

    public Sale(String invoiceNo, Integer customerId, int employeeId, BigDecimal totalAmount, String status) {
        this.invoiceNo = invoiceNo;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.totalAmount = totalAmount;
        this.status = (status != null) ? status : "COMPLETED";
    }

    public Sale(int saleId, String invoiceNo, Integer customerId, int employeeId, Timestamp saleDate, BigDecimal totalAmount, String status) {
        this.saleId = saleId;
        this.invoiceNo = invoiceNo;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.status = (status != null) ? status : "COMPLETED";
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public Timestamp getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(Timestamp saleDate) {
        this.saleDate = saleDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }

    public void addItem(SaleItem item) {
        this.items.add(item);
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    @Override
    public String toString() {
        return String.format("Sale [ID=%d, Invoice='%s', CustomerID=%s, EmployeeID=%d, Date=%s, Total=₹%.2f, Status='%s']",
                saleId, invoiceNo, (customerId != null ? customerId.toString() : "Walk-in"), employeeId, saleDate, totalAmount, status);
    }
}
