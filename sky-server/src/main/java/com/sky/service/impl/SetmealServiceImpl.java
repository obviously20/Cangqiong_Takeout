package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
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
    @Transactional
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

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult page(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.page(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {

        //先判断：起售中的套餐不能删除
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.selectStatusById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        //先删除套餐
        setmealMapper.deleteSetmealByIds(ids);
        //再删除套餐包含的菜品关系
        setmealDishMapper.deleteSetmealDishByIds(ids);

    }

    /**
     * 根据套餐id查询套餐详情
     * @param id
     * @return
     */
    @Override
    @Transactional
    public SetmealVO getById(Long id) {
        //先根据套餐的id参数查询套餐的基本信息
        Setmeal setmeal = setmealMapper.selectById(id);
        //根据套餐的id参数查询套餐包含的菜品关系
        List<SetmealDish> setmealDishes = setmealDishMapper.selectBySetmealId(id);
        //再将查到的封装成VO对象返回
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    /**
     * 修改套餐
     * @param sdo
     */
    @Override
    @Transactional
    public void update(SetmealDTO sdo) {
        //将DTO对象转换为实体对象
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(sdo, setmeal);

        //先根据套餐的id参数查询套餐的基本信息
        setmealMapper.updateById(setmeal);

        //再修改套餐中的菜品关系（先删除再新增）
        //先获取sdo中的套餐菜品关系
        List<SetmealDish> setmealDishes = sdo.getSetmealDishes();
        if(setmealDishes !=null && setmealDishes.size()>0){
            setmealDishes.forEach(sd -> {
                //将新增的套餐id赋值给每个菜品关系
                sd.setSetmealId(setmeal.getId());
            });
        }
        //将setmeal中的强转为List<Long>类型
        List<Long> setmealIds = Collections.singletonList(setmeal.getId());
        //删除该套餐id下的菜品关系记录(批量删除)
        setmealDishMapper.deleteSetmealDishByIds(setmealIds);
        //新增该套餐id下的菜品关系记录(批量新增)
        setmealDishMapper.addSetmealDishBatch(setmealDishes);


    }


}
