package cn.tealc.ntemaid.service.taygedo;

import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.taygedo.DeviceIdentity;
import cn.tealc.taygedo.TaygedoApi;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.BindRoleInfo;
import cn.tealc.taygedo.model.LoginResult;
import cn.tealc.taygedo.model.UserCenterLoginResult;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 塔吉多业务服务实现
 * 每个账号独立持有 deviceId，新增账号自动生成，已有账号沿用原有 deviceId。
 * 账号持久化委托给 TaygedoAccountService。
 */
public class TaygedoLoginService {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoLoginService.class);
    private static final DateTimeFormatter SHANGHAI_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_GAME_ID = "1289";

    private final TaygedoApi api;
    private final TaygedoAccountService accountService;

    @Inject
    public TaygedoLoginService(TaygedoApi api, TaygedoAccountService accountService) {
        this.api = api;
        this.accountService = accountService;
    }

    // ==================== 账号持久化（委托给 TaygedoAccountService） ====================

    public List<TaygedoAccount> getAllAccount() {
        return accountService.getAll();
    }

    public TaygedoAccount loadAccount() {
        return accountService.getFirst().orElse(null);
    }

    public void saveAccount(TaygedoAccount account) {
        accountService.save(account);
    }

    public boolean deleteAccount(String phone) {
        return accountService.delete(phone);
    }

    // ==================== 分步登录流程 ====================

    public void sendCaptcha(String phone, TaygedoAccount account) throws TaygedoException {
        api.sendCaptcha(phone, ensureDeviceId(account));
    }

    public LoginResult loginWithCaptcha(String phone, String captcha, TaygedoAccount account) throws TaygedoException {
        return api.loginWithCaptcha(phone, captcha, ensureDeviceId(account));
    }

    public UserCenterLoginResult userCenterLogin(String token, String userId, TaygedoAccount account) throws TaygedoException {
        return api.userCenterLogin(token, userId, ensureDeviceId(account));
    }

    // ==================== Token 刷新 ====================

    public void refreshToken(TaygedoAccount account) throws TaygedoException {
        String deviceId = ensureDeviceId(account);
        var result = api.refreshToken(account.getRefreshToken(), deviceId);

        account.setAccessToken(result.getAccessToken());
        account.setRefreshToken(result.getRefreshToken());
        account.setTokenUpdatedAt(
                ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).format(SHANGHAI_FORMAT));
        accountService.save(account);
    }

    // ==================== 一键登录 ====================

    public TaygedoAccount login(String phone, String captcha) throws TaygedoException {
        TaygedoAccount account = new TaygedoAccount();
        account.setPhone(phone);
        return login(phone, captcha, account);
    }

    public TaygedoAccount login(String phone, String captcha, TaygedoAccount account) throws TaygedoException {
        ensureDeviceId(account);
        LoginResult laohuResult = api.loginWithCaptcha(phone, captcha, account.getDeviceId());
        return completeLogin(account, laohuResult);
    }

    public TaygedoAccount loginWithPassword(String phone, String password) throws TaygedoException {
        TaygedoAccount account = new TaygedoAccount();
        account.setPhone(phone);
        return loginWithPassword(phone, password, account);
    }

    public TaygedoAccount loginWithPassword(String phone, String password, TaygedoAccount account) throws TaygedoException {
        ensureDeviceId(account);
        LoginResult laohuResult = api.loginWithPassword(phone, password, account.getDeviceId());
        return completeLogin(account, laohuResult);
    }

    private TaygedoAccount completeLogin(TaygedoAccount account, LoginResult laohuResult) throws TaygedoException {
        UserCenterLoginResult tajiduoResult = api.userCenterLogin(
                laohuResult.getToken(), laohuResult.getUserId(), account.getDeviceId());

        account.setName(laohuResult.getNickname());
        account.setAccessToken(tajiduoResult.getAccessToken());
        account.setRefreshToken(tajiduoResult.getRefreshToken());
        account.setUid(tajiduoResult.getUid());
        account.setTokenUpdatedAt(
                ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).format(SHANGHAI_FORMAT));

        try {
            BindRoleInfo bindRole = api.getBindRole(
                    account.getAccessToken(), account.getUid(), DEFAULT_GAME_ID);
            account.setRoleId(bindRole.getRoleId());
            account.setRoleName(bindRole.getRoleName());
            account.setServerId(bindRole.getServerId());
            account.setServerName(bindRole.getServerName());
            account.setGameId(bindRole.getGameId());
            account.setGender(bindRole.getGender());
            LOG.info("绑定角色获取成功: roleId={}, roleName={}, serverName={}",
                    bindRole.getRoleId(), bindRole.getRoleName(), bindRole.getServerName());
        } catch (TaygedoException e) {
            LOG.warn("获取绑定角色失败（登录继续）: {}", e.getMessage());
        }

        LOG.info("登录成功: phone={}, uid={}, deviceId={}", account.getPhone(), account.getUid(), account.getDeviceId());
        return account;
    }

    // ==================== 内部工具 ====================

    /**
     * 确保账号有 deviceId：有则复用，无则生成并设置到 account 上
     */
    private String ensureDeviceId(TaygedoAccount account) {
        if (account.getDeviceId() == null || account.getDeviceId().isBlank()) {
            DeviceIdentity device = DeviceIdentity.generate();
            account.setDeviceId(device.getDeviceId());
            LOG.info("为账号 {} 生成新 deviceId: {}", account.getPhone(), device.getDeviceId());
        }
        return account.getDeviceId();
    }
}
