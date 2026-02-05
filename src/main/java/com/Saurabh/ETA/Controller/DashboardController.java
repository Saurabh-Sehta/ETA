package com.Saurabh.ETA.Controller;

import com.Saurabh.ETA.Io.Dashboard.DashboardResponse;
import com.Saurabh.ETA.Service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({ "", "/"})
    public ResponseEntity<DashboardResponse> getDashboardData(@CurrentSecurityContext(expression = "authentication.name") String email){
        return dashboardService.getData(email);
    }

}
