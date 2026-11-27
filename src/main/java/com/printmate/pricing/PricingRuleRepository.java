package com.printmate.pricing;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    Optional<PricingRule> findByColorModeAndPaperSizeAndPrintSidesAndActiveTrue(
            ColorMode colorMode, PaperSize paperSize, PrintSides printSides);

    List<PricingRule> findByActiveTrue();

    List<PricingRule> findAllByOrderByPaperSizeAscColorModeAsc();
}

