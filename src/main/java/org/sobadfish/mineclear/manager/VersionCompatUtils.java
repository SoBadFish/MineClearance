package org.sobadfish.mineclear.manager;

/**
 * 版本兼容工具类：检测核心是否包含新版本特征类
 */
public class VersionCompatUtils {

    // 检测是否存在新版本的CustomBlock（容器类）
    public static boolean hasCustomContainerBlock() {
        try {
            Class.forName("cn.nukkit.block.custom.container.CustomBlock");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // 检测是否存在CustomBlockDefinition（自定义方块定义类）
    public static boolean hasCustomBlockDefinition() {
        try {
            Class.forName("cn.nukkit.block.custom.CustomBlockDefinition");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}