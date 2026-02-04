package com.project.LoanBookingApplication.DTO;


import com.project.LoanBookingApplication.Entity.LoanType;
import com.project.LoanBookingApplication.Entity.RequestStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.common.protocol.types.Field;

@Getter
@Setter
public class LoanRequestResponse {

    private Long id;
    private String userName;
    private String panNumber;
    private Double amount;
    private Integer tenure;
    private LoanType loanType;
    private RequestStatus requestStatus;
}
