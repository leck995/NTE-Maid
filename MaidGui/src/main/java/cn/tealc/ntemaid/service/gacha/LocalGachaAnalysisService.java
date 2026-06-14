package cn.tealc.ntemaid.service.gacha;

import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaData;
import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaItem;
import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaPool;
import cn.tealc.ntemaid.model.game.gacha.local.LocalGachaType;
import cn.tealc.ntemaid.repository.GameDataRepository;
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
    private static final DateTimeFormatter DATE_SHORT = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            .withZone(ZoneId.systemDefault());

    private final LocalGachaDataService gachaDataService;
    private final GameDataRepository dataRepo;

    @Inject
    public LocalGachaAnalysisService(LocalGachaDataService gachaDataService,
                                      GameDataRepository dataRepo) {
        this.gachaDataService = gachaDataService;
        this.dataRepo = dataRepo;
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
        LocalGachaPool pool = analysisPool(items, 80, true);
        pool.setType(LocalGachaType.WEAPON_POOL);
        return pool;
    }

    private LocalGachaPool analysisRolePool(List<LocalGachaItem> items,LocalGachaType type){
        LocalGachaPool pool = analysisPool(items, 90, false);
        pool.setType(type);
        return pool;
    }

    private LocalGachaPool analysisPool(List<LocalGachaItem> items, int max, boolean isFork){
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
            item.setUp(dataRepo.isUp(item.getCharid(), isFork));
            if (isFork) {
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

        if (isFork) {
            double upSsrAvg = upPityList.isEmpty() ? 0
                    : (double) upPityList.stream().mapToInt(Integer::intValue).sum() / upPityList.size();
            pool.setUpSsrAvg(upSsrAvg);
            pool.setUpSsrCount(upSsrCount);
            pool.setNoUpSsrCount(noUpSsrCount);

            if (avg < 40){
                pool.setLuckyType(5);
            }else if (avg < 50){
                pool.setLuckyType(4);
            }else if (avg < 60){
                pool.setLuckyType(3);
            }else if (avg < 70){
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
