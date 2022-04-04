package com.ibanlookup.datapoll;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IbanPlusProviderServiceTest {

    public static final String TESTBUCKET = "testbucket";
    public static final String BUCKET_NAME = "bucketName";
    private static final String FILE_ZIP = "IbanPlus_20201030131819536-IBANPLUS_20201030_TXT.zip";
    private static final String FILE = "IbanPlus_20201030131819536-IBANPLUS_20201030_TXT.zip";

    private IbanPlusProviderService ibanPlusProviderService;
    @Mock
    private AmazonS3 amazonS3;
    @Mock
    private ObjectListing objectListing;
    @Mock
    private List<S3ObjectSummary> s3ObjectSummaryList;
    @Mock
    private S3Object s3Object;
    @Mock
    private PutObjectResult putObjectResult;

    @Before
    public void setup() {
        ibanPlusProviderService = new IbanPlusProviderService(amazonS3);
    }

    @Test
    public void testLatestS3ObjectNullCase() {
        ReflectionTestUtils.setField(ibanPlusProviderService, "bucketName", "testbucket");
        when(amazonS3.listObjects(ArgumentMatchers.anyString())).thenReturn(objectListing);
        when(objectListing.getObjectSummaries()).thenReturn(s3ObjectSummaryList);
        S3CloseableObject s3ObjectReturn = ibanPlusProviderService.getLatestFile();
        assertThat(s3ObjectReturn, Matchers.nullValue());
    }

    @Test
    public void testLatestS3ObjectRetrieved() {
        ReflectionTestUtils.setField(ibanPlusProviderService, "bucketName", TESTBUCKET);
        S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
        List<S3ObjectSummary> listS3ObjectSummary = new ArrayList<>();
        s3ObjectSummary.setBucketName(TESTBUCKET);
        s3ObjectSummary.setKey("key");
        listS3ObjectSummary.add(s3ObjectSummary);
        when(amazonS3.listObjects(ArgumentMatchers.anyString())).thenReturn(objectListing);
        when(objectListing.getObjectSummaries()).thenReturn(listS3ObjectSummary);
        when(amazonS3.getObject(TESTBUCKET, "key")).thenReturn(s3Object);
        S3CloseableObject referenceCloseable = new S3CloseableObject(s3Object);

        S3CloseableObject s3ObjectReturn = ibanPlusProviderService.getLatestFile();
        assertThat(s3ObjectReturn, Matchers.equalTo(referenceCloseable));
    }

    @Test
    public void testUploadIbanZipFile() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(FILE, IbanPlusProviderServiceTest.class.getResourceAsStream(FILE_ZIP));
        ReflectionTestUtils.setField(ibanPlusProviderService, BUCKET_NAME, TESTBUCKET);
        when(amazonS3.putObject(any())).thenReturn(putObjectResult);
        assertThat(ibanPlusProviderService.uploadIbanZipFile(multipartFile), Matchers.equalTo(true));
    }
}