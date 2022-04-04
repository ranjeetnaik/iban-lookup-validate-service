package com.ibanlookup.service;

import com.paf.pps.api.IbanResponse;
import com.paf.pps.exception.IBANLookupException;
import com.paf.pps.filepolling.PollS3Bucket;
import com.paf.pps.service.impl.IbanLookupServiceImpl;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IbanLookupServiceTest {
    public static final String FI_IBAN = "FI2812345600000785";

    private IbanLookupServiceImpl ibanLookupService;
    @Mock
    private PollS3Bucket pollS3Bucket;

    @Before
    public void setup() throws IOException {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        pollS3Bucket.setIbanDataMap(TestUtil.getIbanDataMap());

        when(pollS3Bucket.getIbanDataMap()).thenReturn(TestUtil.getIbanDataMap());
    }

    @Test
    public void testShouldSucceedWithValidIBANForFinland() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        IbanResponse ibanResponse = ibanLookupService.validateAndFetchIbanInformation("FI2112345600000785");
        System.out.println(ibanResponse.toString());
        assertThat(ibanResponse.getIbanPlusData().getCountryCode(), Matchers.equalTo("FI"));
        assertThat(ibanResponse.getIbanPlusData().getNationalId(), Matchers.equalTo("123"));
        assertThat(ibanResponse.isIbanValid(), Matchers.is(true));
    }

    @Test
    public void testCountryCodeNotFound() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        IbanResponse ibanResponse = ibanLookupService.validateAndFetchIbanInformation("fi2112345600000785");
        assertThat(ibanResponse.getReason(), Matchers.containsString("Iban plus country data not found for requested iban"));
    }

    @Test
    public void testIbanPatternFail() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        IbanResponse ibanResponse = ibanLookupService.validateAndFetchIbanInformation("122112345600000785");
        assertEquals("Iban pattern matching failed", ibanResponse.getReason());
        assertFalse(ibanResponse.isIbanValid());
    }

    @Test
    public void testShouldPassIbanValidationTrue() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        assertTrue(ibanLookupService.validateAndFetchIbanInformation("GB24BARC20201630093459").isIbanValid());
    }

    @Test
    public void testShouldFailForIbanExclusionList() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        assertEquals("Invalid iban, iban found in exclusion list", ibanLookupService.validateAndFetchIbanInformation("PT21065801231234567890192").getReason());
    }

    @Test
    public void testShouldFailWithCheckSumValidationFailed() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        assertEquals("Iban plus checksum validation failed for requested iban", ibanLookupService.validateAndFetchIbanInformation(FI_IBAN).getReason());
    }

    @Test
    public void testIbanChecksumForSingleDigit() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        IbanResponse ibanResponse = ibanLookupService.validateAndFetchIbanInformation("GB02BARC20201530093451");
        assertTrue(ibanResponse.isIbanValid());
    }

    @Test
    public void testShouldSucceedWithValidIBANForBelgium() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        IbanResponse ibanResponse = ibanLookupService.validateAndFetchIbanInformation("BE88271080782541");
        assertEquals("BE", ibanResponse.getIbanPlusData().getCountryCode());
        assertEquals("271", ibanResponse.getIbanPlusData().getNationalId());
    }

    @Test
    public void testShouldSucceedWithValidIBANForYapeal() {
        ibanLookupService = new IbanLookupServiceImpl(pollS3Bucket);
        IbanResponse ibanResponse = ibanLookupService.validateAndFetchIbanInformation("CH1883019POKERFACE000");
        assertNull(ibanResponse.getIbanPlusData());
        assertTrue(ibanResponse.isIbanValid());
    }

    @Test(expected = IBANLookupException.class)
    public void ibanDataMapNullTest() {
        when(pollS3Bucket.getIbanDataMap()).thenReturn(null);
        ibanLookupService.validateAndFetchIbanInformation(FI_IBAN);
    }

}