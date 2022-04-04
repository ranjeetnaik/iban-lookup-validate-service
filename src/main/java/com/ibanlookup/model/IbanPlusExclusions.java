package com.ibanlookup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
public class IbanPlusExclusions {
    String recordKey;
    String countryCode;
    String bic;
    String ibanNationalId;
    String validFrom;
}