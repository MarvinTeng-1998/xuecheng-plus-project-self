package com.xuecheng.media;

import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @description: 文件分块上传测试
 * @author: dengbin
 * @create: 2024-01-14 23:55
 **/
public class BigFileUploadTest {

    /*
     * @Description: 分块测试
     * @Author: dengbin
     * @Date: 14/1/24 23:56
     * @return: void
     **/
    @Test
    public void testChunk() throws IOException {
        File sourceFile = new File("/Users/dengbin/Desktop/test.mp4");
        // 分块文件存储路径
        String chunkFilePath = "/Users/dengbin/Desktop/chunk/";
        // 分块文件的大小
        int chunkSize = 1024 * 1024 * 5;
        // 分块文件的个数
        int chunkNum = (int) Math.ceil(sourceFile.length() * 1.0 / chunkSize);
        // 使用流从源文件读数据，向分块文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");

        // 缓存区
        byte[] bytes = new byte[1024];
        for (int i = 0; i < chunkNum; i++) {
            File chunkFile = new File(chunkFilePath + i);
            // 将分块文件写入流
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;

            while ((len = raf_r.read(bytes)) != -1) {
                raf_rw.write(bytes, 0, len);
                if (chunkFile.length() >= chunkSize) {
                    break;
                }
            }

            raf_rw.close();
        }

        raf_r.close();
    }

    /*
     * @Description: 将分块进行合并
     * @Author: dengbin
     * @Date: 14/1/24 23:56
     * @return: void
     **/
    @Test
    public void testMerge() throws IOException {
        // 块文件合并
        File chunkFolder = new File("/Users/dengbin/Desktop/chunk/");
        // 源文件
        File sourceFile = new File("/Users/dengbin/Desktop/test.mp4");
        // 合并后的文件
        File mergeFile = new File("/Users/dengbin/Desktop/test1.mp4");

        // 取出所有分块文件
        File[] files = chunkFolder.listFiles();
        // 将数组转成list
        assert files != null;
        List<File> fileList = Arrays.asList(files);
        fileList.sort((o1, o2) -> Integer.parseInt(o1.getName()) - Integer.parseInt(o2.getName()));

        // 向合并文件写的流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        // 缓存区
        byte[] bytes = new byte[1024];
        for (File file : fileList) {
            // 读分块的流
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while((len = raf_r.read(bytes)) != -1){
                raf_rw.write(bytes, 0, len);
            }
            raf_r.close();
        }

        raf_rw.close();

        // 合并文件完成后对合并的文件校验
        FileInputStream fis_merge = new FileInputStream(mergeFile);
        FileInputStream fis_source = new FileInputStream(sourceFile);
        String md5_merge = DigestUtils.md5Hex(fis_merge);
        String md5_source = DigestUtils.md5Hex(fis_source);
        if(md5_merge.equals(md5_source)){
            System.out.println("文件合并成功");
        }
    }
}
