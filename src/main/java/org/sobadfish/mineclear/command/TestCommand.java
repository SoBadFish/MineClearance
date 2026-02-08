package org.sobadfish.mineclear.command;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.entity.Entity;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Level;
import org.sobadfish.mineclear.MineClearMainClass;
import org.sobadfish.mineclear.entity.MineClearMineEntity;
import org.sobadfish.mineclear.entity.MineClearNumberEntity;
import org.sobadfish.mineclear.entity.MineClearPieceEntity;
import org.sobadfish.mineclear.game.GameArea;
import org.sobadfish.mineclear.game.GameAreaConfig;



public class TestCommand extends Command {



    public TestCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(sender instanceof Player player && player.isOp()) {
            // 创建表单
            if(args.length == 0) {
                FormWindowCustom form = new FormWindowCustom(
                        MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.form.title")
                );

                // 添加表单元素
                form.addElement(new ElementInput(
                        MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.form.roomname"),
                        MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.form.roomname.placeholder"),
                        MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.form.roomname.default")
                ));
                form.addElement(new ElementSlider(
                        MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.form.width"),
                        5, 20, 1, 9
                ));
                form.addElement(new ElementSlider(
                        MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.form.height"),
                        5, 20, 1, 9
                ));
                form.addElement(new ElementSlider(
                        MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.form.minecount"),
                        1, 50, 1, 10
                ));

                // 显示表单
                player.showFormWindow(form, MineClearMainClass.FORM_ID_CREATE);
            }

            if(args.length >= 1) {
                if(args[0].equalsIgnoreCase("remove")) {
                    if(args.length > 1) {
                        String name = args[1];
                        if (MineClearMainClass.getInstance().gameAreas.containsKey(name)) {
                            GameArea gameArea = MineClearMainClass.getInstance().gameAreas.get(name);
                            gameArea.breakMap();
                            MineClearMainClass.getInstance().gameAreas.remove(name);

                            MineClearMainClass.getInstance().clickRoom.clear();
                            MineClearMainClass.getInstance().gameConfigAreas.remove(name);
                            MineClearMainClass.getInstance().resetAllGameMap();
                            for (GameAreaConfig gameAreaConfig : MineClearMainClass.getInstance().gameConfigAreas.values()) {
                                MineClearMainClass.getInstance().saveToConfig(gameAreaConfig);
                            }
                            player.sendMessage(MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.game.removed", name));
                        } else {
                            player.sendMessage(MineClearMainClass.I18N.tr(player.getLanguageCode(), "mineclear.game.notfound", name));
                        }
                    }

                }
                if(args[0].equalsIgnoreCase("clear")) {
                    //TODO 清空地图中存在的实体
                    for (Level level : Server.getInstance().getLevels().values()) {
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
                    sender.sendMessage(MineClearMainClass.I18N.tr(((Player) sender).getLanguageCode(), "mineclear.other.cleared"));


                }

            }

        }
        return true;
    }
    
   
}
