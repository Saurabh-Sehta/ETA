package com.Saurabh.ETA.Io.Dashboard;

import com.Saurabh.ETA.Entity.ExpenseEntity;
import com.Saurabh.ETA.Entity.IncomeEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private Double totalBalance;
    private Double totalIncome;
    private Double totalExpenses;

    private PeriodTransactionsDTO<ExpenseEntity> last30DaysExpenses;
    private PeriodTransactionsDTO<IncomeEntity> last60DaysIncome;

    private List<RecentTransactionDTO> recentTransactions;

}
