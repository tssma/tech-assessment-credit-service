package org.matthiaskarl.techassessment.creditservice.mapping;

import lombok.Builder;
import org.matthiaskarl.techassessment.creditservice.domain.FinancingObject;
import org.matthiaskarl.techassessment.creditservice.domain.Limit;
import org.matthiaskarl.techassessment.creditservice.domain.Product;

import java.util.List;

@Builder
public record ChildLoanMappingRequest(
        FinancingObject financingObject,
        Limit limit,
        Product product
) {
}
