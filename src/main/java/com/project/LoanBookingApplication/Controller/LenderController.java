package com.project.LoanBookingApplication.Controller;

import com.project.LoanBookingApplication.DTO.LenderRequest;
import com.project.LoanBookingApplication.Entity.Lender;
import com.project.LoanBookingApplication.Service.LenderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lenders")
public class LenderController {

    private final LenderService lenderService;
    public LenderController(LenderService lenderService){
        this.lenderService = lenderService;
    }

    @PostMapping
    public Lender register(@RequestBody LenderRequest request) {
        return lenderService.registerLender(request);
    }

    @GetMapping("/{lenderid}")
    public Lender getLender(@PathVariable Long lenderid) {
        return lenderService.getLender(lenderid);
    }

    @GetMapping
    public List<Lender> getAllLenders(){
        return lenderService.getAllLenders();
    }

}
