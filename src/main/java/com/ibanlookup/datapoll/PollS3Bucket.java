package com.ibanlookup.datapoll;

import com.amazonaws.AmazonClientException;
import com.paf.pps.filehandler.IbanPlusHandler;
import com.paf.pps.model.IbanDataMap;
import com.paf.pps.model.S3CloseableObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;

/**
 * Polling the S3 bucket every minute and during startup
 * Fill the map with the list of country, banks and exclusions.
 */
@Getter
@Setter
@Slf4j
@Service
@RequiredArgsConstructor
public class PollS3Bucket {

    private final IbanPlusHandler ibanPlusHandler;
    private final com.paf.pps.filepolling.IbanPlusProviderService ibanPlusProviderService;
    private IbanDataMap ibanDataMap = null;

    @PostConstruct
    public void pollLatestIBANZipOnStartup() {
        try (var s3CloseableObject = ibanPlusProviderService.getLatestFile()) {
            if (s3CloseableObject != null) {
                ibanDataMap = readAndProcessZipFile(s3CloseableObject);
            } else {
                log.warn("File is not present in configured s3 bucket");
            }
        } catch (AmazonClientException | IOException exception) {
            log.error("pollLatestIBANZipOnStartup exception while processing IbanPlus zip", exception);
        }

    }

    /**
     * Poll S3 bucket which has IBAN zip file every minute and check for last modified
     * if highest last modified time file is different than one present in ibanDataMap then reload new file.
     */
    @Scheduled(cron = "1 * * * * *")
    public void pollS3BucketEveryMinute() {
        try (var s3CloseableObject = ibanPlusProviderService.getLatestFile()) {
            if (ibanDataMap == null)
                ibanDataMap = IbanDataMap.builder().build();
            log.info("Every minute call pollS3BucketEveryMinute, latest file name {} and file stored in memory(ibanDataMap) {}", s3CloseableObject != null ? s3CloseableObject.getKey() : null, ibanDataMap.getFileKey());
            if (s3CloseableObject != null && !s3CloseableObject.getKey().equals(ibanDataMap.getFileKey())) {
                ibanDataMap = readAndProcessZipFile(s3CloseableObject);
            }
        } catch (AmazonClientException | IOException exception) {
            log.error("Exception while processing pollS3BucketEveryMinute", exception);
        }
    }

    /**
     * IbanPlusHandler will read the zip and create dataset
     *
     * @param s3CloseableObject File to be read and processed
     */
    private IbanDataMap readAndProcessZipFile(S3CloseableObject s3CloseableObject) throws IOException {
        log.info("Entry readAndProcessZipFile,  before file {} reading", s3CloseableObject.getKey());
        ibanDataMap = ibanPlusHandler.readAndProcessIbanPlusZipFile(s3CloseableObject);
        log.info("Successfully read and processed file {}", s3CloseableObject.getKey());
        s3CloseableObject.close();
        return ibanDataMap;
    }
}