package org.matthiaskarl.techassessment.creditservice.mapping;

import lombok.RequiredArgsConstructor;
import org.matthiaskarl.techassessment.creditservice.domain.*;
import org.matthiaskarl.techassessment.creditservice.dto.CollateralDto;
import org.matthiaskarl.techassessment.creditservice.dto.LoanDto;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ChildLoanMapper implements Function<ChildLoanMappingRequest, LoanDto> {

    private static final DateTimeFormatter SOURCE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AnnualFrequencyMapper annualFrequencyMapper;

    @Override
    public LoanDto apply(ChildLoanMappingRequest request) {
        FinancingObject financingObject = request.financingObject();
        Limit limit = request.limit();
        Product product = request.product();

        List<String> borrowerNames = financingObject.owners().stream()
                .map(Owner::name)
                .toList();

        String startDate = StringUtils.hasText(product.startDate()) ? LocalDate.parse(product.startDate(), SOURCE_DATE_TIME_FORMATTER).format(DateTimeFormatter.ISO_DATE) + "T00:00:00.000Z" : "";
        String endDate = StringUtils.hasText(product.endDate()) ? LocalDate.parse(product.endDate(), SOURCE_DATE_TIME_FORMATTER).format(DateTimeFormatter.ISO_DATE) + "T00:00:00.000Z" : "";

        String parentLoanId = String.valueOf(financingObject.id());
        return LoanDto.builder()
                .id(String.valueOf(product.id()))
                .loanType("ChildLoan")
                .name(product.name())
                .contractNumber(limit.contractNumber())
                .loanStatus(financingObject.status())
                .currencyCode(product.currencyCode())
                .outstandingAmount(String.valueOf(product.amount()))
                .creditLimit(String.valueOf(limit.limitAmount()))
                .interestRate(String.valueOf(product.interestRate()))
                .interestDue(String.valueOf(product.interestDue()))
                .isOverdue(product.isOverdue())
                .parentLoanId(parentLoanId)
                .startDate(startDate)
                .endDate(endDate)
                .borrower(borrowerNames)
                .defaultSettlementAccountNumber(product.defaultSettlementAccountNumber())
                .paymentFrequency(null)
                .interestPaymentFrequency(annualFrequencyMapper.apply(product.interestPaymentFrequency()))
                .collateral(mapCollaterals(limit))
                .build();
    }

    public List<CollateralDto> mapCollaterals(Limit limit) {
        List<CollateralDto> collaterals = new ArrayList<>();
        for (RealSecurity realSecurity : limit.realSecurities()) {
            CollateralDto collateralDto = CollateralDto.builder()
                    .type(realSecurity.type())
                    .currentValue(String.valueOf(realSecurity.collateralValue()))
                    .currencyCode(realSecurity.currency())
                    .specification(realSecurity.address())
                    .nextRevaluationDate(LocalDate.parse(realSecurity.nextRevaluationDate(), SOURCE_DATE_TIME_FORMATTER).format(DateTimeFormatter.ISO_DATE))
                    .amortisationPaymentAmount(null)
                    .build();
            collaterals.add(collateralDto);
        }
        return collaterals;
    }

}
