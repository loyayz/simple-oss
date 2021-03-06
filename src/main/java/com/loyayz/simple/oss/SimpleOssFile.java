package com.loyayz.simple.oss;

import lombok.Data;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Data
public class SimpleOssFile {

    /**
     * 文件地址
     */
    private String link;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件大小
     */
    private long length;
    /**
     * 文件 contentType
     */
    private String contentType;
    /**
     * 文件上传时间
     */
    private long createTime;

}
