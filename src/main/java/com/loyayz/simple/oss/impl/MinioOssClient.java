package com.loyayz.simple.oss.impl;

import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssFile;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import io.minio.*;
import io.minio.messages.DeleteObject;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class MinioOssClient implements SimpleOssClient {
    private final SimpleOssRule ossRule;
    private final SimpleOssProperties ossProperties;
    private final MinioClient ossClient;

    @Override
    @SneakyThrows
    public void createBucket(String bucketName) {
        this.ossRule.validBucketName(bucketName);

        if (ossClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            return;
        }
        ossClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

    @Override
    @SneakyThrows
    public void deleteBucket(String bucketName) {
        this.ossRule.validBucketName(bucketName);

        ossClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
    }

    @Override
    @SneakyThrows
    public SimpleOssFile fileMetadata(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        StatObjectResponse metadata =
                ossClient.statObject(StatObjectArgs.builder().bucket(bucketName).object(objectKey).build());
        SimpleOssFile result = new SimpleOssFile();
        result.setLink(this.fileLink(bucketName, objectKey));
        result.setName(objectKey);
        result.setLength(metadata.size());
        result.setContentType(metadata.contentType());
        result.setCreateTime(metadata.lastModified().toInstant().toEpochMilli());
        return result;
    }

    @Override
    public String fileLink(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        return String.format("%s/%s/%s",
                this.ossProperties.getEndpoint(), bucketName, objectKey);
    }

    @Override
    @SneakyThrows
    public SimpleOssFile uploadFile(String bucketName, String objectKey, File file) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        try (FileInputStream stream = new FileInputStream(file)) {
            return this.uploadFile(bucketName, objectKey, stream);
        }
    }

    @Override
    @SneakyThrows
    public SimpleOssFile uploadFile(String bucketName, String objectKey,
                                    InputStream stream, String contentType) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        PutObjectArgs.Builder builder = PutObjectArgs.builder()
                .bucket(bucketName)
                .object(objectKey)
                .stream(stream, stream.available(), -1);
        if (contentType == null) {
            contentType = this.ossRule.getContentType(objectKey);
        }
        if (contentType != null) {
            builder.contentType(contentType);
        }
        ossClient.putObject(builder.build());
        return this.fileMetadata(bucketName, objectKey);
    }

    @Override
    @SneakyThrows
    public void deleteFile(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        ossClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectKey).build());
    }

    @Override
    public void deleteFile(String bucketName, List<String> objectKeys) {
        this.ossRule.validBucketObjectKey(bucketName, objectKeys.toArray(new String[]{}));

        RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                .bucket(bucketName)
                .objects(objectKeys.stream().map(DeleteObject::new).collect(Collectors.toList()))
                .build();
        ossClient.removeObjects(args);
    }

    @Override
    @SneakyThrows
    public void copyFile(String sourceBucketName, String sourceObjectKey,
                         String targetBucketName, String targetObjectKey) {
        this.ossRule.validBucketObjectKey(sourceBucketName, sourceObjectKey);
        this.ossRule.validBucketObjectKey(targetBucketName, targetObjectKey);

        CopyObjectArgs args = CopyObjectArgs.builder()
                .source(CopySource.builder().bucket(sourceBucketName).object(sourceObjectKey).build())
                .bucket(targetBucketName)
                .object(targetObjectKey)
                .build();
        ossClient.copyObject(args);
    }

}
