package org.java.learn.summary.suanfa.sort;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: java_summary
 * @Package org.java.learn.summary.suanfa.sort
 * @Description: TODO
 * @date Date : 2019年10月17日 18:53
 */
public class BigdataSort {

    String resultPath = "D:\\file\\result.txt";
    String filePath = "D:\\temp.txt";
    String createFilePath = "D:\\file\\";

    public static void main(String[] args) throws IOException {
        BigdataSort b = new BigdataSort();
        b.readFile();
    }

    public void readFile() throws IOException {
        FileChannel fw = new RandomAccessFile(filePath, "rw").getChannel();
        Scanner scaner = new Scanner(fw);
        // 3M 以读取
        ByteBuffer buf = ByteBuffer.allocate(1024 * 3);
        while (scaner.hasNext()) {
            buf.flip();
            String line = scaner.nextLine();
            int no;
            if (line.split(",")[0].length() == 1) {
                no = Integer.valueOf(line.split(",")[0]);
            } else {
                no = Integer.valueOf(line.split(",")[0].substring(0, 2));
            }
            FileChannel fr = new RandomAccessFile(createFilePath + no, "rw").getChannel();
            fr.write(ByteBuffer.wrap(line.getBytes()), fr.size());
            fr.write(ByteBuffer.wrap("\r\n".getBytes()), fr.size());
            buf.clear();
            fr.close();
        }
        fw.close();
        FileChannel fw2 = new RandomAccessFile(resultPath, "rw").getChannel();
        fw2.truncate(0);
        // 循环每个文件
        for (int i = 0; i < 100; i++) {
            if (!(new File(createFilePath + i)).exists()) {
                continue;
            }
            // 排序
            List<String> list = new ArrayList<String>();
            FileChannel fr2 = new RandomAccessFile(createFilePath + i, "rw").getChannel();
            Scanner sc = new Scanner(fr2);
            // 将每个文件每行存入到内存中
            while (sc.hasNext()) {
                list.add(sc.nextLine());
            }
            // 排序
            Collections.sort(list);
            for (String line : list) {
                fw2.write(ByteBuffer.wrap(line.getBytes()), fw2.size());
                fw2.write(ByteBuffer.wrap("\r\n".getBytes()), fw2.size());
            }
            list = null;
            System.gc();
            fr2.close();
        }
        fw2.close();
    }
}
