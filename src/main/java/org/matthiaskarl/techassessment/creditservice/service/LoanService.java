package org.matthiaskarl.techassessment.creditservice.service;

import lombok.RequiredArgsConstructor;
import org.matthiaskarl.techassessment.creditservice.domain.FinancingObject;
import org.matthiaskarl.techassessment.creditservice.domain.Limit;
import org.matthiaskarl.techassessment.creditservice.domain.Product;
import org.matthiaskarl.techassessment.creditservice.dto.LoanDto;
import org.matthiaskarl.techassessment.creditservice.mapping.ChildLoanMapper;
import org.matthiaskarl.techassessment.creditservice.mapping.ChildLoanMappingRequest;
import org.matthiaskarl.techassessment.creditservice.mapping.ParentLoanMapper;
import org.matthiaskarl.techassessment.creditservice.mapping.ParentLoanMappingRequest;
import org.matthiaskarl.techassessment.creditservice.repository.FinancingObjectRepository;
import org.matthiaskarl.techassessment.creditservice.repository.LimitsRepository;
import org.matthiaskarl.techassessment.creditservice.repository.ProductsRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final FinancingObjectRepository financingObjectRepository;
    private final LimitsRepository limitsRepository;
    private final ProductsRepository productsRepository;

    private final ParentLoanMapper parentLoanMapper;
    private final ChildLoanMapper childLoanMapper;

    public List<LoanDto> getLoansByUserId(long userId) {
        List<LoanDto> loanDtos = new ArrayList<>();

        for (FinancingObject financingObject : financingObjectRepository.findByOwnerId(userId)) {
            Limit limit = limitsRepository.findById(financingObject.limit());
            List<Product> products = productsRepository.findByIds(financingObject.products());

            ParentLoanMappingRequest parentLoanMappingRequest = ParentLoanMappingRequest.builder()
                    .financingObject(financingObject)
                    .limit(limit)
                    .products(products)
                    .build();
            loanDtos.add(parentLoanMapper.apply(parentLoanMappingRequest));

            for (Product product : products) {
                ChildLoanMappingRequest childLoanMappingRequest = ChildLoanMappingRequest.builder()
                        .financingObject(financingObject)
                        .limit(limit)
                        .product(product)
                        .build();
                loanDtos.add(childLoanMapper.apply(childLoanMappingRequest));
            }

        }

        return loanDtos;

    }
}
