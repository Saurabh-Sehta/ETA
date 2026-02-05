package com.Saurabh.ETA.Io.Income;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AddIncomeRequest {

    private String icon;
    @NotNull(message = "Source is required")
    private String source;
    @Positive(message = "Amount should be greater that 0")
    private Double amount;
    @NotNull(message = "Date is required")
    @PastOrPresent(message = "Date cannot be in the future")
    private LocalDate date;
}
