package com.loyayz.simple.oss.impl;

import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssFile;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class TencentOssClient implements SimpleOssClient {
    private final SimpleOssRule ossRule;
    private final COSClient ossClient;

    public TencentOssClient(SimpleOssRule ossRule, SimpleOssProperties ossProperties) {
        this(ossRule, defaultClient(ossProperties));
    }

    public TencentOssClient(SimpleOssRule ossRule, COSClient cosClient) {
        this.ossRule = ossRule;
        this.ossClient = cosClient;
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

        ClientConfig config = ossClient.getClientConfig();
        String endpoint = config.getEndpointBuilder().buildGeneralApiEndpoint(bucketName);
        return endpoint + "/" + objectKey;
    }

    @Override
    public SimpleOssFile uploadFile(String bucketName, String objectKey, File file) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        String contentType = this.ossRule.getContentType(objectKey);
        ObjectMetadata metadata = new ObjectMetadata();
        if (contentType != null) {
            metadata.setContentType(contentType);
        }
        PutObjectRequest request = new PutObjectRequest(bucketName, objectKey, file).withMetadata(metadata);
        ossClient.putObject(request);
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
        request.withKeys(objectKeys.toArray(new String[]{}));
        ossClient.deleteObjects(request);
    }

    @Override
    public void copyFile(String sourceBucketName, String sourceObjectKey,
                         String targetBucketName, String targetObjectKey) {
        this.ossRule.validBucketObjectKey(sourceBucketName, sourceObjectKey);
        this.ossRule.validBucketObjectKey(targetBucketName, targetObjectKey);

        ossClient.copyObject(sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey);
    }

    public static COSClient defaultClient(SimpleOssProperties ossProperties) {
        COSCredentials credentials = new BasicCOSCredentials(ossProperties.getAccessKey(), ossProperties.getAccessSecret());
        // COS 地域 https://cloud.tencent.com/document/product/436/6224
        Region region = new Region(ossProperties.getRegion());
        return new COSClient(credentials, new ClientConfig(region));
    }

}
