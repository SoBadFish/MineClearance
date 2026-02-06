package org.sobadfish.mineclear.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityClimateVariant;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class MineClearPieceEntity extends Entity implements CustomEntity {

    public String roomName;

    public static final EntityDefinition DEF_NUMBER =
            EntityDefinition
                    .builder()
                    .identifier("mineclear:piece")
                    //.summonable(true)
                    .spawnEgg(true)
                    .implementation(MineClearPieceEntity.class)
                    .build();





    public MineClearPieceEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        setImmobile();
        setScale(1.8f);
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
        if (roomName != null) {
            this.namedTag.putString("roomName", roomName);
        }
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
