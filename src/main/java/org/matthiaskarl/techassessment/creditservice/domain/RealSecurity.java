package org.matthiaskarl.techassessment.creditservice.domain;

public record RealSecurity(
        String type,
        String address,
        double collateralValue,
        String currency,
        String nextRevaluationDate
) {
}
