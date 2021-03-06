package me.flash.distributedbatch.consumer.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PrimeFactorsTest {

    private static PrimeFactors primeFactors = new PrimeFactors();

    static Stream<Arguments> divisorCounts() {
        return Stream.of(
                arguments(-1L, -1),
                arguments(0L, -1),
                arguments(1L, -1),
                arguments(2L, 1),
                arguments(3L, 1),
                arguments(4L, 2),
                arguments(5L, 1),
                arguments(511L, 2),
                arguments(6701L, 1),
                arguments(3628800L, 15),
                arguments(770527199232000L, 39),
                arguments(1235131L, 1)
        );
    }

    @ParameterizedTest(name = "There should be {1} prime factors for number {0}.")
    @MethodSource("divisorCounts")
    @DisplayName("Calculation of prime factors count")
    void testCountOfFactors(long number, int expectedCount) {
        assertThat(primeFactors.countOfFactors(number)).isEqualTo(expectedCount);
    }
}