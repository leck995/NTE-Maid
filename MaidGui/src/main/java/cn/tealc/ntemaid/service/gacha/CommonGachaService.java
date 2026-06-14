package cn.tealc.ntemaid.service.gacha;

import cn.tealc.ntemaid.dao.CommonGachaDao;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class CommonGachaService {
    private static final Logger LOG = LoggerFactory.getLogger(CommonGachaService.class);

    private final CommonGachaDao dao;
    private final ObjectMapper mapper;

    @Inject
    public CommonGachaService(CommonGachaDao dao, ObjectMapper mapper) {
        this.dao = dao;
        this.mapper = mapper;
    }

    /**
     * 从 JSON 文件导入抽卡数据，通过 UNIQUE(player_id, time, sort) 自动去重，
     * 已存在的跳过，不存在的写入。相同 time 的十连记录按数组顺序分配 sort。
     *
     * @return 本次新增的记录数
     */
    public int importFromFile(File file, String playerId) throws IOException {
        List<CommonGachaItem> items = parseGachaFile(file, playerId);
        if (items.isEmpty()) {
            return 0;
        }

        // 分配 sort：相同 time 的记录按数组顺序递增（0,1,2...），单抽 sort=0
        String prevTime = null;
        int groupSort = 0;
        for (CommonGachaItem item : items) {
            String itemTime = item.getTime();
            if (itemTime.equals(prevTime)) {
                groupSort++;
            } else {
                groupSort = 0;
                prevTime = itemTime;
            }
            item.setSort(groupSort);
        }

        long before;
        try {
            before = dao.count();
            dao.saveAll(items);
            int added = (int) (dao.count() - before);
            LOG.info("导入完成, playerId={}, 总 {} 条, 新增 {} 条", playerId, items.size(), added);
            return added;
        } catch (SQLException e) {
            throw new IOException("保存抽卡数据失败", e);
        }
    }

    /**
     * 获取所有不重复的 playerId
     */
    public List<String> getDistinctPlayerIds() {
        try {
            return dao.findDistinctPlayerIds();
        } catch (SQLException e) {
            LOG.error("查询playerId列表失败", e);
            return Collections.emptyList();
        }
    }

    /**
     * 从 DB 获取指定玩家的所有抽卡记录，按 time DESC, sort ASC 排序
     */
    public List<CommonGachaItem> getByPlayerId(String playerId) {
        try {
            return dao.findByPlayerIdOrderByTimeDescSortAsc(playerId);
        } catch (SQLException e) {
            LOG.error("查询抽卡数据失败, playerId={}", playerId, e);
            return Collections.emptyList();
        }
    }

    public void deleteByPlayerId(String playerId) throws IOException {
        try {
            dao.deleteByPlayerId(playerId);
            LOG.info("已删除 playerId={} 的抽卡数据", playerId);
        } catch (SQLException e) {
            throw new IOException("删除失败", e);
        }
    }

    private List<CommonGachaItem> parseGachaFile(File file, String playerId) throws IOException {
        JsonNode root = mapper.readTree(file);
        JsonNode listNode = root.path("nte").path("list");
        if (listNode.isMissingNode() || !listNode.isArray()) {
            throw new IOException("无效的JSON文件：缺少 nte.list 数组");
        }

        List<CommonGachaItem> items = mapper.readerFor(new TypeReference<List<CommonGachaItem>>() {})
                .readValue(listNode);

        if (playerId != null && !playerId.isEmpty()) {
            for (CommonGachaItem item : items) {
                item.setPlayerId(playerId);
            }
        }

        return items;
    }
}
