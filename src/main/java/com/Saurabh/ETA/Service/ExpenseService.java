package com.Saurabh.ETA.Service;

import com.Saurabh.ETA.Entity.ExpenseEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Expense.AddExpenseRequest;
import com.Saurabh.ETA.Io.Expense.AddExpenseResponse;
import com.Saurabh.ETA.Io.Expense.DeleteResponse;
import com.Saurabh.ETA.Repository.ExpenseRepository;
import com.Saurabh.ETA.Repository.SummaryRepository;
import com.Saurabh.ETA.Repository.UsersRepository;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UsersRepository usersRepository;
    private final SummaryService summaryService;

    @Transactional
    public ResponseEntity<AddExpenseResponse> addExpense(AddExpenseRequest request, String email) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Unauthorized"));
        try{
            ExpenseEntity expense = ExpenseEntity.builder()
                    .user(user)
                    .icon(request.getIcon())
                    .category(request.getCategory())
                    .amount(request.getAmount())
                    .date(request.getDate())
                    .build();
            expenseRepository.save(expense);

            LocalDate expenseDate = expense.getDate();
            YearMonth expenseMonth = YearMonth.from(expenseDate);
            YearMonth currentMonth = YearMonth.now();

            if (expenseMonth.isBefore(currentMonth)) {
                summaryService.regenerateMonthlySummary(
                        user,
                        expenseMonth.getYear(),
                        expenseMonth.getMonthValue()
                );
            } else {
                summaryService.checkAndGenerateMonthlySummary(user);
            }

            AddExpenseResponse response = convertToAddExpenseResponse(user.getId(), expense);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private AddExpenseResponse convertToAddExpenseResponse(Long userId, ExpenseEntity expense) {
        return new AddExpenseResponse(
                userId,
                expense.getIcon(),
                expense.getCategory(),
                expense.getAmount(),
                expense.getDate(),
                expense.getId(),
                expense.getCreatedAt(),
                expense.getUpdatedAt()
        );
    }


    public ResponseEntity<List<AddExpenseResponse>> getAllExpense(String email) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Unauthorized"));
        List<AddExpenseResponse> response = new ArrayList<>();
        try{
            List<ExpenseEntity> expenses = expenseRepository.findByUserOrderByDateDesc(user);
            for(ExpenseEntity expense : expenses){
                AddExpenseResponse res = convertToAddExpenseResponse(user.getId(), expense);
                response.add(res);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<DeleteResponse> deleteExpense(String email, Long id) {
        try{
            UserEntity user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Not Authorized"));
            ExpenseEntity expense = expenseRepository.findByUserAndId(user, id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found"));

            LocalDate expenseDate = expense.getDate();
            YearMonth expenseMonth = YearMonth.from(expenseDate);
            YearMonth currentMonth = YearMonth.now();

            expenseRepository.delete(expense);

            if (expenseMonth.isBefore(currentMonth)) {
                summaryService.regenerateMonthlySummary(
                        user,
                        expenseMonth.getYear(),
                        expenseMonth.getMonthValue()
                );
            } else {
                summaryService.checkAndGenerateMonthlySummary(user);
            }

            return new ResponseEntity<>(new DeleteResponse("Expense deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public void downloadExpenseExcel(String email, HttpServletResponse response) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Unauthorized"));

        List<ExpenseEntity> expenses =
                expenseRepository.findByUserOrderByDateDesc(user);

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Expense");

            // Header Row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Category");
            headerRow.createCell(1).setCellValue("Amount");
            headerRow.createCell(2).setCellValue("Date");

            int rowIdx = 1;

            for (ExpenseEntity expense : expenses) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(expense.getCategory());
                row.createCell(1).setCellValue(expense.getAmount());
                row.createCell(2).setCellValue(
                        expense.getDate() != null
                                ? expense.getDate().toString()
                                : ""
                );
            }

            // Auto size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            // Response headers (same as Node.js)
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=expense_details.xlsx"
            );

            workbook.write(response.getOutputStream());

        } catch (IOException e) {
            throw new RuntimeException("Error generating Excel file", e);
        }
    }
}
