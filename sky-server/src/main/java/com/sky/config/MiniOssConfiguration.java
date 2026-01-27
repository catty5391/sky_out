package com.sky.config;

import com.sky.properties.MiniOssProperties;
import com.sky.utils.MiniOssUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

@Configuration
@Slf4j
public class MiniOssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MiniOssUtil miniOssUtil(MiniOssProperties miniOssProperties){
        log.info("开始创建对象文件上传工具类对象{}", miniOssProperties);
        return new MiniOssUtil(miniOssProperties.getEndPoint(),
                miniOssProperties.getAccessKeyId(),
                miniOssProperties.getAccessKeySecret(),
                miniOssProperties.getBucketName());
    }
}
