package com.Saurabh.ETA.Service;

import com.Saurabh.ETA.Entity.IncomeEntity;
import com.Saurabh.ETA.Entity.UserEntity;
import com.Saurabh.ETA.Io.Expense.DeleteResponse;
import com.Saurabh.ETA.Io.Income.AddIncomeRequest;
import com.Saurabh.ETA.Io.Income.AddIncomeResponse;
import com.Saurabh.ETA.Repository.IncomeRepository;
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
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UsersRepository usersRepository;
    private final SummaryService summaryService;

    @Transactional
    public ResponseEntity<AddIncomeResponse> addIncome(AddIncomeRequest request, String email) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Unauthorized"));
        try{
            IncomeEntity income = IncomeEntity.builder()
                    .user(user)
                    .icon(request.getIcon())
                    .source(request.getSource())
                    .amount(request.getAmount())
                    .date(request.getDate())
                    .build();
            incomeRepository.save(income);

            LocalDate incomeDate = income.getDate();
            YearMonth incomeMonth = YearMonth.from(incomeDate);
            YearMonth currentMonth = YearMonth.now();

            if (incomeMonth.isBefore(currentMonth)) {
                summaryService.regenerateMonthlySummary(
                        user,
                        incomeMonth.getYear(),
                        incomeMonth.getMonthValue()
                );
            } else {
                summaryService.checkAndGenerateMonthlySummary(user);
            }
            AddIncomeResponse response = convertToAddIncomeResponse(user.getId(), income);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    private AddIncomeResponse convertToAddIncomeResponse(Long userId, IncomeEntity income) {
        return new AddIncomeResponse(
                userId,
                income.getIcon(),
                income.getSource(),
                income.getAmount(),
                income.getDate(),
                income.getId(),
                income.getCreatedAt(),
                income.getUpdatedAt()
        );
    }

    public ResponseEntity<List<AddIncomeResponse>> getAllIncome(String email) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Unauthorized"));
        List<AddIncomeResponse> response = new ArrayList<>();
        try{
            List<IncomeEntity> incomes = incomeRepository.findByUserOrderByDateDesc(user);
            for(IncomeEntity income : incomes){
                AddIncomeResponse res = convertToAddIncomeResponse(user.getId(), income);
                response.add(res);
            }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public ResponseEntity<DeleteResponse> deleteIncome(String email, Long id) {
        try{
            UserEntity user = usersRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Not Authorized"));
            IncomeEntity income = incomeRepository.findByUserAndId(user, id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income not found"));

            LocalDate incomeDate = income.getDate();
            YearMonth incomeMonth = YearMonth.from(incomeDate);
            YearMonth currentMonth = YearMonth.now();

            incomeRepository.delete(income);

            if (incomeMonth.isBefore(currentMonth)) {
                summaryService.regenerateMonthlySummary(
                        user,
                        incomeMonth.getYear(),
                        incomeMonth.getMonthValue()
                );
            } else {
                summaryService.checkAndGenerateMonthlySummary(user);
            }
            return new ResponseEntity<>(new DeleteResponse("Income deleted successfully"), HttpStatus.OK);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    public void downloadIncomeExcel(String email, HttpServletResponse response) {
        UserEntity user = usersRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Unauthorized"));

        List<IncomeEntity> incomes =
                incomeRepository.findByUserOrderByDateDesc(user);

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Income");

            // Header row
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("Source");
            headerRow.createCell(1).setCellValue("Amount");
            headerRow.createCell(2).setCellValue("Date");

            int rowIdx = 1;

            for (IncomeEntity income : incomes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(income.getSource());
                row.createCell(1).setCellValue(income.getAmount());
                row.createCell(2).setCellValue(
                        income.getDate() != null
                                ? income.getDate().toString()
                                : ""
                );
            }

            // Auto-size columns
            for (int i = 0; i < 3; i++) {
                sheet.autoSizeColumn(i);
            }

            // Response headers
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename=income_details.xlsx"
            );

            workbook.write(response.getOutputStream());

        } catch (IOException e) {
            throw new RuntimeException("Error generating Income Excel file", e);
        }
    }
}
