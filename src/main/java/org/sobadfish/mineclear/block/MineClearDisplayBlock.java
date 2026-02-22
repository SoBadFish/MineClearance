package org.sobadfish.mineclear.block;

import cn.nukkit.block.custom.CustomBlockDefinition;
import cn.nukkit.block.custom.CustomBlockManager;
import cn.nukkit.block.custom.container.CustomBlock;
import cn.nukkit.block.custom.container.data.Materials;
import cn.nukkit.item.ItemTool;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.types.inventory.creative.CreativeItemCategory;
import org.sobadfish.mineclear.manager.BlockManager;

public class MineClearDisplayBlock extends CustomBlock {


    public MineClearDisplayBlock() {
        super(BlockManager.BLOCK_DISPLAY_NAME);
    }


    public static void register(){
        Materials materials = Materials.builder()
                .any(Materials.RenderMethod.OPAQUE, "block_display");


        CustomBlockManager.get().registerCustomBlock(
                BlockManager.BLOCK_DISPLAY_NAME,
                CustomBlockDefinition.builder(new MineClearDisplayBlock())
                        .creativeCategory(CreativeItemCategory.NATURE)
                        .materials(materials)
                        .geometry("geometry.mine.clear.display")
                        .collisionBox(
                                new Vector3f(-8, 0.0f, -8),
                                new Vector3f(16, 8, 16)
                        )
                        .selectionBox(
                                new Vector3f(-8.0f, 0.0f, -8.0f),
                                new Vector3f(16.0f, 8, 16.0f)
                        )

                        .build(),

                MineClearDisplayBlock::new
        );

    }

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


}
