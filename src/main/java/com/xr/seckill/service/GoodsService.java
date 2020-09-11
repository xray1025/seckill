package com.xr.seckill.service;

import com.xr.seckill.dao.GoodsDao;
import com.xr.seckill.domain.SeckillGoods;
import com.xr.seckill.vo.GoodsVo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    public static final int DEFAULT_MAX_RETRIES = 5;

    public List<GoodsVo> listGoodsVo() {
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoByGoodsId(goodsId);
    }

    public boolean reduceStock(GoodsVo goodsVo) {
        int numAttempts = 0;
        int ret = 0;
        SeckillGoods sg = new SeckillGoods();
        sg.setGoodsId(goodsVo.getId());
        sg.setVersion(goodsVo.getVersion());
        while (numAttempts < DEFAULT_MAX_RETRIES){
            numAttempts++;
            try {
                sg.setVersion(goodsDao.getVersionByGoodsId(goodsVo.getId()));
                ret = goodsDao.reduceStock(sg);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (ret != 0)
                break;
        }

        return ret > 0;
    }
}
