package org.matthiaskarl.techassessment.creditservice.mapping;

import lombok.Builder;
import org.matthiaskarl.techassessment.creditservice.domain.FinancingObject;
import org.matthiaskarl.techassessment.creditservice.domain.Limit;
import org.matthiaskarl.techassessment.creditservice.domain.Product;

import java.util.List;

@Builder
public record ParentLoanMappingRequest(
        FinancingObject financingObject,
        Limit limit,
        List<Product> products
) {
}
