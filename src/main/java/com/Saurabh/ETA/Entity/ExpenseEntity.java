package com.Saurabh.ETA.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expense_tbl")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String icon;
    @NotNull(message = "Category is required")
    private String category;
    @Positive(message = "Amount should be greater that 0")
    private Double amount;
    @NotNull(message = "Date is required")
    private LocalDate date;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
//
//    @CreationTimestamp
//    @Column(updatable = false)
//    private Timestamp createdAt;
//
//    @UpdateTimestamp
//    private Timestamp updatedAt;
}

