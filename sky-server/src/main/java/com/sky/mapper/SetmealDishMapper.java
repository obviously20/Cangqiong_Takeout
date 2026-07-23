package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

    /**
     * 根据套餐id删除套餐菜品关系
     * @param ids
     */
    void deleteSetmealDishByIds(List<Long> ids);

    /**
     * 根据套餐id查询套餐包含的菜品关系
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> selectBySetmealId(Long id);
}
