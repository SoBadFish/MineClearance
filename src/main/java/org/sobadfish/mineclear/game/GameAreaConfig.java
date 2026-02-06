package org.sobadfish.mineclear.game;

import cn.nukkit.level.Position;

public class GameAreaConfig {

    /**
     * 游戏范围
     * */
    public int startX;

    public int endX;

    public int y;

    public int startZ;

    public int endZ;

    /**
     * 地图
     * */
    public String levelName;

    /**
     * 房间名称
     * */
    public String roomName;

    /**
     * 雷的个数
     * */
    public int mine;

    

    public GameAreaConfig(Position start, Position end, float y, int mine, String levelName, String roomName) {
        this.startX = (int) start.x;
        this.endX = (int) end.x;
        this.y = (int) y;
        this.startZ = (int) start.z;
        this.endZ = (int) end.z;
        this.mine = mine;
        this.levelName = levelName;
        this.roomName = roomName;
    }

}
