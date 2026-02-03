package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.Entity.EmiSchedule;
import com.project.LoanBookingApplication.Service.EmiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emi")
public class EMIController {

    private final EmiService emiService;
    public EMIController(EmiService emiService){
        this.emiService = emiService;
    }

    @GetMapping
    public List<EmiSchedule> getEMI(@RequestParam(required = false) Long id, @RequestParam(required = false) String loan_number){
        return emiService.getEMI(id,loan_number);
    }
}
