package com.Saurabh.ETA.Controller;

import com.Saurabh.ETA.Entity.SummaryEntity;
import com.Saurabh.ETA.Io.Summary.SummaryResponse;
import com.Saurabh.ETA.Service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/summary")
public class SummaryController {

    private final SummaryService summaryService;

    @GetMapping("/get")
    public ResponseEntity<List<SummaryResponse>> getLast12MonthsSummary(
            @CurrentSecurityContext(expression = "authentication.name") String email) {

        return ResponseEntity.ok(summaryService.getLast12MonthsSummary(email));
    }
}
