package com.loyayz.simple.oss.autoconfigure;

import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import com.loyayz.simple.oss.impl.LocalOssClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;

/**
 * @author loyayz (loyayz@foxmail.com)
 */
@Configuration
@AutoConfigureAfter(SimpleOssAutoConfiguration.class)
@ConditionalOnProperty(value = "simple.oss.provider", havingValue = "local")
@RequiredArgsConstructor
public class SimpleOssLocalAutoConfiguration {
    private final SimpleOssProperties ossProperties;
    private final SimpleOssRule ossRule;

    @Bean
    @ConditionalOnMissingBean(SimpleOssClient.class)
    public SimpleOssClient simpleOssClient() {
        String endpoint = ossProperties.getEndpoint();
        String region = ossProperties.getRegion();
        if (endpoint == null) {
            ossProperties.setEndpoint(region);
        }
        Assert.hasText(region, "Please set base directory on simple.oss.region");
        return new LocalOssClient(ossRule, ossProperties);
    }

}
