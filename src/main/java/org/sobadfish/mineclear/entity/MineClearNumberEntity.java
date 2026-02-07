package org.sobadfish.mineclear.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityClimateVariant;
import cn.nukkit.entity.custom.CustomEntity;
import cn.nukkit.entity.custom.EntityDefinition;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public class MineClearNumberEntity extends Entity implements CustomEntity, EntityClimateVariant {

    public static final EntityDefinition DEF_NUMBER =
            EntityDefinition
                    .builder()
                    .identifier("mineclear:number")
                    //.summonable(true)
                    .spawnEgg(true)
                    .implementation(MineClearNumberEntity.class)
                    .build();

    /**
     * 对应的数值
     * */
    public int number;




    public MineClearNumberEntity(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        setImmobile();
        if(this.namedTag.contains("number")){
            this.number = this.namedTag.getInt("number");
        }

        if(this.namedTag.contains("roomName")){
            this.roomName = this.namedTag.getString("roomName");
        }
        setScale(0.7f);

    }

    public String roomName;
    @Override
    protected void initEntity() {
        super.initEntity();
        if (this.namedTag.contains("variant")) {
            this.setVariant(Variant.get(this.namedTag.getString("variant")));
        } else {
            this.setVariant(this.getBiomeVariant(this.getLevel().getBiomeId(this.getFloorX(), this.getFloorZ())));
        }
        if(this.namedTag.contains("number")){
            this.number = this.namedTag.getInt("number");
        }

        setNumberModel(number);
        if(this.namedTag.contains("roomName")){
            this.roomName = this.namedTag.getString("roomName");
        }
    }

    @Override
    public EntityDefinition getEntityDefinition() {
        return DEF_NUMBER;
    }


    @Override
    public void saveNBT() {
//        super.saveNBT();
//        if (roomName != null) {
//            this.namedTag.putString("roomName", roomName);
//        }
//        this.namedTag.putInt("number", number);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return false;
    }

    public void setNumberModel(int number){
        this.number = number;
        this.number = Math.max(1,this.number);
        this.number = Math.min(8,this.number);
//        this.setDataFlag(0, 9, hasFish);

        this.setDataProperty(new IntEntityData(2,this.number - 1));
    }

    @Override
    public int getNetworkId() {
        return DEF_NUMBER.getRuntimeId();
    }
}
