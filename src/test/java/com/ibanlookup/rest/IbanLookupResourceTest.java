package com.ibanlookup.rest;

import com.ibanlookup.service.IbanLookupService;
import com.ibanlookup.exception.GlobalExceptionHandler;
import com.ibanlookup.datapoll.PollS3Bucket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class IbanLookupResourceTest {

    public static final String IBAN = "FI2112345600000785";
    public static final String IBAN_INVALID = "FI5612345600000785";

    private MockMvc mockMvc;
    @Mock
    private PollS3Bucket pollS3Bucket;

    @Before
    public void setup() throws IOException {
        IbanLookupService ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        IbanLookupServiceResource ibanLookupServiceResource = new IbanLookupServiceResource(ibanLookupService);
        mockMvc = MockMvcBuilders.standaloneSetup(ibanLookupServiceResource)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        pollS3Bucket.setIbanDataMap(TestUtil.getIbanDataMap());
        when(pollS3Bucket.getIbanDataMap()).thenReturn(TestUtil.getIbanDataMap());
    }

    @Test
    public void testForValidIBAN() throws Exception {
        mockMvc.perform(get("/v1/iban/lookup").param("iban", IBAN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban_valid", is(true)))
                .andExpect(jsonPath("$.iban_plus_data.country_code", is("FI")))
                .andExpect(jsonPath("$.iban_plus_data.bic", is("NDEAFIHHXXX")))
                .andExpect(jsonPath("$.iban_plus_data.record_key", is("IB00000034MI")))
                .andExpect(jsonPath("$.iban_plus_data.institution_name", is("NORDEA BANK FINLAND PLC")))
                .andExpect(jsonPath("$.iban_plus_data.national_id", is("123")))
                .andExpect(jsonPath("$.iban_plus_data.sepa", is(true)));
    }

    @Test
    public void testForInValidIBANShouldThrowChecksumFailException() throws Exception {
        mockMvc.perform(get("/v1/iban/lookup")
                        .param("iban", IBAN_INVALID))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.iban_valid", is(false)))
                .andExpect(jsonPath("$.reason", is("Iban plus checksum validation failed for requested iban")));
    }
}