package com.Saurabh.ETA.Service;

import com.Saurabh.ETA.Entity.ExpenseEntity;
import com.Saurabh.ETA.Entity.IncomeEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Dashboard.DashboardResponse;
import com.Saurabh.ETA.Io.Dashboard.PeriodTransactionsDTO;
import com.Saurabh.ETA.Io.Dashboard.RecentTransactionDTO;
import com.Saurabh.ETA.Repository.ExpenseRepository;
import com.Saurabh.ETA.Repository.IncomeRepository;
import com.Saurabh.ETA.Repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UsersRepository usersRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;

    public ResponseEntity<DashboardResponse> getData(String email) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Not authorized"));
        try{
            Double totalIncome = incomeRepository.getTotalIncome(user);
            Double totalExpense = expenseRepository.getTotalExpense(user);

            LocalDate last30Days = LocalDate.now().minusDays(30);
            LocalDate last60Days = LocalDate.now().minusDays(60);

            List<ExpenseEntity> last30Expenses =
                    expenseRepository.findByUserAndDateAfterOrderByDateDesc(user, last30Days);
            List<IncomeEntity> last60Income =
                    incomeRepository.findByUserAndDateAfterOrderByDateDesc(user, last60Days);

            Double expense30Total = last30Expenses.stream()
                    .mapToDouble(ExpenseEntity::getAmount)
                    .sum();

            Double income60Total = last60Income.stream()
                    .mapToDouble(IncomeEntity::getAmount)
                    .sum();

            List<RecentTransactionDTO> recentTransactions = new ArrayList<>();

            incomeRepository.findTop5ByUserOrderByDateDesc(user)
                    .forEach(i -> recentTransactions.add(
                            RecentTransactionDTO.builder()
                                    .id(i.getId())
                                    .type("income")
                                    .icon(i.getIcon())
                                    .title(i.getSource())
                                    .amount(i.getAmount())
                                    .date(i.getDate())
                                    .build()
                    ));

            expenseRepository.findTop5ByUserOrderByDateDesc(user)
                    .forEach(e -> recentTransactions.add(
                            RecentTransactionDTO.builder()
                                    .id(e.getId())
                                    .type("expense")
                                    .icon(e.getIcon())
                                    .title(e.getCategory())
                                    .amount(e.getAmount())
                                    .date(e.getDate())
                                    .build()
                    ));

            recentTransactions.sort(
                    Comparator.comparing(RecentTransactionDTO::getDate).reversed()
            );

            if (recentTransactions.size() > 5) {
                recentTransactions.subList(5, recentTransactions.size()).clear();
            }

            DashboardResponse response =
                    DashboardResponse.builder()
                    .totalIncome(totalIncome)
                    .totalExpenses(totalExpense)
                    .totalBalance(totalIncome - totalExpense)
                    .last30DaysExpenses(
                            PeriodTransactionsDTO.<ExpenseEntity>builder()
                                    .total(expense30Total)
                                    .transactions(last30Expenses)
                                    .build()
                    )
                    .last60DaysIncome(
                            PeriodTransactionsDTO.<IncomeEntity>builder()
                                    .total(income60Total)
                                    .transactions(last60Income)
                                    .build()
                    )
                    .recentTransactions(recentTransactions)
                    .build();
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
