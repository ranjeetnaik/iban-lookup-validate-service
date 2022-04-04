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
public class IbanPlusCountry {

    private String countryCode;

    private int bankCodePosition;

    private int bankCodeLength;

    private int branchCodePosition;

    private int branchCodeLength;

    private String reusedBy;

    private int ibanIdLength;

    private int ibanNationalIdLength;

    private int bankIdentifierPosition;

    private String sepa;

}