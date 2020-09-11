package com.xr.seckill.service;

import com.xr.seckill.dao.OrderDao;
import com.xr.seckill.domain.OrderInfo;
import com.xr.seckill.domain.SeckillOrder;
import com.xr.seckill.domain.SeckillUser;
import com.xr.seckill.redis.OrderKey;
import com.xr.seckill.redis.RedisService;
import com.xr.seckill.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public SeckillOrder getSeckillOrderByUserIdGoodsId(Long userId, long goodsId){
//        return orderDao.getSeckillOrderByUserIdByGoodsId(userId, goodsId);
        return redisService.get(OrderKey.getSeckillOrderByUidGid, ""+userId+""+goodsId,SeckillOrder.class);
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    @Transactional
    public OrderInfo createOrder(SeckillUser user, GoodsVo goodsVo) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goodsVo.getId());
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setGoodsPrice(goodsVo.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo);

        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setGoodsId(goodsVo.getId());
        seckillOrder.setOrderId(orderInfo.getId());
        seckillOrder.setUserId(user.getId());
        orderDao.insertSeckillOrder(seckillOrder);

        redisService.set(OrderKey.getSeckillOrderByUidGid, ""+user.getId()+""+goodsVo.getId(),seckillOrder);

        return  orderInfo;
    }
}
