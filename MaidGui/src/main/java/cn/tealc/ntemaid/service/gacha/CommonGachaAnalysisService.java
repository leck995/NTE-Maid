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
    /** itemId → rarity */
    private static final Map<String, Integer> CHARACTER_RARITY_MAP = new HashMap<>();
    /** itemId → rarity */
    private static final Map<String, Integer> WEAPON_RARITY_MAP = new HashMap<>();
    /** itemId → zh 名称 */
    private static final Map<String, String> ITEM_NAME_MAP = new HashMap<>();

    /** 常驻5★角色（非UP），其余5★为限定 */
    private static final Set<String> STANDARD_ROLE_5 = Set.of(
            "1055","1054","1039","1025","1023","1003");
    /** 常驻5★弧盘（非UP），其余5★为限定 */
    private static final Set<String> STANDARD_FORK_5 = Set.of(
            "fork_butterfly","fork_blackBook","fork_mofeikesi"
            ,"fork_jingmotingyuan","fork_wushoutieyu","fork_bitGame","fork_rishi"
            ,"fork_nestBird","fork_arachne","fork_whale");

    static {
        loadDataFile("resources/data/character.json", CHARACTER_RARITY_MAP);
        loadDataFile("resources/data/weapon.json", WEAPON_RARITY_MAP);
    }

    /** 加载 {id: {rarity, zh, ...}} 格式的 JSON，提取 rarity 和中文名称 */
    private static void loadDataFile(String filePath, Map<String, Integer> rarityTarget) {
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
                    rarityTarget.put(entry.getKey(), Integer.parseInt(value.get("rarity")));
                }
                if (value.containsKey("zh")) {
                    ITEM_NAME_MAP.put(entry.getKey(), value.get("zh"));
                }
            }
        } catch (IOException | NumberFormatException e) {
            // 加载失败时 map 为空，后续按默认值处理
        }
    }

    /**
     * 抽卡数据分析入口：
     * 1. 按 poolId 分组（ForkLottery_* 合并为 "ForkLottery"）
     * 2. 对每个卡池分析 SSR/SR/R 的出貨抽数及 UP 统计
     * 3. 汇总到 CommonGachaData
     */
    public CommonGachaData analysis(List<CommonGachaItem> items) {
        Map<String, List<CommonGachaItem>> map = new LinkedHashMap<>();
        for (CommonGachaItem item : items) {
            String key = item.getPoolId().startsWith("ForkLottery_") ? "ForkLottery" : item.getPoolId();
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(item);
        }

        List<CommonGachaPool> pools = new ArrayList<>();
        for (Map.Entry<String, List<CommonGachaItem>> entry : map.entrySet()) {
            CommonGachaPool pool = analyzePool(entry.getValue(), entry.getKey());
            if (pool != null) {
                pools.add(pool);
            }
        }

        CommonGachaData data = new CommonGachaData();
        data.setPools(pools);

        if (!pools.isEmpty()) {
            int lucky = (int) pools.stream().mapToInt(CommonGachaPool::getLuckyType).average().orElse(0);
            data.setLuckyType(lucky);
        }

        return data;
    }

    private CommonGachaPool analyzePool(List<CommonGachaItem> items, String poolId) {
        if (items == null || items.isEmpty()) {
            return null;
        }

        LocalGachaType poolType = getPoolType(poolId);
        boolean isForkPool = poolId.startsWith("ForkLottery");
        int max = isForkPool ? 80 : 90;

        List<CommonGachaItem> ssrList = new ArrayList<>();
        List<CommonGachaItem> srList = new ArrayList<>();
        List<CommonGachaItem> rList = new ArrayList<>();

        int totalCount = 0;
        int ssrPity = 0;
        int srPity = 0;
        int rPity = 0;

        int upSsrCount = 0;
        int noUpSsrCount = 0;
        List<Integer> upSsrPityList = new ArrayList<>();
        int upAccumulator = 0;

        for (int i = items.size() - 1; i >= 0; i--) {
            CommonGachaItem item = items.get(i);

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

            // 优先使用数据文件中的中文名称
            String dataName = ITEM_NAME_MAP.get(item.getItemId());
            if (dataName != null) {
                item.setItemName(dataName);
            }

            if (rarity == 5) {
                item.setUpCount(ssrPity);
                ssrList.add(item);

                upAccumulator += ssrPity;
                if (isUp(item.getItemId(),isForkPool)) {
                    upSsrCount++;
                    upSsrPityList.add(upAccumulator);
                    upAccumulator = 0;
                    item.setUp(true);
                } else {
                    noUpSsrCount++;
                    item.setUp(false);
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

        CommonGachaPool pool = new CommonGachaPool();
        pool.setPoolName(items.get(0).getPoolName());
        pool.setType(poolType);
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
        if (upSsrCount > 0 && !upSsrPityList.isEmpty()) {
            int sum = upSsrPityList.stream().mapToInt(Integer::intValue).sum();
            pool.setUpSsrAvg((double) sum / upSsrCount);
        }
        int totalSsr = upSsrCount + noUpSsrCount;
        if (totalSsr > 0) {
            pool.setNonBannerRate((double) upSsrCount / totalSsr);
        }

        // 运气评级（基于五星平均）
        double ssrAvg = pool.getSsrAvg();
        pool.setLuckyType(calcLuckyType(ssrAvg));

        // 真实运气评级（弧盘池基于UP平均，排除歪的常驻；角色池同普通运气）
        if (isForkPool && pool.getUpSsrAvg() > 0) {
            pool.setReallyLuckyType(calcLuckyType(pool.getUpSsrAvg()));
        } else {
            pool.setReallyLuckyType(pool.getLuckyType());
        }

        pool.setTime(getDateRange(items));
        return pool;
    }

    private static int calcLuckyType(double avg) {
        if (avg <= 0) return 1;
        if (avg < 40) return 5;
        if (avg < 50) return 4;
        if (avg < 65) return 3;
        if (avg < 75) return 2;
        return 1;
    }

    /** 判断是否为UP（限定）物品：不在对应常驻列表中的5★即为UP */
    private static boolean isUp(String itemId,boolean isFork) {
        if (itemId == null) return false;
        if (isFork){
            return !STANDARD_FORK_5.contains(itemId.toLowerCase());
        }else {
            return !STANDARD_ROLE_5.contains(itemId);
        }

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
        return LocalGachaType.UP_ROLE_POOL;
    }

    // ---- 日期范围 ----

    private String getDateRange(List<CommonGachaItem> items) {
        if (items == null || items.isEmpty()) return "";
        return items.get(items.size() - 1).getTime() + " - " + items.get(0).getTime();
    }
}
