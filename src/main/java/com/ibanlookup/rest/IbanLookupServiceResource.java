package com.ibanlookup.rest;

import com.ibanlookup.model.IbanResponse;
import com.ibanlookup.service.IbanLookupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class IbanLookupServiceResource {
    private final IbanLookupService ibanLookupService;

    public IbanLookupServiceResource(IbanLookupService ibanLookupService) {
        this.ibanLookupService = ibanLookupService;
    }

    public ResponseEntity<IbanResponse> validateAndFetchIbanInformation(String iban) {
        long startTime = System.currentTimeMillis();
        log.info("Validate and fetch iban information.");
        var ibanResponse = ibanLookupService.validateAndFetchIbanInformation(iban);
        log.info("Iban lookup service response time {}ms", System.currentTimeMillis() - startTime);
        return ResponseEntity.ok(ibanResponse);
    }
}