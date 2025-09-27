package org.matthiaskarl.techassessment.creditservice.dto;

public record CollateralDto(
        String type,
        String currentValue,
        String currencyCode,
        String specification,
        String nextRevaluationDate,
        String amortisationPaymentAmount
) {
}
