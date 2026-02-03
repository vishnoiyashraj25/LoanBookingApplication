package com.project.LoanBookingApplication.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.LoanBookingApplication.DTO.LenderRequest;
import com.project.LoanBookingApplication.Entity.Lender;
import com.project.LoanBookingApplication.Entity.LenderType;
import com.project.LoanBookingApplication.Exception.ResourceNotFoundException;
import com.project.LoanBookingApplication.Repository.LenderRepository;

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
    public Lender registerLender(LenderRequest request){

        Lender lender = new Lender();

        lender.setLenderName(request.getLenderName());
        lender.setLenderType(request.getLenderType());

        return lenderRepository.save(lender);
    }

    @Cacheable(value = "lenders",
            key = "#lenderId + '-' + #lenderName + '-' + #lenderType")
    public String getAllLendersJson(
            Long lenderId,
            String lenderName,
            LenderType lenderType
    ) throws Exception {

        // 🔥 YOUR EXACT BUSINESS LOGIC (unchanged)

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

        return mapper.writeValueAsString(lenders);
    }

    public List<Lender> parse(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<List<Lender>>() {});
    }
}
