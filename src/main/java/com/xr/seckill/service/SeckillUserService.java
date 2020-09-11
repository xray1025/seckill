package com.xr.seckill.service;

import com.xr.seckill.dao.SeckillUserDao;
import com.xr.seckill.domain.SeckillUser;
import com.xr.seckill.exception.GlobalException;
import com.xr.seckill.redis.RedisService;
import com.xr.seckill.redis.SeckillUserKey;
import com.xr.seckill.result.CodeMsg;
import com.xr.seckill.util.MD5Util;
import com.xr.seckill.util.UUIDUtil;
import com.xr.seckill.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class SeckillUserService {

    public static final String COOKI_NAME_TOKEN = "token";

    @Autowired
    SeckillUserDao seckillUserDao;

    @Autowired
    RedisService redisService;

    public SeckillUser getById(long id){

        //取缓存
        SeckillUser user = redisService.get(SeckillUserKey.getById, ""+id, SeckillUser.class);
        if(user!=null){
            return user;
        }
        //取数据库
        user = seckillUserDao.getById(id);
        if(user != null){
            redisService.set(SeckillUserKey.getById, ""+id, user);
        }
        return user;
    }

    public boolean updatePassword(String token, long id, String formPass){
        SeckillUser user = getById(id);
        if(user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //更新数据库
        SeckillUser toBeUpdate = new SeckillUser();
        toBeUpdate.setId(id);
        toBeUpdate.setPassword(MD5Util.formPassToDbPass(formPass, user.getSalt()));
        seckillUserDao.update(toBeUpdate);
        //更新缓存
        redisService.delete(SeckillUserKey.getById,""+id);
        user.setPassword(toBeUpdate.getPassword());
        redisService.set(SeckillUserKey.token, token, user);
        return true;
    }

    public SeckillUser getByToken(HttpServletResponse response, String token) {
        if(StringUtils.isEmpty(token)){
            return null;
        }
        SeckillUser seckillUser = redisService.get(SeckillUserKey.token, token, SeckillUser.class);
        if(seckillUser != null) {
            addCookie(response, token, seckillUser);
        }
        return seckillUser;
    }

    public String login(HttpServletResponse response, LoginVo loginVo) {
        if(loginVo == null){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断用户是否存在
        SeckillUser seckillUser = getById(Long.parseLong(mobile));
        if(seckillUser == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = seckillUser.getPassword();
        String saltDb = seckillUser.getSalt();
        String calcPass = MD5Util.formPassToDbPass(formPass, saltDb);
//        System.out.println(dbPass);
//        System.out.println(saltDb);
//        System.out.println(calcPass);
//        System.out.println(formPass);
        if(!calcPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        String token = UUIDUtil.uuid();
        addCookie(response, token, seckillUser);
        return token;
    }

    private void addCookie(HttpServletResponse response, String token, SeckillUser seckillUser){

        redisService.set(SeckillUserKey.token, token, seckillUser);
        Cookie cookie = new Cookie(COOKI_NAME_TOKEN, token);
        cookie.setMaxAge(SeckillUserKey.token.expireSeconds());
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
