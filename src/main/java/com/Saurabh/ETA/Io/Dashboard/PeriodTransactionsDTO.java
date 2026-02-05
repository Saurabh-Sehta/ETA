package com.Saurabh.ETA.Io.Dashboard;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PeriodTransactionsDTO<T> {
    private Double total;
    private List<T> transactions;
}
