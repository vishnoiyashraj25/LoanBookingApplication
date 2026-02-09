package com.project.LoanBookingApplication.dto;


import com.project.LoanBookingApplication.enums.LenderType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LenderResponse {

    private Long lenderId;
    private String lenderName;
    private LenderType lenderType;
}
