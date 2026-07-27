package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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
   @Autowired
   private RedisTemplate redisTemplate;

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

      //新增完菜品后清除缓存，以防数据库和redis缓存数据信息不匹配
      String key = "dish_" + dishDTO.getCategoryId();
      cleanCache(key);

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

      //清除缓存
      cleanCache("dish_*");

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

      //清除缓存
      cleanCache("dish_*");

      return Result.success();
   }

   /**
    * 启用/停用菜品
    * @param status
    * @param id
    * @return
    */
   @ApiOperation(value = "启用/停用菜品")
   @PostMapping("/status/{status}")
   public Result startOrStop(@PathVariable Integer status,Long id){
      log.info("启用/停用菜品id:{},状态:{}", id, status);
      dishService.statusStartOrStop(id,status);

      //清除缓存
      cleanCache("dish_*");

      return Result.success();
   }

   /**
    * 根据分类id查询菜品
    * @param categoryId
    * @return
    */
   @ApiOperation(value = "根据分类id查询菜品")
   @GetMapping("list")
   public Result<List<Dish>> list(Long categoryId){
      log.info("根据分类id查询菜品：{}", categoryId);
      //调用服务层根据分类id查询菜品
      List<Dish> dishList = dishService.listByCategoryId(categoryId);
      return Result.success(dishList);
   }

   /**
    * 清楚缓存
    * @param pattern
    */
   private void cleanCache(String pattern){
      Set keys = redisTemplate.keys(pattern);
      redisTemplate.delete(keys);
   }

}
