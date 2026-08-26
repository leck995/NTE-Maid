package cn.tealc.ntemaid.service.taygedo;

import cn.tealc.ntemaid.dao.TaygedoAccountDao;
import cn.tealc.ntemaid.model.taygedo.TaygedoAccount;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 塔吉多账号业务服务
 * 封装 TaygedoAccountDao，统一处理数据库异常和日志
 */
public class TaygedoAccountService {
    private static final Logger LOG = LoggerFactory.getLogger(TaygedoAccountService.class);
    private final TaygedoAccountDao dao;

    @Inject
    public TaygedoAccountService(TaygedoAccountDao dao) {
        this.dao = dao;
    }

    /** 查询所有账号，异常时返回空列表 */
    public List<TaygedoAccount> getAll() {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            LOG.error("查询所有账号失败", e);
            return Collections.emptyList();
        }
    }

    /** 查询单个账号，Optional.empty() = 不存在，异常时同样返回 empty 但会记日志 */
    public Optional<TaygedoAccount> getByPhone(String phone) {
        try {
            return dao.findByPhone(phone);
        } catch (SQLException e) {
            LOG.error("查询账号 {} 失败", phone, e);
            return Optional.empty();
        }
    }

    /** 获取首个账号 */
    public Optional<TaygedoAccount> getFirst() {
        try {
            return dao.findFirst();
        } catch (SQLException e) {
            LOG.error("查询首个账号失败", e);
            return Optional.empty();
        }
    }

    /** 保存账号，失败返回 false */
    public boolean save(TaygedoAccount account) {
        try {
            int rows = dao.saveOrUpdate(account);
            LOG.info("账号 {} 已保存", account.getPhone());
            return rows > 0;
        } catch (SQLException e) {
            LOG.error("保存账号 {} 失败", account.getPhone(), e);
            return false;
        }
    }

    /** 刷新账号的最后签到时间为当前时间 */
    public void refreshLastSignTime(TaygedoAccount account) {
        try {
            long now = System.currentTimeMillis();
            dao.updateLastSignTime(account.getPhone(), now);
            account.setLastSignTime(now);
        } catch (SQLException e) {
            LOG.error("更新签到时间失败, phone={}", account.getPhone(), e);
        }
    }

    /** 获取今日未签到的账号列表 */
    public List<TaygedoAccount> getNotSignedTodayList() {
        long todayStart = ZonedDateTime.now(ZoneId.of("Asia/Shanghai"))
                .toLocalDate().atStartOfDay(ZoneId.of("Asia/Shanghai"))
                .toInstant().toEpochMilli();
        return getAll().stream()
                .filter(a -> a.getLastSignTime() == null || a.getLastSignTime() < todayStart)
                .toList();
    }

    /** 删除账号，失败返回 false */
    public boolean delete(String phone) {
        try {
            boolean deleted = dao.delete(phone);
            if (deleted) {
                LOG.info("账号 {} 已删除", phone);
            }
            return deleted;
        } catch (SQLException e) {
            LOG.error("删除账号 {} 失败", phone, e);
            return false;
        }
    }
}
