package com.ruoyi.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业务数据范围过滤注解（按分销商/商家隔离）
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScopeBiz
{
    /**
     * 分销商表的别名
     */
    public String distributorAlias() default "";

    /**
     * 商家表的别名
     */
    public String merchantAlias() default "";
}
