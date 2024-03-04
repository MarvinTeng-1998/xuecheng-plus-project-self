package com.xuecheng.media;

import com.alibaba.nacos.common.utils.IoUtils;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


import java.io.*;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public void clearBucket(){
        try {
            // 递归列举某个bucket下的所有文件，然后循环删除
            Iterable<Result<Item>> iterable = minioClient.listObjects(ListObjectsArgs.builder()
                    .bucket("video")
                    .recursive(true)
                    .build());
            for (Result<Item> itemResult : iterable) {
                RemoveObjectArgs removeObjectArgs = RemoveObjectArgs.builder()
                        .bucket("video")
                        .object(itemResult.get().objectName())
                        .build();
                minioClient.removeObject(removeObjectArgs);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    /*
     * @Description: 将分块文件上传到minio
     * @Author: dengbin
     * @Date: 15/1/24 02:15
     * @return: void
     **/
    @Test
    public void uploadChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        for (int i = 0; i < 22; i++) {
            // 上传文件的参数信息
            UploadObjectArgs uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket("testbucket")// 桶
                    .filename("/Users/dengbin/Desktop/chunk/" + i)// 指定本地文件地址
                    .object("chunk/" + i) // 对象名
                    .build();
            minioClient.uploadObject(uploadObjectArgs);
            System.out.println("上传分块" + i + "成功！");
        }
    }

    /*
     * @Description: 在Minio上合并分块文件
     * @Author: dengbin
     * @Date: 15/1/24 02:21
     * @return: void
     **/
    @Test
    public void testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        /*
        List<ComposeSource> sources = null;
        for (int i = 0; i < 22; i++) {
            // 指定分块文件的信息
            ComposeSource composeSource = ComposeSource.builder()
                    .bucket("testbucket")
                    .object("chunk/" + i)
                    .build();
            sources.add(composeSource);
        }
        */

        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(22).map(i -> ComposeSource.builder()
                .bucket("testbucket")
                .object("chunk/" + i)
                .build()).collect(Collectors.toList());

        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge01.mp4")
                .sources(sources) // 指定源文件
                .build();
        // Minio的默认的分块大小应该是5M
        minioClient.composeObject(composeObjectArgs);
    }


}
