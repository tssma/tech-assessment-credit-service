package org.matthiaskarl.techassessment.creditservice.dto;

import lombok.Builder;

@Builder
public record CollateralDto(
        String type,
        String currentValue,
        String currencyCode,
        String specification,
        String nextRevaluationDate,
        String amortisationPaymentAmount
) {
}
