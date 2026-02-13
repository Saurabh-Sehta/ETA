package com.Saurabh.ETA.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "summary_tbl",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "year", "month"})
        }
)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SummaryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @NotNull
    private Integer year;
    @NotNull
    private Integer month;

    private Double totalIncome;
    private Double totalExpense;
    private Double totalBalance;

    private String topMostExpense;
    private String secondMostExpense;

    private Double topMostExpenseAmount;
    private Double secondMostExpenseAmount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

}
