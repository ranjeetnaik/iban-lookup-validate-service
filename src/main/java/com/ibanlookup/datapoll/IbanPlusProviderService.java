package com.ibanlookup.datapoll;

import com.amazonaws.AmazonClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class IbanPlusProviderService {

    private final AmazonS3 amazonS3;

    @Value("${s3.bucket-name:}")
    private String bucketName;

    /**
     * Returns the latest modified Object from configured S3 bucket
     *
     * @return S3Object latest S3ObjectFile
     */
    public S3CloseableObject getLatestFile() throws AmazonClientException {
        S3CloseableObject s3CloseableObject = null;
        var objectListing = amazonS3.listObjects(bucketName);
        List<S3ObjectSummary> s3ObjectSummaries = objectListing.getObjectSummaries();
        Optional<S3ObjectSummary> s3ObjectSummaryOptional = s3ObjectSummaries.stream().max(Comparator.comparing(S3ObjectSummary::getLastModified));
        if (s3ObjectSummaryOptional.isPresent()) {
            s3CloseableObject = new S3CloseableObject(amazonS3.getObject(bucketName, s3ObjectSummaryOptional.get().getKey()));
        } else {
            log.info("s3 bucket {} is empty", bucketName);
        }
        return s3CloseableObject;
    }

    /**
     * Upload iban zip file
     *
     * @param multipartFile file to upload in s3 bucket
     * @return file upload success(true)/failure(false)
     */
    public boolean uploadIbanZipFile(MultipartFile multipartFile) throws AmazonClientException, IOException {
        try (var inputStream = multipartFile.getInputStream()) {
            var objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(multipartFile.getBytes().length);
            objectMetadata.setLastModified(new Date());
            var putObjectRequest = new PutObjectRequest(bucketName, multipartFile.getOriginalFilename(), inputStream, objectMetadata);
            amazonS3.putObject(putObjectRequest);
        }
        return true;
    }
}
