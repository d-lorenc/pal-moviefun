package org.superbiz.moviefun.blobstore;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {

    private final AmazonS3Client s3Client;
    private final String s3BucketName;

    public S3Store(AmazonS3Client s3Client, String s3BucketName) {
        this.s3Client = s3Client;
        this.s3BucketName = s3BucketName;
    }

    @Override
    public void put(Blob blob) throws IOException {
        s3Client.putObject(s3BucketName, blob.name, blob.inputStream, new ObjectMetadata());
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        S3Object s3Object = s3Client.getObject(s3BucketName, name);
        return Optional.of(new Blob(name, s3Object.getObjectContent(), "blob"));
    }

    @Override
    public void deleteAll() {

    }
}
