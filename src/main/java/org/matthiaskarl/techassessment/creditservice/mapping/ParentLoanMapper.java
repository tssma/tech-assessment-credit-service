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
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ParentLoanMapper implements Function<ParentLoanMappingRequest, LoanDto> {

    private static final DateTimeFormatter SOURCE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Override
    public LoanDto apply(ParentLoanMappingRequest request) {
        FinancingObject financingObject = request.financingObject();
        Limit limit = request.limit();
        List<Product> products = request.products();

        String outstandingAmount = haveSameCurrency(products)
                ? products.stream()
                .map(Product::amount)
                .reduce(0L, Long::sum)
                .toString()
                : "";

        boolean anyOverdue = products.stream().anyMatch(Product::isOverdue);

        String minStartDate = products.stream()
                .map(Product::startDate)
                .filter(StringUtils::hasText)
                .map(startDate -> LocalDate.parse(startDate, SOURCE_DATE_TIME_FORMATTER))
                .min(Comparator.naturalOrder())
                .map(date -> date.format(DateTimeFormatter.ISO_DATE) + "T00:00:00.000Z")
                .orElse("");

        String maxEndDate = products.stream()
                .map(Product::endDate)
                .filter(StringUtils::hasText)
                .map(endDate -> LocalDate.parse(endDate, SOURCE_DATE_TIME_FORMATTER))
                .max(Comparator.naturalOrder())
                .map(date -> date.format(DateTimeFormatter.ISO_DATE) + "T00:00:00.000Z")
                .orElse("");

        List<String> borrowerNames = financingObject.owners().stream()
                .map(Owner::name)
                .toList();

        String interestDue = products.stream()
                .map(Product::interestDue)
                .min(Comparator.naturalOrder())
                .map(String::valueOf)
                .orElse("");

        String parentLoanId = String.valueOf(financingObject.id());
        return LoanDto.builder()
                .id(parentLoanId)
                .loanType("ParentLoan")
                .name(limit.name())
                .contractNumber(limit.contractNumber())
                .loanStatus(financingObject.status())
                .currencyCode(haveSameCurrency(products) ? products.getFirst().currencyCode() : "")
                .outstandingAmount(outstandingAmount)
                .creditLimit(String.valueOf(limit.limitAmount()))
                .interestRate(null)
                .interestDue(interestDue)
                .isOverdue(anyOverdue)
                .parentLoanId(parentLoanId)
                .startDate(minStartDate)
                .endDate(maxEndDate)
                .borrower(borrowerNames)
                .defaultSettlementAccountNumber(null)
                .paymentFrequency(String.valueOf(limit.agreedAmortisationFrequency()))
                .interestPaymentFrequency(null)
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
                    .amortisationPaymentAmount(String.valueOf(limit.amortisationAmountAnnual() / limit.agreedAmortisationFrequency()))
                    .build();
            collaterals.add(collateralDto);
        }
        return collaterals;
    }

    private static boolean haveSameCurrency(List<Product> products) {
        return products.stream().map(Product::currencyCode).distinct().count() > 1;
    }

}
