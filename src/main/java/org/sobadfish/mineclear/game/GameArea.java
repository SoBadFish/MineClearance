package org.sobadfish.mineclear.game;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import org.sobadfish.mineclear.block.MineClearDisplayBlock;
import org.sobadfish.mineclear.block.MineClearGroundBlock;
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

    public LinkedHashMap<Integer, MineClearMineEntity>  mineEntitys = new LinkedHashMap<>();

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
        clear();
        //TODO 重新生成区域
        MapGenerateManager.generateMap(this);
    }

    public void clear(){
        // 重置雷的位置
        mines = new int[0];
        // 关闭所有数字实体
        for (MineClearNumberEntity numberEntity : numberEntitys.values()) {
            if (numberEntity != null) {
                numberEntity.close();
            }
        }
        for (MineClearMineEntity mineEntity : mineEntitys.values()) {
            if (mineEntity != null) {
                mineEntity.close();
            }
        }
        // 关闭所有旗子实体
        for (MineClearPieceEntity flagEntity : flagEntitys.values()) {
            if (flagEntity != null) {
                flagEntity.close();
            }
        }
        flagEntitys.clear();
        mineEntitys.clear();
        numberEntitys.clear();
        // 重置玩家和开始时间
        player = null;
        startTime = 0;
        // 重置游戏状态
        isRunning = 0;
    }
    public void breakMap() {
       clear();
        Level level = Server.getInstance().getLevelByName(this.gameAreaConfig.levelName);
        // 生成未挖掘的方块
        for (int x = this.gameAreaConfig.startX; x <= this.gameAreaConfig.endX; x++) {
            for (int z = this.gameAreaConfig.startZ; z <= this.gameAreaConfig.endZ; z++) {
                Position pos = new Position(x, this.gameAreaConfig.y, z, level);
                level.setBlock(pos, Block.get(0));
                level.setBlock(pos.add(0,-1,0), Block.get(0));
            }
        }

    }
}
