package com.xuecheng.media;

import com.alibaba.nacos.common.utils.IoUtils;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @description: 测试Minio的SDK
 * @author: dengbin
 * @create: 2024-01-13 01:53
 **/
public class MinioTest {

    static MinioClient minioClient = MinioClient.builder()
            .endpoint("http://localhost:9000")
            .credentials("minio", "minio123")
            .build();

    @Test
    public void test_upload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 上传文件的参数信息
        UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                .bucket("testbucket")// 桶
                .filename("/Users/dengbin/Desktop/2.jpg")// 指定本地文件地址
                .object("2.jpg") // 对象名
                .build();

        // 上传文件
        minioClient.uploadObject(uploadObjectArgs);
    }

    @Test
    public void test_delete() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("2.jpg")
                .build();
        minioClient.removeObject(removeObjectArgs);
    }

    @Test
    public void test_GetFile() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        GetObjectArgs getObjectArgs = GetObjectArgs.builder().bucket("testbucket").object("53993.mp4").build();
        // GetObjectResponse 继承了FileInputStream
        FilterInputStream inputStream = minioClient.getObject(getObjectArgs);
        FileOutputStream fileOutputStream = new FileOutputStream(new File("/Users/dengbin/Desktop/1.mp4"));
        IoUtils.copy(inputStream, fileOutputStream);

        // 校验文件的完整性对文件的内容进行md5验证
        String source_md5 = DigestUtils.md5Hex(inputStream); // minio中文件的md5
        InputStream fileInputStream = Files.newInputStream(new File("/Users/dengbin/Desktop/1.mp4").toPath());
        String local_md5 = DigestUtils.md5Hex(fileInputStream);
        if (source_md5.equals(local_md5)) {
            System.out.println("下载成功");
        }
    }
}
