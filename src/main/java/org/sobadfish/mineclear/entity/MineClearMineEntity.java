package org.sobadfish.mineclear.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class MineClearMineEntity extends Entity implements CustomEntity {

    public String roomName;


    public static final EntityDefinition DEF_NUMBER =
            EntityDefinition
                    .builder()
                    .identifier("mineclear:mine")
                    //.summonable(true)
                    .spawnEgg(true)
                    .implementation(MineClearMineEntity.class)
                    .build();





    public MineClearMineEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        setImmobile();
        if(this.namedTag.contains("roomName")){
            this.roomName = this.namedTag.getString("roomName");
        }

    }

    @Override
    protected void initEntity() {
        super.initEntity();
        if(this.namedTag.contains("roomName")){
            this.roomName = this.namedTag.getString("roomName");
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        this.namedTag.putString("roomName",roomName);
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return DEF_NUMBER;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return false;
    }


    @Override
    public int getNetworkId() {
        return DEF_NUMBER.getRuntimeId();
    }
}
