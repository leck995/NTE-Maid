package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.model.game.gacha.LocalGachaData;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaItem;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaPool;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaType;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class LocalGachaAnalysisService {
    private static final Logger LOG = LoggerFactory.getLogger(LocalGachaAnalysisService.class);
    private final LocalGachaDataService gachaDataService;
    private List<String> WEAPON_LIST = List.of("fork_butterfly","fork_blackBook","fork_mofeikesi"
            ,"fork_jingmotingyuan","fork_wushoutieyu","fork_bitGame","fork_rishi"
            ,"fork_nestBird","fork_arachne","fork_whale");
    private List<String> ROLE_LIST = List.of("1055","1054","1039","1025","1023","1003");
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    @Inject
    public LocalGachaAnalysisService(LocalGachaDataService gachaDataService) {
        this.gachaDataService = gachaDataService;
    }


    public LocalGachaData analysis(String roleId){
        List<LocalGachaItem> upRoleList = gachaDataService.getAfterTimeDescByRoleIdAndPoolType(roleId, LocalGachaType.UP_ROLE_POOL,0);
        List<LocalGachaItem> defaultRoleList = gachaDataService.getAfterTimeDescByRoleIdAndPoolType(roleId, LocalGachaType.DEFAULT_ROLE_POOL,0);
        List<LocalGachaItem> weaponList = gachaDataService.getAfterTimeDescByRoleIdAndPoolType(roleId, LocalGachaType.WEAPON_POOL,0);

        LocalGachaPool upRolePool = analysisRolePool(upRoleList, LocalGachaType.UP_ROLE_POOL);
        upRolePool.setPoolName("限定卡池");
        LocalGachaPool defaultRolePool = analysisRolePool(defaultRoleList, LocalGachaType.DEFAULT_ROLE_POOL);
        defaultRolePool.setPoolName("常驻卡池");
        LocalGachaPool weaponPool = analysisWeaponPool(weaponList);
        weaponPool.setPoolName("弧盘池");

        LocalGachaData gachaData = new LocalGachaData();
        gachaData.setPools(List.of(upRolePool,defaultRolePool,weaponPool));

        int lucky = gachaData.getPools().stream().mapToInt(LocalGachaPool::getLuckyType).sum() / gachaData.getPools().size();
        gachaData.setLuckyType(lucky);

        return gachaData;
    }



    private LocalGachaPool analysisWeaponPool(List<LocalGachaItem> items){
        LocalGachaPool pool = analysisPool(items, 80, WEAPON_LIST, true);
        pool.setType(LocalGachaType.WEAPON_POOL);
        return pool;
    }

    private LocalGachaPool analysisRolePool(List<LocalGachaItem> items,LocalGachaType type){
        LocalGachaPool pool = analysisPool(items, 90, ROLE_LIST, false);
        pool.setType(type);
        return pool;
    }

    private LocalGachaPool analysisPool(List<LocalGachaItem> items, int max, List<String> standardList, boolean trackUp){
        if (items == null || items.isEmpty()) {
            LocalGachaPool pool = new LocalGachaPool();
            pool.setItems(List.of());
            pool.setMax(max);
            return pool;
        }
        int sum = 0;
        int upSsrCount = 0;
        int noUpSsrCount = 0;
        List<Integer> upPityList = new ArrayList<>();
        int accumulatedPulls = 0;

        for (int i = items.size() - 1; i >= 0; i--) {
            LocalGachaItem item = items.get(i);
            sum += item.getRareCount();
            item.setUp(!standardList.contains(item.getCharid().toLowerCase()));
            if (trackUp) {
                item.setUpReallyCount(true);
                accumulatedPulls += item.getRareCount();
                if (item.isUp()) {
                    upSsrCount++;
                    upPityList.add(accumulatedPulls);
                    accumulatedPulls = 0;
                } else {
                    noUpSsrCount++;
                }
            }
        }

        double avg = (double) sum / items.size();
        double percent = (double) items.size() / sum;

        LocalGachaPool pool = new LocalGachaPool();
        pool.setItems(items);
        pool.setCount(sum);
        pool.setMax(max);
        pool.setSsrCount(items.size());
        pool.setSsrAvg(avg);
        pool.setSsrPercent(percent);
        pool.setTime(getDateRange(items));

        if (trackUp) {
            double upSsrAvg = upPityList.isEmpty() ? 0
                    : (double) upPityList.stream().mapToInt(Integer::intValue).sum() / upPityList.size();
            pool.setUpSsrAvg(upSsrAvg);
            pool.setUpSsrCount(upSsrCount);
            pool.setNoUpSsrCount(noUpSsrCount);

            if (upSsrAvg < 40){
                pool.setLuckyType(5);
            }else if (upSsrAvg < 50){
                pool.setLuckyType(4);
            }else if (upSsrAvg < 60){
                pool.setLuckyType(3);
            }else if (upSsrAvg < 70){
                pool.setLuckyType(2);
            }else {
                pool.setLuckyType(1);
            }
        }else {
            if (avg < 40){
                pool.setLuckyType(5);
            }else if (avg < 50){
                pool.setLuckyType(4);
            }else if (avg < 65){
                pool.setLuckyType(3);
            }else if (avg < 75){
                pool.setLuckyType(2);
            }else {
                pool.setLuckyType(1);
            }
        }
        return pool;
    }

    private String getDateRange(List<LocalGachaItem> items) {
        if (items == null || items.isEmpty()) return "";
        long first = items.getFirst().getTimeStamp();
        long last = items.getLast().getTimeStamp();
        return DATE_SHORT.format(Instant.ofEpochMilli(last)) + " - " + DATE_SHORT.format(Instant.ofEpochMilli(first));
    }
}
