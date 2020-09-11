package com.xr.seckill.controller;


import com.xr.seckill.access.AccessLimit;
import com.xr.seckill.domain.OrderInfo;
import com.xr.seckill.domain.SeckillOrder;
import com.xr.seckill.domain.SeckillUser;
import com.xr.seckill.rabbitMQ.MQSender;
import com.xr.seckill.rabbitMQ.SeckillMessage;
import com.xr.seckill.redis.AccessKey;
import com.xr.seckill.redis.GoodsKey;
import com.xr.seckill.redis.RedisService;
import com.xr.seckill.redis.SeckillKey;
import com.xr.seckill.result.CodeMsg;
import com.xr.seckill.result.Result;
import com.xr.seckill.service.GoodsService;
import com.xr.seckill.service.OrderService;
import com.xr.seckill.service.SeckillService;
import com.xr.seckill.service.SeckillUserService;
import com.xr.seckill.util.MD5Util;
import com.xr.seckill.util.UUIDUtil;
import com.xr.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.List;

@Controller
@RequestMapping("/seckill")
public class SeckillController implements InitializingBean {

    @Autowired
    SeckillUserService seckillUserService;

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @Autowired
    MQSender mqSender;

    /**
     * 系统初始化，将库存读入redis
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if (goodsList == null) {
            return;
        }
        for (GoodsVo goods : goodsList) {
            redisService.set(GoodsKey.getSeckillGoodsStock, "" + goods.getId(), goods.getStockCount());
        }
    }

    @RequestMapping(value = "{path}/do_seckill", method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> seckill(Model model, SeckillUser seckillUser, @RequestParam("goodsId") long goodsId, @PathVariable("path") String path){
        model.addAttribute("seckillUser",seckillUser);
        if(seckillUser == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //验证秒杀path
        boolean check = seckillService.checkSeckillPath(seckillUser, goodsId, path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        //预减库存
        long stock = redisService.decr(GoodsKey.getSeckillGoodsStock,""+goodsId);
        if(stock < 0){
            return Result.error(CodeMsg.SECKILL_OVER);
        }
        //判断重复秒杀
        SeckillOrder seckillOrder = orderService.getSeckillOrderByUserIdGoodsId(seckillUser.getId(), goodsId);
        if(seckillOrder != null){
            return Result.error(CodeMsg.REPEATE_SECKILL);
        }
        //入队
        SeckillMessage msg = new SeckillMessage();
        msg.setUser(seckillUser);
        msg.setGoodsId(goodsId);
        mqSender.sendSeckillMessage(msg);
        return Result.success(0);

//        //判断商品库存
//        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
//        int stock = goodsVo.getGoodsStock();
//        if(stock <= 0){
//            return Result.error(CodeMsg.SECKILL_OVER);
//        }
//        //判断重复秒杀
//        SeckillOrder seckillOrder = orderService.getSeckillOrderByUserIdByGoodsId(seckillUser.getId(), goodsId);
//        if(seckillOrder != null){
//            return Result.error(CodeMsg.REPEATE_SECKILL);
//        }
//        //减库存 下订单 写订单
//        OrderInfo orderInfo = seckillService.seckill(seckillUser, goodsVo);
//        return Result.success(orderInfo);
    }

    /**
     * success: orderId
     * fail: -1
     * queue: 0
     */

    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> getSeckillResult(Model model, SeckillUser seckillUser, @Param("goodsId") long goodsId) {
        model.addAttribute("seckillUser", seckillUser);
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        long orderId = seckillService.getSeckillResult(seckillUser.getId(), goodsId);
        return Result.success(orderId);
    }

    @AccessLimit(seconds=5,maxCount=5,needLogin=true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getSeckillPath(HttpServletRequest request, SeckillUser seckillUser,
                                         @RequestParam("goodsId") long goodsId,
                                         @RequestParam(value = "verifyCode",defaultValue = "0")int verifyCode) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //检测验证码是否正确
        boolean check = seckillService.checkVerifyCode(seckillUser, goodsId, verifyCode);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = seckillService.createSeckillPath(seckillUser, goodsId);
        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getVerifyCode(HttpServletResponse response, SeckillUser seckillUser, @RequestParam("goodsId")long goodsId) {
        if (seckillUser == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        try {
            BufferedImage image = seckillService.createVerifyCode(seckillUser, goodsId);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image,"JPEG",out);
            out.flush();
            out.close();
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return Result.error(CodeMsg.SECKILL_FAIL);
        }
    }
}
