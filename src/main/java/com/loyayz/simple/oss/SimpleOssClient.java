package com.loyayz.simple.oss;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * 文件服务
 *
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SimpleOssClient {

    /**
     * 创建存储桶
     *
     * @param bucketName 存储桶名称
     */
    void createBucket(String bucketName);

    /**
     * 删除存储桶
     *
     * @param bucketName 存储桶名称
     */
    void deleteBucket(String bucketName);

    /**
     * 获取文件信息
     *
     * @param bucketName 存储桶名称
     * @param objectKey  文件名称
     * @return InputStream
     */
    SimpleOssFile fileMetadata(String bucketName, String objectKey);

    /**
     * 获取文件完整路径
     *
     * @param bucketName 存储桶名称
     * @param objectKey  文件名称
     * @return String
     */
    String fileLink(String bucketName, String objectKey);

    /**
     * 上传文件
     *
     * @param bucketName 存储桶名称
     * @param objectKey  文件名称
     * @param file       文件
     * @return 文件信息
     */
    SimpleOssFile uploadFile(String bucketName, String objectKey, File file);

    /**
     * 上传文件
     *
     * @param bucketName 存储桶名称
     * @param objectKey  文件名称
     * @param stream     文件流
     * @return 文件信息
     */
    default SimpleOssFile uploadFile(String bucketName, String objectKey, InputStream stream) {
        return this.uploadFile(bucketName, objectKey, stream, null);
    }

    /**
     * 上传文件
     *
     * @param bucketName  存储桶名称
     * @param objectKey   文件名称
     * @param stream      文件流
     * @param contentType contentType
     * @return 文件信息
     */
    SimpleOssFile uploadFile(String bucketName, String objectKey, InputStream stream, String contentType);

    /**
     * 删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectKey  文件名称
     */
    void deleteFile(String bucketName, String objectKey);

    /**
     * 批量删除文件
     *
     * @param bucketName 存储桶名称
     * @param objectKeys 文件名称列表
     */
    default void deleteFile(String bucketName, List<String> objectKeys) {
        objectKeys.forEach(objectKey -> {
            this.deleteFile(bucketName, objectKey);
        });
    }

    /**
     * 复制文件
     *
     * @param sourceBucketName 原存储桶名称
     * @param objectKey        原文件名称
     * @param targetBucketName 目标存储桶名称
     */
    default void copyFile(String sourceBucketName, String objectKey, String targetBucketName) {
        this.copyFile(sourceBucketName, objectKey, targetBucketName, objectKey);
    }

    /**
     * 复制文件
     *
     * @param sourceBucketName 原存储桶名称
     * @param sourceObjectKey  原文件名称
     * @param targetBucketName 目标存储桶名称
     * @param targetObjectKey  目标文件名称
     */
    void copyFile(String sourceBucketName, String sourceObjectKey, String targetBucketName, String targetObjectKey);

    /**
     * 迁移文件
     *
     * @param sourceBucketName 原存储桶名称
     * @param objectKey        原文件名称
     * @param targetBucketName 目标存储桶名称
     */
    default void moveFile(String sourceBucketName, String objectKey, String targetBucketName) {
        this.moveFile(sourceBucketName, objectKey, targetBucketName, objectKey);
    }

    /**
     * 迁移文件
     *
     * @param sourceBucketName 原存储桶名称
     * @param sourceObjectKey  原文件名称
     * @param targetBucketName 目标存储桶名称
     * @param targetObjectKey  目标文件名称
     */
    default void moveFile(String sourceBucketName, String sourceObjectKey, String targetBucketName, String targetObjectKey) {
        if (sourceBucketName.equals(targetBucketName) && sourceObjectKey.equals(targetObjectKey)) {
            return;
        }
        this.copyFile(sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey);
        this.deleteFile(sourceBucketName, sourceObjectKey);
    }

}
