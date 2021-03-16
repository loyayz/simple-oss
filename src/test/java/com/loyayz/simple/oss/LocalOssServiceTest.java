package com.loyayz.simple.oss;

import com.loyayz.simple.oss.impl.DefaultOssRule;
import com.loyayz.simple.oss.impl.LocalOssClient;
import lombok.SneakyThrows;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
public class LocalOssServiceTest {
    private final static SimpleOssClient ossClient;
    private final static String baseDir = "C:\\";

    static {
        SimpleOssProperties ossProperties = new SimpleOssProperties();
        ossProperties.setRegion(baseDir);
        ossProperties.setEndpoint(baseDir);
        SimpleOssRule ossRule = new DefaultOssRule(ossProperties);
        ossClient = new LocalOssClient(ossRule, ossProperties);
    }

    @Test
    public void bucket() {
        String bucketName = "test";
        Path bucketPath = filePath(bucketName);

        Assert.assertFalse(Files.exists(bucketPath));
        ossClient.createBucket(bucketName);
        Assert.assertTrue(Files.exists(bucketPath));

        ossClient.deleteBucket("test");
        Assert.assertFalse(Files.exists(bucketPath));
    }

    @Test
    public void fileLink() {
        String bucketName = "test";
        String objectKey = "abc";
        String link = ossClient.fileLink(bucketName, objectKey);
        Assert.assertEquals(baseDir + File.separator + bucketName + File.separator + objectKey, link);
    }

    @Test
    @SneakyThrows
    public void uploadFile() {
        String bucketName = "test_upload";
        String objectKey = "abc.txt";
        String fileContent = UUID.randomUUID().toString();
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));

        Path filePath = filePath(bucketName, objectKey);
        Assert.assertFalse(Files.exists(filePath));
        ossClient.uploadFile(bucketName, objectKey, tempFile.toFile());
        Assert.assertTrue(Files.exists(filePath));

        Assert.assertEquals(1, Files.readAllLines(filePath).size());
        Assert.assertEquals(fileContent, Files.readAllLines(filePath).get(0));

        ossClient.deleteBucket(bucketName);
        Files.delete(tempFile);
    }

    @Test
    @SneakyThrows
    public void copyFile() {
        String sourceBucketName = "test_copy_source";
        String sourceObjectKey = "source.txt";
        String targetBucketName = "test_copy_target";
        String targetObjectKey = "target.txt";

        String fileContent = UUID.randomUUID().toString();
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        ossClient.uploadFile(sourceBucketName, sourceObjectKey, tempFile.toFile());

        Path sourceFilePath = filePath(sourceBucketName, sourceObjectKey);
        Path targetFilePath = filePath(targetBucketName, targetObjectKey);
        Assert.assertFalse(Files.exists(targetFilePath));
        ossClient.copyFile(sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey);
        Assert.assertTrue(Files.exists(targetFilePath));

        Assert.assertArrayEquals(
                Files.readAllLines(sourceFilePath).toArray(),
                Files.readAllLines(targetFilePath).toArray()
        );

        ossClient.deleteBucket(sourceBucketName);
        ossClient.deleteBucket(targetBucketName);
        Files.delete(tempFile);
    }

    @Test
    @SneakyThrows
    public void moveFile() {
        String sourceBucketName = "test_move_source";
        String sourceObjectKey = "source.txt";
        String targetBucketName = "test_move_target";
        String targetObjectKey = "target.txt";

        String fileContent = UUID.randomUUID().toString();
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, fileContent.getBytes(StandardCharsets.UTF_8));
        ossClient.uploadFile(sourceBucketName, sourceObjectKey, tempFile.toFile());

        Path sourceFilePath = filePath(sourceBucketName, sourceObjectKey);
        Path targetFilePath = filePath(targetBucketName, targetObjectKey);
        Assert.assertFalse(Files.exists(targetFilePath));
        List<String> sourceFileContent = Files.readAllLines(sourceFilePath);
        ossClient.moveFile(sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey);
        Assert.assertTrue(Files.exists(targetFilePath));
        Assert.assertFalse(Files.exists(sourceFilePath));

        Assert.assertArrayEquals(
                sourceFileContent.toArray(),
                Files.readAllLines(targetFilePath).toArray()
        );

        ossClient.deleteBucket(sourceBucketName);
        ossClient.deleteBucket(targetBucketName);
        Files.delete(tempFile);
    }

    private Path filePath(String bucketName, String... objectKeys) {
        String result = baseDir + File.separator + bucketName;
        for (String objectKey : objectKeys) {
            result = result.concat(File.separator).concat(objectKey);
        }
        result = result.replace("//", "/");
        result = result.replace("/", File.separator);
        return Paths.get(result);
    }

}
