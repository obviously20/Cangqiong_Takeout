package com.sky.service.impl;

import com.sky.dto.SetmealDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    public void addSetmeal(SetmealDTO setmealDTO) {
        //将DTO对象转换为实体对象
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //新增套餐基本信息
        setmealMapper.addSetmeal(setmeal);
        //新增套餐包含的菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //安全检查，检查该套餐是否包含菜品关系
        if(setmealDishes !=null && setmealDishes.size()>0){
            //那就将新增的套餐id赋值给每个菜品关系
            setmealDishes.forEach(sd -> {
                sd.setSetmealId(setmeal.getId());
            });
            //新增该套餐id下的菜品关系记录(批量新增)
            setmealDishMapper.addSetmealDishBatch(setmealDishes);
        }
    }


}
