package com.loyayz.simple.oss.impl;

import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssFile;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.FileInfo;
import com.qiniu.util.Auth;
import lombok.SneakyThrows;

import java.io.File;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class QiniuOssClient implements SimpleOssClient {
    private final SimpleOssRule ossRule;
    private final SimpleOssProperties ossProperties;
    private final Auth auth;
    private final BucketManager bucketManager;
    private final UploadManager uploadManager;

    public QiniuOssClient(SimpleOssRule ossRule, SimpleOssProperties ossProperties) {
        this(ossRule, ossProperties, defaultConfiguration(ossProperties));
    }

    public QiniuOssClient(SimpleOssRule ossRule, SimpleOssProperties ossProperties, Configuration config) {
        this.ossRule = ossRule;
        this.ossProperties = ossProperties;
        this.auth = Auth.create(ossProperties.getAccessKey(), ossProperties.getAccessSecret());
        this.bucketManager = new BucketManager(auth, config);
        this.uploadManager = new UploadManager(config);
    }

    @Override
    @SneakyThrows
    public void createBucket(String bucketName) {
        this.ossRule.validBucketName(bucketName);

        String[] buckets = this.bucketManager.buckets();
        boolean exist = buckets != null && Arrays.asList(buckets).contains(bucketName);
        if (exist) {
            return;
        }
        this.bucketManager.createBucket(bucketName, ossProperties.getEndpoint());
    }

    @Override
    public void deleteBucket(String bucketName) {
        throw new IllegalArgumentException("Unsupported delete bucket");
    }

    @Override
    @SneakyThrows
    public SimpleOssFile fileMetadata(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        FileInfo metadata = this.bucketManager.stat(bucketName, objectKey);
        SimpleOssFile result = new SimpleOssFile();
        result.setLink(this.fileLink(bucketName, objectKey));
        result.setName(objectKey);
        result.setLength(metadata.fsize);
        result.setContentType(metadata.mimeType);
        result.setCreateTime(metadata.putTime);
        return result;
    }

    @Override
    public String fileLink(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        return String.format("%s/%s", ossProperties.getEndpoint(), objectKey);
    }

    @Override
    @SneakyThrows
    public SimpleOssFile uploadFile(String bucketName, String objectKey, File file) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        String contentType = this.ossRule.getContentType(objectKey);
        String token = this.auth.uploadToken(bucketName, objectKey);
        this.uploadManager.put(file, objectKey, token, null, contentType, false);
        return this.fileMetadata(bucketName, objectKey);
    }

    @Override
    @SneakyThrows
    public SimpleOssFile uploadFile(String bucketName, String objectKey,
                                    InputStream stream, String contentType) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        if (contentType == null) {
            contentType = this.ossRule.getContentType(objectKey);
        }
        String token = this.auth.uploadToken(bucketName, objectKey);
        this.uploadManager.put(stream, objectKey, token, null, contentType);
        return this.fileMetadata(bucketName, objectKey);
    }

    @Override
    @SneakyThrows
    public void deleteFile(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        this.bucketManager.delete(bucketName, objectKey);
    }

    @Override
    @SneakyThrows
    public void copyFile(String sourceBucketName, String sourceObjectKey,
                         String targetBucketName, String targetObjectKey) {
        this.ossRule.validBucketObjectKey(sourceBucketName, sourceObjectKey);
        this.ossRule.validBucketObjectKey(targetBucketName, targetObjectKey);

        this.bucketManager.copy(sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey, true);
    }

    @Override
    @SneakyThrows
    public void moveFile(String sourceBucketName, String sourceObjectKey,
                         String targetBucketName, String targetObjectKey) {
        this.ossRule.validBucketObjectKey(sourceBucketName, sourceObjectKey);
        this.ossRule.validBucketObjectKey(targetBucketName, targetObjectKey);

        this.bucketManager.move(sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey, true);
    }

    public static Configuration defaultConfiguration(SimpleOssProperties ossProperties) {
        Region region = null;
        String regionStr = ossProperties.getRegion();
        if ("z0".equals(regionStr) || "huadong".equals(regionStr)) {
            region = Region.huadong();
            regionStr = "z0";
        } else if ("z1".equals(regionStr) || "huabei".equals(regionStr)) {
            region = Region.huabei();
            regionStr = "z1";
        } else if ("z2".equals(regionStr) || "huanan".equals(regionStr)) {
            region = Region.huanan();
            regionStr = "z2";
        } else if ("na0".equals(regionStr) || "beimei".equals(regionStr)) {
            region = Region.beimei();
            regionStr = "na0";
        } else if ("as0".equals(regionStr) || "xinjiapo".equals(regionStr) || "dongnanya".equals(regionStr)) {
            region = Region.xinjiapo();
            regionStr = "as0";
        }
        ossProperties.setRegion(regionStr);
        if (region == null) {
            throw new IllegalArgumentException("Unsupported region");
        }
        return new com.qiniu.storage.Configuration(region);
    }

}
