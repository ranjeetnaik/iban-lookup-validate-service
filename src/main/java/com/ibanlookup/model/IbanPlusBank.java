package com.ibanlookup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class IbanPlusBank {
    private String bankName;
    private String bankCode;
    private String countryCode;
    private String ibanBIC;
    private String recordKey;
}