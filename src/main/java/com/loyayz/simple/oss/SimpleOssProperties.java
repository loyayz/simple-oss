package com.loyayz.simple.oss;

import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleOssProperties {

    /**
     * 文件服务提供者
     * local,minio,aliyun,tencent,qiniu
     */
    private String provider;
    /**
     * 密钥id
     */
    private String accessKey;
    /**
     * 密钥
     */
    private String secretKey;
    /**
     * 对象存储服务的 URL
     */
    private String endpoint;
    /**
     * 区域
     * provider=local 时：根目录，例  C:\
     * provider=tencent 时：COS 地域
     * provider=qiniu 时：七牛 地域
     */
    private String region;
    /**
     * 存储桶，有值时只能存到该存储桶内
     */
    private String bucket;

}
