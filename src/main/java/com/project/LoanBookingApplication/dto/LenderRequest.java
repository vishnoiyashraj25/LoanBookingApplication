package com.project.LoanBookingApplication.dto;

import com.project.LoanBookingApplication.enums.LenderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LenderRequest {

    @NotBlank(message = "Lender name is required")
    @Size(min = 2, max = 255, message = "Lender name must be between 2 and 255 characters")
    private String lenderName;

    @NotNull(message = "Lender type is required")
    private LenderType lenderType;
}
