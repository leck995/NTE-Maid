package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.taygedo.TaygedoApi;

import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.SigninReward;
import cn.tealc.taygedo.model.SigninState;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 塔吉多签到业务服务
 * 封装签到相关的 TaygedoApi 调用，统一处理异常和日志
 */
public class TaygedoSignInService {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoSignInService.class);
    private static final String DEFAULT_GAME_ID = "1289";

    private final TaygedoApi api;

    @Inject
    public TaygedoSignInService(TaygedoApi api) {
        this.api = api;
    }

    public List<SigninReward> getSigninRewards(TaygedoAccount account) {
        try {
            return api.getSigninRewards(account.getAccessToken(), DEFAULT_GAME_ID);
        } catch (TaygedoException e) {
            return Collections.emptyList();
        }
    }

    public SigninState getSigninState(TaygedoAccount account) throws TaygedoException  {
        return api.getSigninState(account.getAccessToken(), DEFAULT_GAME_ID);
    }

    public void gameSignin(TaygedoAccount account) throws TaygedoException {
        api.gameSignin(account.getAccessToken(), account.getRoleId(), DEFAULT_GAME_ID);
    }
}
