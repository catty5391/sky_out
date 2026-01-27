package com.sky.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

@Data
@Slf4j
@AllArgsConstructor
public class MiniOssUtil {
    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;

    /**
     *
     * @param bytes 待上传数据
     * @param objectName 上传数据名称(伪路径)
     * @return 上传地点（供下次访问）
     */
    public String upload(byte[] bytes, String objectName){
        S3Client s3 = null;
        try {
            s3 = S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.US_EAST_1)
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(accessKeyId, accessKeySecret)
                            )
                    ).build();
            // 2) PutObject 请求
            PutObjectRequest putReq = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectName)
                    // .contentType("text/plain") // 可选
                    .build();
           ByteArrayInputStream in = new ByteArrayInputStream(bytes);
           s3.putObject(putReq, RequestBody.fromInputStream(in, bytes.length));



        }catch (S3Exception se) {
            // 服务端返回了错误（权限、桶不存在、签名、region/endpoint 不匹配等）
            System.out.println("Caught an S3Exception, which means your request made it to S3, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message: " + se.getMessage());
            System.out.println("Status Code: " + se.statusCode());
            System.out.println("Request ID: " + se.requestId());
            System.out.println("Extended Request ID: " + se.extendedRequestId());
            if (se.awsErrorDetails() != null) {
                System.out.println("Error Code: " + se.awsErrorDetails().errorCode());
                System.out.println("Error Details: " + se.awsErrorDetails().errorMessage());
            }
        } catch (SdkClientException ce) {
            // 客户端侧异常（网络不可达、TLS 失败、DNS、连接超时等）
            System.out.println("Caught an SdkClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ce.getMessage());
        } finally {
            if (s3 != null) {
                s3.close();
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(endpoint)
                .append("/")
                .append(bucketName)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
