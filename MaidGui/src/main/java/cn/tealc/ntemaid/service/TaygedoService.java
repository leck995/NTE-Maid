package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.dao.TaygedoAccountDao;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.taygedo.DeviceIdentity;
import cn.tealc.taygedo.TaygedoApi;
import cn.tealc.taygedo.TaygedoException;
import cn.tealc.taygedo.model.BindRoleInfo;
import cn.tealc.taygedo.model.LoginResult;
import cn.tealc.taygedo.model.UserCenterLoginResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 塔吉多业务服务实现
 * 每个账号独立持有 deviceId，新增账号自动生成，已有账号沿用原有 deviceId。
 * 账号数据通过 TaygedoAccountDao 持久化到 SQLite 数据库。
 */
public class TaygedoService{
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoService.class);
    private static final DateTimeFormatter SHANGHAI_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_GAME_ID = "1289";

    private final TaygedoApi api;
    private final TaygedoAccountDao accountDao;

    public TaygedoService() {
        this.api = new TaygedoApi();
        this.accountDao = new TaygedoAccountDao();
    }


    public boolean deleteAccountByPhone(String phone) {
        try {
            return accountDao.delete(phone);
        } catch (Exception e) {
            LOG.error("删除账号 {} 失败", phone, e);
            return false;
        }
    }

    // ==================== 账号持久化 ====================
    public List<TaygedoAccount> getAllAccount() {
        try {
            return accountDao.findAll();
        } catch (Exception e) {
            LOG.error("查询所有账号失败", e);
            return List.of();
        }
    }

    public TaygedoAccount loadAccount() {
        try {
            return accountDao.findFirst().orElse(null);
        } catch (Exception e) {
            LOG.error("加载账号失败", e);
            return null;
        }
    }

    public void saveAccount(TaygedoAccount account) {
        try {
            accountDao.saveOrUpdate(account);
        } catch (Exception e) {
            LOG.error("保存账号 {} 失败", account.getPhone(), e);
        }
    }

    public boolean deleteAccount(String phone) {
        try {
            return accountDao.delete(phone);
        } catch (Exception e) {
            LOG.error("删除账号 {} 失败", phone, e);
            return false;
        }
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

    public UserCenterLoginResult refreshToken(TaygedoAccount account) throws TaygedoException {
        String deviceId = ensureDeviceId(account);
        var result = api.refreshToken(account.getRefreshToken(), deviceId);
        UserCenterLoginResult loginResult = new UserCenterLoginResult();
        loginResult.setAccessToken(result.getAccessToken());
        loginResult.setRefreshToken(result.getRefreshToken());
        if (result.getUid() != null) {
            loginResult.setUid(result.getUid());
        }
        return loginResult;
    }

    // ==================== 一键登录 ====================

    public TaygedoAccount login(String phone, String captcha) throws TaygedoException {
        TaygedoAccount account = new TaygedoAccount();
        account.setPhone(phone);
        ensureDeviceId(account);

        LoginResult laohuResult = api.loginWithCaptcha(phone, captcha, account.getDeviceId());
        return completeLogin(account, laohuResult);
    }

    public TaygedoAccount loginWithPassword(String phone, String password) throws TaygedoException {
        TaygedoAccount account = new TaygedoAccount();
        account.setPhone(phone);
        ensureDeviceId(account);

        LoginResult laohuResult = api.loginWithPassword(phone, password, account.getDeviceId());
        return completeLogin(account, laohuResult);
    }

    private TaygedoAccount completeLogin(TaygedoAccount account, LoginResult laohuResult) throws TaygedoException {
        UserCenterLoginResult tajiduoResult = api.userCenterLogin(
                laohuResult.getToken(), laohuResult.getUserId(), account.getDeviceId());

        account.setName(laohuResult.getNickname());
        account.setLaohuToken(laohuResult.getToken());
        account.setLaohuUserId(laohuResult.getUserId());
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
            account.setOpenudid(device.getOpenudid());
            account.setVendorid(device.getVendorid());
            LOG.info("为账号 {} 生成新 deviceId: {}", account.getPhone(), device.getDeviceId());
        }
        return account.getDeviceId();
    }
}
