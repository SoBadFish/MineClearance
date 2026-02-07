package org.sobadfish.mineclear;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.EntityManager;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerFormRespondedEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerMoveEvent;

import cn.nukkit.form.response.FormResponseCustom;
import cn.nukkit.form.response.FormResponseModal;

import cn.nukkit.form.window.FormWindowModal;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.PluginTask;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.sobadfish.mineclear.block.MineClearDisplayBlock;
import org.sobadfish.mineclear.block.MineClearGroundBlock;
import org.sobadfish.mineclear.command.TestCommand;
import org.sobadfish.mineclear.entity.MineClearMineEntity;
import org.sobadfish.mineclear.entity.MineClearNumberEntity;
import org.sobadfish.mineclear.entity.MineClearPieceEntity;
import org.sobadfish.mineclear.game.GameArea;
import org.sobadfish.mineclear.game.GameAreaConfig;
import org.sobadfish.mineclear.manager.MapGenerateManager;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityLevelChangeEvent;
import cn.nukkit.lang.PluginI18n;
import cn.nukkit.lang.PluginI18nManager;
import cn.nukkit.lang.LangCode;

public class MineClearMainClass extends PluginBase implements Listener {

    public static PluginI18n I18N;
    public static LangCode serverLangCode;

    private static MineClearMainClass instance;

    public static final int FORM_ID_CREATE = 10022;

    public static final int FORM_ID_NEXT = 10023;

    public LinkedHashMap<String,GameAreaConfig> gameConfigAreas = new LinkedHashMap<>();

    public LinkedHashMap<String,GameArea> gameAreas = new LinkedHashMap<>();


    public void loadGameAreas(){
        for(String key : gameConfigAreas.keySet()){
            GameAreaConfig config = gameConfigAreas.get(key);
            gameAreas.put(key,MapGenerateManager.generateMap(new GameArea(config)));
        }
    }

    public static MineClearMainClass getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        EntityManager.get().registerDefinition(MineClearNumberEntity.DEF_NUMBER);
        EntityManager.get().registerDefinition(MineClearMineEntity.DEF_NUMBER);
        EntityManager.get().registerDefinition(MineClearPieceEntity.DEF_NUMBER);
        MineClearDisplayBlock.register();
        MineClearGroundBlock.register();
        
        // 注册插件的 i18n
        I18N = PluginI18nManager.register(this);
        initServerLangCode();
    }
    
    /**
     * 初始化服务器语言代码
     */
    public void initServerLangCode() {
        switch (Server.getInstance().getLanguage().getLang()) {
            case "eng" -> {
                serverLangCode = LangCode.en_US;
            }
            case "chs" -> {
                serverLangCode = LangCode.zh_CN;
            }
            case "deu" -> {
                serverLangCode = LangCode.de_DE;
            }
            case "rus" -> {
                serverLangCode = LangCode.ru_RU;
            }
            default -> {
                try {
                    serverLangCode = LangCode.valueOf(Server.getInstance().getLanguage().getLang());
                } catch (IllegalArgumentException e) {
                    serverLangCode = LangCode.en_US;
                }
            }
        }
    }

    @Override
    public void onEnable() {
        this.getLogger().info(I18N.tr(serverLangCode, "mineclear.log.starting"));
        this.getServer().getCommandMap().register("mineclear",new TestCommand("mt"));
        this.getServer().getPluginManager().registerEvents(this,this);
        //TODO 使用配置文件加载游戏区域
        saveResource("room.yml",false);
        gameConfigAreas = MapGenerateManager.loadGameAreas(new Config(this.getDataFolder() + "/room.yml", Config.YAML));
        //在服务启动后 重置游戏地图 也就是杀死所有的雷//数字//棋子
        Server.getInstance().getScheduler().scheduleDelayedTask(new PluginTask<MineClearMainClass>(this) {
            @Override
            public void onRun(int currentTick) {
                resetAllGameMap();
                loadGameAreas();
            }
        }, 10);
       
    }
    
    @Override
    public void onDisable() {
        this.getLogger().info(I18N.tr(serverLangCode, "mineclear.log.stopping"));
        resetAllGameMap();
    }

    @EventHandler
    public void onFrom(PlayerFormRespondedEvent event){
        if(event.wasClosed()){
            return;
        }
        Player player = event.getPlayer();
        if(event.getResponse() instanceof FormResponseModal model){
            if (event.getFormID() == MineClearMainClass.FORM_ID_NEXT){
                if(clickRoom.containsKey(player) && model.getClickedButtonId() == 0){
                    GameArea gameArea = clickRoom.get(player);
                    if(gameArea != null){
                        gameArea.resetGame();
                    }
                }
                return;
            }
        }

        if(event.getResponse() instanceof FormResponseCustom response){
             if (event.getFormID() == MineClearMainClass.FORM_ID_CREATE) {
                    String roomName = response.getInputResponse(0);
                    if(gameConfigAreas.containsKey(roomName)){
                        player.sendMessage(I18N.tr(player.getLanguageCode(), "mineclear.game.exist", roomName));
                        return;
                    }

                    int width = (int) response.getSliderResponse(1);
                    int height = (int) response.getSliderResponse(2);
                    int mineCount = (int) response.getSliderResponse(3);
                    
                    // 创建游戏区域配置
                    Position startPos = player.getPosition();
                    Position endPos = player.getPosition().add(width - 1, 0, height - 1);
                    
                    GameAreaConfig config = new GameAreaConfig(
                            startPos,
                            endPos,
                            (float) player.getPosition().y,
                            mineCount,
                            player.getLevel().getFolderName(),
                            roomName
                    );
                    
                    // 生成游戏地图
                    GameArea gameArea = new GameArea(config);
                    MapGenerateManager.generateMap(gameArea);
                    
                    // 添加到游戏区域列表
                    MineClearMainClass.getInstance().gameAreas.put(roomName, gameArea);
                    
                    // 保存到配置文件
                    saveToConfig(config);
                    
                    player.sendMessage(I18N.tr(player.getLanguageCode(), "mineclear.game.created"));
                }
        }

    }

     /**
     * 保存游戏区域配置到配置文件
     * @param config 游戏区域配置
     */
    public void saveToConfig(GameAreaConfig config) {
        // 获取配置文件
        File configFile = new File(MineClearMainClass.getInstance().getDataFolder() + "/room.yml");
        Config yamlConfig = new Config(configFile, Config.YAML);
        
        // 获取现有的房间列表
        List<Map<String, Object>> roomList = yamlConfig.getList("room", new ArrayList<>());
        
        // 创建新的房间配置
        Map<String, Object> roomConfig = new HashMap<>();
        roomConfig.put("startX", config.startX);
        roomConfig.put("endX", config.endX);
        roomConfig.put("y", config.y);
        roomConfig.put("startZ", config.startZ);
        roomConfig.put("endZ", config.endZ);
        roomConfig.put("mine", config.mine);
        roomConfig.put("levelName", config.levelName);
        roomConfig.put("roomName", config.roomName);
        
        // 添加到房间列表
        roomList.add(roomConfig);
        
        // 保存配置文件
        yamlConfig.set("room", roomList);
        yamlConfig.save();
    }
    
    /**
     * 保存玩家用时到配置文件
     * @param player 玩家
     * @param roomName 房间名称
     * @param timeUsed 用时（秒）
     */
    private void savePlayerTime(Player player, String roomName, long timeUsed) {
        // 获取配置文件
        File configFile = new File(MineClearMainClass.getInstance().getDataFolder() + "/player.yml");
        Config yamlConfig = new Config(configFile, Config.YAML);
        
        // 获取玩家数据
        String playerName = player.getName();
        Map<String, Object> playerData = yamlConfig.getSection(playerName);
        if (playerData == null) {
            playerData = new HashMap<>();
        }
        
        // 获取房间用时数据
        Map<String, Object> roomData = (Map<String, Object>) playerData.get(roomName);
        if (roomData == null) {
            roomData = new HashMap<>();
        }
        
        // 保存用时
        roomData.put("timeUsed", timeUsed);
        roomData.put("lastPlayed", System.currentTimeMillis());
        
        // 更新配置
        playerData.put(roomName, roomData);
        yamlConfig.set(playerName, playerData);
        yamlConfig.save();
    }

    public void resetAllGameMap() {
        for (Level level : this.getServer().getLevels().values()) {
            // 杀死所有雷
            for (Entity entity : level.getEntities()) {
                if (entity instanceof MineClearMineEntity) {
                    entity.close();
                }
                // 杀死所有数字
                if (entity instanceof MineClearNumberEntity) {
                    entity.close();
                }
                // 杀死所有棋子
                if (entity instanceof MineClearPieceEntity) {
                    entity.close();
                }
            }

        
        }
        //GameRoom 重置
        for (GameArea gameArea : gameAreas.values()) {
            gameArea.clear();
        }
    }

    public LinkedHashMap<Player,GameArea> clickRoom = new LinkedHashMap<>();


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if(event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            //TODO 根据交互的位置与坐标范围 获取房间
            Position pos = event.getBlock();
            GameArea gameArea = null;
            for (GameArea rArea : gameAreas.values()) {
                if (rArea.gameAreaConfig.startX <= (int) pos.x && (int) pos.x <= rArea.gameAreaConfig.endX &&
                        rArea.gameAreaConfig.startZ <= (int) pos.z && (int) pos.z <= rArea.gameAreaConfig.endZ) {
                    // 点击的位置在当前游戏区域内
                    gameArea = rArea;
                    break;
                }
            }


            if (gameArea != null) {
                if (gameArea.isRunning == 2) {
                    clickRoom.put(player, gameArea);
                    FormWindowModal form = new FormWindowModal(
                            I18N.tr(player.getLanguageCode(), "mineclear.form.reset.title"),
                            I18N.tr(player.getLanguageCode(), "mineclear.form.reset.content"),
                            I18N.tr(player.getLanguageCode(), "mineclear.form.reset.yes"),
                            I18N.tr(player.getLanguageCode(), "mineclear.form.reset.no")
                    );
                    // 显示表单
                    player.showFormWindow(form, MineClearMainClass.FORM_ID_NEXT);
//                gameArea.resetGame();
                }

                int x = (int) pos.x;
                int z = (int) pos.z;

                // 检查点击的方块是否在游戏区域内
                if (x >= gameArea.gameAreaConfig.startX && x <= gameArea.gameAreaConfig.endX &&
                        z >= gameArea.gameAreaConfig.startZ && z <= gameArea.gameAreaConfig.endZ) {
                    // 第一次点击，开始游戏
                    if (gameArea.isRunning == 0) {
                        gameArea.isRunning = 1;
                        gameArea.player = player;
                        gameArea.startTime = System.currentTimeMillis();
                    }
                    // 挖掘方块
                    boolean isMine = MapGenerateManager.digBlock(player, pos, gameArea);

                    if (isMine) {
                        gameArea.isRunning = 2;
                        long endTime = System.currentTimeMillis();
                        long timeUsed = (endTime - gameArea.startTime) / 1000; // 转换为秒
                        player.sendTitle("§c挑战失败", "§7游戏结束，用时：" + timeUsed + "秒");
                        player.sendMessage(I18N.tr(player.getLanguageCode(), "mineclear.game.mine", String.valueOf(timeUsed)));
                    } else {
                        // 检查是否获胜
                        if (MapGenerateManager.checkWin(gameArea)) {
                            gameArea.isRunning = 2;
                            long endTime = System.currentTimeMillis();
                            long timeUsed = (endTime - gameArea.startTime) / 1000; // 转换为秒
                            player.sendMessage(I18N.tr(player.getLanguageCode(), "mineclear.game.win", String.valueOf(timeUsed)));
                            player.sendTitle("§a恭喜！", "§7游戏结束，用时：" + timeUsed + "秒");
                            // 保存用时到 player.yml
                            savePlayerTime(player, gameArea.gameAreaConfig.roomName, timeUsed);

                        }
                    }

                }
            }
        }
    }
    
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        // 检查玩家是否在游戏中
        for (GameArea gameArea : gameAreas.values()) {
            if (gameArea.player == player && gameArea.isRunning == 1) {
                // 计算玩家与游戏区域中心的距离
                int centerX = (gameArea.gameAreaConfig.startX + gameArea.gameAreaConfig.endX) / 2;
                int centerZ = (gameArea.gameAreaConfig.startZ + gameArea.gameAreaConfig.endZ) / 2;
                double distance = Math.sqrt(
                        Math.pow(player.getX() - centerX, 2) +
                        Math.pow(player.getZ() - centerZ, 2)
                );
                // 如果距离超过5米，重置游戏
                if (distance > (Math.abs(gameArea.gameAreaConfig.endX - gameArea.gameAreaConfig.startX)) / 2f + 5) {
                    gameArea.resetGame();
                    player.sendMessage(I18N.tr(player.getLanguageCode(), "mineclear.game.reset"));
                }
                break;
            }
        }
    }
    
    @EventHandler
    public void onPlayerLevelChange(EntityLevelChangeEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player) {
             // 检查玩家是否在游戏中
            for (GameArea gameArea : gameAreas.values()) {
                if (gameArea.player == player && gameArea.isRunning == 1) {
                    // 玩家切换地图，重置游戏
                    gameArea.resetGame();
                    player.sendMessage(I18N.tr(player.getLanguageCode(), "mineclear.game.levelchange"));
                    break;
                }
            }
        }
       
    }

    
}
