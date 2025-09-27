package org.matthiaskarl.techassessment.creditservice.contract;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.matthiaskarl.techassessment.creditservice.contract.ContractTestUtil.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContractHierarchyTest {

    @LocalServerPort
    int port;

    @Test
    void parent_child_hierarchy_and_aggregations() throws Exception {
        JsonNode loans = fetchLoans(port, "11110001");

        List<JsonNode> parentLoans = new ArrayList<>();
        List<JsonNode> childLoans = new ArrayList<>();

        loans.forEach(loan -> {
            String type = getText(loan, "loanType");
            if ("ParentLoan".equals(type)) {
                parentLoans.add(loan);
            } else if ("ChildLoan".equals(type)) {
                childLoans.add(loan);
            } else {
                throw new IllegalStateException("Unexpected loanType: " + type);
            }
        });

        assertThat(parentLoans).as("At least one ParentLoan must exist").isNotEmpty();

        Map<String, List<JsonNode>> childLoansByParentLoanId = childLoans.stream()
                .collect(Collectors.groupingBy(child -> Objects.requireNonNull(getText(child, "parentLoanId"))));

        for (JsonNode parentLoan : parentLoans) {
            String parentId = getText(parentLoan, "id");

            assertThat(getText(parentLoan, "parentLoanId"))
                    .as("ParentLoan.parentLoanId equals its own id")
                    .isEqualTo(parentId);

            List<JsonNode> childLoansForParentId = childLoansByParentLoanId.getOrDefault(parentId, List.of());
            assertThat(childLoansForParentId).as("Each ParentLoan must have at least one ChildLoan").isNotEmpty();

            for (JsonNode childLoanForParentId : childLoansForParentId) {
                assertThat(getText(childLoanForParentId, "loanType")).isEqualTo("ChildLoan");
                assertThat(getText(childLoanForParentId, "parentLoanId")).isEqualTo(parentId);
            }

            BigDecimal sumOutstanding = childLoansForParentId.stream()
                    .map(k -> toBigDecimal(getText(k, "outstandingAmount")))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros();

            BigDecimal sumInterestDue = childLoansForParentId.stream()
                    .map(k -> toBigDecimal(getText(k, "interestDue")))
                    .reduce(BigDecimal.ZERO, BigDecimal::add).stripTrailingZeros();

            BigDecimal totalAmount = childLoansForParentId.stream()
                    .map(k -> toBigDecimal(getText(k, "outstandingAmount")))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal weightedRate = BigDecimal.ZERO;
            if (totalAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal numerator = childLoansForParentId.stream()
                        .map(k -> toBigDecimal(getText(k, "outstandingAmount"))
                                .multiply(toBigDecimal(getText(k, "interestRate"))))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                weightedRate = numerator.divide(totalAmount, 6, RoundingMode.HALF_UP).stripTrailingZeros();
            }

            assertThat(toBigDecimal(getText(parentLoan, "outstandingAmount")).stripTrailingZeros())
                    .as("Parent outstandingAmount == sum(children outstandingAmount)")
                    .isEqualByComparingTo(sumOutstanding);

            assertThat(toBigDecimal(getText(parentLoan, "interestDue")).stripTrailingZeros())
                    .as("Parent interestDue == sum(children interestDue)")
                    .isEqualByComparingTo(sumInterestDue);

            assertThat(toBigDecimal(getText(parentLoan, "interestRate")).stripTrailingZeros())
                    .as("Parent interestRate == weighted average(children)")
                    .isEqualByComparingTo(weightedRate);

            boolean anyKidOverdue = childLoansForParentId.stream().anyMatch(k -> k.get("isOverdue").asBoolean());
            assertThat(parentLoan.get("isOverdue").asBoolean())
                    .as("Parent isOverdue == any child overdue")
                    .isEqualTo(anyKidOverdue);

            Optional<OffsetDateTime> minStart = childLoansForParentId.stream()
                    .map(k -> parseOffsetDateTime(getText(k, "startDate")))
                    .min(Comparator.naturalOrder());

            Optional<OffsetDateTime> maxEnd = childLoansForParentId.stream()
                    .map(k -> k.hasNonNull("endDate") ? parseOffsetDateTime(getText(k, "endDate")) : null)
                    .filter(Objects::nonNull)
                    .max(Comparator.naturalOrder());

            assertThat(parseOffsetDateTime(getText(parentLoan, "startDate")))
                    .as("Parent startDate == earliest child startDate")
                    .isEqualTo(minStart.orElse(null));

            if (parentLoan.hasNonNull("endDate")) {
                assertThat(parseOffsetDateTime(getText(parentLoan, "endDate")))
                        .as("Parent endDate == latest non-null child endDate")
                        .isEqualTo(maxEnd.orElse(null));
            } else {
                assertThat(maxEnd).as("Parent endDate null when all children have null endDate")
                        .isEmpty();
            }

            var childPayFreq = childLoansForParentId.stream().map(k -> getText(k, "paymentFrequency")).collect(Collectors.toSet());
            assertThat(childPayFreq.size()).as("Children share paymentFrequency").isEqualTo(1);
            assertThat(getText(parentLoan, "paymentFrequency")).isEqualTo(childPayFreq.iterator().next());

            var childIntFreq = childLoansForParentId.stream().map(k -> getText(k, "interestPaymentFrequency")).collect(Collectors.toSet());
            String expectedParentIntFreq = (childIntFreq.size() == 1) ? childIntFreq.iterator().next() : "Custom";
            assertThat(getText(parentLoan, "interestPaymentFrequency")).isEqualTo(expectedParentIntFreq);

            String parentLimit = getText(parentLoan, "creditLimit");
            for (JsonNode k : childLoansForParentId) {
                assertThat(getText(k, "creditLimit")).isEqualTo(parentLimit);
            }

            var childCurrencies = childLoansForParentId.stream().map(k -> getText(k, "currencyCode")).toList();
            var set = new HashSet<>(childCurrencies);
            String parentCurrency = getText(parentLoan, "currencyCode");
            if (set.size() == 1) {
                assertThat(parentCurrency).isEqualTo(childCurrencies.getFirst());
            } else {
                var dominant = counts(childCurrencies).entrySet().stream()
                        .max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse("");
                assertThat(parentCurrency).isEqualTo(dominant);
            }

            var parentBorrowers = asStringList(parentLoan.get("borrower"));
            for (JsonNode k : childLoansForParentId) {
                assertThat(asStringList(k.get("borrower"))).isEqualTo(parentBorrowers);
            }

            int parentCollSize = parentLoan.get("collateral").size();
            for (JsonNode k : childLoansForParentId) {
                assertThat(k.get("collateral").size()).isEqualTo(parentCollSize);
            }
        }
    }
}
