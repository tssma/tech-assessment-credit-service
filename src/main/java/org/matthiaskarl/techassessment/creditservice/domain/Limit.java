package org.matthiaskarl.techassessment.creditservice.domain;

import java.util.List;

public record Limit(
        long id,
        String name,
        String type,
        double limitAmount,
        double amortisationAmountAnnual,
        int agreedAmortisationFrequency,
        String contractNumber,
        List<RealSecurity> realSecurities
) {
}
