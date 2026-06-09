package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.dao.LocalGachaDataDao;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaData;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaType;
import cn.tealc.taygedo.model.GameGachaItem;
import cn.tealc.taygedo.model.GameGachaPool;
import cn.tealc.taygedo.model.GameGachaResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class LocalGachaDataService {
    private static final Logger LOG = LoggerFactory.getLogger(LocalGachaDataService.class);
    private final LocalGachaDataDao dao = new LocalGachaDataDao();

    public boolean save(LocalGachaData data) {
        try {
            return dao.save(data) > 0;
        } catch (SQLException e) {
            LOG.error("保存抽卡数据失败", e);
            return false;
        }
    }

    public void saveAll(List<LocalGachaData> list) {
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
        long latestTimeStamp;
        try {
            latestTimeStamp = dao.findLatestTimeStampByRoleId(roleId);
        } catch (SQLException e) {
            LOG.error("查询roleId={}最新timeStamp失败", roleId, e);
            return;
        }

        List<LocalGachaData> toSave = new ArrayList<>();
        for (GameGachaPool pool : result.getGachaDetails()) {
            if (pool.getDetails() == null) continue;

            //识别卡池类型
            LocalGachaType type = switch (pool.getTab()){
                case "限定卡池" -> LocalGachaType.UP_ROLE_POOL;
                case "常驻卡池" -> LocalGachaType.DEFAULT_ROLE_POOL;
                case "弧盘池" -> LocalGachaType.WEAPON_POOL;
                default -> LocalGachaType.UNKNOWN;
            };
            //无法识别，跳过
            if (type == LocalGachaType.UNKNOWN)
                continue;

            for (int i = pool.getDetails().size() - 1; i >= 0; i--) {
                GameGachaItem item = pool.getDetails().get(i);
                if (item.getTimeStamp() > latestTimeStamp) {
                    toSave.add(fromGameGachaItem(roleId,type, item));
                }
            }
        }

        if (!toSave.isEmpty()) {
            saveAll(toSave);
        }
    }

    private LocalGachaData fromGameGachaItem(String roleId, LocalGachaType type, GameGachaItem item) {
        LocalGachaData data = new LocalGachaData(roleId, type);
        data.setCharid(item.getCharid());
        data.setLuckyType(item.getLuckyType());
        data.setRareCount(item.getRareCount());
        data.setTime(item.getTime());
        data.setTimeStamp(item.getTimeStamp());
        return data;
    }

    public Optional<LocalGachaData> getById(long id) {
        try {
            return dao.findById(id);
        } catch (SQLException e) {
            LOG.error("查询抽卡数据失败, id={}", id, e);
            return Optional.empty();
        }
    }

    public List<LocalGachaData> getByRoleId(String roleId) {
        try {
            return dao.findByRoleId(roleId);
        } catch (SQLException e) {
            LOG.error("根据roleId查询抽卡数据失败, roleId={}", roleId, e);
            return Collections.emptyList();
        }
    }

    public List<LocalGachaData> getAll() {
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
