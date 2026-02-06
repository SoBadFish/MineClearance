package org.sobadfish.mineclear.game;

import cn.nukkit.Player;
import org.sobadfish.mineclear.entity.MineClearMineEntity;
import org.sobadfish.mineclear.entity.MineClearNumberEntity;
import org.sobadfish.mineclear.entity.MineClearPieceEntity;
import org.sobadfish.mineclear.manager.MapGenerateManager;

import java.util.LinkedHashMap;

/**
 * 游戏房间区域
 * */
public class GameArea {


    public int isRunning = 0;

    public GameAreaConfig gameAreaConfig;

    /**
     * 当前游戏玩家
     * */
    public Player player;

    /**
     * 游戏开始时间
     * */
    public long startTime;

    /**
     * 旗子实体
     * */
    public LinkedHashMap<Integer, MineClearPieceEntity>  flagEntitys = new LinkedHashMap<>();

    public MineClearMineEntity clearMineEntity;

    /**
     * 预设雷的位置
     * */
    public int[] mines = new int[0];

    /**
     * 数字实体
     * */
    public LinkedHashMap<Integer, MineClearNumberEntity>  numberEntitys = new LinkedHashMap<>();


    public GameArea(GameAreaConfig gameAreaConfig) {

        this.gameAreaConfig = gameAreaConfig;
    }

    //TODO 写重置方法
    public void resetGame(){
        // 重置雷的位置
        mines = new int[0];
        // 关闭所有数字实体
        for (MineClearNumberEntity numberEntity : numberEntitys.values()) {
            if (numberEntity != null) {
                numberEntity.close();
            }
        }
        if(clearMineEntity != null){
            clearMineEntity.close();
        }
        // 关闭所有旗子实体
        for (MineClearPieceEntity flagEntity : flagEntitys.values()) {
            if (flagEntity != null) {
                flagEntity.close();
            }
        }
        flagEntitys.clear();
        clearMineEntity = null;
        numberEntitys.clear();
        // 重置玩家和开始时间
        player = null;
        startTime = 0;
        // 重置游戏状态
        isRunning = 0;
        //TODO 重新生成区域
        MapGenerateManager.generateMap(this);
    }

}
