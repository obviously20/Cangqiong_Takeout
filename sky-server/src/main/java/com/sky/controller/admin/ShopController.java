package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺管理")
@Slf4j
public class ShopController {

    private final String SHOP_STATUS_KEY = "ShopStatus";

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 设置店铺状态
     * @param status
     * @return
     */
    @PutMapping("/{status}")
    @ApiOperation("设置店铺状态")
    public Result setShopStatus(@PathVariable Integer status){
        log.info("设置店铺状态为：{}", status==1?"营业中":"休息中");
        //将店铺状态写入Redis
        redisTemplate.opsForValue().set(SHOP_STATUS_KEY, status);
        return Result.success();
    }


    /**
     * 获取店铺状态
     * @return
     */
    @GetMapping("/status")
    @ApiOperation("获取店铺状态")
    public Result getShopStatus(){
        Integer status = (Integer) redisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        log.info("店铺状态为：{}", status==1?"营业中":"休息中");
        return Result.success(status);
    }


}
