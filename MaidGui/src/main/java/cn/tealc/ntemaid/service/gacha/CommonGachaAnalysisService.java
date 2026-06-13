package cn.tealc.ntemaid.service.gacha;

import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaData;
import cn.tealc.ntemaid.model.game.gacha.common.CommonGachaItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommonGachaAnalysisService {


    public CommonGachaData analysis(List<CommonGachaItem> items){
        Map<String,List<CommonGachaItem>> map = new HashMap<>();

        //LocalGachaAnalysisService，帮我完成抽卡分析，items中有所有的抽卡数据，包括SSR,SR,R，
        //按照CommonGachaItem.poolId将items分类到map，key是poolId将items分类到map，每个对应一个卡池
        //然后对map每个列表进行遍历进行分析，分析参考LocalGachaAnalysisService，分析数据保存在CommonGachaPool
        //所有记录都是按时间倒叙，你必须统计出SSR之前的SR,SR订单数量，才能知道SSR的出货抽数
        //最后把CommonGachaPool汇总到CommonGachaData

    }
}
