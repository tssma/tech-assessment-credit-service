package org.matthiaskarl.techassessment.creditservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.matthiaskarl.techassessment.creditservice.domain.FinancingObject;
import org.matthiaskarl.techassessment.creditservice.domain.Owner;
import org.matthiaskarl.techassessment.creditservice.repository.FinancingObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.matthiaskarl.techassessment.creditservice.ContractTestUtil.fetchLoans;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CreditServiceApplicationTests {

    @LocalServerPort
    int port;

    @Autowired
    protected FinancingObjectRepository financingObjectRepository;

    @Test
    void contextLoads() {
    }

    @ParameterizedTest
    @DisplayName("The list of loans returned only contains loans owned by the user with the provided userId")
    @MethodSource("org.matthiaskarl.techassessment.creditservice.ContractTestUtil#getUserIds")
    void onlyReturnsLoansForUserId(String userId) throws Exception {
        ArrayNode loans = fetchLoans(port, userId);

        List<String> borrowerNames = StreamSupport.stream(loans.spliterator(), false)
                .map(loan -> loan.get("borrower").textValue())
                .distinct()
                .toList();

        assertThat(borrowerNames.size()).isEqualTo(1);

        List<FinancingObject> financingObjects = financingObjectRepository.findByOwnerId(Long.parseLong(userId));
        financingObjects.forEach(financingObject -> assertThat(
                financingObject.owners().stream()
                        .map(Owner::id)
                        .toList())
                .contains(Long.valueOf(userId)));
    }

    @ParameterizedTest
    @DisplayName("""
            GIVEN a financing object with two products "prodA" & "prodB"
            AND "prodA" outstandingAmount = 120'000
            AND "prodB" outstandingAmount = 85'000
            WHEN the data is returned from the API
            THEN the parentLoan outstandingAmount is set to 205'000
            AND the two childLoan oustandingAmounts are set to 120'000 and 85'000 respectively
            """)
    @MethodSource("org.matthiaskarl.techassessment.creditservice.ContractTestUtil#getUserIds")
    void sumAmountsInParentLoan(String userId) throws Exception {
        ArrayNode loans = fetchLoans(port, userId);
        JsonNode parentLoan = StreamSupport.stream(loans.spliterator(), false)
                .filter(loan -> loan.get("loanType").asText().equals("ParentLoan"))
                .findAny()
                .orElseThrow();

        BigDecimal childLoanSum = StreamSupport.stream(loans.spliterator(), false)
                .filter(loan -> loan.get("loanType").asText().equals("ChildLoan"))
                .map(childLoan -> childLoan.get("outstandingAmount").decimalValue())
                .reduce(BigDecimal::add)
                .orElseThrow();

        Assertions.assertThat(parentLoan.get("outstandingAmount").decimalValue()).isEqualByComparingTo(childLoanSum);
    }

    @Test
    @DisplayName("""
                GIVEN a financing object with two products "prodA" & "prodB"
                AND "prodA" startDate = 15.12.2020, endDate = 15.12.2030
                AND "prodB" startDate = 01.11.2020, endDate = 01.11.2025
                WHEN the data is returned from the API
                THEN the parentLoan startDate = 01.11.2020, endDate = 15.12.2030
                AND the two childLoan startDate and endDate are set to startDate = 15.12.2020, endDate = 15.12.2030 and startDate = 01.11.2020, endDate = 01.11.2025 respectively
            """)
    void parentLoanHasOverallStartAndEndDates() throws Exception {
        ArrayNode loans = fetchLoans(port, "11110039");

        JsonNode parentLoan = StreamSupport.stream(loans.spliterator(), false)
                .filter(loan -> loan.get("loanType").asText().equals("ParentLoan"))
                .findAny()
                .orElseThrow();

        List<JsonNode> childLoans = StreamSupport.stream(loans.spliterator(), false)
                .filter(loan -> loan.get("loanType").asText().equals("ChildLoan"))
                .toList();

        assertThat(parentLoan.get("startDate").asText()).isEqualTo("2019-04-14T00:00:00.000Z");
        assertThat(parentLoan.get("endDate").asText()).isEqualTo("2033-03-22T00:00:00.000Z");

        assertThat(childLoans.stream().anyMatch(cl ->
                cl.get("startDate").asText().equals("2019-04-14T00:00:00.000Z") && cl.get("endDate").asText().equals("2029-04-14T00:00:00.000Z")
        )).isTrue();

        assertThat(childLoans.stream().anyMatch(cl ->
                cl.get("startDate").asText().equals("2023-03-22T00:00:00.000Z") && cl.get("endDate").asText().equals("2033-03-22T00:00:00.000Z")
        )).isTrue();
    }

    @ParameterizedTest
    @DisplayName("""
            #1
            GIVEN a financing object with two products "prodA" & "prodB"
            AND "prodA" isOverdue = true
            AND "prodB" isOverdue = false
            WHEN the data is returned from the API
            THEN the parentLoan isOverdue = true
            AND the two childLoan isOverdue is set to true and false respectively
            #2
            GIVEN a financing object with two products "prodA" & "prodB"
            AND "prodA" isOverdue = false
            AND "prodB" isOverdue = false
            WHEN the data is returned from the API
            THEN the parentLoan isOverdue = false
            AND the two childLoan isOverdue is set to false for both
            """)
    @MethodSource("org.matthiaskarl.techassessment.creditservice.ContractTestUtil#getUserIds")
    void lendingOverdue(String userId) throws Exception {
        ArrayNode loans = fetchLoans(port, userId);

        JsonNode parentLoan = StreamSupport.stream(loans.spliterator(), false)
                .filter(loan -> loan.get("loanType").asText().equals("ParentLoan"))
                .findAny()
                .orElseThrow();

        List<JsonNode> childLoans = StreamSupport.stream(loans.spliterator(), false)
                .filter(loan -> loan.get("loanType").asText().equals("ChildLoan"))
                .toList();

        boolean anyChildOverdue = childLoans.stream()
                .anyMatch(cl -> cl.get("isOverdue").booleanValue());

        assertThat(parentLoan.get("isOverdue").booleanValue()).isEqualTo(anyChildOverdue);
    }

    @Test
    @DisplayName("""
                GIVEN a financing object with a limit "LimA"
                AND "limA" amortisationAmountAnnual = 5000
                AND "limA" agreedAmortisationFrequency = 4
                WHEN the data is returned from the API
                THEN the parentLoan amortisationPaymentAmount = 1250 (5000/4)
                AND the parentLoan paymentFrequency = 4
                AND the childLoan(s) amortisationPaymentAmount = null and paymentFrequency = null
            """)
    void parentLoanAmortisationPaymentAmountAndFrequency() throws Exception {
        ArrayNode loans = fetchLoans(port, "11110039");

        JsonNode parentLoan = StreamSupport.stream(loans.spliterator(), false)
                .filter(loan -> loan.get("loanType").asText().equals("ParentLoan"))
                .findAny()
                .orElseThrow();

        assertThat(parentLoan.get("collateral").get(0).get("amortisationPaymentAmount").textValue()).isEqualTo("1666.67");
        assertThat(parentLoan.get("paymentFrequency").textValue()).isEqualTo("6");

        loans.forEach(loan -> {
            if (loan.get("loanType").asText().equals("ChildLoan")) {
                assertThat(loan.get("collateral").get(0).get("amortisationPaymentAmount").isNull()).isTrue();
                assertThat(loan.get("paymentFrequency").isNull()).isTrue();
            }
        });
    }

}
