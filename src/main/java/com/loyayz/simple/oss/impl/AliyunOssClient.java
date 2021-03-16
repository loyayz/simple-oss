package com.loyayz.simple.oss.impl;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.DeleteObjectsRequest;
import com.aliyun.oss.model.ObjectMetadata;
import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssFile;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class AliyunOssClient implements SimpleOssClient {
    private final SimpleOssRule ossRule;
    private final OSSClient ossClient;

    public AliyunOssClient(SimpleOssRule ossRule, SimpleOssProperties ossProperties) {
        this(ossRule, defaultClient(ossProperties));
    }

    public AliyunOssClient(SimpleOssRule ossRule, OSSClient ossClient) {
        this.ossRule = ossRule;
        this.ossClient = ossClient;
    }

    @Override
    public void createBucket(String bucketName) {
        this.ossRule.validBucketName(bucketName);

        if (ossClient.doesBucketExist(bucketName)) {
            return;
        }
        ossClient.createBucket(bucketName);
    }

    @Override
    public void deleteBucket(String bucketName) {
        this.ossRule.validBucketName(bucketName);

        ossClient.deleteBucket(bucketName);
    }

    @Override
    public SimpleOssFile fileMetadata(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        ObjectMetadata metadata = ossClient.getObjectMetadata(bucketName, objectKey);
        SimpleOssFile result = new SimpleOssFile();
        result.setLink(this.fileLink(bucketName, objectKey));
        result.setName(objectKey);
        result.setLength(metadata.getContentLength());
        result.setContentType(metadata.getContentType());
        result.setCreateTime(metadata.getLastModified().getTime());
        return result;
    }

    @Override
    public String fileLink(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        URI endpoint = ossClient.getEndpoint();
        return String.format("%s://%s.%s/%s",
                endpoint.getScheme(), bucketName, endpoint.getHost(), objectKey);
    }

    @Override
    public SimpleOssFile uploadFile(String bucketName, String objectKey, File file) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        String contentType = this.ossRule.getContentType(objectKey);
        ObjectMetadata metadata = null;
        if (contentType != null) {
            metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
        }
        ossClient.putObject(bucketName, objectKey, file, metadata);
        return this.fileMetadata(bucketName, objectKey);
    }

    @Override
    public SimpleOssFile uploadFile(String bucketName, String objectKey,
                                    InputStream stream, String contentType) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        if (contentType == null) {
            contentType = this.ossRule.getContentType(objectKey);
        }
        ObjectMetadata metadata = null;
        if (contentType != null) {
            metadata = new ObjectMetadata();
            metadata.setContentType(contentType);
        }
        ossClient.putObject(bucketName, objectKey, stream, metadata);
        return this.fileMetadata(bucketName, objectKey);
    }

    @Override
    public void deleteFile(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        ossClient.deleteObject(bucketName, objectKey);
    }

    @Override
    public void deleteFile(String bucketName, List<String> objectKeys) {
        this.ossRule.validBucketObjectKey(bucketName, objectKeys.toArray(new String[]{}));

        DeleteObjectsRequest request = new DeleteObjectsRequest(bucketName);
        request.setKeys(objectKeys);
        ossClient.deleteObjects(request);
    }

    @Override
    public void copyFile(String sourceBucketName, String sourceObjectKey,
                         String targetBucketName, String targetObjectKey) {
        this.ossRule.validBucketObjectKey(sourceBucketName, sourceObjectKey);
        this.ossRule.validBucketObjectKey(targetBucketName, targetObjectKey);

        ossClient.copyObject(sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey);
    }

    public static OSSClient defaultClient(SimpleOssProperties ossProperties) {
        String endpoint = ossProperties.getEndpoint();
        String accessKey = ossProperties.getAccessKey();
        String accessSecret = ossProperties.getAccessSecret();
        CredentialsProvider credentialProvider = new DefaultCredentialProvider(accessKey, accessSecret);
        ClientConfiguration config = new ClientConfiguration();
        return new OSSClient(endpoint, credentialProvider, config);
    }

}
