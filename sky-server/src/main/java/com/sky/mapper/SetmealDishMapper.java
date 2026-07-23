package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品id查询套餐id
     * @param dishIds
     * @return
     */
    public List<Long> selectMealIdsByDishId(List<Long> dishIds);


    /**
     * 批量新增套餐菜品关系
     * @param setmealDishes
     */
    void addSetmealDishBatch(List<SetmealDish> setmealDishes);
}
