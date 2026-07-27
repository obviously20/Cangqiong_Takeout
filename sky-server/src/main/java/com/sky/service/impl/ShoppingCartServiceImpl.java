package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        // 新增购物车逻辑
        // 先将购物车DTO转换为购物车实体
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        // 设置用户id,确保购物车与当前用户关联
        shoppingCart.setUserId(BaseContext.getCurrentId());

        // 查看购物车的菜品/套餐是否存在
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);

        // 如果存在,则数量+1
        if(list.size()>0 && list != null){
            // 更新数量
            list.get(0).setNumber(list.get(0).getNumber()+1);
            shoppingCartMapper.updateNumberById(list.get(0));
        }else{
            // 如果不存在,则新增购物车

            //判断是菜品还是套餐
            Long dishId = shoppingCart.getDishId();
            if(dishId != null){
                //是菜品
                Dish dish = dishMapper.selectById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            }else{
                //是套餐
                Setmeal setmeal = setmealMapper.selectById(shoppingCart.getSetmealId());
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            // 新增购物车
            shoppingCartMapper.insert(shoppingCart);

        }

    }

    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart().builder().userId(userId).build();
        return shoppingCartMapper.list(shoppingCart);
    }
}
