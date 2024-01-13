package com.bosicloud.web;

import com.bosicloud.DemoService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RefreshScope
@RestController
@RequestMapping("/demo")
class DemoController {

    private final Logger logger = Logger.getLogger(getClass());

    @Autowired
    private DemoService demoService;

    @Autowired
    private JdbcTemplate template;


    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private MongoTemplate mongoTemplate;


    @GetMapping("/mysql")
    public String mysql() {
        this.selectById(1);
        return "mysql";
    }

    public Object selectById(Integer id) {
        return template.queryForObject("SELECT id, username, password FROM t_user WHERE id = ?",
                new BeanPropertyRowMapper<>(Object.class), // 结果转换成对应的对象。Object 理论来说是 UserDO.class ，这里偷懒了。
                id);
    }



    @GetMapping("/redis")
    public String redis() {
        redisTemplate.boundValueOps("name").set("steffens");
        this.get("demo");
        this.get("name");
        return "redis";
    }

    public void get(String key) {
        redisTemplate.opsForValue().get(key);
    }



    @GetMapping("/mongodb")
    public String mongodb() {
        this.findById(1);
        return "mongodb";
    }

    public UserDO findById(Integer id) {
        return mongoTemplate.findOne(new Query(Criteria.where("_id").is(id)), UserDO.class);
    }

    @GetMapping("/async")
    public String echoasync() {
        demoService.async();
        return "async";
    }


    @GetMapping("/echo")
    public String echo() {
        return "echo";
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/plugin")
    public String helloplugin() {

        String replace = StringUtils.replace("oldString", "old","replaced");
        System.out.println(replace);

        logger.info("====>我的替换字符串" + replace);

        return "hello";
    }





}