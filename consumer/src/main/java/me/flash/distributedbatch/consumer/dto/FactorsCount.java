package me.flash.distributedbatch.consumer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FactorsCount {
    private long number;
    private int count;
}
