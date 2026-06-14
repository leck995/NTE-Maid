package cn.tealc.ntemaid.service.gacha;

import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaData;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaPool;
import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CommonGachaAnalysisService {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    /** itemId → rarity，来自 character.json */
    private static final Map<String, Integer> CHARACTER_RARITY_MAP = new HashMap<>();
    /** itemId → rarity，来自 weapon.json */
    private static final Map<String, Integer> WEAPON_RARITY_MAP = new HashMap<>();

    /** 常驻5★角色（非UP），其余5★为限定 */
    private static final Set<String> STANDARD_ROLE_5 = Set.of(
            "1003", "1004", "1010", "1023", "1025", "1039", "1054", "1055");
    /** 常驻5★弧盘（非UP），其余5★为限定 */
    private static final Set<String> STANDARD_FORK_5 = Set.of(
            "fork_Butterfly", "fork_BlackBook", "fork_mofeikesi",
            "fork_jingmotingyuan", "fork_wushoutieyu", "fork_BitGame",
            "fork_rishi", "fork_NestBird", "fork_Arachne", "fork_Whale");

    static {
        loadRarityMap("resources/data/character.json", CHARACTER_RARITY_MAP);
        loadRarityMap("resources/data/weapon.json", WEAPON_RARITY_MAP);
    }

    /** 从本地文件加载 {id: {rarity: "5"}} 格式的 JSON，提取 id → rarity 映射 */
    private static void loadRarityMap(String filePath, Map<String, Integer> target) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }
        try {
            Map<String, Map<String, String>> raw = MAPPER.readValue(file,
                    new TypeReference<Map<String, Map<String, String>>>() {});
            for (Map.Entry<String, Map<String, String>> entry : raw.entrySet()) {
                Map<String, String> value = entry.getValue();
                if (value.containsKey("rarity")) {
                    target.put(entry.getKey(), Integer.parseInt(value.get("rarity")));
                }
            }
        } catch (IOException | NumberFormatException e) {
            // 加载失败时 map 为空，后续默认按R处理
        }
    }

    /**
     * 抽卡数据分析入口：
     * 1. 按 poolId 分组（ForkLottery_* 合并为 "ForkLottery"）
     * 2. 对每个卡池分析 SSR/SR/R 的出貨抽数及 UP 统计
     * 3. 汇总到 CommonGachaData
     */
    public CommonGachaData analysis(List<CommonGachaItem> items) {
        // 按卡池ID分组：弧盘池统一归到 "ForkLottery"，角色池按原始 poolId
        Map<String, List<CommonGachaItem>> map = new LinkedHashMap<>();
        for (CommonGachaItem item : items) {
            String key = item.getPoolId().startsWith("ForkLottery_") ? "ForkLottery" : item.getPoolId();
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }

        // 逐个卡池分析
        List<CommonGachaPool> pools = new ArrayList<>();
        for (Map.Entry<String, List<CommonGachaItem>> entry : map.entrySet()) {
            CommonGachaPool pool = analyzePool(entry.getValue(), entry.getKey());
            if (pool != null) {
                pools.add(pool);
            }
        }

        // 汇总结果
        CommonGachaData data = new CommonGachaData();
        data.setPools(pools);

        // 综合运气值 = 各卡池运气值的平均值
        if (!pools.isEmpty()) {
            int lucky = (int) pools.stream().mapToInt(CommonGachaPool::getLuckyType).average().orElse(0);
            data.setLuckyType(lucky);
        }

        return data;
    }

    /**
     * 分析单个卡池：
     * - 从旧到新遍历，分别追踪 SSR/SR/R 的保底计数
     * - 记录每个条目的 size（出货抽数）和 rarity
     * - 统计各稀有度的数量、平均、最小、最大
     * - 统计 UP 五星的出货抽数和不歪率
     */
    private CommonGachaPool analyzePool(List<CommonGachaItem> items, String poolId) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        boolean isForkPool = poolId.startsWith("ForkLottery");
        int max = isForkPool ? 80 : 90;

        List<CommonGachaItem> ssrList = new ArrayList<>();   // 5★
        List<CommonGachaItem> srList = new ArrayList<>();    // 4★
        List<CommonGachaItem> rList = new ArrayList<>();     // 3★

        int totalCount = 0;
        int ssrPity = 0;  // 距上一个5★的抽数
        int srPity = 0;   // 距上一个4★的抽数
        int rPity = 0;    // 距上一个3★的抽数

        int upSsrCount = 0;
        int noUpSsrCount = 0;
        List<Integer> upSsrPityList = new ArrayList<>();
        int upAccumulator = 0; // 累计UP保底数，只在UP出货时重置，包含中间常驻5★的抽数

        // 从旧到新遍历（items 是新→旧存储）
        for (int i = items.size() - 1; i >= 0; i--) {
            CommonGachaItem item = items.get(i);

            // 角色池中 roll_points==0 为免费赠送，不计入抽数
            if (!isForkPool && item.getRollPoints() == 0) {
                continue;
            }
            if (!isForkPool && item.getRollPoints() > 6) {
                continue;
            }

            totalCount++;
            ssrPity++;
            srPity++;
            rPity++;

            int rarity = getRarity(item);
            item.setRarity(rarity);

            if (rarity == 5) {
                item.setUpCount(ssrPity);
                ssrList.add(item);

                upAccumulator += ssrPity; // 常驻5★的抽数也要累计到UP保底中
                boolean up = isUp(item.getItemId());
                item.setUp(up);
                if (up) {
                    upSsrCount++;
                    upSsrPityList.add(upAccumulator);
                    upAccumulator = 0; // 只有UP出货才重置
                } else {
                    noUpSsrCount++;
                }
                ssrPity = 0;
            } else if (rarity == 4) {
                item.setUpCount(srPity);
                srList.add(item);
                srPity = 0;
            } else {
                item.setUpCount(rPity);
                rList.add(item);
                rPity = 0;
            }
        }

        // 填充分析结果
        CommonGachaPool pool = new CommonGachaPool();
        pool.setPoolName(items.get(0).getPoolName());
        pool.setType(getPoolType(poolId));
        pool.setMax(max);
        pool.setTotalCount(totalCount);

        pool.setSsrDataList(ssrList);
        pool.setSrDataList(srList);
        pool.setrDataList(rList);

        pool.setSsrCount(ssrList.size());
        pool.setSrCount(srList.size());
        pool.setrCount(rList.size());

        pool.setNoUpSsrSize(ssrPity);
        pool.setNoUpSrSize(srPity);
        pool.setNoUpRSize(rPity);

        // 五星统计
        if (!ssrList.isEmpty()) {
            pool.setSsrAvg(avgSize(ssrList));
            pool.setSsrMin(minSize(ssrList));
            pool.setSsrMax(maxSize(ssrList));
        }
        // 四星统计
        if (!srList.isEmpty()) {
            pool.setSrAvg(avgSize(srList));
            pool.setSrMin(minSize(srList));
            pool.setSrMax(maxSize(srList));
        }
        // 三星统计
        if (!rList.isEmpty()) {
            pool.setrAvg(avgSize(rList));
            pool.setrMin(minSize(rList));
            pool.setrMax(maxSize(rList));
        }

        // UP 五星统计
        pool.setUpSsrCount(upSsrCount);
        pool.setNoUpSsrCount(noUpSsrCount);
        //计算限定SSR平均抽数
        if (!upSsrPityList.isEmpty()) {
            int sum = upSsrPityList.stream().mapToInt(Integer::intValue).sum();
            pool.setUpSsrAvg((double) sum / upSsrCount);
        }
        int totalSsr = upSsrCount + noUpSsrCount;
        if (totalSsr > 0) {
            pool.setNonBannerRate((double) upSsrCount / totalSsr); // 不歪率 = UP数/总五星数
        }

        // 运气评级（基于五星平均出货抽数）
        double ssrAvg = pool.getSsrAvg();
        if (ssrAvg > 0) {
            if (ssrAvg < 40) pool.setLuckyType(5);
            else if (ssrAvg < 50) pool.setLuckyType(4);
            else if (ssrAvg < 65) pool.setLuckyType(3);
            else if (ssrAvg < 75) pool.setLuckyType(2);
            else pool.setLuckyType(1);
        }

        pool.setTime(getDateRange(items));
        return pool;
    }

    /** 判断是否为UP（限定）物品：不在对应常驻列表中的5★即为UP */
    private boolean isUp(String itemId) {
        if (itemId == null) return false;
        if (CHARACTER_RARITY_MAP.containsKey(itemId)) {
            return !STANDARD_ROLE_5.contains(itemId);
        }
        if (WEAPON_RARITY_MAP.containsKey(itemId)) {
            return !STANDARD_FORK_5.contains(itemId);
        }
        return false;
    }

    // ---- 稀有度查表 ----

    public static int getRarity(String itemId) {
        if (itemId == null) return 3;
        Integer r = CHARACTER_RARITY_MAP.get(itemId);
        if (r != null) return r;
        r = WEAPON_RARITY_MAP.get(itemId);
        if (r != null) return r;
        return 3;
    }

    private int getRarity(CommonGachaItem item) {
        return getRarity(item.getItemId());
    }

    // ---- 统计工具 ----

    private static double avgSize(List<CommonGachaItem> list) {
        return list.stream().mapToInt(CommonGachaItem::getUpCount).average().orElse(0);
    }

    private static int minSize(List<CommonGachaItem> list) {
        return list.stream().mapToInt(CommonGachaItem::getUpCount).min().orElse(0);
    }

    private static int maxSize(List<CommonGachaItem> list) {
        return list.stream().mapToInt(CommonGachaItem::getUpCount).max().orElse(0);
    }

    // ---- 池子类型 ----

    private LocalGachaType getPoolType(String poolId) {
        if (poolId.startsWith("ForkLottery")) return LocalGachaType.WEAPON_POOL;
        if ("CardPool_NewRole".equals(poolId)) return LocalGachaType.DEFAULT_ROLE_POOL;
        return LocalGachaType.UP_ROLE_POOL; // CardPool_Character
    }

    // ---- 日期范围 ----

    private String getDateRange(List<CommonGachaItem> items) {
        if (items == null || items.isEmpty()) return "";
        return items.get(items.size() - 1).getTime() + " - " + items.get(0).getTime();
    }
}
