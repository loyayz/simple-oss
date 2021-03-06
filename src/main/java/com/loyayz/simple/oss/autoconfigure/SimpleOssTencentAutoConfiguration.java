package com.loyayz.simple.oss.autoconfigure;

import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import com.loyayz.simple.oss.impl.TencentOssClient;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@AutoConfigureAfter(SimpleOssAutoConfiguration.class)
@ConditionalOnClass(COSClient.class)
@ConditionalOnProperty(value = "simple.oss.provider", havingValue = "tencent")
@RequiredArgsConstructor
public class SimpleOssTencentAutoConfiguration {
    private final SimpleOssProperties ossProperties;
    private final SimpleOssRule ossRule;

    @Bean
    @ConditionalOnMissingBean(COSClient.class)
    public COSClient cosClient() {
        COSCredentials credentials = new BasicCOSCredentials(ossProperties.getAccessKey(), ossProperties.getSecretKey());
        // COS 地域 https://cloud.tencent.com/document/product/436/6224
        Region region = new Region(ossProperties.getRegion());
        return new COSClient(credentials, new ClientConfig(region));
    }

    @Bean
    @ConditionalOnMissingBean(SimpleOssClient.class)
    public SimpleOssClient simpleOssClient(COSClient cosClient) {
        return new TencentOssClient(ossRule, cosClient);
    }

}
