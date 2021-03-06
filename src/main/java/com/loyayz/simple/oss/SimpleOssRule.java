package com.loyayz.simple.oss;

import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.util.Assert;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public interface SimpleOssRule {

    /**
     * 校验存储桶名
     *
     * @param bucketName 存储桶名
     * @throws IllegalArgumentException 不通过则抛异常
     */
    void validBucketName(String bucketName) throws IllegalArgumentException;

    /**
     * 校验文件名
     *
     * @param objectKey 文件名
     * @throws IllegalArgumentException 不通过则抛异常
     */
    void validObjectKey(String objectKey) throws IllegalArgumentException;

    /**
     * 校验存储桶名和文件名
     *
     * @param bucketName 存储桶名
     * @param objectKeys 文件名
     * @throws IllegalArgumentException 不通过则抛异常
     */
    default void validBucketObjectKey(String bucketName, String... objectKeys) throws IllegalArgumentException {
        this.validBucketName(bucketName);
        Assert.notNull(objectKeys, "objectKey must have text");
        for (String objectKey : objectKeys) {
            this.validObjectKey(objectKey);
        }
    }

    /**
     * 根据文件名获取对应的 content_type
     *
     * @param objectKey 文件名
     * @return content_type
     */
    default String getContentType(String objectKey) {
        String result = null;
        try {
            MediaType mediaType = MediaTypeFactory.getMediaType(objectKey).orElse(null);
            result = mediaType == null ? null : mediaType.toString();
        } catch (Exception ignored) {
        }
        return result;
    }

}
