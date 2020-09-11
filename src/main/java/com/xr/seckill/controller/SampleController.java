package com.xr.seckill.controller;


import com.xr.seckill.domain.User;
import com.xr.seckill.rabbitMQ.MQSender;
import com.xr.seckill.redis.RedisService;
import com.xr.seckill.redis.UserKey;
import com.xr.seckill.result.Result;
import com.xr.seckill.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender mqSender;

    @RequestMapping("/thymeleaf")
    public String thymeleaf(Model model){
        model.addAttribute("name","xr");
        return "hello";
    }

    @RequestMapping("/db/get")
    @ResponseBody
    public Result<User> dbGet(){
        User user = userService.getById(1);
        return Result.success(user);
    }

    @RequestMapping("/db/tx")
    @ResponseBody
    public Result<Boolean> dbTx(){
        userService.tx();
        return Result.success(true);
    }

    @RequestMapping("/redis/get")
    @ResponseBody
    public Result<User> redisGet(){
        User user = redisService.get(UserKey.getById,""+1,User.class);
        return Result.success(user);
    }

    @RequestMapping("/redis/set")
    @ResponseBody
    public Result<Boolean> redisSet(){
        User user = new User();
        user.setId(1);
        user.setName("1111");
        redisService.set(UserKey.getById,""+1,user);//UserKey:id1
        return Result.success(true);
    }

//    @RequestMapping("/mq")
//    @ResponseBody
//    public Result<String> mq(){
//        mqSender.send("hello from rabbitMQ");
//        return Result.success("hello from rabbitMQ");
//    }

    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> topic(){
        mqSender.sendTopic("hello from rabbitMQ topic");
        return Result.success("hello from rabbitMQ topic");
    }

//    @RequestMapping("/mq/fanout")
//    @ResponseBody
//    public Result<String> fanout(){
//        mqSender.sendFanout("hello from rabbitMQ fanout");
//        return Result.success("hello from rabbitMQ fanout");
//    }
}
