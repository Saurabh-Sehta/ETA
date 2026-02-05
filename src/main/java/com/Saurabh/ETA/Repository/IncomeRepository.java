package com.Saurabh.ETA.Repository;

import com.Saurabh.ETA.Entity.IncomeEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {

    List<IncomeEntity> findByUserOrderByDateDesc(UserEntity user);

    Optional<IncomeEntity> findByUserAndId(UserEntity user, Long id);

    // ✅ total income
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeEntity i WHERE i.user = :user")
    Double getTotalIncome(@Param("user") UserEntity user);

    // ✅ last 60 days income transactions
    List<IncomeEntity> findByUserAndDateAfterOrderByDateDesc(
            UserEntity user,
            LocalDate date
    );

    // ✅ recent income
    List<IncomeEntity> findTop5ByUserOrderByDateDesc(UserEntity user);
}
