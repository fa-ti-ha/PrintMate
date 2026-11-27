package com.printmate.pricing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pricing_rules", uniqueConstraints = {
    @UniqueConstraint(name = "uk_active_pricing_rule", columnNames = {"color_mode", "paper_size", "print_sides", "active"})
})
public class PricingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "color_mode", nullable = false, length = 30)
    private ColorMode colorMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "paper_size", nullable = false, length = 20)
    private PaperSize paperSize;

    @Enumerated(EnumType.STRING)
    @Column(name = "print_sides", nullable = false, length = 30)
    private PrintSides printSides;

    @Column(name = "price_per_page", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerPage;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "updated_by", length = 120)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PricingRule() {
    }

    public PricingRule(ColorMode colorMode, PaperSize paperSize, PrintSides printSides,
                       BigDecimal pricePerPage, boolean active, String updatedBy) {
        this.colorMode = colorMode;
        this.paperSize = paperSize;
        this.printSides = printSides;
        this.pricePerPage = pricePerPage;
        this.active = active;
        this.updatedBy = updatedBy;
    }

    @PrePersist
    @PreUpdate
    protected void onPersist() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getPricePerPage() {
        return pricePerPage;
    }

    public void setPricePerPage(BigDecimal pricePerPage) {
        this.pricePerPage = pricePerPage;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
