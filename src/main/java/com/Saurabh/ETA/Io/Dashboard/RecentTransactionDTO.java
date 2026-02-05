package com.Saurabh.ETA.Io.Dashboard;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RecentTransactionDTO {
    private Long id;
    private String type; // "income" or "expense"
    private String title; // source / category
    private Double amount;
    private String icon;
    private LocalDate date;
}
