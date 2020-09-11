package com.xr.seckill.service;

import com.xr.seckill.domain.OrderInfo;
import com.xr.seckill.domain.SeckillOrder;
import com.xr.seckill.domain.SeckillUser;
import com.xr.seckill.redis.RedisService;
import com.xr.seckill.redis.SeckillKey;
import com.xr.seckill.util.MD5Util;
import com.xr.seckill.util.UUIDUtil;
import com.xr.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class SeckillService {

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Transactional//减库存 下订单 写订单
    public OrderInfo seckill(SeckillUser user, GoodsVo goodsVo){
        boolean success = goodsService.reduceStock(goodsVo);
        if(success){
            return orderService.createOrder(user,goodsVo);
        }else{
            setGoodsOver(goodsVo.getId());
            return null;
        }
    }

    public long getSeckillResult(Long userId, long goodsId){
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(userId, goodsId);
        if(order != null){
            return order.getOrderId();
        }else{
            boolean isOver = getGoodsOver(goodsId);
            if(isOver){
                return -1;
            }else {
                return 0;
            }
        }
    }


    private void setGoodsOver(long goodsId) {
        redisService.set(SeckillKey.isGoodsOver, ""+goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisService.exists(SeckillKey.isGoodsOver,""+goodsId);
    }

    public boolean checkSeckillPath(SeckillUser seckillUser, long goodsId, String path) {
        if(seckillUser == null || path == null){
            return false;
        }
        String pathOld = redisService.get(SeckillKey.getSeckillPath, ""+seckillUser.getId()+"_"+goodsId, String.class);
        return path.equals(pathOld);
    }

    public String createSeckillPath(SeckillUser seckillUser, long goodsId) {
        if(seckillUser == null || goodsId <= 0){
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid()+"8239172338");
        redisService.set(SeckillKey.getSeckillPath, ""+seckillUser.getId()+"_"+goodsId, str);
        return str;
    }

    public BufferedImage createVerifyCode(SeckillUser seckillUser, long goodsId) {
        if(seckillUser == null || goodsId <= 0){
            return null;
        }

        int width = 80;
        int height = 32;

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics g = image.getGraphics();

        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0,0,width-1,height-1);

        g.setColor(Color.BLACK);
        g.drawRect(0,0,width-1,height-1);

        Random rdm = new Random();

        for(int i = 0; i < 50; i++){
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x,y,0,0);
        }

        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0,100,0));
        g.setFont(new Font("Candara",Font.BOLD,24));
        g.drawString(verifyCode,8,24);
        g.dispose();

        int md = calc(verifyCode);
        System.out.println(md);
        redisService.set(SeckillKey.getVerifyCode,seckillUser.getId()+","+goodsId,md);

        return image;
    }

    public boolean checkVerifyCode(SeckillUser seckillUser, long goodsId, int verifyCode) {
        if(seckillUser == null || goodsId <= 0){
            return false;
        }
        Integer codeOld = redisService.get(SeckillKey.getVerifyCode,seckillUser.getId()+","+goodsId,Integer.class);
        if(codeOld == null || codeOld - verifyCode != 0){
            return false;
        }
        redisService.delete(SeckillKey.getVerifyCode,seckillUser.getId()+","+goodsId);
        return true;
    }

    private static char[] ops = new char[]{'+','-','*'};

    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private static int calc(String exp) {
        try{
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer) engine.eval(exp);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }
}
