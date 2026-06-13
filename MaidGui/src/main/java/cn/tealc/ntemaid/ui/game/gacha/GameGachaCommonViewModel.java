package cn.tealc.ntemaid.ui.game.gacha;

import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;
import cn.tealc.ntemaid.ui.base.BaseViewModel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class GameGachaCommonViewModel extends BaseViewModel {
    @Inject
    private ObjectMapper mapper;

    public void init(){



    }

    /**
     * 导入抽卡数据
     * @param file     JSON 文件
     * @param playerID 玩家ID（可用于关联记录）
     * @return 解析后的抽卡记录列表
     * @throws RuntimeException 解析或读取失败时抛出
     */
    public List<CommonGachaItem> importGachaData(File file, String playerID) {
        try {
            // 读取整个 JSON 树
            JsonNode root = mapper.readTree(file);
            // 获取 nte.list 节点
            JsonNode listNode = root.path("nte").path("list");
            if (listNode.isMissingNode() || !listNode.isArray()) {
                throw new RuntimeException("Invalid JSON: missing nte.list array");
            }

            List<CommonGachaItem> items = mapper.readerFor(new TypeReference<List<CommonGachaItem>>() {})
                    .readValue(listNode);

            // 可选：为每个 item 关联 playerID（假设 CommonGachaItem 有 setPlayerId 方法）
            if (playerID != null && !playerID.isEmpty()) {
                for (CommonGachaItem item : items) {
                    item.setPlayerId(playerID);
                }
            }

            return items;
        } catch (IOException e) {
            throw new RuntimeException("Failed to import gacha data from " + file.getAbsolutePath(), e);
        }
    }

}
