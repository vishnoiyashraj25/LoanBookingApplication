package com.project.LoanBookingApplication.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.LoanBookingApplication.dto.LenderRequest;
import com.project.LoanBookingApplication.dto.LenderResponse;
import com.project.LoanBookingApplication.entity.Lender;
import com.project.LoanBookingApplication.enums.LenderType;
import com.project.LoanBookingApplication.exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.repository.LenderRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LenderService {

    private final LenderRepository lenderRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public LenderService(LenderRepository lenderRepository){
        this.lenderRepository = lenderRepository;
    }

    @CacheEvict(value = "lenders", allEntries = true)
    public LenderResponse registerLender(LenderRequest request){

        Lender lender = new Lender();
        lender.setLenderName(request.getLenderName());
        lender.setLenderType(request.getLenderType());

        Lender saveLender = lenderRepository.save(lender);
        return mapToResponse(saveLender);
    }

    @Cacheable(value = "lenders",
            key = "#lenderId + '-' + #lenderName + '-' + #lenderType")
    public List<LenderResponse> getAllLendersJson(
            Long lenderId,
            String lenderName,
            LenderType lenderType
    ) throws Exception {

        List<Lender> lenders = lenderRepository.findAll();

        if (lenderId != null) {
            lenders = lenders.stream()
                    .filter(l -> l.getLenderId().equals(lenderId))
                    .toList();
        }

        if (lenderName != null) {
            lenders = lenders.stream()
                    .filter(l -> l.getLenderName().equalsIgnoreCase(lenderName))
                    .toList();
        }

        if (lenderType != null) {
            lenders = lenders.stream()
                    .filter(l -> l.getLenderType() == lenderType)
                    .toList();
        }

        if (lenders.isEmpty()) {
            throw new ResourceNotFoundException("No Lender found");
        }
        List<LenderResponse> lenderRespons = lenders.stream().map(this::mapToResponse).toList();
        String json = mapper.writeValueAsString(lenderRespons);
        return parse(json);
    }

    public List<LenderResponse> parse(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<List<LenderResponse>>() {});
    }

    private LenderResponse mapToResponse(Lender lender) {
        LenderResponse dto = new LenderResponse();
        dto.setLenderId(lender.getLenderId());
        dto.setLenderType(lender.getLenderType());
        dto.setLenderName(lender.getLenderName());
        return dto;
    }
}
