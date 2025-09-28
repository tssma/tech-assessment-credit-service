package org.matthiaskarl.techassessment.creditservice.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record LoanDto(
        String id,
        String loanType,
        String name,
        String contractNumber,
        String loanStatus,
        String currencyCode,
        String outstandingAmount,
        String creditLimit,
        String interestRate,
        String interestDue,
        Boolean isOverdue,
        String parentLoanId,
        String startDate,
        String endDate,
        List<String> borrower,
        String defaultSettlementAccountNumber,
        String paymentFrequency,
        String interestPaymentFrequency,
        List<CollateralDto> collateral
) {
}
