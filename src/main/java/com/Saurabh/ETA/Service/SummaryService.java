package com.Saurabh.ETA.Service;

import com.Saurabh.ETA.Entity.ExpenseEntity;
import com.Saurabh.ETA.Entity.IncomeEntity;
import com.Saurabh.ETA.Entity.SummaryEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Summary.SummaryResponse;
import com.Saurabh.ETA.Repository.ExpenseRepository;
import com.Saurabh.ETA.Repository.IncomeRepository;
import com.Saurabh.ETA.Repository.SummaryRepository;
import com.Saurabh.ETA.Repository.UsersRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final IncomeRepository incomeRepository;
    private final ExpenseRepository expenseRepository;
    private final UsersRepository usersRepository;

    @Transactional
    public void regenerateMonthlySummary(UserEntity user, int year, int month) {

        summaryRepository.deleteByUserAndYearAndMonth(user, year, month);
        summaryRepository.flush();

        generateMonthlySummary(user, year, month);

        cleanupOldTransactions(user);
    }

    @Transactional
    public void checkAndGenerateMonthlySummary(UserEntity user) {

        LocalDate now = LocalDate.now();
        LocalDate previousMonth = now.minusMonths(1);

        int year = previousMonth.getYear();
        int month = previousMonth.getMonthValue();

        boolean exists = summaryRepository
                .existsByUserAndYearAndMonth(user, year, month);

        if (!exists) {
            generateMonthlySummary(user, year, month);
            cleanupOldTransactions(user);
        }
    }

    private void generateMonthlySummary(UserEntity user, int year, int month) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());

        List<IncomeEntity> incomes =
                incomeRepository.findByUserAndDateBetween(user, start, end);

        List<ExpenseEntity> expenses =
                expenseRepository.findByUserAndDateBetween(user, start, end);

        Double totalIncome = incomes.stream()
                .mapToDouble(IncomeEntity::getAmount)
                .sum();

        Double totalExpense = expenses.stream()
                .mapToDouble(ExpenseEntity::getAmount)
                .sum();

        Double balance = totalIncome - totalExpense;

        Map<String, Double> categoryMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        ExpenseEntity::getCategory,
                        Collectors.summingDouble(ExpenseEntity::getAmount)
                ));

        List<Map.Entry<String, Double>> sorted =
                categoryMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                        .toList();

        String top = null;
        Double topAmount = 0.0;
        String second = null;
        Double secondAmount = 0.0;

        if (!sorted.isEmpty()) {
            top = sorted.get(0).getKey();
            topAmount = sorted.get(0).getValue();
        }

        if (sorted.size() > 1) {
            second = sorted.get(1).getKey();
            secondAmount = sorted.get(1).getValue();
        }

        SummaryEntity summary = SummaryEntity.builder()
                .user(user)
                .year(year)
                .month(month)
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .totalBalance(balance)
                .topMostExpense(top)
                .topMostExpenseAmount(topAmount)
                .secondMostExpense(second)
                .secondMostExpenseAmount(secondAmount)
                .build();

        summaryRepository.save(summary);
    }

    private void cleanupOldTransactions(UserEntity user) {

        LocalDate cutoff = LocalDate.now().minusMonths(12);

        expenseRepository.deleteByUserAndDateBefore(user, cutoff);
        incomeRepository.deleteByUserAndDateBefore(user, cutoff);
    }

    public List<SummaryResponse> getLast12MonthsSummary(String email) {

        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No user found"));

        Pageable pageable = PageRequest.of(0, 12);

        return summaryRepository
                .findRecentSummaries(user, pageable)
                .stream()
                .map(summary -> new SummaryResponse(
                        summary.getYear(),
                        summary.getMonth(),
                        summary.getTotalIncome(),
                        summary.getTotalExpense(),
                        summary.getTotalBalance(),
                        summary.getTopMostExpense(),
                        summary.getSecondMostExpense(),
                        summary.getTopMostExpenseAmount(),
                        summary.getSecondMostExpenseAmount()
                ))
                .toList();
    }
}
