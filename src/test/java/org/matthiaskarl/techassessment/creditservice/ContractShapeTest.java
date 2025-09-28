package org.matthiaskarl.techassessment.creditservice;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.matthiaskarl.techassessment.creditservice.ContractTestUtil.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ContractShapeTest {

    @LocalServerPort
    int port;

    @ParameterizedTest
    @MethodSource("org.matthiaskarl.techassessment.creditservice.ContractTestUtil#getUserIds")
    void loansByUser_response_shape_and_formats(String userId) throws Exception {
        JsonNode loans = fetchLoans(port, userId);
        assertThat(loans.size()).isGreaterThan(0);

        for (JsonNode loan : loans) {
            validateLoanShape(loan);
        }
    }

    static void validateLoanShape(JsonNode loan) {
        assertRequiredText(loan, "id");
        assertRequiredText(loan, "name");
        assertRequiredText(loan, "contractNumber");
        assertRequiredText(loan, "currencyCode");
        assertRequiredText(loan, "outstandingAmount");
        assertRequiredText(loan, "creditLimit");
        if (isParentLoan(loan)) {
            assertThat(loan.get("interestRate").isNull()).isTrue();
        } else {
            assertRequiredText(loan, "interestRate");
        }
        assertRequiredText(loan, "interestDue");

        if (isParentLoan(loan)) {
            assertRequiredText(loan, "paymentFrequency");
        } else {
            assertThat(loan.get("paymentFrequency").isNull()).isTrue();
        }

        if (isParentLoan(loan)) {
            assertThat(loan.get("interestPaymentFrequency").isNull()).isTrue();
        } else {
            assertRequiredText(loan, "interestPaymentFrequency");
        }
        assertRequiredArray(loan, "borrower");
        assertRequiredArray(loan, "collateral");

        assertThat(LOAN_TYPES).contains(getText(loan, "loanType"));
        assertThat(LOAN_STATUS).contains(getText(loan, "loanStatus"));
        assertThat(getText(loan, "currencyCode")).hasSize(3);

        assertNumericString(loan, "outstandingAmount");
        assertNumericString(loan, "creditLimit");

        if (!isParentLoan(loan)) {
            assertNumericString(loan, "interestRate");
        }

        assertNumericString(loan, "interestDue");

        assertThat(loan.get("isOverdue")).isNotNull();
        assertThat(loan.get("isOverdue").isBoolean()).isTrue();

        if (StringUtils.hasText(loan.get("startDate").textValue())) {
            assertIsoInstant(loan, "startDate");
        }
        if (StringUtils.hasText(loan.get("endDate").textValue())) {
            assertIsoInstant(loan, "endDate");
        }

        if (loan.has("parentLoanId") && !loan.get("parentLoanId").isNull()) {
            assertThat(loan.get("parentLoanId").isTextual()).isTrue();
        }

        if (isParentLoan(loan)) {
            assertThat(loan.get("defaultSettlementAccountNumber").isNull()).isTrue();
        } else {
            assertRequiredText(loan, "defaultSettlementAccountNumber");
        }

        for (JsonNode col : loan.get("collateral")) {
            assertRequiredText(col, "type");
            assertRequiredText(col, "currentValue");
            assertRequiredText(col, "currencyCode");
            assertRequiredText(col, "specification");
            assertRequiredText(col, "nextRevaluationDate");

            if (isParentLoan(loan)) {
                assertRequiredText(col, "amortisationPaymentAmount");
            } else {
                assertThat(col.get("amortisationPaymentAmount").isNull()).isTrue();
            }

            assertThat(getText(col, "currencyCode")).hasSize(3);
            assertNumericString(col, "currentValue");


            if (isParentLoan(loan)) {
                assertNumericString(col, "amortisationPaymentAmount");
            }

            assertThat(isIsoDate(getText(col, "nextRevaluationDate"))).isTrue();
        }
    }

    private static boolean isParentLoan(JsonNode loan) {
        return "ParentLoan".equals(loan.get("loanType").asText());
    }

}
