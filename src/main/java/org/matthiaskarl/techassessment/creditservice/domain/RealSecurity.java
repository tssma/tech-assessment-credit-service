package org.matthiaskarl.techassessment.creditservice.domain;

public record RealSecurity(
        String type,
        String address,
        long collateralValue,
        String currency,
        String nextRevaluationDate
) {
}
