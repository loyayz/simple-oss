package com.loyayz.simple.oss.autoconfigure;

import com.loyayz.simple.oss.SimpleOssClient;
import com.loyayz.simple.oss.SimpleOssProperties;
import com.loyayz.simple.oss.SimpleOssRule;
import com.loyayz.simple.oss.impl.MinioOssClient;
import io.minio.MinioClient;
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
@ConditionalOnClass(MinioClient.class)
@ConditionalOnProperty(value = "simple.oss.provider", havingValue = "minio")
@RequiredArgsConstructor
public class SimpleOssMinioAutoConfiguration {
    private final SimpleOssProperties ossProperties;
    private final SimpleOssRule ossRule;

    @Bean
    @ConditionalOnMissingBean(MinioClient.class)
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(ossProperties.getEndpoint())
                .credentials(ossProperties.getAccessKey(), ossProperties.getSecretKey())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(SimpleOssClient.class)
    public SimpleOssClient simpleOssClient(MinioClient minioClient) {
        return new MinioOssClient(ossRule, ossProperties, minioClient);
    }

}
