package org.matthiaskarl.techassessment.creditservice.controller;

import lombok.RequiredArgsConstructor;
import org.matthiaskarl.techassessment.creditservice.dto.LoanDto;
import org.matthiaskarl.techassessment.creditservice.service.LoanService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/service/v1")
@RequiredArgsConstructor
public class LoansController {

    private final LoanService loanService;

    @GetMapping("/loansByUser/{userId}")
    public ResponseEntity<List<LoanDto>> loansByUser(@PathVariable String userId) {
        long parsedUserId = Long.parseLong(userId);
        List<LoanDto> loans = loanService.getLoansByUserId(parsedUserId);
        return ResponseEntity.ok(loans);
    }

}
