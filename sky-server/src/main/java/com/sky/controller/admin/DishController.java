package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品管理
 */
@Slf4j
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品管理")
public class DishController {

   @Autowired
   private DishService dishService;

   /**
    * 新增菜品
    * @param dishDTO
    * @return
    */
   @PostMapping
   @ApiOperation(value = "新增菜品")
   public Result save(@RequestBody DishDTO dishDTO) {

      log.info("新增菜品：{}", dishDTO);
      //调用服务层新增菜品
      dishService.savewithFlavor(dishDTO);
      return Result.success();

   }

   /**
    * 分页查询菜品
    * @param dishPageQueryDTO
    * @return
    */
   @ApiOperation(value = "分页查询菜品")
   @GetMapping("/page")
   public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
      log.info("分页查询菜品：{}", dishPageQueryDTO);
      //调用服务层分页查询菜品
      PageResult pageResult = dishService.page(dishPageQueryDTO);
      return Result.success(pageResult);
   }

   /**
    * 删除菜品
    * @param ids
    * @return
    */
   @ApiOperation(value = "删除菜品")
   @DeleteMapping
   public Result delete(@RequestParam List<Long> ids){
      log.info("删除菜品：{}", ids);
      //调用服务层删除菜品
      dishService.delete(ids);
      return Result.success();
   }

   /**
    * 根据菜品id查询菜品
    * @param id
    * @return
    */
   @ApiOperation(value = "根据菜品id查询菜品")
   @GetMapping("/{id}")
   public Result<DishVO> getById(@PathVariable Long id){
      log.info("根据菜品id查询菜品：{}", id);
      //调用服务层根据菜品id查询菜品
      DishVO dishVO = dishService.getById(id);
      return Result.success(dishVO);
   }

   /**
    * 修改菜品
    * @param dishDTO
    * @return
    */
   @ApiOperation(value = "修改菜品")
   @PutMapping
   public Result updateWithFlavor(@RequestBody DishDTO dishDTO){
      log.info("修改菜品：{}", dishDTO);
      //调用服务层修改菜品
      dishService.updateWithFlavor(dishDTO);
      return Result.success();
   }

}
