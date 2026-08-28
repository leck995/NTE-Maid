package cn.tealc.ntemaid.service.taygedo;

import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.taygedo.TaygedoApi;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.RoleHome;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 塔吉多角色面板业务服务
 * 封装 TaygedoApi 的角色详情接口，供 UI 层调用
 */
public class TaygedoRoleService {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoRoleService.class);

    private final TaygedoApi api;

    @Inject
    public TaygedoRoleService(TaygedoApi api) {
        this.api = api;
    }

    /**
     * 获取角色综合面板数据
     *
     * @param account 已登录的塔吉多账号（需含 accessToken 和 roleId）
     * @return 角色综合面板（头像/等级/成就总览/区域总览/房产/载具/角色简版）
     * @throws TaygedoException 请求失败或令牌过期时抛出
     */
    public RoleHome getRoleHome(TaygedoAccount account) throws TaygedoException {
        if (account == null || account.getAccessToken() == null) {
            throw new TaygedoException("账号未登录，无有效令牌");
        }
        if (account.getRoleId() == null || account.getRoleId().isBlank()) {
            throw new TaygedoException("账号未绑定角色");
        }
        return api.getRoleHome(account.getAccessToken(), account.getRoleId());
    }
}
