package com.project.LoanBookingApplication.DTO;

import com.project.LoanBookingApplication.Entity.LenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LenderRequest {

    @NotBlank(message = "Lender name is required")
    private String lenderName;

    @NotNull(message = "Lender type is required")
    private LenderType lenderType;
}
