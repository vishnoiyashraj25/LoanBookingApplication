package com.project.LoanBookingApplication.DTO;

//import com.project.LoanBookingApplication.Entity.LenderStatus;
import com.project.LoanBookingApplication.Entity.LenderType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LenderRequest {
    private String lenderName;
    private LenderType lenderType;
//    private LenderStatus lenderStatus;
}
