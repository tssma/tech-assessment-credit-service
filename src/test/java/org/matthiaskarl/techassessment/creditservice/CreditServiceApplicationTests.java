package org.matthiaskarl.techassessment.creditservice;

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

import java.util.List;
import java.util.stream.StreamSupport;

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

        Assertions.assertThat(borrowerNames.size()).isEqualTo(1);

        List<FinancingObject> financingObjects = financingObjectRepository.findByOwnerId(Long.parseLong(userId));
        financingObjects.forEach(financingObject -> Assertions.assertThat(
                        financingObject.owners().stream()
                                .map(Owner::id)
                                .toList())
                .contains(Long.valueOf(userId)));
    }

}
