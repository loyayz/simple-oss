package com.loyayz.simple.oss.autoconfigure;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import com.loyayz.simple.oss.impl.AliyunOssClient;
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
@ConditionalOnClass(OSSClient.class)
@ConditionalOnProperty(value = "simple.oss.provider", havingValue = "aliyun")
@RequiredArgsConstructor
public class SimpleOssAliyunAutoConfiguration {
    private final SimpleOssProperties ossProperties;
    private final SimpleOssRule ossRule;

    @Bean
    @ConditionalOnMissingBean(OSSClient.class)
    public OSSClient ossClient() {
        CredentialsProvider credentialProvider = new DefaultCredentialProvider(ossProperties.getAccessKey(), ossProperties.getSecretKey());
        return new OSSClient(ossProperties.getEndpoint(), credentialProvider, new ClientConfiguration());
    }

    @Bean
    @ConditionalOnMissingBean(SimpleOssClient.class)
    public SimpleOssClient simpleFileService(OSSClient ossClient) {
        return new AliyunOssClient(ossRule, ossClient);
    }

}
