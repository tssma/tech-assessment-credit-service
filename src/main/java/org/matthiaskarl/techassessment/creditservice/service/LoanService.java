package org.matthiaskarl.techassessment.creditservice.service;

import lombok.RequiredArgsConstructor;
import org.matthiaskarl.techassessment.creditservice.domain.FinancingObject;
import org.matthiaskarl.techassessment.creditservice.domain.Limit;
import org.matthiaskarl.techassessment.creditservice.domain.Owner;
import org.matthiaskarl.techassessment.creditservice.domain.Product;
import org.matthiaskarl.techassessment.creditservice.dto.LoanDto;
import org.matthiaskarl.techassessment.creditservice.mapping.LoanMapper;
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

    public List<LoanDto> getLoansByUserId(long userId) {
        List<LoanDto> loanDtos = new ArrayList<>();

        for (FinancingObject financingObject : financingObjectRepository.findByOwnerId(userId)) {
            Limit limit = limitsRepository.findById(financingObject.limit());

            List<Product> products = new ArrayList<>();
            for (Long productId : financingObject.products()) {
                Product product = productsRepository.findById(productId);
                if (product != null) {
                    products.add(product);
                }
            }

            List<String> borrowerNames = financingObject.owners().stream().map(Owner::name).toList();
            loanDtos.add(LoanMapper.toParentLoan(financingObject.id(), financingObject.status(), borrowerNames, limit, products));

            for (Product product : products) {
                loanDtos.add(LoanMapper.toChildLoan(financingObject.id(), financingObject.status(), borrowerNames, limit, product));
            }
        }
        return loanDtos;
    }
}
