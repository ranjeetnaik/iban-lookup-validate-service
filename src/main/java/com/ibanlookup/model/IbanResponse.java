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
@Schema(description = "IBAN response")
public class IbanResponse implements Serializable {
    @Schema(description = "Iban validation true or false", example = "true", required = true)
    private boolean ibanValid;
    @Schema(description = "Iban plus data details", example = "\"iban_plus_data\":{\"record_key\":\"IB00000034MI\",\"institution_name\":" +
            "\"NORDEA BANK FINLAND PLC\",\"country_code\":\"FI\",\"bic\":\"NDEAFIHHXXX\",\"national_id\":\"123\",\"sepa\":true}")
    private IbanPlusData ibanPlusData;
    @Schema(description = "Iban validation fail reason", example = "Iban plus checksum validation failed for requested iban")
    private String reason;
}
