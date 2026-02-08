package org.sobadfish.mineclear.block;

import cn.nukkit.block.custom.CustomBlockManager;
import cn.nukkit.item.ItemTool;
import org.sobadfish.mineclear.manager.BlockManager;
import org.sobadfish.mineclear.manager.VersionCompatUtils;

import java.lang.reflect.Method;
import java.util.Objects;

public class MineClearDisplayBlock extends ProxyCustomBlock {

    public MineClearDisplayBlock() {
        super(BlockManager.BLOCK_DISPLAY_NAME);
    }

    /**
     * 兼容式注册：新版本注册自定义方块，旧版本注册普通方块
     */
    public static void register() {
        // 新版本：反射调用自定义方块API（避免编译时依赖）
        if (VersionCompatUtils.hasCustomBlockDefinition() && VersionCompatUtils.hasCustomContainerBlock()) {
            try {
                // ========== 1. 反射构建Materials ==========
                Class<?> materialsClass = Class.forName("cn.nukkit.block.custom.container.data.Materials");
                Class<?> renderMethodClass = Class.forName("cn.nukkit.block.custom.container.data.Materials$RenderMethod");
                Object opaqueRender = renderMethodClass.getField("OPAQUE").get(null);

                // Materials.builder().any(OPAQUE, "block_display").build()
                Method materialsBuilderMethod = materialsClass.getMethod("builder");
                Object materialsBuilder = materialsBuilderMethod.invoke(null);
                Method anyMethod = materialsBuilder.getClass().getMethod("any", renderMethodClass, String.class);
                anyMethod.invoke(materialsBuilder, opaqueRender, "block_display");
                Object materials = materialsBuilder.getClass().getMethod("build").invoke(materialsBuilder);

                // ========== 2. 反射构建CustomBlockDefinition ==========
                Class<?> cbdClass = Class.forName("cn.nukkit.block.custom.CustomBlockDefinition");
                // 注意：builder方法的参数是CustomBlock（BlockContainer子类）
                Class<?> customBlockClass = Class.forName("cn.nukkit.block.custom.container.CustomBlock");
                Method cbdBuilderMethod = cbdClass.getMethod("builder", customBlockClass);
                // 传入MineClearDisplayBlock实例（向上转型为CustomBlock/BlockContainer）
                Object cbdBuilder = cbdBuilderMethod.invoke(null, new MineClearDisplayBlock());

                // 设置creativeCategory
                Class<?> creativeCatClass = Class.forName("cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory");
                Object natureCat = creativeCatClass.getField("NATURE").get(null);
                cbdBuilder.getClass().getMethod("creativeCategory", creativeCatClass)
                        .invoke(cbdBuilder, natureCat);

                // 设置materials/geometry/collisionBox/selectionBox
                cbdBuilder.getClass().getMethod("materials", materialsClass).invoke(cbdBuilder, materials);
                cbdBuilder.getClass().getMethod("geometry", String.class)
                        .invoke(cbdBuilder, "geometry.mine.clear.display");

                // 构建碰撞盒/选择盒
                Class<?> vec3fClass = Class.forName("cn.nukkit.math.Vector3f");
                Object collisionMin = vec3fClass.getConstructor(float.class, float.class, float.class)
                        .newInstance(-8f, 0.0f, -8f);
                Object collisionMax = vec3fClass.getConstructor(float.class, float.class, float.class)
                        .newInstance(16f, 8f, 16f);
                cbdBuilder.getClass().getMethod("collisionBox", vec3fClass, vec3fClass)
                        .invoke(cbdBuilder, collisionMin, collisionMax);

                Object selectMin = vec3fClass.getConstructor(float.class, float.class, float.class)
                        .newInstance(-8.0f, 0.0f, -8.0f);
                Object selectMax = vec3fClass.getConstructor(float.class, float.class, float.class)
                        .newInstance(16.0f, 8f, 16.0f);
                cbdBuilder.getClass().getMethod("selectionBox", vec3fClass, vec3fClass)
                        .invoke(cbdBuilder, selectMin, selectMax);

                // 构建CustomBlockDefinition
                Object customBlockDef = cbdBuilder.getClass().getMethod("build").invoke(cbdBuilder);

                // ========== 3. 注册自定义方块（修复核心） ==========
                Class<?> cbmClass = Class.forName("cn.nukkit.block.custom.CustomBlockManager");
                Object cbmInstance = cbmClass.getMethod("get").invoke(null);

                // 获取BlockContainer类（新版本核心中的容器方块父类）
                Class<?> blockContainerClass = Class.forName("cn.nukkit.block.custom.container.BlockContainer");

                // 显式创建Supplier<BlockContainer>实例（适配泛型）
                Object supplier = java.lang.reflect.Proxy.newProxyInstance(
                        Thread.currentThread().getContextClassLoader(),
                        new Class[]{Class.forName("java.util.function.Supplier")},
                        (proxy, method, args) -> {
                            if ("get".equals(method.getName())) {
                                // 返回MineClearDisplayBlock实例（自动向上转型为BlockContainer）
                                return new MineClearDisplayBlock();
                            }
                            return method.invoke(proxy, args);
                        }
                );

                // 精准获取registerCustomBlock方法
                Method registerMethod = cbmClass.getMethod(
                        "registerCustomBlock",
                        String.class,
                        cbdClass,
                        Class.forName("java.util.function.Supplier")
                );

                // 调用注册方法
                int blockId = (Integer) registerMethod.invoke(
                        cbmInstance,
                        BlockManager.BLOCK_DISPLAY_NAME,
                        customBlockDef,
                        supplier
                );

                System.out.println("新版本自定义方块「底座」注册成功，ID：" + blockId);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("新版本方块注册失败：" + e.getMessage());
            }
        } else {
            // 旧版本：降级为普通方块注册（根据旧版本API调整）
            try {
                // 旧版本Nukkit注册普通方块的逻辑（示例）
                Method registerBlockMethod = Class.forName("cn.nukkit.block.Block")
                        .getMethod("registerBlock", Class.class, String.class, boolean.class);
                registerBlockMethod.invoke(null, MineClearDisplayBlock.class, BlockManager.BLOCK_DISPLAY_NAME, true);
                System.out.println("旧版本普通方块「底座」注册成功");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("旧版本方块注册失败：" + e.getMessage());
            }
        }
    }

    // ========== 保留原有核心功能 ==========
    @Override
    public String getName() {
        return "底座";
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_AXE;
    }

    @Override
    public int getBurnChance() {
        return 5;
    }

    @Override
    public int getBurnAbility() {
        return 20;
    }

    // 旧版本必需：返回方块ID（需在BlockManager中配置）
    @Override
    public int getId() {
        return CustomBlockManager.get().getBlockId(BlockManager.BLOCK_DISPLAY_NAME);
    }

    // 适配新版本equals/hashCode（委托给CustomBlock）
    @Override
    public boolean equals(Object o) {
        if (VersionCompatUtils.hasCustomContainerBlock() && customBlockDelegate != null) {
            try {
                return (Boolean) customBlockDelegate.getClass().getMethod("equals", Object.class)
                        .invoke(customBlockDelegate, o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        if (VersionCompatUtils.hasCustomContainerBlock() && customBlockDelegate != null) {
            try {
                return (Integer) customBlockDelegate.getClass().getMethod("hashCode").invoke(customBlockDelegate);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return Objects.hash(blockName);
    }
}