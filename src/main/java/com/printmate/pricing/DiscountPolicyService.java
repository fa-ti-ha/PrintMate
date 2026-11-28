package com.printmate.pricing;

import com.printmate.common.exception.InvalidOperationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class DiscountPolicyService {
    public record Settings(int threshold, BigDecimal percentage) {
        public BigDecimal rate() { return percentage.movePointLeft(2); }
        public boolean isEnabled() { return percentage.signum() > 0; }
    }

    private final DiscountPolicyRepository repository;
    private final Settings defaults;

    public DiscountPolicyService(DiscountPolicyRepository repository,
            @Value("${printmate.pricing.bulk-discount-threshold:50}") int threshold,
            @Value("${printmate.pricing.bulk-discount-rate:0.10}") BigDecimal rate) {
        this.repository = repository;
        this.defaults = new Settings(threshold, rate.movePointRight(2));
    }

    @Transactional(readOnly = true)
    public Settings getSettings() {
        return repository.findById(1L).map(p -> new Settings(p.getThreshold(), p.getPercentage())).orElse(defaults);
    }

    @Transactional
    public void update(int threshold, BigDecimal percentage, String administrator) {
        if (threshold < 1 || threshold > 1_000_000 || percentage == null || percentage.signum() < 0
                || percentage.compareTo(new BigDecimal("99")) > 0 || percentage.scale() > 2) {
            throw new InvalidOperationException("Use a page threshold of 1–1,000,000 and a discount of 0–99% with at most two decimal places.");
        }
        DiscountPolicy policy = repository.findById(1L).orElseGet(() -> new DiscountPolicy(threshold, percentage, administrator));
        policy.update(threshold, percentage, administrator);
        repository.saveAndFlush(policy);
    }
}
