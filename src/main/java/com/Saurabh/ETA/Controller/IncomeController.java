package com.Saurabh.ETA.Controller;

import com.Saurabh.ETA.Io.Expense.DeleteResponse;
import com.Saurabh.ETA.Io.Income.AddIncomeRequest;
import com.Saurabh.ETA.Io.Income.AddIncomeResponse;
import com.Saurabh.ETA.Service.IncomeService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/income")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping("/add")
    public ResponseEntity<AddIncomeResponse> addIncome(@Valid @RequestBody AddIncomeRequest request, @CurrentSecurityContext(expression = "authentication.name") String email){
        return incomeService.addIncome(request, email);
    }

    @GetMapping("/get")
    public ResponseEntity<List<AddIncomeResponse>> getAllIncome(@CurrentSecurityContext(expression = "authentication.name") String email){
        return incomeService.getAllIncome(email);
    }

    @GetMapping("/downloadexcel")
    public void downloadIncomeExcel(
            @CurrentSecurityContext(expression = "authentication.name") String email,
            HttpServletResponse response) {

        incomeService.downloadIncomeExcel(email, response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<DeleteResponse> deleteIncome(@CurrentSecurityContext(expression = "authentication.name") String email, @PathVariable Long id){
        return incomeService.deleteIncome(email, id);
    }
}
