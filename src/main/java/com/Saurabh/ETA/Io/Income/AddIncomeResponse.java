package com.Saurabh.ETA.Io.Income;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class AddIncomeResponse {

    private Long userId;
    private String icon;
    private String source;
    private Double amount;
    private LocalDate date;
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
