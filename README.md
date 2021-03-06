# simple-oss
[![Maven central](https://maven-badges.herokuapp.com/maven-central/com.loyayz/simple-oss/badge.svg)](https://mvnrepository.com/artifact/com.loyayz/simple-oss)

基于`本地文件/MinIO/阿里云oss/腾讯云cos/七牛`实现的简单oss客户端。


## 1 安装
```xml
<dependencies>
  <dependency>
    <groupId>com.loyayz</groupId>
    <artifactId>simple-oss</artifactId>
    <version>1.0.0</version>
  </dependency>

  <!-- 本项目未引入各oss客户端，使用时请根据实际情况自行添加以下依赖之一 -->

  <!-- 阿里云 oss -->
  <dependency>
      <groupId>com.aliyun.oss</groupId>
      <artifactId>aliyun-sdk-oss</artifactId>
      <version>${aliyun.oss.version}</version>
  </dependency>
  <!-- 腾讯 cos -->
  <dependency>
      <groupId>com.qcloud</groupId>
      <artifactId>cos_api</artifactId>
      <version>${tencent.oss.version}</version>
  </dependency>
  <!-- 七牛 -->
  <dependency>
    <groupId>com.qiniu</groupId>
    <artifactId>qiniu-java-sdk</artifactId>
    <version>${qiniu.oss.version}</version>
  </dependency>
  <!-- MinIO -->
  <dependency>
      <groupId>io.minio</groupId>
      <artifactId>minio</artifactId>
      <version>${minio.oss.version}</version>
  </dependency>
</dependencies>
```
## 2 快速开始

### 2.1 配置
Spring Boot 项目将根据 application.yml 中的 simple.oss 自动配置。

```yml
# application.yml
simple:
  oss:
    # 文件服务提供者 local,minio,aliyun,tencent
    provider:
    # 密钥id
    accessKey:
    # 密钥
    secretKey:
    # 对象存储服务的 URL
    endpoint:
    # 区域
    # provider=local 时：根目录，例  C:\
    # provider=tencent 时：COS 地域
    # provider=qiniu 时：七牛 地域
    region:
    # 存储桶，有值时只能存到该存储桶内
    bucket:
```

### 2.2 使用
```java
@Autowired
private SimpleOssClient simpleOssClient;

public void test() {
    simpleOssClient.createBucket("myFirstBucket");
}

```
## 3 SimpleOssClient 方法列表

- `void createBucket(String bucketName)`：创建存储桶
- `void deleteBucket(String bucketName)`：删除存储桶
- `SimpleOssFile fileMetadata(String bucketName, String objectKey)`：获取文件信息
- `String fileLink(String bucketName, String objectKey)`：获取文件完整路径
- `SimpleOssFile uploadFile(String bucketName, String objectKey, File file)`：上传文件
- `SimpleOssFile uploadFile(String bucketName, String objectKey, InputStream stream)`：上传文件
- `SimpleOssFile uploadFile(String bucketName, String objectKey, InputStream stream, String contentType)`：上传文件
- `void deleteFile(String bucketName, String objectKey)`：删除文件
- `void deleteFile(String bucketName, List<String> objectKeys)`：批量删除文件
- `void copyFile(String sourceBucketName, String objectKey, String targetBucketName)`：复制文件
- `void copyFile(String sourceBucketName, String sourceObjectKey, String targetBucketName, String targetObjectKey)`：复制文件
- `void moveFile(String sourceBucketName, String objectKey, String targetBucketName)`：迁移文件
- `void moveFile(String sourceBucketName, String sourceObjectKey, String targetBucketName, String targetObjectKey)`：迁移文件

[使用示例](https://github.com/loyayz/simple-sample)
