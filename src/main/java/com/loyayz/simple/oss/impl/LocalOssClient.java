package com.loyayz.simple.oss.impl;

import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssFile;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@RequiredArgsConstructor
public class LocalOssClient implements SimpleOssClient {
    private final SimpleOssRule ossRule;
    private final SimpleOssProperties ossProperties;

    private static void deleteRecursively(Path root) throws IOException {
        if (root == null || Files.notExists(root)) {
            return;
        }
        if (!Files.isDirectory(root)) {
            Files.delete(root);
            return;
        }

        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Override
    @SneakyThrows
    public void createBucket(String bucketName) {
        this.ossRule.validBucketName(bucketName);

        Path path = this.filePath(bucketName);
        if (Files.exists(path)) {
            return;
        }
        Files.createDirectories(path);
    }

    @Override
    @SneakyThrows
    public void deleteBucket(String bucketName) {
        this.ossRule.validBucketName(bucketName);

        Path path = this.filePath(bucketName);
        deleteRecursively(path);
    }

    @Override
    @SneakyThrows
    public SimpleOssFile fileMetadata(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        Path filePath = this.filePath(bucketName, objectKey);
        SimpleOssFile result = new SimpleOssFile();
        result.setLink(this.fileLink(bucketName, objectKey));
        result.setName(objectKey);
        result.setLength(Files.size(filePath));
        result.setContentType(this.ossRule.getContentType(objectKey));
        result.setCreateTime(Files.getLastModifiedTime(filePath).toMillis());
        return result;
    }

    @Override
    public String fileLink(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        String result = String.format("%s/%s/%s",
                this.ossProperties.getEndpoint(), bucketName, objectKey);
        result = result.replace("//", "/");
        return result.replace("/", File.separator);
    }

    @Override
    @SneakyThrows
    public SimpleOssFile uploadFile(String bucketName, String objectKey, File file) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        try (InputStream stream = new FileInputStream(file)) {
            return this.uploadFile(bucketName, objectKey, stream);
        }
    }

    @Override
    @SneakyThrows
    public SimpleOssFile uploadFile(String bucketName, String objectKey,
                                    InputStream stream, String contentType) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        Path filePath = this.filePath(bucketName, objectKey);
        Files.createDirectories(filePath.getParent());
        Files.copy(stream, filePath, StandardCopyOption.REPLACE_EXISTING);
        return this.fileMetadata(bucketName, objectKey);
    }

    @Override
    @SneakyThrows
    public void deleteFile(String bucketName, String objectKey) {
        this.ossRule.validBucketObjectKey(bucketName, objectKey);

        Path filePath = this.filePath(bucketName, objectKey);
        deleteRecursively(filePath);
    }

    @Override
    @SneakyThrows
    public void copyFile(String sourceBucketName, String sourceObjectKey,
                         String targetBucketName, String targetObjectKey) {
        copyOrMove(true, sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey);
    }

    @Override
    public void moveFile(String sourceBucketName, String sourceObjectKey,
                         String targetBucketName, String targetObjectKey) {
        copyOrMove(false, sourceBucketName, sourceObjectKey, targetBucketName, targetObjectKey);
    }

    @SneakyThrows
    private void copyOrMove(boolean copy,
                            String sourceBucketName, String sourceObjectKey,
                            String targetBucketName, String targetObjectKey) {
        this.ossRule.validBucketObjectKey(sourceBucketName, sourceObjectKey);
        this.ossRule.validBucketObjectKey(targetBucketName, targetObjectKey);
        if (sourceBucketName.equals(targetBucketName) && sourceObjectKey.equals(targetObjectKey)) {
            return;
        }
        Path sourceFilePath = this.filePath(sourceBucketName, sourceObjectKey);
        Path targetFilePath = this.filePath(targetBucketName, targetObjectKey);
        Files.createDirectories(targetFilePath.getParent());
        if (copy) {
            Files.copy(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        } else {
            Files.move(sourceFilePath, targetFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Path filePath(String bucketName, String... objectKeys) {
        String result = this.ossProperties.getRegion() + File.separator + bucketName;
        for (String objectKey : objectKeys) {
            result = result.concat(File.separator).concat(objectKey);
        }
        result = result.replace("//", "/");
        result = result.replace("/", File.separator);
        return Paths.get(result);
    }

}
