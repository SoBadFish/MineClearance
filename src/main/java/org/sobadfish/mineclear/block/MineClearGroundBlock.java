package org.sobadfish.mineclear.block;

import cn.nukkit.block.custom.CustomBlockManager;
import cn.nukkit.item.ItemTool;
import org.sobadfish.mineclear.manager.BlockManager;
import org.sobadfish.mineclear.manager.VersionCompatUtils;

import java.lang.reflect.Method;

/**
 * 扫雷区域方块 - 兼容新旧版本核心
 * 保留原有所有功能，新增版本适配逻辑
 */
public class MineClearGroundBlock extends ProxyCustomBlock {

    public MineClearGroundBlock() {
        super(BlockManager.BLOCK_GROUND_NAME);
    }

    /**
     * 兼容式注册：新版本反射注册自定义方块，旧版本注册普通方块
     */
    public static void register() {
        // 新版本：反射调用自定义方块API
        if (VersionCompatUtils.hasCustomBlockDefinition() && VersionCompatUtils.hasCustomContainerBlock()) {
            try {
                // ========== 1. 反射构建Materials ==========
                Class<?> materialsClass = Class.forName("cn.nukkit.block.custom.container.data.Materials");
                Class<?> renderMethodClass = Class.forName("cn.nukkit.block.custom.container.data.Materials$RenderMethod");
                Object opaqueRender = renderMethodClass.getField("OPAQUE").get(null);

                // Materials.builder().any(OPAQUE, "block_ground")
                Method materialsBuilderMethod = materialsClass.getMethod("builder");
                Object materials = materialsBuilderMethod.invoke(null);
                Method anyMethod = materials.getClass().getMethod("any", renderMethodClass, String.class);
                anyMethod.invoke(materials, opaqueRender, "block_ground");

                // ========== 2. 反射构建CustomBlockDefinition ==========
                Class<?> cbdClass = Class.forName("cn.nukkit.block.custom.CustomBlockDefinition");
                // builder方法的参数类型是BlockContainer接口
                Class<?> blockContainerClass = Class.forName("cn.nukkit.block.custom.container.BlockContainer");
                Method cbdBuilderMethod = cbdClass.getMethod("builder", blockContainerClass);
                // 通过customBlockDelegate传入CustomBlock实例（实现了BlockContainer接口）
                MineClearGroundBlock groundBlock = new MineClearGroundBlock();
                Object cbdBuilder = cbdBuilderMethod.invoke(null, groundBlock.getCustomBlockDelegate());

                // 设置创意分类
                Class<?> creativeCatClass = Class.forName("cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory");
                Object natureCat = creativeCatClass.getField("NATURE").get(null);
                cbdBuilder.getClass().getMethod("creativeCategory", creativeCatClass)
                        .invoke(cbdBuilder, natureCat);

                // 设置核心属性
                cbdBuilder.getClass().getMethod("materials", materialsClass).invoke(cbdBuilder, materials);
                cbdBuilder.getClass().getMethod("geometry", String.class)
                        .invoke(cbdBuilder, "geometry.mine.clear.ground");

                // 构建碰撞盒
                Class<?> vec3fClass = Class.forName("cn.nukkit.math.Vector3f");
                Object collisionMin = vec3fClass.getConstructor(float.class, float.class, float.class)
                        .newInstance(-8f, 0.0f, -8f);
                Object collisionMax = vec3fClass.getConstructor(float.class, float.class, float.class)
                        .newInstance(16f, 16f, 16f);
                cbdBuilder.getClass().getMethod("collisionBox", vec3fClass, vec3fClass)
                        .invoke(cbdBuilder, collisionMin, collisionMax);

                // 构建选择盒
                Object selectMin = vec3fClass.getConstructor(float.class, float.class, float.class)
                        .newInstance(-8.0f, 0.0f, -8.0f);
                Object selectMax = vec3fClass.getConstructor(float.class, float.class, float.class)
                        .newInstance(16.0f, 16.0f, 16.0f);
                cbdBuilder.getClass().getMethod("selectionBox", vec3fClass, vec3fClass)
                        .invoke(cbdBuilder, selectMin, selectMax);

                // 构建CustomBlockDefinition实例
                Object customBlockDef = cbdBuilder.getClass().getMethod("build").invoke(cbdBuilder);

                // ========== 3. 注册自定义方块（修复Supplier问题） ==========
                Class<?> cbmClass = Class.forName("cn.nukkit.block.custom.CustomBlockManager");
                Object cbmInstance = cbmClass.getMethod("get").invoke(null);

                // 创建Supplier<BlockContainer>，返回CustomBlock实例（实现了BlockContainer接口）
                Class<?> customBlockClass = Class.forName("cn.nukkit.block.custom.container.CustomBlock");
                java.lang.reflect.Constructor<?> customBlockCtor = customBlockClass.getConstructor(String.class);
                Object supplier = java.lang.reflect.Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{Class.forName("java.util.function.Supplier")},
                        (proxy, method, args) -> {
                            if ("get".equals(method.getName())) {
                                return customBlockCtor.newInstance(BlockManager.BLOCK_GROUND_NAME);
                            }
                            return method.invoke(proxy, args);
                        }
                );

                // 反射调用注册方法
                Method registerMethod = cbmClass.getMethod(
                        "registerCustomBlock",
                        String.class,
                        cbdClass,
                        Class.forName("java.util.function.Supplier")
                );
                int blockId = (Integer) registerMethod.invoke(
                        cbmInstance,
                        BlockManager.BLOCK_GROUND_NAME,
                        customBlockDef,
                        supplier
                );

                System.out.println("新版本扫雷区域方块注册成功，ID：" + blockId);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("新版本扫雷区域方块注册失败：" + e.getMessage());
            }
        } else {
            // 旧版本：降级注册为普通方块
            try {
                Method registerBlockMethod = Class.forName("cn.nukkit.block.Block")
                        .getMethod("registerBlock", Class.class, String.class, boolean.class);
                registerBlockMethod.invoke(null, MineClearGroundBlock.class, BlockManager.BLOCK_GROUND_NAME, true);
                System.out.println("旧版本扫雷区域方块注册成功");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("旧版本扫雷区域方块注册失败：" + e.getMessage());
            }
        }
    }

    // ========== 保留原有所有核心功能 ==========
    @Override
    public String getName() {
        return "扫雷区域";
    }

    @Override
    public int getId() {
        // 旧版本使用BlockManager中配置的专属ID，避免硬编码0
        return CustomBlockManager.get().getBlockId(BlockManager.BLOCK_GROUND_NAME);
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_NONE;
    }

    @Override
    public int getBurnChance() {
        return 5;
    }

    @Override
    public int getBurnAbility() {
        return 20;
    }
}