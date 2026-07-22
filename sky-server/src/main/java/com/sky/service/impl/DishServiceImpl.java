package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dishDTO
     */
    @Override
    @Transactional//开启事务
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

    /**
     * 分页查询菜品
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(DishPageQueryDTO dishPageQueryDTO) {
        //分页查询菜品
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        //调用DishMapper分页查询菜品VO
        Page<DishVO> page = dishMapper.page(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Override
    @Transactional//开启事务
    public void delete(List<Long> ids) {
        //当菜品的status为1(起售)时，不能删除
        ids.forEach(id -> {
            Dish dish = dishMapper.selectById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        });

        //当菜品的在套餐中时，不能删除
        List<Long> mealIds = setmealDishMapper.selectMealIdsByDishId(ids);
        if (mealIds != null && mealIds.size() > 0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        //调用DishMapper删除菜品
//        ids.forEach(id -> {
//            dishMapper.delete(id);
//            //删除完菜品时将菜品口味也删除
//            dishFlavorMapper.deleteByDishId(id);
//        });

        //性能优化：批量删除菜品和菜品口味
        //批量删除菜品
        dishMapper.deleteBatch(ids);
        //批量删除菜品口味
        dishFlavorMapper.deleteBatch(ids);


    }

    /**
     * 根据菜品id查询菜品
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        //根据id查询菜品信息
        Dish dish = dishMapper.selectById(id);
        //根据菜品id查询菜品口味
        List<DishFlavor> flavors = dishFlavorMapper.selectByDishId(id);

        //将菜品信息和菜品口味合并到一个VO中
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 修改菜品
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {

        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        //根据id修改菜品基本信息
        dishMapper.updateById(dish);

        //根据id修改菜品口味
        //先删除菜品口味
        dishFlavorMapper.deleteByDishId(dish.getId());
        //根据菜品id修改菜品口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0 ){
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            //调用DishFlavorMapper新增菜品口味(批量新增)
            dishFlavorMapper.insertBatch(flavors);
        }
    }

}
