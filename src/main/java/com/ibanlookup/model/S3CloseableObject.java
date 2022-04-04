package com.ibanlookup.model;

import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Closeable;
import java.io.IOException;

@Data
@AllArgsConstructor
public class S3CloseableObject implements Closeable {
    private final S3Object s3Object;

    @Override
    public void close() throws IOException {
        s3Object.getObjectContent().abort();
        s3Object.close();
    }

    public String getKey() {
        return s3Object.getKey();
    }

    public S3ObjectInputStream getObjectContent() {
        return s3Object.getObjectContent();
    }
}