package org.sobadfish.mineclear.manager;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.ParticleEffect;
import cn.nukkit.level.Position;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.Config;
import org.sobadfish.mineclear.MineClearMainClass;
import org.sobadfish.mineclear.entity.MineClearMineEntity;
import org.sobadfish.mineclear.entity.MineClearNumberEntity;
import org.sobadfish.mineclear.entity.MineClearPieceEntity;
import org.sobadfish.mineclear.game.GameArea;
import org.sobadfish.mineclear.game.GameAreaConfig;

import java.util.*;

/**
 * 扫雷地图生成器
 * */
public class MapGenerateManager {





    //TODO 加载游戏区域配置文件
    public static LinkedHashMap<String,GameAreaConfig> loadGameAreas(Config config){
        /**
         * room.yml
         * room:
         * - startX: 100
         *   endX: 200
         *   y: 100
         *   startZ: 100
         *   endZ: 200
         *   mine: 10
         *   levelName: world
         *   roomName: 测试房间
         * */ 
        LinkedHashMap<String,GameAreaConfig> gameAreaConfigs = new LinkedHashMap<>();
        
        // 加载 room 列表
        List<Map<String, Object>> roomList = config.getList("room", new ArrayList<>());
        for (Map<String, Object> roomConfig : roomList) {
            String roomName = (String) roomConfig.get("roomName");
            String levelName = (String) roomConfig.get("levelName");
            Level level = Server.getInstance().getLevelByName(levelName);
            
            if (level != null) {
                Position start = new Position(
                        (int) roomConfig.get("startX"),
                        (int) roomConfig.get("y"),
                        (int) roomConfig.get("startZ"),
                        level
                );
                Position end = new Position(
                        (int) roomConfig.get("endX"),
                        (int) roomConfig.get("y"),
                        (int) roomConfig.get("endZ"),
                        level
                );
                GameAreaConfig gameAreaConfig = new GameAreaConfig(
                        start,
                        end,
                        (int) roomConfig.get("y"),
                        (int) roomConfig.get("mine"),
                        levelName,
                        roomName
                );
                gameAreaConfigs.put(roomName, gameAreaConfig);
            }
        }
        return gameAreaConfigs;


    }
    
    /**
     * 生成扫雷地图区域
     * @return 游戏区域对象
     * */
    public static GameArea generateMap(GameArea gameArea) {

//        gameArea.gameAreaConfig = config;
        
        // 计算区域大小
        int width = gameArea.gameAreaConfig.endX - gameArea.gameAreaConfig.startX + 1;
        int height = gameArea.gameAreaConfig.endZ - gameArea.gameAreaConfig.startZ + 1;
        int totalBlocks = width * height;
        
        // 随机生成雷的位置
        List<Integer> minePositions = new ArrayList<>();
        Random random = new Random();
        
        while (minePositions.size() < gameArea.gameAreaConfig.mine) {
            int pos = random.nextInt(totalBlocks);
            if (!minePositions.contains(pos)) {
                minePositions.add(pos);
            }
        }
        
        gameArea.mines = new int[minePositions.size()];
        for (int i = 0; i < minePositions.size(); i++) {
            gameArea.mines[i] = minePositions.get(i);
        }
        Level level = Server.getInstance().getLevelByName(gameArea.gameAreaConfig.levelName);
        // 生成未挖掘的方块
        for (int x = gameArea.gameAreaConfig.startX; x <= gameArea.gameAreaConfig.endX; x++) {
            for (int z = gameArea.gameAreaConfig.startZ; z <= gameArea.gameAreaConfig.endZ; z++) {
                Position pos = new Position(x, gameArea.gameAreaConfig.y, z, level);
                level.setBlock(pos, MineClearMainClass.getInstance().getDisplayBlock());
                level.setBlock(pos.add(0,-1,0),MineClearMainClass.getInstance().getGroundBlock());
            }
        }
        
        return gameArea;
    }
    
    /**
     * 计算指定位置周围的雷数
     * @param x X坐标
     * @param z Z坐标
     * @param gameArea 游戏区域
     * @return 周围雷数
     * */
    public static int countMinesAround(int x, int z, GameArea gameArea) {
        GameAreaConfig config = gameArea.gameAreaConfig;
        int width = config.endX - config.startX + 1;
        int count = 0;
        
        // 遍历周围8个方向
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) continue;
                
                int nx = x + dx;
                int nz = z + dz;
                
                // 检查是否在边界内
                if (nx >= config.startX && nx <= config.endX && nz >= config.startZ && nz <= config.endZ) {
                    // 计算位置索引
                    int index = (nx - config.startX) + (nz - config.startZ) * width;
                    // 检查是否是雷
                    for (int mine : gameArea.mines) {
                        if (mine == index) {
                            count++;
                            break;
                        }
                    }
                }
            }
        }
        
        return count;
    }

    /**
     * 挖掘方块
     * @param player 玩家
     * @param block 要挖掘的方块位置
     * @param gameArea 游戏区域
     * @return 是否挖到雷
     * */
    public static boolean digBlock(Player player, Position block, GameArea gameArea) {
        GameAreaConfig config = gameArea.gameAreaConfig;
        int width = config.endX - config.startX + 1;
        int index = (int) ((block.x - config.startX) + (block.z - config.startZ) * width);

        // 检查是否已经挖掘
        if (gameArea.numberEntitys.containsKey(index)) {
            return false;
        }
        if(block.y != gameArea.gameAreaConfig.y){
            return false;
        }
        // 潜行插旗子 就可以不用挖掘出雷
        if(player.isSneaking()){
            // 检查是否已经插旗子
            if(gameArea.flagEntitys.containsKey(index)){
                // 插旗后取消
                gameArea.flagEntitys.get(index).close();
                gameArea.flagEntitys.remove(index);
                return false;
            }
            // 生成旗子实体
            Position pos = new Position(block.x + 0.5, block.y + 0.5 , block.z + 0.5, block.getLevel());
            CompoundTag compoundTag =  Entity.getDefaultNBT(pos);
            compoundTag.putString("roomName", config.roomName);
            MineClearPieceEntity flagEntity = new MineClearPieceEntity(block.getChunk(),
                    compoundTag);
            flagEntity.roomName = config.roomName;
            flagEntity.spawnToAll();
            gameArea.flagEntitys.put(index, flagEntity);
            return false;
        }
        // 如果插旗了 就不能挖掘出雷
        if(gameArea.flagEntitys.containsKey(index)){
            return false;
        }
        // 检查是否是雷
        boolean isMineHit = false;
        for (int mine : gameArea.mines) {
            if (mine == index) {
                isMineHit = true;
                break;
            }
        }

        // 如果挖到雷，显示所有剩余的雷
        if (isMineHit) {
            //给个炸开的粒子
            block.getLevel().addSound(block, Sound.CAULDRON_EXPLODE);
            block.getLevel().addParticleEffect(block, ParticleEffect.LARGE_EXPLOSION_LEVEL);
            // 遍历所有雷的位置，逐个生成雷实体并显示
            for (int mineIndex : gameArea.mines) {
                // 反向计算雷的x、z坐标（从index还原坐标）
                int x = config.startX + (mineIndex % width);
                int z = config.startZ + (mineIndex / width);
                int y = config.y;

                // 雷实体的显示位置（居中）
                Position minePos = new Position(x + 0.5, y, z + 0.5, block.getLevel());
                CompoundTag compoundTag = Entity.getDefaultNBT(minePos);
                compoundTag.putString("roomName", config.roomName);

                // 生成雷实体
                MineClearMineEntity mineEntity = new MineClearMineEntity(block.getChunk(), compoundTag);
                mineEntity.roomName = config.roomName;
                mineEntity.spawnToAll();

                // 将雷实体存入游戏区域
                gameArea.mineEntitys.put(mineIndex, mineEntity);

                // 清除雷位置的方块（显示雷实体）
                Position blockPos = new Position(x, y, z, block.getLevel());
                block.getLevel().setBlock(blockPos, Block.get(0));
            }
            // 挖到雷，返回true
            return true;
        }

        // 不是雷，计算周围雷数
        int mineCount = countMinesAround((int) block.x, (int) block.z, gameArea);

        // 更新方块为已挖掘
        Position pos = new Position(block.x, block.y, block.z, block.getLevel());
        block.getLevel().setBlock(pos, Block.get(0));

        // 将当前方块标记为已挖掘
        gameArea.numberEntitys.put(index, null);

        // 如果周围有雷，生成数字实体
        if (mineCount > 0) {
            Position entityPos = new Position(block.x + 0.5, block.y +0.05, block.z + 0.5, block.getLevel());
            CompoundTag compoundTag =  Entity.getDefaultNBT(entityPos);
            compoundTag.putString("roomName", config.roomName);
            MineClearNumberEntity numberEntity = new MineClearNumberEntity(block.getChunk(),compoundTag
            );
            numberEntity.setNumberModel(mineCount);
            numberEntity.roomName = config.roomName;
            numberEntity.spawnToAll();
            gameArea.numberEntitys.put(index, numberEntity);
        } else {
            // 周围没有雷，递归挖掘周围的方块
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    int nx = (int) (block.x + dx);
                    int nz = (int) (block.z + dz);

                    // 检查是否在边界内
                    if (nx >= config.startX && nx <= config.endX && nz >= config.startZ && nz <= config.endZ) {
                        int nIndex = (nx - config.startX) + (nz - config.startZ) * width;
                        boolean isMine = false;
                        for (int mine : gameArea.mines) {
                            if (mine == nIndex) {
                                isMine = true;
                                break;
                            }
                        }
                        // 只有不是雷、且未挖掘的方块，才递归挖掘
                        if (!isMine && !gameArea.numberEntitys.containsKey(nIndex)) {
                            digBlock(player, new Position(nx, block.y, nz, block.getLevel()), gameArea);
                        }
                    }
                }
            }
        }

        return false;
    }
    
    /**
     * 检查是否获胜
     * @param gameArea 游戏区域
     * @return 是否获胜
     * */
    public static boolean checkWin(GameArea gameArea) {
        GameAreaConfig config = gameArea.gameAreaConfig;
        int width = config.endX - config.startX + 1;
        int height = config.endZ - config.startZ + 1;
        int totalBlocks = width * height;
        
        // 检查已挖掘的方块数是否等于总方块数减去雷数
        return gameArea.numberEntitys.size() == totalBlocks - gameArea.mines.length;
    }
}
