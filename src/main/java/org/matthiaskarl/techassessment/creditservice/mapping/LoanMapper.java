package org.matthiaskarl.techassessment.creditservice.mapping;

import org.matthiaskarl.techassessment.creditservice.domain.Limit;
import org.matthiaskarl.techassessment.creditservice.domain.Product;
import org.matthiaskarl.techassessment.creditservice.domain.RealSecurity;
import org.matthiaskarl.techassessment.creditservice.dto.CollateralDto;
import org.matthiaskarl.techassessment.creditservice.dto.LoanDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class LoanMapper {

    private static final DateTimeFormatter SOURCE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static LoanDto toChildLoan(
            long financingObjectId,
            String status,
            List<String> borrowerNames,
            Limit limit,
            Product product
    ) {
        String id = "product-" + product.id();
        String parentLoanId = "fo-" + financingObjectId;

        String startDateIso = toIsoDateTime(product.startDate());
        String endDateIso = (product.endDate() == null || product.endDate().isBlank()) ? null : toIsoDateTime(product.endDate());

        return new LoanDto(
                id,
                "ChildLoan",
                product.name(),
                product.productNumber(),
                status,
                product.currencyCode(),
                toStringAmount(product.amount()),
                toStringAmount(limit.limitAmount()),
                toStringAmount(product.interestRate()),
                toStringAmount(product.interestDue()),
                product.isOverdue(),
                parentLoanId,
                startDateIso,
                endDateIso,
                borrowerNames,
                product.defaultSettlementAccountNumber(),
                amortisationFrequencyToText(limit.agreedAmortisationFrequency()),
                interestFrequencyToText(product.interestPaymentFrequency()),
                mapCollateral(limit)
        );
    }

    public static LoanDto toParentLoan(
            long financingObjectId,
            String status,
            List<String> borrowerNames,
            Limit limit,
            List<Product> products
    ) {
        String parentLoanId = "fo-" + financingObjectId;
        String name = (limit != null && limit.name() != null && !limit.name().isBlank()) ? limit.name() : ("Financing Object " + financingObjectId);
        String contractNumber = (limit != null) ? limit.contractNumber() : "";

        BigDecimal sumOutstanding = products.stream()
                .map(product -> BigDecimal.valueOf(product.amount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal sumInterestDue = products.stream()
                .map(product -> BigDecimal.valueOf(product.interestDue()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = products.stream()
                .map(product -> BigDecimal.valueOf(product.amount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal weightedRate = BigDecimal.ZERO;
        if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal numerator = products.stream()
                    .map(product -> BigDecimal.valueOf(product.amount()).multiply(BigDecimal.valueOf(product.interestRate())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            weightedRate = numerator.divide(totalAmount, 6, RoundingMode.HALF_UP);
        }

        boolean anyOverdue = products.stream().anyMatch(Product::isOverdue);

        Optional<LocalDate> minStart = products.stream()
                .map(product -> LocalDate.parse(product.startDate(), SOURCE_DATE_TIME_FORMATTER))
                .min(Comparator.naturalOrder());

        Optional<LocalDate> maxEnd = products.stream()
                .map(Product::endDate)
                .filter(string -> string != null && !string.isBlank())
                .map(string -> LocalDate.parse(string, SOURCE_DATE_TIME_FORMATTER))
                .max(Comparator.naturalOrder());

        String startDateIso = minStart.map(LoanMapper::toIsoDateTime).orElse(null);
        String endDateIso = maxEnd.map(LoanMapper::toIsoDateTime).orElse(null);

        String currency = dominantCurrency(products);

        String settlement = products.isEmpty() ? "" : products.getFirst().defaultSettlementAccountNumber();

        String interestPaymentFreqText = mergedInterestPaymentFrequency(products);

        return new LoanDto(
                parentLoanId,
                "ParentLoan",
                name,
                contractNumber,
                status,
                currency,
                toPlain(sumOutstanding),
                (limit != null) ? toStringAmount(limit.limitAmount()) : "0",
                toPlain(weightedRate),
                toPlain(sumInterestDue),
                anyOverdue,
                parentLoanId,
                startDateIso,
                endDateIso,
                borrowerNames,
                settlement,
                (limit != null) ? amortisationFrequencyToText(limit.agreedAmortisationFrequency()) : "Custom",
                interestPaymentFreqText,
                mapCollateral(limit)
        );
    }

    private static String toIsoDateTime(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ISO_DATE) + "T00:00:00.000Z";
    }

    private static String toIsoDateTime(String ddMMyyyy) {
        LocalDate localDate = LocalDate.parse(ddMMyyyy, SOURCE_DATE_TIME_FORMATTER);
        return toIsoDateTime(localDate);
    }

    private static List<CollateralDto> mapCollateral(Limit limit) {
        if (limit == null || limit.realSecurities() == null) return List.of();
        String amortisationAmountAnnual = toStringAmount(limit.amortisationAmountAnnual());
        List<CollateralDto> collateralDtos = new ArrayList<>();
        for (RealSecurity realSecurity : limit.realSecurities()) {
            String next = LocalDate.parse(realSecurity.nextRevaluationDate(), SOURCE_DATE_TIME_FORMATTER).format(DateTimeFormatter.ISO_DATE);
            collateralDtos.add(new CollateralDto(
                    realSecurity.type(),
                    toStringAmount(realSecurity.collateralValue()),
                    realSecurity.currency(),
                    realSecurity.address(),
                    next,
                    amortisationAmountAnnual
            ));
        }
        return collateralDtos;
    }

    private static String toStringAmount(double doubleValue) {
        return BigDecimal.valueOf(doubleValue).stripTrailingZeros().toPlainString();
    }

    private static String toPlain(BigDecimal bigDecimal) {
        return bigDecimal.stripTrailingZeros().toPlainString();
    }

    private static String amortisationFrequencyToText(int frequency) {
        return switch (frequency) {
            case 1 -> "Annual";
            case 2 -> "Semiannual";
            case 3 -> "Every 4 months";
            case 4 -> "Quarterly";
            case 6 -> "Bi-monthly";
            case 12 -> "Monthly";
            default -> "Custom";
        };
    }

    private static String interestFrequencyToText(int frequency) {
        return switch (frequency) {
            case 1 -> "Monthly";
            case 2 -> "Bi-monthly";
            case 3 -> "Quarterly";
            case 4 -> "Triannual";
            case 6 -> "Semiannual";
            case 12 -> "Annual";
            default -> "Custom";
        };
    }

    private static String mergedInterestPaymentFrequency(List<Product> products) {
        Set<String> freq = products.stream()
                .map(product -> interestFrequencyToText(product.interestPaymentFrequency()))
                .collect(Collectors.toSet());
        return (freq.size() == 1) ? freq.iterator().next() : "Custom";
    }

    private static String dominantCurrency(List<Product> products) {
        if (products.isEmpty()) return "";
        return products.stream()
                .collect(Collectors.groupingBy(Product::currencyCode, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(products.getFirst().currencyCode());
    }

}
