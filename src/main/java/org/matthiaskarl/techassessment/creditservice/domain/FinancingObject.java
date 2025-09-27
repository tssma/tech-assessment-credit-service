package org.matthiaskarl.techassessment.creditservice.domain;

import java.util.List;

public record FinancingObject(
        long id,
        List<Owner> owners,
        long limit,
        List<Long> products,
        String status
) {
}
