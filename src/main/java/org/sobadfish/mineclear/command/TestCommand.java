package org.sobadfish.mineclear.command;

import cn.nukkit.Player;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.form.element.ElementInput;
import cn.nukkit.form.element.ElementSlider;
import cn.nukkit.form.window.FormWindowCustom;
import cn.nukkit.level.Position;
import org.sobadfish.mineclear.game.GameArea;
import org.sobadfish.mineclear.game.GameAreaConfig;
import org.sobadfish.mineclear.manager.MapGenerateManager;
import org.sobadfish.mineclear.MineClearMainClass;
import cn.nukkit.utils.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestCommand extends Command {



    public TestCommand(String name) {
        super(name);
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if(sender instanceof Player player && player.isOp()) {
            // 创建表单
            FormWindowCustom form = new FormWindowCustom("创建扫雷游戏区域");
            
            // 添加表单元素
            form.addElement(new ElementInput("房间名称", "输入游戏区域的名称", "测试房间"));
            form.addElement(new ElementSlider("宽度", 5, 20, 1, 9));
            form.addElement(new ElementSlider("高度", 5, 20, 1, 9));
            form.addElement(new ElementSlider("雷数", 1, 50, 1, 10));
            
            // 显示表单
            player.showFormWindow(form, MineClearMainClass.FORM_ID_CREATE);
        }
        return true;
    }
    
   
}
