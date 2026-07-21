package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import org.aspectj.lang.reflect.MethodSignature;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.scheduling.quartz.LocalDataSourceJobStore;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义自动填充切面：实现公共字段自动填充处理逻辑
 */
@Slf4j
@Component
@Aspect// 标识这是一个切面类
public class AutoFillAspect {

    /**
     * 定义切入点
     */
    // 定义一个切入点，用于匹配所有Mapper接口的所有类和类中的方法&&带有AutoFill注解的方法
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void AutoFillPointCut() {}

    /**
     * 定义通知
     */
    @Before("AutoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("自动填充公共字段");

        // 从注解中获取数据操作类型（INSERT/UPDATE）
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();// 获取当前方法的签名对象
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);// 获取当前方法的AutoFill注解对象
        OperationType operationType = autoFill.value();// 获取注解中的数据操作操作类型（INSERT/UPDATE）

        // 获取注解注释的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();// 获取当前方法的参数数组
        if(args.length == 0 || args == null){
            return;
        }
        Object entity = args[0];// 获取第一个参数，即实体对象

        // 定义要填充的字段基本对象（create_time, update_time, create_user, update_user）的对应值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();


        // 根据前面的数据操作类型，判断是插入还是更新操作，根据操作类型进行不同的自动填充处理
        if (operationType == OperationType.INSERT) {
            // 插入操作，填充create_time, update_time, create_user, update_user字段
            try {
                // 反射获取实体对象的方法
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 调用方法，填充字段值
                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType == OperationType.UPDATE) {
            // 更新操作，填充update_time, update_user字段
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                // 调用方法，填充字段值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }


    }


}
