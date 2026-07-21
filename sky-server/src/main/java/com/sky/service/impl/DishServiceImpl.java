package com.sky.service.impl;

import com.sky.dto.DishDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    public void savewithFlavor(DishDTO dishDTO) {
        //将菜品DTO转换为菜品实体
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //调用DishMapper新增菜品
        dishMapper.insert(dish);

        //获取新增菜品的id
        Long dishId = dish.getId();

        //如果菜品口味不为空，调用DishFlavorMapper新增菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0 ){
            //先将菜品id赋值给每个口味实体
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            //调用DishFlavorMapper新增菜品口味(批量新增)
            dishFlavorMapper.insertBatch(flavors);
        }


    }

}
