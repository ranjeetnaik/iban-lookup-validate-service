package com.ibanlookup.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IbanDataMap {
    private String fileKey;
    private List<IbanPlusCountry> ibanPlusCountryList;
    private List<IbanPlusBank> ibanPlusBanksList;
    private List<IbanPlusExclusions> exclusionsList;
}