package com.ruoyi.framework.aspectj;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;

/**
 * 业务数据范围过滤处理（按分销商/商家隔离）
 * 支持超级管理员切换分销商视角
 */
@Aspect
@Component
public class DataScopeBizAspect
{
    public static final String DATA_SCOPE_BIZ = "dataScopeBiz";

    @Before("@annotation(dataScopeBiz)")
    public void doBefore(JoinPoint point, DataScopeBiz dataScopeBiz) throws Throwable
    {
        clearDataScope(point);
        handleDataScope(point, dataScopeBiz);
    }

    protected void handleDataScope(final JoinPoint joinPoint, DataScopeBiz dataScopeBiz)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser))
        {
            return;
        }

        StringBuilder sqlString = new StringBuilder();

        if (SecurityUtils.isAdmin(loginUser.getUserId()))
        {
            // 超级管理员：平台视角不限制，分销商视角需过滤
            if ("DISTRIBUTOR".equals(loginUser.getActiveViewType())
                    && loginUser.getActiveDistributorId() != null
                    && StringUtils.isNotBlank(dataScopeBiz.distributorAlias()))
            {
                sqlString.append(StringUtils.format(" AND {}.distributor_id = {} ",
                        checkAlias(dataScopeBiz.distributorAlias()), loginUser.getActiveDistributorId()));
            }
            else
            {
                // 平台视角：不限制
                return;
            }
        }
        else
        {
            // 非超级管理员：根据有效范围过滤
            Long effectiveDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
            Long effectiveMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();

            if (effectiveDistributorId != null && StringUtils.isNotBlank(dataScopeBiz.distributorAlias()))
            {
                sqlString.append(StringUtils.format(" AND {}.distributor_id = {} ",
                        checkAlias(dataScopeBiz.distributorAlias()), effectiveDistributorId));
            }
            else if (effectiveMerchantId != null && StringUtils.isNotBlank(dataScopeBiz.merchantAlias()))
            {
                sqlString.append(StringUtils.format(" AND {}.merchant_id = {} ",
                        checkAlias(dataScopeBiz.merchantAlias()), effectiveMerchantId));
            }
        }

        if (StringUtils.isNotBlank(sqlString.toString()))
        {
            Object params = joinPoint.getArgs()[0];
            if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
            {
                BaseEntity baseEntity = (BaseEntity) params;
                baseEntity.getParams().put(DATA_SCOPE_BIZ, sqlString.toString());
            }
        }
    }

    private void clearDataScope(final JoinPoint joinPoint)
    {
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
        {
            BaseEntity baseEntity = (BaseEntity) params;
            baseEntity.getParams().put(DATA_SCOPE_BIZ, "");
        }
    }

    private String checkAlias(String alias) {
        if (StringUtils.isNotBlank(alias) && !alias.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("Invalid data scope alias");
        }
        return alias;
    }
}
