package com.Saurabh.ETA.Repository;

import com.Saurabh.ETA.Entity.ExpenseEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    List<ExpenseEntity> findByUserOrderByDateDesc(UserEntity user);

    Optional<ExpenseEntity> findByUserAndId(UserEntity user, Long id);

    void deleteByUserAndId(UserEntity user, Long id);

    // total expense
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e WHERE e.user = :user")
    Double getTotalExpense(@Param("user") UserEntity user);

    // last 30 days expenses
    List<ExpenseEntity> findByUserAndDateAfterOrderByDateDesc(
            UserEntity user,
            LocalDate date
    );

    // recent expenses
    List<ExpenseEntity> findTop5ByUserOrderByDateDesc(UserEntity user);

    // Expenses in particular month
    List<ExpenseEntity> findByUserAndDateBetween(UserEntity user, LocalDate start, LocalDate end);

    void deleteByUserAndDateBefore(UserEntity user, LocalDate cutoff);
}
