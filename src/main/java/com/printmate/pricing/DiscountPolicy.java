package com.printmate.pricing;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** One persisted shop-wide volume discount; existing orders keep their price snapshots. */
@Entity
@Table(name = "discount_policy")
public class DiscountPolicy {
    @Id private Long id = 1L;
    @Column(nullable = false) private int threshold;
    @Column(nullable = false, precision = 5, scale = 2) private BigDecimal percentage;
    @Column(nullable = false) private String updatedBy;
    @Column(nullable = false) private LocalDateTime updatedAt;
    @Version private Long version;

    protected DiscountPolicy() {}
    public DiscountPolicy(int threshold, BigDecimal percentage, String updatedBy) {
        update(threshold, percentage, updatedBy);
    }
    public void update(int threshold, BigDecimal percentage, String updatedBy) {
        this.threshold = threshold;
        this.percentage = percentage;
        this.updatedBy = updatedBy;
        this.updatedAt = LocalDateTime.now();
    }
    public int getThreshold() { return threshold; }
    public BigDecimal getPercentage() { return percentage; }
}
