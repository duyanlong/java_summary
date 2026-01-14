package org.java.learn.summary.spring.controller;

//import org.java.learn.summary.spring.utils.CliFrontend;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO.
 *
 * @author : duyanlong
 * @version V1.0
 * @Project: java_summary
 * @Package org.java.learn.summary.spring.controller
 * @date Date : 2020年11月03日 14:14
 */
@RestController
@RequestMapping("/flink")
public class FlinkController {

    /**
     * 提交flink作业.
     * @return String
     */
    @PostMapping("/submitJob")
    public String submitFlinkJob(@RequestParam("arg") String arg){
//        if(CliFrontend.main(arg)){
//            return "提交成功";
//        }
        return "提交失败";
    }

}
