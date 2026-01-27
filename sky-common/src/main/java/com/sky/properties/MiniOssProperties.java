package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("sky.minioss")
@Data
public class MiniOssProperties {
    private String accessKeyId;
    private String accessKeySecret;
    private String endPoint;
    private String bucketName;
}
