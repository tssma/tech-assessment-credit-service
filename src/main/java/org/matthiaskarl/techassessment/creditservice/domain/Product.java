package org.matthiaskarl.techassessment.creditservice.domain;

public record Product(
        long id,
        String name,
        String type,
        double amount,
        String currencyCode,
        double interestRate,
        String startDate,
        String endDate,
        String productNumber,
        String defaultSettlementAccountNumber,
        double interestDue,
        boolean isOverdue,
        int interestPaymentFrequency
) {
}
