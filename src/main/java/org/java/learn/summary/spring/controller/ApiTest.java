package org.java.learn.summary.spring.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : duyanlong
 * @version V1.0
 * @Project: qualitytemplate
 * @Package org.java.learn.summary.spring.controller
 * @Description: TODO
 * @date Date : 2019年07月23日 17:00
 */
@RestController
@RequestMapping("/test")
public class ApiTest {

    @GetMapping(value = "/json")
    public String testJson(@RequestParam(value = "path",required = false) String path) throws
        IOException {
        if(StringUtils.isEmpty(path)){
            path = "/home/hdp-qdata-qdg/test_json.json";
        }
        FileInputStream fio = new FileInputStream(new File(path));
        byte[] bys = new byte[1024];
        StringBuilder sb = new StringBuilder();
        while (fio.read(bys)>-1){
            sb.append(new String(bys));
        }
        return sb.toString();
    }

    @GetMapping(value = "/array")
    public String testArray(){
        return "aa0001\tbb0001\tcc0001\n"
            + "aa0002\tbb0002\tcc0002\n"
            + "aa0003\tbb0003\tcc0003\n"
            + "aa0004\tbb0004\tcc0004\n"
            + "aa0005\tbb0005\tcc0005\n"
            + "aa0006\tbb0006\tcc0006\n"
            + "aa0007\tbb0007\tcc0007";
    }

    @GetMapping(value = "/ngnix")
    public String testNginx(){
        return "test.php?aa=aa0001&bb=bb0001&cc=cc0001\n"
            + "test.php?aa=aa0002&bb=bb0002&cc=cc0002\n"
            + "test.php?aa=aa0003&bb=bb0003&cc=cc0003\n"
            + "test.php?aa=aa0004&bb=bb0004&cc=cc0004\n"
            + "test.php?aa=aa0005&bb=bb0005&cc=cc0005\n"
            + "test.php?aa=aa0006&bb=bb0006&cc=cc0006\n"
            + "test.php?aa=aa0007&bb=bb0007&cc=cc0007";
    }
}
