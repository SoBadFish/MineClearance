package org.sobadfish.mineclear.block;

import cn.nukkit.block.Block;
import org.sobadfish.mineclear.manager.VersionCompatUtils;

/**
 * 适配层：旧版本继承基础Block，新版本通过委托持有CustomBlock实例
 * 避免直接继承不存在的类导致类加载失败
 */
public abstract class ProxyCustomBlock extends Block {

    protected String blockName;
    // 新版本的CustomBlock委托对象（避免直接继承）
    protected Object customBlockDelegate;

    public ProxyCustomBlock(String blockName) {// 旧版本Block构造需传ID，暂用0（后续可配置）
        this.blockName = blockName;

        // 新版本：反射创建CustomBlock实例作为委托
        if (VersionCompatUtils.hasCustomContainerBlock()) {
            try {
                Class<?> customBlockClass = Class.forName("cn.nukkit.block.custom.container.CustomBlock");
                this.customBlockDelegate = customBlockClass.getConstructor(String.class)
                        .newInstance(blockName);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // 子类必须实现的核心方法（保留原有逻辑）
    @Override
    public abstract String getName();

    // 获取新版本委托对象（按需使用）
    public Object getCustomBlockDelegate() {
        return customBlockDelegate;
    }
}