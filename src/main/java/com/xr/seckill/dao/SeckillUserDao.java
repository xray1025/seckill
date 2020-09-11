package com.xr.seckill.dao;

import com.xr.seckill.domain.SeckillUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillUserDao {

    @Select("select * from sk_user where id = #{id}")
    public SeckillUser getById(@Param("id")long id);

    @Update("update sk_user set password = #{password} where id = #{id}")
    public void update(SeckillUser toBeUpdate);
}
