package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.dao.LocalGachaDataDao;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaItem;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaType;
import cn.tealc.taygedo.model.GameGachaItem;
import cn.tealc.taygedo.model.GameGachaPool;
import cn.tealc.taygedo.model.GameGachaResult;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LocalGachaDataService {
    private static final Logger LOG = LoggerFactory.getLogger(LocalGachaDataService.class);
    private final LocalGachaDataDao dao;

    @Inject
    public LocalGachaDataService(LocalGachaDataDao dao) {
        this.dao = dao;
    }

    public boolean save(LocalGachaItem data) {
        try {
            return dao.save(data) > 0;
        } catch (SQLException e) {
            LOG.error("保存抽卡数据失败", e);
            return false;
        }
    }

    public void saveAll(List<LocalGachaItem> list) {
        try {
            dao.saveAll(list);
            LOG.info("批量保存抽卡数据 {} 条成功", list.size());
        } catch (SQLException e) {
            LOG.error("批量保存抽卡数据失败", e);
        }
    }

    /**
     * 从 GameGachaResult 中提取所有卡池抽卡项，仅保存 timeStamp 比数据库中该 roleId 最新记录更晚的项
     */
    public void saveAll(GameGachaResult result) {
        if (result == null || result.getGachaDetails() == null) return;

        String roleId = result.getRoleid();
        List<LocalGachaItem> toSave = new ArrayList<>();
        for (GameGachaPool pool : result.getGachaDetails()) {
            if (pool.getDetails() == null) continue;

            LocalGachaType type = LocalGachaType.fromName(pool.getTab());
            if (type == LocalGachaType.UNKNOWN)
                continue;

            long latestTimeStamp;
            try {
                latestTimeStamp = dao.findLatestTimeStampByRoleIdAndType(roleId, type.getCode());
            } catch (SQLException e) {
                LOG.error("查询roleId={}, type={}最新timeStamp失败", roleId, type, e);
                continue;
            }

            for (int i = pool.getDetails().size() - 1; i >= 0; i--) {
                GameGachaItem item = pool.getDetails().get(i);
                if (item.getTimeStamp() > latestTimeStamp) {
                    toSave.add(fromGameGachaItem(roleId, type, item));
                }
            }
        }

        if (!toSave.isEmpty()) {
            saveAll(toSave);
        }
    }

    private LocalGachaItem fromGameGachaItem(String roleId, LocalGachaType type, GameGachaItem item) {
        LocalGachaItem data = new LocalGachaItem(roleId, type);
        data.setCharid(item.getCharid());
        data.setLuckyType(item.getLuckyType());
        data.setRareCount(item.getRareCount());
        data.setTime(item.getTime());
        data.setTimeStamp(item.getTimeStamp());
        return data;
    }

    public Optional<LocalGachaItem> getById(long id) {
        try {
            return dao.findById(id);
        } catch (SQLException e) {
            LOG.error("查询抽卡数据失败, id={}", id, e);
            return Optional.empty();
        }
    }

    public List<LocalGachaItem> getByRoleId(String roleId) {
        try {
            return dao.findByRoleId(roleId);
        } catch (SQLException e) {
            LOG.error("根据roleId查询抽卡数据失败, roleId={}", roleId, e);
            return Collections.emptyList();
        }
    }




    public List<LocalGachaItem> getAfterTimeByRoleId(String roleId, long timeStamp) {
        try {
            return dao.findByRoleIdAfterTimeStamp(roleId, timeStamp);
        } catch (SQLException e) {
            LOG.error("根据roleId和timeStamp查询抽卡数据失败, roleId={}, timeStamp={}", roleId, timeStamp, e);
            return Collections.emptyList();
        }
    }

    /**
     * 注意返回列表从晚到早排序
     * @param roleId
     * @param type
     * @param timeStamp
     * @return {@link List }<{@link LocalGachaItem }>
     * @author leck
     * @date 2026/06/09
     */
    public List<LocalGachaItem> getAfterTimeDescByRoleIdAndPoolType(String roleId, LocalGachaType type, long timeStamp) {
        try {
            return dao.findByRoleIdAndTypeAfterTimeStamp(roleId, type.getCode(), timeStamp);
        } catch (SQLException e) {
            LOG.error("根据roleId、卡池类型和timeStamp查询抽卡数据失败, roleId={}, type={}, timeStamp={}", roleId, type, timeStamp, e);
            return Collections.emptyList();
        }
    }

    public List<LocalGachaItem> getAll() {
        try {
            return dao.findAll();
        } catch (SQLException e) {
            LOG.error("查询所有抽卡数据失败", e);
            return Collections.emptyList();
        }
    }

    public boolean deleteById(long id) {
        try {
            return dao.deleteById(id);
        } catch (SQLException e) {
            LOG.error("删除抽卡数据失败, id={}", id, e);
            return false;
        }
    }

    public int deleteByRoleId(String roleId) {
        try {
            int rows = dao.deleteByRoleId(roleId);
            LOG.info("删除roleId={}的抽卡数据 {} 条", roleId, rows);
            return rows;
        } catch (SQLException e) {
            LOG.error("删除roleId={}的抽卡数据失败", roleId, e);
            return 0;
        }
    }
}
