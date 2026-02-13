package com.Saurabh.ETA.Io.Summary;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SummaryResponse {
    private int year;
    private int month;
    private Double totalIncome;
    private Double totalExpense;
    private Double totalBalance;
    private String topMostExpense;
    private String secondMostExpense;
    private Double topMostExpenseAmount;
    private Double secondMostExpenseAmount;
}
