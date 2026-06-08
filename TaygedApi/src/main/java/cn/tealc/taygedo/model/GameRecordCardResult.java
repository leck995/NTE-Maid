package cn.tealc.taygedo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * 游戏角色卡列表
 * 从游戏记录卡接口获取的所有游戏角色绑定信息
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GameRecordCardResult {
    /** 角色卡列表 */
    private List<GameRecordCard> cards;

    public GameRecordCardResult() {
    }

    public List<GameRecordCard> getCards() {
        return cards;
    }

    public void setCards(List<GameRecordCard> cards) {
        this.cards = cards;
    }
}
