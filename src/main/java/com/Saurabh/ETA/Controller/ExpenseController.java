package com.Saurabh.ETA.Controller;

import com.Saurabh.ETA.Io.Expense.AddExpenseRequest;
import com.Saurabh.ETA.Io.Expense.AddExpenseResponse;
import com.Saurabh.ETA.Io.Expense.DeleteResponse;
import com.Saurabh.ETA.Service.ExpenseService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expense")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/add")
    public ResponseEntity<AddExpenseResponse> addExpense(@Valid @RequestBody AddExpenseRequest request, @CurrentSecurityContext(expression = "authentication.name") String email){
        return expenseService.addExpense(request, email);
    }

    @GetMapping("/get")
    public ResponseEntity<List<AddExpenseResponse>> getAllExpense(@CurrentSecurityContext(expression = "authentication.name") String email){
        return expenseService.getAllExpense(email);
    }

    @GetMapping("/downloadexcel")
    public void downloadExpenseExcel(
            @CurrentSecurityContext(expression = "authentication.name") String email,
            HttpServletResponse response) {

        expenseService.downloadExpenseExcel(email, response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteExpense(@CurrentSecurityContext(expression = "authentication.name") String email, @PathVariable Long id){
        return expenseService.deleteExpense(email, id);
    }
}
