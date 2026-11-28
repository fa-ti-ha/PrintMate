package com.printmate.order;

import com.printmate.document.Document;
import com.printmate.payment.PaymentStatus;
import com.printmate.pricing.ColorMode;
import com.printmate.pricing.PaperSize;
import com.printmate.pricing.PrintSides;
import com.printmate.user.User;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "print_orders", indexes = {
    @Index(name = "idx_order_number", columnList = "order_number", unique = true),
    @Index(name = "idx_order_customer", columnList = "customer_id"),
    @Index(name = "idx_order_status", columnList = "order_status"),
    @Index(name = "idx_order_created_at", columnList = "created_at")
})
public class PrintOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 32)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Enumerated(EnumType.STRING)
    @Column(name = "color_mode", nullable = false, length = 30)
    private ColorMode colorMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "paper_size", nullable = false, length = 20)
    private PaperSize paperSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "print_sides", nullable = false, length = 30)
    private PrintSides printSides;

    @Column(name = "page_start", nullable = false)
    private Integer pageStart;

    @Column(name = "page_end", nullable = false)
    private Integer pageEnd;

    @Column(name = "selected_page_count", nullable = false)
    private Integer selectedPageCount;

    @Column(name = "copies", nullable = false)
    private Integer copies;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "discount_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    private PaymentStatus paymentStatus;

    @Column(name = "pickup_code_hash", length = 255)
    private String pickupCodeHash;

    @Column(name = "pickup_code", length = 20)
    private String pickupCode;

    @Column(name = "customer_note", length = 1000)
    private String customerNote;

    @Column(name = "pickup_code_expires_at")
    private LocalDateTime pickupCodeExpiresAt;

    @Column(name = "pickup_attempts", nullable = false)
    private Integer pickupAttempts = 0;

    @Column(name = "operator_notes", length = 1000)
    private String operatorNotes;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public PrintOrder() {
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
        if (this.orderStatus == null) {
            this.orderStatus = OrderStatus.PENDING;
        }
        if (this.pickupAttempts == null) {
            this.pickupAttempts = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public int getBillablePages() {
        return (selectedPageCount != null && copies != null) ? (selectedPageCount * copies) : 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public ColorMode getColorMode() {
        return colorMode;
    }

    public void setColorMode(ColorMode colorMode) {
        this.colorMode = colorMode;
    }

    public PaperSize getPaperSize() {
        return paperSize;
    }

    public void setPaperSize(PaperSize paperSize) {
        this.paperSize = paperSize;
    }

    public PrintSides getPrintSides() {
        return printSides;
    }

    public void setPrintSides(PrintSides printSides) {
        this.printSides = printSides;
    }

    public Integer getPageStart() {
        return pageStart;
    }

    public void setPageStart(Integer pageStart) {
        this.pageStart = pageStart;
    }

    public Integer getPageEnd() {
        return pageEnd;
    }

    public void setPageEnd(Integer pageEnd) {
        this.pageEnd = pageEnd;
    }

    public Integer getSelectedPageCount() {
        return selectedPageCount;
    }

    public void setSelectedPageCount(Integer selectedPageCount) {
        this.selectedPageCount = selectedPageCount;
    }

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPickupCodeHash() {
        return pickupCodeHash;
    }

    public void setPickupCodeHash(String pickupCodeHash) {
        this.pickupCodeHash = pickupCodeHash;
    }

    public LocalDateTime getPickupCodeExpiresAt() {
        return pickupCodeExpiresAt;
    }

    public void setPickupCodeExpiresAt(LocalDateTime pickupCodeExpiresAt) {
        this.pickupCodeExpiresAt = pickupCodeExpiresAt;
    }

    public Integer getPickupAttempts() {
        return pickupAttempts;
    }

    public void setPickupAttempts(Integer pickupAttempts) {
        this.pickupAttempts = pickupAttempts;
    }

    public String getOperatorNotes() {
        return operatorNotes;
    }

    public void setOperatorNotes(String operatorNotes) {
        this.operatorNotes = operatorNotes;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public String getPickupCode() {
        return pickupCode;
    }

    public void setPickupCode(String pickupCode) {
        this.pickupCode = pickupCode;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    // Convenience aliases for UI templates
    public OrderStatus getStatus() {
        return orderStatus;
    }

    public BigDecimal getFinalPrice() {
        return totalPrice;
    }

    public int getTotalPages() {
        return selectedPageCount != null ? selectedPageCount : 0;
    }

    public String getOperatorNote() {
        return operatorNotes;
    }
}

