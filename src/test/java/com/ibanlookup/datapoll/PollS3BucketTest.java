package com.ibanlookup.datapoll;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.extern.slf4j.Slf4j;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.InputStream;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class PollS3BucketTest {
    private static final String KEY_1 = "IbanPlus_20201030131819536-IBANPLUS_20201030_TXT.zip";
    private static final String KEY_2 = "IbanPlus_20201030131819536-IBANPLUS_20201029_TXT.zip";
    private static final String FILE_KEY_1 = "IbanPlus_20201030131819536-IBANPLUS_20201030_TXT.zip";

    @Mock
    private IbanPlusHandler ibanPlusHandler;
    @Mock
    private IbanPlusProviderService ibanPlusProviderService;

    private PollS3Bucket pollS3Bucket;
    @Mock
    private S3Object s3Object;

    @Mock
    private S3ObjectInputStream s3ObjectInputStream;

    @Before
    public void setup() {
        pollS3Bucket = new PollS3Bucket(ibanPlusHandler, ibanPlusProviderService);

        // mock the input stream content
        InputStream inputStream = PollS3BucketTest.class.getResourceAsStream(FILE_KEY_1);
        PollS3BucketTest.class.getResourceAsStream(FILE_KEY_1);
        s3Object.setKey(KEY_1);
        s3Object.setObjectContent(inputStream);
        S3CloseableObject s3CloseableObject = new S3CloseableObject(s3Object);
        when(s3Object.getObjectContent()).thenReturn(s3ObjectInputStream);
        when(s3Object.getKey()).thenReturn(KEY_1);
        when(ibanPlusProviderService.getLatestFile()).thenReturn(s3CloseableObject);
    }

    @Test
    public void testIbanDataMapListAndKeyPollLatestIBANZipOnStartup() {
        IbanDataMap ibanDataMap = buildIbanDataMap(KEY_1, 20, 20, 20);
        when(ibanPlusHandler.readAndProcessIbanPlusZipFile(any())).thenReturn(ibanDataMap);
        pollS3Bucket.pollLatestIBANZipOnStartup();
        assertThat(pollS3Bucket.getIbanDataMap().getFileKey(), Matchers.equalTo(KEY_1));
        assertThat(pollS3Bucket.getIbanDataMap().getIbanPlusBanksList().size(), Matchers.is(20));
        assertThat(pollS3Bucket.getIbanDataMap().getIbanPlusCountryList().size(), Matchers.is(20));
        assertThat(pollS3Bucket.getIbanDataMap().getExclusionsList().size(), Matchers.is(20));
    }

    @Test
    public void pollLatestIBANZipOnStartupNoLatestFileFail() {
        when(ibanPlusProviderService.getLatestFile()).thenReturn(null);

        try {
            pollS3Bucket.pollLatestIBANZipOnStartup();
            assertNull(pollS3Bucket.getIbanDataMap());
        } catch (Exception e) {
            fail("Should not have received an exception: " + e);
        }
    }

    @Test
    public void testIbanDataMapListAndKeyPollS3BucketEveryMinute() {
        IbanDataMap ibanDataMap = buildIbanDataMap(KEY_1, 20, 20, 20);
        when(ibanPlusHandler.readAndProcessIbanPlusZipFile(any())).thenReturn(ibanDataMap);
        pollS3Bucket.pollS3BucketEveryMinute();
        assertThat(pollS3Bucket.getIbanDataMap(), Matchers.notNullValue());
        assertThat(pollS3Bucket.getIbanDataMap().getIbanPlusBanksList().size(), Matchers.is(20));
        assertThat(pollS3Bucket.getIbanDataMap().getIbanPlusCountryList().size(), Matchers.is(20));
        assertThat(pollS3Bucket.getIbanDataMap().getExclusionsList().size(), Matchers.is(20));
    }

    @Test
    public void pollS3BucketEveryMinuteNoLatestFileFail() {
        when(ibanPlusProviderService.getLatestFile()).thenReturn(null);

        try {
            pollS3Bucket.pollS3BucketEveryMinute();
            assertNotNull(pollS3Bucket.getIbanDataMap());
            assertNull(pollS3Bucket.getIbanDataMap().getFileKey());
            assertNull(pollS3Bucket.getIbanDataMap().getIbanPlusCountryList());
            assertNull(pollS3Bucket.getIbanDataMap().getIbanPlusBanksList());
            assertNull(pollS3Bucket.getIbanDataMap().getExclusionsList());
        } catch (Exception e) {
            fail("Should not have received an exception: " + e);
        }
    }

    @Test
    public void pollS3BucketEveryMinuteIbanDataMapNullFail() {
        S3CloseableObject s3CloseableObject = new S3CloseableObject(s3Object);
        when(ibanPlusProviderService.getLatestFile()).thenReturn(s3CloseableObject);
        pollS3Bucket.setIbanDataMap(null);

        try {
            pollS3Bucket.pollS3BucketEveryMinute();
            log.info("ibanDataMap: {}", pollS3Bucket.getIbanDataMap());
        } catch (Exception e) {
            fail("Should not have received an exception: " + e);
        }
    }

    private List<IbanPlusCountry> mockIbanPlusCountryList(int size) {
        List<IbanPlusCountry> ibanPlusCountryList = mock(List.class);
        when(ibanPlusCountryList.size()).thenReturn(size);
        return ibanPlusCountryList;
    }

    private List<IbanPlusBank> mockIbanPlusBankList(int size) {
        List<IbanPlusBank> ibanPlusBanksList = mock(List.class);
        when(ibanPlusBanksList.size()).thenReturn(size);
        return ibanPlusBanksList;
    }

    private List<IbanPlusExclusions> mockIbanPlusExclusionsList(int size) {
        List<IbanPlusExclusions> ibanPlusExclusionsList = mock(List.class);
        when(ibanPlusExclusionsList.size()).thenReturn(size);
        return ibanPlusExclusionsList;
    }

    private IbanDataMap buildIbanDataMap(String key, int i, int i2, int i3) {
        return IbanDataMap.builder().fileKey(key).ibanPlusCountryList(mockIbanPlusCountryList(i)).
                ibanPlusBanksList(mockIbanPlusBankList(i2)).
                exclusionsList(mockIbanPlusExclusionsList(i3)).build();
    }
}