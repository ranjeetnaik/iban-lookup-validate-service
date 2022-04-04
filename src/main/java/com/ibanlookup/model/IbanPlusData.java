package com.ibanlookup.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Iban+ data")
public class IbanPlusData implements Serializable {
    @Schema(description = "Record key", example = "IB000000L1PU", required = true)
    private String recordKey;

    @Schema(description = "Institution Name", example = "BARCLAYS BANK UK PLC", required = true)
    private String institutionName;

    @Schema(description = "Country code", example = "GB", required = true)
    private String countryCode;

    @Schema(description = "bic", example = "BUKBGB22XXX", required = true)
    private String bic;

    @Schema(description = "National id", example = "BUKBGB22XXX", required = true)
    private String nationalId;

    @Schema(description = "sepa ", example = "true", required = true)
    private boolean sepa;
}
