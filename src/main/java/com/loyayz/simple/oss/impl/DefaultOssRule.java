package com.loyayz.simple.oss.impl;

import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import lombok.RequiredArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.File;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class DefaultOssRule implements SimpleOssRule {
    private final SimpleOssProperties ossProperties;

    @Override
    public void validBucketName(String bucketName) throws IllegalArgumentException {
        Assert.hasText(bucketName, "bucket must have text");
        Assert.doesNotContain(bucketName, " ", "bucket must not contains whitespace");
        Assert.doesNotContain(bucketName, "*", "bucket must not contains *");
        Assert.doesNotContain(bucketName, File.separator, "bucket must not contains " + File.separator);
        Assert.doesNotContain(bucketName, "/", "bucket must not contains /");
        String definedBucket = ossProperties.getBucket();
        if (StringUtils.hasText(definedBucket)) {
            Assert.isTrue(definedBucket.equals(bucketName), "bucket can only be " + definedBucket);
        }
    }

    @Override
    public void validObjectKey(String objectKey) throws IllegalArgumentException {
        Assert.hasText(objectKey, "objectKey must have text");
        Assert.doesNotContain(objectKey, " ", "objectKey must not contains whitespace");
        Assert.doesNotContain(objectKey, "*", "objectKey must not contains *");
        Assert.isTrue(!objectKey.startsWith(File.separator), "objectKey must not starts with " + File.separator);
        Assert.isTrue(!objectKey.startsWith("/"), "objectKey must not starts with /");
    }

}
