package com.xr.seckill.controller;

import com.xr.seckill.domain.SeckillUser;
import com.xr.seckill.redis.RedisService;
import com.xr.seckill.result.Result;
import com.xr.seckill.service.SeckillUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/info")
    @ResponseBody
    public Result<SeckillUser> info(Model model, SeckillUser seckillUser){
        return Result.success(seckillUser);
    }
}
