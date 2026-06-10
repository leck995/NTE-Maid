package cn.tealc.ntemaid.service;

import cn.tealc.ntemaid.model.game.gacha.LocalGachaData;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaItem;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaPool;
import cn.tealc.ntemaid.model.game.gacha.LocalGachaType;
import cn.tealc.taygedo.model.GameGachaItem;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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





        return null;
    }



    private LocalGachaPool analysisRolePool(List<LocalGachaItem> items){
        for (LocalGachaItem item : items) {
            item.setUp(!ROLE_LIST.contains(item.getCharid().toLowerCase()));
        }
        int sum = items.stream().mapToInt(GameGachaItem::getRareCount).sum();
        double avg = (double) sum / items.size();
        double percent = (double) items.size() / sum;
        LocalGachaPool pool = new LocalGachaPool();
        pool.setItems(items);
        pool.setCount(sum);
        pool.setMax(90);
        pool.setSsrCount(items.size());
        pool.setSsrAvg(avg);
        pool.setSsrPercent(percent);
        pool.setTime(getDateRange(items));

        if (avg < 40){ //超欧
            pool.setLuckyType(5);
        }else if (avg < 50){ //欧
            pool.setLuckyType(4);
        }else if (avg < 65){ //一般
            pool.setLuckyType(3);
        }else if (avg < 75){//小非
            pool.setLuckyType(2);
        }else {//大非
            pool.setLuckyType(1);
        }
        return pool;
    }


    private LocalGachaPool analysisWeaponPool(List<LocalGachaItem> items){
        for (LocalGachaItem item : items) {
            item.setUp(!WEAPON_LIST.contains(item.getCharid().toLowerCase()));
        }
        int sum = items.stream().mapToInt(GameGachaItem::getRareCount).sum();
        double avg = (double) sum / items.size();
        double percent = (double) items.size() / sum;
        LocalGachaPool pool = new LocalGachaPool();
        pool.setItems(items);
        pool.setCount(sum);
        pool.setMax(90);
        pool.setSsrCount(items.size());
        pool.setSsrAvg(avg);
        pool.setSsrPercent(percent);
        pool.setTime(getDateRange(items));




        if (avg < 40){ //超欧
            pool.setLuckyType(5);
        }else if (avg < 50){ //欧
            pool.setLuckyType(4);
        }else if (avg < 65){ //一般
            pool.setLuckyType(3);
        }else if (avg < 75){//小非
            pool.setLuckyType(2);
        }else {//大非
            pool.setLuckyType(1);
        }
        return pool;
    }

    private String getDateRange(List<LocalGachaItem> items) {
        if (items == null || items.isEmpty()) return "";
        long first = items.getFirst().getTimeStamp();
        long last = items.getLast().getTimeStamp();
        return DATE_SHORT.format(Instant.ofEpochMilli(first)) + " - " + DATE_SHORT.format(Instant.ofEpochMilli(last));
    }
}
