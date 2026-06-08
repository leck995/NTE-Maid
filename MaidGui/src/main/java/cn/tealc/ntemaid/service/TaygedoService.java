package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.dao.TaygedoAccountDao;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import cn.tealc.taygedo.DeviceIdentity;
import cn.tealc.taygedo.TaygedoApi;
import cn.tealc.taygedo.TaygedoException;
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

    private final TaygedoApi api;
    private final TaygedoAccountDao accountDao;

    public TaygedoService() {
        this.api = new TaygedoApi();
        this.accountDao = new TaygedoAccountDao();
    }


    // ==================== 账号持久化 ====================
    public List<TaygedoAccount> getAllAccount() {
        return accountDao.findAll();
    }

    public TaygedoAccount loadAccount() {
        return accountDao.findFirst();
    }


    public void saveAccount(TaygedoAccount account) {
        accountDao.saveOrUpdate(account);
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
        // 创建新账号上下文，自动生成deviceId
        TaygedoAccount account = new TaygedoAccount();
        account.setPhone(phone);
        ensureDeviceId(account);

        // 第一步：老虎平台验证码登录
        LoginResult laohuResult = api.loginWithCaptcha(phone, captcha, account.getDeviceId());

        // 第二步：换取塔吉多令牌
        UserCenterLoginResult tajiduoResult = api.userCenterLogin(
                laohuResult.getToken(), laohuResult.getUserId(), account.getDeviceId());

        // 第三步：填充完整账号信息
        account.setName(laohuResult.getNickname());
        account.setLaohuToken(laohuResult.getToken());
        account.setLaohuUserId(laohuResult.getUserId());
        account.setAccessToken(tajiduoResult.getAccessToken());
        account.setRefreshToken(tajiduoResult.getRefreshToken());
        account.setUid(tajiduoResult.getUid());
        //account.setRoleName(tajiduoResult.);
        account.setTokenUpdatedAt(
                ZonedDateTime.now(ZoneId.of("Asia/Shanghai")).format(SHANGHAI_FORMAT));

        LOG.info("登录成功: phone={}, uid={}, deviceId={}", phone, account.getUid(), account.getDeviceId());
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
