package com.xr.seckill.rabbitMQ;

import com.xr.seckill.domain.SeckillUser;

public class SeckillMessage {

    private SeckillUser user;
    private long goodsId;

    public SeckillUser getUser() {
        return user;
    }

    public void setUser(SeckillUser user) {
        this.user = user;
    }

    public long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(long goodsId) {
        this.goodsId = goodsId;
    }
}
