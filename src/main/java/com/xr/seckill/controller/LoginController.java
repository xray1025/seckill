package com.xr.seckill.controller;

import com.xr.seckill.redis.RedisService;
import com.xr.seckill.service.SeckillUserService;
import com.xr.seckill.vo.LoginVo;
import com.xr.seckill.result.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/login")
public class LoginController {

//    private static Logger log = (Logger) LoggerFactory.getLogger(LoginController.class);

    private static Logger log = LoggerFactory.getLogger(LoginController.class);//需要引用正确的包org.slf4j

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/to_login")
    public String toLogin(){
        return "login";
    }

    @RequestMapping("/do_login")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){
        log.info(loginVo.toString());
        String token = seckillUserService.login(response, loginVo);
        return Result.success(token);
    }
}
