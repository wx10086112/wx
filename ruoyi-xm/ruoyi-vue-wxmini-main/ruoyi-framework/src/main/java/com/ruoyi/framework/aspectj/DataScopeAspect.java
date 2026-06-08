package com.ruoyi.framework.aspectj;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.BaseEntity;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.security.context.PermissionContextHolder;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 数据过滤处理
 * 不再向 Mapper 注入原始 SQL 片段，只注入受控参数。
 */
@Aspect
@Component
public class DataScopeAspect
{
    public static final String DATA_SCOPE_ALL = "1";
    public static final String DATA_SCOPE_CUSTOM = "2";
    public static final String DATA_SCOPE_DEPT = "3";
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";
    public static final String DATA_SCOPE_SELF = "5";

    public static final String DATA_SCOPE = "dataScope";
    public static final String DATA_SCOPE_CUSTOM_ROLE_IDS = "dataScopeCustomRoleIds";
    public static final String DATA_SCOPE_DEPT_ID = "dataScopeDeptId";
    public static final String DATA_SCOPE_DEPT_AND_CHILD_ID = "dataScopeDeptAndChildId";
    public static final String DATA_SCOPE_USER_ID = "dataScopeUserId";
    public static final String DATA_SCOPE_DENY_ALL = "dataScopeDenyAll";

    @Before("@annotation(controllerDataScope)")
    public void doBefore(JoinPoint point, DataScope controllerDataScope)
    {
        clearDataScope(point);
        handleDataScope(point, controllerDataScope);
    }

    protected void handleDataScope(final JoinPoint joinPoint, DataScope controllerDataScope)
    {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (StringUtils.isNull(loginUser))
        {
            return;
        }

        SysUser currentUser = loginUser.getUser();
        if (StringUtils.isNotNull(currentUser) && !currentUser.isAdmin())
        {
            String permission = StringUtils.defaultIfEmpty(controllerDataScope.permission(), PermissionContextHolder.getContext());
            dataScopeFilter(joinPoint, currentUser, controllerDataScope.deptAlias(), controllerDataScope.userAlias(), permission);
        }
    }

    public static void dataScopeFilter(JoinPoint joinPoint, SysUser user, String deptAlias, String userAlias, String permission)
    {
        BaseEntity baseEntity = getBaseEntity(joinPoint);
        if (baseEntity == null)
        {
            return;
        }

        boolean hasPermissionScope = false;
        boolean includeDept = false;
        boolean includeDeptAndChild = false;
        boolean includeSelf = false;
        boolean selfWithoutAlias = false;
        Set<Long> customRoleIds = new LinkedHashSet<>();

        for (SysRole role : user.getRoles())
        {
            String dataScope = role.getDataScope();
            if (StringUtils.equals(role.getStatus(), UserConstants.ROLE_DISABLE))
            {
                continue;
            }
            if (!StringUtils.containsAny(role.getPermissions(), Convert.toStrArray(permission)))
            {
                continue;
            }

            hasPermissionScope = true;

            if (DATA_SCOPE_ALL.equals(dataScope))
            {
                return;
            }
            if (DATA_SCOPE_CUSTOM.equals(dataScope))
            {
                customRoleIds.add(role.getRoleId());
            }
            else if (DATA_SCOPE_DEPT.equals(dataScope))
            {
                checkAlias(deptAlias);
                includeDept = true;
            }
            else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope))
            {
                checkAlias(deptAlias);
                includeDeptAndChild = true;
            }
            else if (DATA_SCOPE_SELF.equals(dataScope))
            {
                if (StringUtils.isNotBlank(userAlias))
                {
                    checkAlias(userAlias);
                    includeSelf = true;
                }
                else
                {
                    selfWithoutAlias = true;
                }
            }
        }

        Map<String, Object> params = baseEntity.getParams();
        if (!customRoleIds.isEmpty())
        {
            checkAlias(deptAlias);
            params.put(DATA_SCOPE_CUSTOM_ROLE_IDS, new ArrayList<>(customRoleIds));
        }
        if (includeDept)
        {
            params.put(DATA_SCOPE_DEPT_ID, user.getDeptId());
        }
        if (includeDeptAndChild)
        {
            params.put(DATA_SCOPE_DEPT_AND_CHILD_ID, user.getDeptId());
        }
        if (includeSelf)
        {
            params.put(DATA_SCOPE_USER_ID, user.getUserId());
        }

        boolean hasAnyStructuredScope = !customRoleIds.isEmpty() || includeDept || includeDeptAndChild || includeSelf;
        if (!hasPermissionScope || (!hasAnyStructuredScope && selfWithoutAlias))
        {
            params.put(DATA_SCOPE_DENY_ALL, Boolean.TRUE);
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
        params.remove(DATA_SCOPE);
        params.remove(DATA_SCOPE_CUSTOM_ROLE_IDS);
        params.remove(DATA_SCOPE_DEPT_ID);
        params.remove(DATA_SCOPE_DEPT_AND_CHILD_ID);
        params.remove(DATA_SCOPE_USER_ID);
        params.remove(DATA_SCOPE_DENY_ALL);
    }

    private static BaseEntity getBaseEntity(JoinPoint joinPoint)
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

    private static void checkAlias(String alias)
    {
        if (StringUtils.isBlank(alias) || !alias.matches("^[a-zA-Z0-9_]+$"))
        {
            throw new IllegalArgumentException("Invalid data scope alias");
        }
    }
}
