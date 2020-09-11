package com.xr.seckill.redis;

public class SeckillUserKey extends BasePrefix {

    public static final int TOKEN_ECPIRE = 3600 * 24 * 3;
    public SeckillUserKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static SeckillUserKey token = new SeckillUserKey(TOKEN_ECPIRE, "tk");
    public static SeckillUserKey getById = new SeckillUserKey(0, "id");
}
