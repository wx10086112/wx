package com.ruoyi.framework.aspectj;

import com.ruoyi.common.annotation.DataScopeBiz;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.MallDataScopeHelper;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 业务数据范围过滤处理（按分销商/商家隔离）
 * 不再向 Mapper 注入原始 SQL 片段，只注入受控参数。
 */
@Aspect
@Component
public class DataScopeBizAspect
{
    public static final String DATA_SCOPE_BIZ = "dataScopeBiz";
    public static final String DATA_SCOPE_BIZ_DISTRIBUTOR_ID = "dataScopeBizDistributorId";
    public static final String DATA_SCOPE_BIZ_MERCHANT_ID = "dataScopeBizMerchantId";

    @Before("@annotation(dataScopeBiz)")
    public void doBefore(JoinPoint point, DataScopeBiz dataScopeBiz)
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

        BaseEntity baseEntity = getBaseEntity(joinPoint);
        if (baseEntity == null)
        {
            return;
        }

        Map<String, Object> params = baseEntity.getParams();
        validateAliases(dataScopeBiz);

        if (SecurityUtils.isAdmin(loginUser.getUserId()))
        {
            if ("DISTRIBUTOR".equals(loginUser.getActiveViewType())
                    && loginUser.getActiveDistributorId() != null)
            {
                params.put(DATA_SCOPE_BIZ_DISTRIBUTOR_ID, loginUser.getActiveDistributorId());
            }
            return;
        }

        Long effectiveDistributorId = MallDataScopeHelper.currentEffectiveDistributorId();
        Long effectiveMerchantId = MallDataScopeHelper.currentEffectiveMerchantId();

        if (effectiveDistributorId != null)
        {
            params.put(DATA_SCOPE_BIZ_DISTRIBUTOR_ID, effectiveDistributorId);
        }
        if (effectiveMerchantId != null)
        {
            params.put(DATA_SCOPE_BIZ_MERCHANT_ID, effectiveMerchantId);
        }
    }

    private void clearDataScope(final JoinPoint joinPoint)
    {
        BaseEntity baseEntity = getBaseEntity(joinPoint);
        if (baseEntity == null)
        {
            return;
        }

        Map<String, Object> params = baseEntity.getParams();
        params.remove(DATA_SCOPE_BIZ);
        params.remove(DATA_SCOPE_BIZ_DISTRIBUTOR_ID);
        params.remove(DATA_SCOPE_BIZ_MERCHANT_ID);
    }

    private BaseEntity getBaseEntity(JoinPoint joinPoint)
    {
        if (joinPoint.getArgs() == null || joinPoint.getArgs().length == 0)
        {
            return null;
        }
        Object params = joinPoint.getArgs()[0];
        if (StringUtils.isNotNull(params) && params instanceof BaseEntity)
        {
            return (BaseEntity) params;
        }
        return null;
    }

    private void validateAliases(DataScopeBiz dataScopeBiz)
    {
        checkAlias(dataScopeBiz.distributorAlias());
        checkAlias(dataScopeBiz.merchantAlias());
    }

    private void checkAlias(String alias)
    {
        if (StringUtils.isNotBlank(alias) && !alias.matches("^[a-zA-Z0-9_]+$"))
        {
            throw new IllegalArgumentException("Invalid data scope alias");
        }
    }
}
