package org.matthiaskarl.techassessment.creditservice.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

final class ContractTestUtil {
    static final ObjectMapper MAPPER = new ObjectMapper();

    static final Set<String> LOAN_TYPES = Set.of("ParentLoan", "ChildLoan");
    static final Set<String> LOAN_STATUS = Set.of("active", "inactive");
    static final Set<String> PAYMENT_FREQ = Set.of("Annual", "Semiannual", "Every 4 months", "Quarterly", "Bi-monthly", "Monthly", "Custom");
    static final Set<String> INTEREST_PAYMENT_FREQ = Set.of("Monthly", "Bi-monthly", "Quarterly", "Triannual", "Semiannual", "Annual", "Custom");

    // TODO extract from data
    static Stream<String> getUserIds() {
        return Stream.of(
                "11110001",
                "11110002",
                "11110003",
                "11110004",
                "11110005",
                "11110006",
                "11110007",
                "11110008",
                "11110009",
                "11110010",
                "11110011",
                "11110012",
                "11110013",
                "11110014",
                "11110015",
                "11110016",
                "11110017",
                "11110018",
                "11110019",
                "11110020"
        );
    }

    static JsonNode fetchLoans(int port, String userId) throws Exception {
        String base = "http://localhost:" + port;

        Response response = RestAssured.given()
                .when()
                .get(base + "/service/v1/loansByUser/{userId}", userId);

        return MAPPER.readTree(response.getBody().asString());
    }

    static void assertRequiredText(JsonNode node, String field) {
        assertThat(node.get(field)).as("%s present", field).isNotNull();
        assertThat(node.get(field).isTextual()).as("%s is string", field).isTrue();
        assertThat(node.get(field).asText()).as("%s not blank", field).isNotBlank();
    }

    static void assertRequiredArray(JsonNode node, String field) {
        assertThat(node.get(field)).as("%s present", field).isNotNull();
        assertThat(node.get(field).isArray()).as("%s is array", field).isTrue();
    }

    static void assertNumericString(JsonNode node, String field) {
        String v = getText(node, field);
        assertThat(v).as("%s numeric-like string", field).matches("^-?\\d+(\\.\\d+)?$");
    }

    static void assertIsoInstant(JsonNode node, String field) {
        String v = getText(node, field);
        try {
            OffsetDateTime.parse(Objects.requireNonNull(v));
        } catch (DateTimeParseException e) {
            throw new AssertionError(field + " must be ISO-8601 datetime, but was: " + v, e);
        }
    }

    static boolean isIsoDate(String v) {
        return v != null && v.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }

    static String getText(JsonNode node, String inputField) {
        JsonNode field = node.get(inputField);
        return (field == null || field.isNull()) ? null : field.asText();
    }

    static BigDecimal toBigDecimal(String s) {
        return new BigDecimal(s);
    }

    static OffsetDateTime parseOffsetDateTime(String string) {
        return OffsetDateTime.parse(string);
    }

    static List<String> asStringList(JsonNode arrayNode) {
        List<String> list = new ArrayList<>();
        arrayNode.forEach(n -> list.add(n.asText()));
        return list;
    }

    static Map<String, Long> counts(List<String> list) {
        return list.stream().collect(Collectors.groupingBy(x -> x, Collectors.counting()));
    }

}
