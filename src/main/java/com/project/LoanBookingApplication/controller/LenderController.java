package com.project.LoanBookingApplication.controller;

import com.project.LoanBookingApplication.dto.LenderRequest;
import com.project.LoanBookingApplication.dto.LenderResponse;
import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.service.LenderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/lenders")
public class LenderController {

    private final LenderService lenderService;
    public LenderController(LenderService lenderService){
        this.lenderService = lenderService;
    }

    @PostMapping
    public LenderResponse register(@Valid @RequestBody LenderRequest request) {
        return lenderService.registerLender(request);
    }

    @GetMapping
    public List<LenderResponse> getAllLenders(

            @RequestParam(required = false)
            @Positive(message = "lenderId must be positive")
            Long lenderId,

            @RequestParam(required = false)
            @Size(min = 2, message = "lenderName must be at least 2 characters")
            String lenderName,

            @RequestParam(required = false)
            LenderType lenderType
    ) throws Exception {

        return lenderService.getAllLendersJson(lenderId, lenderName, lenderType);

    }


}
