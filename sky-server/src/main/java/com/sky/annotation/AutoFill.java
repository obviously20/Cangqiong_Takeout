package com.sky.annotation;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用于标识某个方法需要进行功能字段自动填充处理
 */

// 用于方法上
@Target(ElementType.METHOD)
// 运行时注解，需要在运行时获取注解信息（大白话：在运行时可以通过反射获取到注解信息）
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoFill {
    // 数据操作类型，用于判断是插入还是更新操作（INSERT/UPDATE）
    // 用于判断是插入还是更新操作，根据操作类型进行不同的自动填充处理
    OperationType value();
}
