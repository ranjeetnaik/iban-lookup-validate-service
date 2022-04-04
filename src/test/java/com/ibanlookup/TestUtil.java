package com.ibanlookup;

import com.amazonaws.services.s3.model.S3Object;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paf.pps.filehandler.IbanPlusHandlerUTC;
import com.paf.pps.model.IbanDataMap;
import com.paf.pps.model.S3CloseableObject;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;

@UtilityClass
public class TestUtil {
    private static final String FILENAME = "IbanPlus_20201030131819536-IBANPLUS_20201030_TXT.zip";
    public static IbanDataMap ibanDataMap = new IbanDataMap();

    public static byte[] toJson(Object object) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return objectMapper.writeValueAsBytes(object);
    }

    public static IbanDataMap getIbanDataMap() throws IOException {
        if (ibanDataMap.getFileKey() == null) {
            try (InputStream inputStream = IbanPlusHandlerUTC.class.getResourceAsStream("/" + FILENAME)) {
                S3Object s3Object = new S3Object();
                s3Object.setKey(FILENAME);
                s3Object.setObjectContent(inputStream);
                S3CloseableObject s3CloseableObject = new S3CloseableObject(s3Object);
                IbanPlusHandler ibanPlusHandler = new IbanPlusHandler();
                ibanDataMap = ibanPlusHandler.readAndProcessIbanPlusZipFile(s3CloseableObject);
            }
        }
        return ibanDataMap;
    }
}