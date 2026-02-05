package com.Saurabh.ETA.Io.Expense;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AddExpenseResponse {

        private Long userId;
        private String icon;
        private String category;
        private Double amount;
        private LocalDate date;
        private Long id;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

}
