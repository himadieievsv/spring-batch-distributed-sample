package me.flash.distributedbatch.consumer.domain;

/**
 * Prime factors processing. Emulates workload.
 */
public class PrimeFactors {
    /**
     * Retrieve count of prime factors.
     *
     * @param number Number to count prime factors
     * @return Count of prime factors
     */
    public int countOfFactors(final long number) {
        long reminder = number;
        long divisor = 2L;
        int count = 0;
        if (number < 2) {
            return -1;
        }
        while (reminder > 1) {
            while (reminder % divisor == 0) {
                count++;
                reminder /= divisor;
            }
            divisor++;
        }
        return count;
    }
}
