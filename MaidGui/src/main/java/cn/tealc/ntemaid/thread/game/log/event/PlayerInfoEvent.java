package cn.tealc.ntemaid.thread.game.log.event;

import cn.tealc.ntemaid.base.AppInjector;
import cn.tealc.ntemaid.base.AppRuntimeData;
import cn.tealc.ntemaid.model.game.Player;

import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* 记录联机状态下其他玩家的信息
* */
public class PlayerInfoEvent implements Consumer<String> {
    private static final Pattern PLAYER_PATTERN =
            Pattern.compile("player\\[([^\\]]+)\\]\\[([^\\]]+)\\]");
    private final AppRuntimeData runtimeData;

    public PlayerInfoEvent() {
        runtimeData = AppInjector.getInstance(AppRuntimeData.class);
    }

    @Override
    public void accept(String s) {
        if (s.contains("UHTUI_PlayerHeadUpInfo")){
            Player player = parsePlayer(s);
            runtimeData.getHistoryPlayers().add(player);
        }
    }



    /**
     * 从日志行中提取第一个匹配的玩家名称和ID
     * @param logLine 日志行字符串
     * @return 包含name和id的数组，若未匹配则返回null
     */
    public static Player parsePlayer(String logLine) {
        Matcher matcher = PLAYER_PATTERN.matcher(logLine);
        if (matcher.find()) {
            String name = matcher.group(1);  // 第一个捕获组：名称
            long id = Long.parseLong(matcher.group(2));    // 第二个捕获组：ID
            return new Player(id,name);
        }
        return null;
    }
}
