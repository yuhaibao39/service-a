package com.bosicloud.web;


import org.apache.log4j.Logger;
import org.apache.skywalking.apm.toolkit.opentracing.SkywalkingTracer;
import org.apache.skywalking.apm.toolkit.trace.ActiveSpan;
import org.apache.skywalking.apm.toolkit.trace.Tag;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class ComputeController {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private DiscoveryClient client;

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    @Autowired
    RestTemplate restTemplate;//定义为私有可能会报错

    //调用自身
    @RequestMapping(value = "/index", method = RequestMethod.GET)
    public String add(@RequestParam Integer a, @RequestParam Integer b) {
        ServiceInstance instance = client.getLocalServiceInstance();
        Integer r = a + b;
        logger.info("/add, host:" + instance.getHost() + ", service_id:" + instance.getServiceId() + ", result:" + r);
        return "From Service-A, Result is " + r;
    }

    //ribbon调用
    @RequestMapping(value = "/ribbon", method = RequestMethod.GET)
    public String ribbon(@RequestParam Integer a, @RequestParam Integer b) {

        ServiceInstance instance = client.getLocalServiceInstance();

        this.loadBalancerClient.choose("service-B");//随机访问策略

        String result = restTemplate.getForEntity("http://service-b/ribbon?a=" + a + "&b=" + b, String.class).getBody();

        logger.info("robbin /ribbon, host:" + instance.getHost() + ", service_id:" + instance.getServiceId() + ", result:");

        return "From Service-A, Result is ----> " + result;
    }



    //A服务调用B服务
    @RequestMapping(value = "/skywalking", method = RequestMethod.GET)
    public String testauthorizationok(@RequestHeader Map<String, String> headerMap, @RequestParam Integer a, @RequestParam Integer b) {

        logger.info("love A Print headerMap:" + headerMap);

        ServiceInstance instance = client.getLocalServiceInstance();
        Integer r = a + b;
        logger.info(" love Service-A, /love, host:" + instance.getHost() + ", service_id:" + instance.getServiceId() + ", result:" + r);

        String result = restTemplate.getForObject("http://service-b/skywalking?a=" + a + "&b=" + b, String.class);

        //远程服务调用测试
        return result;
    }

    //获取追踪Id，并可以在rocketBot中查询
    @RequestMapping("/getTraceId")
    public String getTraceId() {
        //使当前链路报错，并提示报错信息
        ActiveSpan.error(new RuntimeException("Test-Error-Throwable"));
        //打印当前info信息
        ActiveSpan.info("Test-Error-Throwable");
        //打印debug信息
        ActiveSpan.debug("Test-debug-Throwable");
        System.out.println("traceid:" + TraceContext.traceId());

        logger.info("Print TraceId: 获取追踪Id -- " + TraceContext.traceId());

        //获取tranceId
        return TraceContext.traceId();
    }

    @RequestMapping("/getError")
    public void getError() {
        int i = 1 / 0;
    }


    @Trace
    public String hello() {
        logger.info("function: hello");
        return "success";
    }

    @Trace
    public String world() {
        hello();
        logger.info("function: world");
        return "success";
    }


    @Trace
    public String mik() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        world();
        logger.info("function: michaels");
        return "success";
    }

    @RequestMapping(value = "/traceid")
    @Tag(key ="testTranceId",value="returnedObj")
    public String testTranceId() throws InterruptedException {
        logger.info("方法内部链路测试：tranceId： ====>" + TraceContext.traceId() );
        mik();
        return "success";
    }

    @GetMapping("/buildspan")
    public String buildspan() {
        // <X>创建一个 Span
        SkywalkingTracer tracer = new SkywalkingTracer();
        for(int icount=0; icount < 5; icount++ ) {
            logger.info("buildspan iot_operation-"+ icount );
            tracer.buildSpan("iot_operation-"+icount).withTag("mp", "IOT-Span").startManual().finish();
        }
        // 返回
        return "opentracing";
    }






}
