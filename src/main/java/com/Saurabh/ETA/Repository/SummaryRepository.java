package com.Saurabh.ETA.Repository;

import com.Saurabh.ETA.Entity.SummaryEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SummaryRepository extends JpaRepository<SummaryEntity, Long> {

    Optional<SummaryEntity> findByUserAndYearAndMonth(UserEntity user, int year, int month);

    List<SummaryEntity> findTop12ByUserOrderByYearDescMonthDesc(
            UserEntity user
    );

    @Query("""
    SELECT s FROM SummaryEntity s
    WHERE s.user = :user
    ORDER BY s.year DESC, s.month DESC
""")
    List<SummaryEntity> findRecentSummaries(
            @Param("user") UserEntity user,
            Pageable pageable
    );

    boolean existsByUserAndYearAndMonth(UserEntity user, int year, int month);

    void deleteByUserAndYearAndMonth(UserEntity user, int year, int month);
}
