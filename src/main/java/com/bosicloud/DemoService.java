package com.bosicloud;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DemoService {

    @Async
    public void async() {
        System.out.println("异步任务的执行");
    }

}
