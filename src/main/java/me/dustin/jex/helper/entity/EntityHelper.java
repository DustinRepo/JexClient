package me.dustin.jex.helper.entity;

import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.combat.killaura.Killaura;
import me.dustin.jex.module.impl.player.AutoEat;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.HitResult;

import java.util.UUID;

public enum EntityHelper {
    INSTANCE;

    public boolean isAuraBlocking() {
        if (AutoEat.isEating)
            return false;
        Killaura killaura = (Killaura) Module.get(Killaura.class);
        if (killaura.getState()) {
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (killaura.isValid(entity, false) && (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= killaura.autoblockDistance || Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= killaura.reach)) {
                    return killaura.autoBlock && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() instanceof ShieldItem;
                }
            }
        }
        if (Wrapper.INSTANCE.getLocalPlayer().isUsingItem())
            if (Wrapper.INSTANCE.getLocalPlayer().getActiveItem() != null && Wrapper.INSTANCE.getLocalPlayer().getActiveItem().getItem() instanceof ShieldItem)
                return true;
        return false;
    }

    public boolean isPassiveMob(Entity entity) {
        if (isNeutralMob(entity))
            return !isAngryAtPlayer(entity);
        return doesPlayerOwn(entity) || entity instanceof WanderingTraderEntity || entity instanceof FishEntity || entity instanceof DolphinEntity || entity instanceof SquidEntity || entity instanceof BatEntity || entity instanceof VillagerEntity || entity instanceof OcelotEntity || entity instanceof HorseEntity || entity instanceof AnimalEntity;
    }

    public boolean isNeutralMob(Entity entity) {
        return entity instanceof ZombifiedPiglinEntity || entity instanceof BeeEntity || entity instanceof PiglinEntity || entity instanceof PandaEntity || entity instanceof WolfEntity || entity instanceof PolarBearEntity || entity instanceof IronGolemEntity || entity instanceof EndermanEntity;
    }

    public boolean doesPlayerOwn(Entity entity) {
        return doesPlayerOwn(entity, Wrapper.INSTANCE.getLocalPlayer());
    }

    public boolean doesPlayerOwn(Entity entity, PlayerEntity playerEntity) {
        if (entity instanceof LivingEntity)
            return getOwnerUUID((LivingEntity)entity) != null && getOwnerUUID((LivingEntity)entity).toString().equals(playerEntity.getUuid().toString());
        return false;
    }

    public UUID getOwnerUUID(LivingEntity livingEntity) {
        if (livingEntity instanceof TameableEntity) {
            TameableEntity tameableEntity = (TameableEntity) livingEntity;
            if (tameableEntity.isTamed()) {
                return tameableEntity.getOwnerUuid();
            }
        }
        if (livingEntity instanceof HorseBaseEntity) {
            HorseBaseEntity horseBaseEntity = (HorseBaseEntity) livingEntity;
            return horseBaseEntity.getOwnerUuid();
        }
        return null;
    }

    public boolean isHostileMob(Entity entity) {
        if (isNeutralMob(entity))
            return isAngryAtPlayer(entity);
        return entity instanceof ShulkerEntity || entity instanceof GhastEntity || entity instanceof HostileEntity || entity instanceof SlimeEntity || entity instanceof EnderDragonEntity || entity instanceof PhantomEntity;
    }

    public boolean canBreed(AnimalEntity entity) {
        return !entity.isBaby() && entity.canEat() && entity.isBreedingItem(Wrapper.INSTANCE.getLocalPlayer().getMainHandStack());
    }

    public boolean canPlayerSprint() {
        return Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getHungerManager().getFoodLevel() > 6 && !Wrapper.INSTANCE.getLocalPlayer().horizontalCollision;
    }

    public boolean isOnSameTeam(PlayerEntity player_1, PlayerEntity player_2, boolean armor) {
        String all = "0123456789abcdef";
        for (int i = 0; i < all.length(); i++) {
            char s = all.charAt(i);
            if (player_1.getDisplayName().getString().toLowerCase().startsWith("§" + s) && player_2.getDisplayName().getString().toLowerCase().startsWith("§" + s)) {
                return true;
            }
        }
        if (armor) {
            ItemStack player_1Armor = player_1.inventory.getArmorStack(3);
            ItemStack player_2Armor = player_2.inventory.getArmorStack(3);
            if (player_1Armor != null && player_1Armor.getItem() instanceof ArmorItem && player_2Armor != null && player_2Armor.getItem() instanceof ArmorItem) {
                ArmorItem armorItemP1 = (ArmorItem) player_1Armor.getItem();
                ArmorItem armorItemP2 = (ArmorItem) player_2Armor.getItem();
                if (armorItemP1.getMaterial() == ArmorMaterials.LEATHER && armorItemP2.getMaterial() == ArmorMaterials.LEATHER) {
                    int colorP1 = ((DyeableArmorItem) armorItemP1).getColor(player_1Armor);
                    int colorP2 = ((DyeableArmorItem) armorItemP2).getColor(player_2Armor);
                    if (colorP1 == colorP2)
                        return true;
                }
            }
        }
        return false;
    }

    public float distanceFromGround(Entity entity) {
        float dist = 9999;
        float pitch = Wrapper.INSTANCE.getLocalPlayer().pitch;
        Wrapper.INSTANCE.getLocalPlayer().pitch = 90;
        HitResult result = Wrapper.INSTANCE.getLocalPlayer().raycast(256, 1, false);// Wrapper.clientWorld().rayTraceBlock(getVec(entity), getVec(entity).add(0, -256, 0), false, true, false);
        if (result != null)
            dist = ClientMathHelper.INSTANCE.getDistance(ClientMathHelper.INSTANCE.getVec(entity), result.getPos());
        Wrapper.INSTANCE.getLocalPlayer().pitch = pitch;
        if (dist > 256 || dist < 0)
            dist = 0;
        return dist;
    }

    public boolean isNPC(PlayerEntity player) {
        try {
            PlayerListEntry p = Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerListEntry(player.getUuid());
            if (p.getGameMode().isSurvivalLike() || p.getGameMode().isCreative()) {
                return false;
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    public boolean isAngryAtPlayer(Entity entity) {
        if (entity instanceof BeeEntity && (((BeeEntity) entity).getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid() || (((BeeEntity) entity).getAngryAt() == null && ((BeeEntity) entity).isAttacking())))
            return true;
        if (entity instanceof PiglinEntity && (((PiglinEntity) entity).isAngryAt(Wrapper.INSTANCE.getLocalPlayer())))
            return true;
        if (entity instanceof ZombifiedPiglinEntity/* && ((ZombifiedPiglinEntity) entity).shouldAngerAt(Wrapper.INSTANCE.getLocalPlayer())*/)
            return true;
        if (entity instanceof PandaEntity && ((PandaEntity) entity).isAttacking())
            return true;
        if (entity instanceof PolarBearEntity && (((PolarBearEntity) entity).getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid() || (((PolarBearEntity) entity).getAngryAt() == null && ((PolarBearEntity) entity).isAttacking())))
            return true;
        if (entity instanceof EndermanEntity && (((EndermanEntity) entity).getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid() || (((EndermanEntity) entity).getAngryAt() == null && ((EndermanEntity) entity).isAngry())))
            return true;
        if (entity instanceof IronGolemEntity && (((IronGolemEntity) entity).getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid() || (((IronGolemEntity) entity).getAngryAt() == null && ((IronGolemEntity) entity).isAttacking())))
            return true;
        if (entity instanceof WolfEntity && ((WolfEntity) entity).isAttacking() && !doesPlayerOwn(entity))
            return true;
        return false;
    }

}
