package com.xr.seckill.rabbitMQ;

import com.xr.seckill.domain.SeckillOrder;
import com.xr.seckill.domain.SeckillUser;
import com.xr.seckill.redis.RedisService;
import com.xr.seckill.service.GoodsService;
import com.xr.seckill.service.OrderService;
import com.xr.seckill.service.SeckillService;
import com.xr.seckill.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    SeckillService seckillService;

    @RabbitListener(queues=MQConfig.SECKILL_QUEUE)
    public void receive(String message){
        log.info("receive message:"+message);
        SeckillMessage m = RedisService.stringToBean(message, SeckillMessage.class);
        SeckillUser user = m.getUser();
        long goodsId = m.getGoodsId();

        GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
        int stock = goodsVo.getStockCount();
        if(stock <= 0){
            return;
        }

        //判断重复秒杀
        SeckillOrder order = orderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return;
        }

        //减库存 下订单 写入秒杀订单
        seckillService.seckill(user, goodsVo);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)
    public void receiveTopic1(String message) {
        log.info(" topic  queue1 message:" + message);
    }

    @RabbitListener(queues = MQConfig.TOPIC_QUEUE2)
    public void receiveTopic2(String message) {
        log.info(" topic  queue2 message:" + message);
    }
}
