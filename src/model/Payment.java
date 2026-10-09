package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Model representing a Payment transaction.
 */
public class Payment {
    private int paymentId;
    private int saleId;
    private String paymentMethod; // 'CASH', 'CARD', 'UPI'
    private BigDecimal amount;
    private Timestamp paymentDate;
    private String paymentStatus; // 'SUCCESS'

    // Display fields
    private String invoiceNo;

    public Payment() {
        this.paymentStatus = "SUCCESS";
    }

    public Payment(int saleId, String paymentMethod, BigDecimal amount, String paymentStatus) {
        this.saleId = saleId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentStatus = (paymentStatus != null) ? paymentStatus : "SUCCESS";
    }

    public Payment(int paymentId, int saleId, String paymentMethod, BigDecimal amount, Timestamp paymentDate, String paymentStatus) {
        this.paymentId = paymentId;
        this.saleId = saleId;
        this.paymentMethod = paymentMethod;
        this.amount = amount;
        this.paymentDate = paymentDate;
        this.paymentStatus = (paymentStatus != null) ? paymentStatus : "SUCCESS";
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Timestamp getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Timestamp paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    @Override
    public String toString() {
        return String.format("Payment [ID=%d, SaleID=%d, Method='%s', Amount=₹%.2f, Date=%s, Status='%s']",
                paymentId, saleId, paymentMethod, amount, paymentDate, paymentStatus);
    }
}
