package me.flash.distributedbatch.consumer;

/**
 * Prime factors processing. Emulates workload.
 */
public class PrimeFactors {
    /**
     * Retrieve count of prime factors
     *
     * @param number Number to count prime factors
     * @return Count of prime factors
     */
    public int countOfFactors(final long number) {
        long reminder = number;
        int divisor = 2, count = 0;
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
