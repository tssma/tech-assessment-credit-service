package org.matthiaskarl.techassessment.creditservice.mapping;

import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
public class AnnualFrequencyMapper implements Function<Number, String> {

    @Override
    public String apply(Number frequency) {
        return switch (frequency.intValue()) {
            case 1 -> "Annual";
            case 2 -> "Semiannual";
            case 3 -> "Every 4 months";
            case 4 -> "Quarterly";
            case 6 -> "Bi-monthly";
            case 12 -> "Monthly";
            default -> "Every " + frequency + " months";
        };
    }

}
