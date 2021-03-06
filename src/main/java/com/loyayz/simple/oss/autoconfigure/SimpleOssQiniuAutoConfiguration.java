package com.loyayz.simple.oss.autoconfigure;

import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import com.loyayz.simple.oss.impl.QiniuOssClient;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@AutoConfigureAfter(SimpleOssAutoConfiguration.class)
@ConditionalOnProperty(value = "simple.oss.provider", havingValue = "qiniu")
@RequiredArgsConstructor
public class SimpleOssQiniuAutoConfiguration {
    private final SimpleOssProperties ossProperties;
    private final SimpleOssRule ossRule;

    @Bean
    @ConditionalOnMissingBean(com.qiniu.storage.Configuration.class)
    public com.qiniu.storage.Configuration qiniuConfiguration() {
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

    @Bean
    @ConditionalOnMissingBean(SimpleOssClient.class)
    public SimpleOssClient simpleOssClient(com.qiniu.storage.Configuration config) {
        return new QiniuOssClient(ossRule,ossProperties, config);
    }

}
