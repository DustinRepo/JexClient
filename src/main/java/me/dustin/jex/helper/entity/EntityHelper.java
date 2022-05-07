package me.dustin.jex.helper.entity;

import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.Dolphin;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Ocelot;
import net.minecraft.world.entity.animal.Panda;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Squid;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.allay.Allay;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.piglin.PiglinArmPose;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeableArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.UUID;

public enum EntityHelper {
    INSTANCE;

    public boolean isAuraBlocking() {
        if (AutoEat.isEating)
            return false;
        KillAura killaura = Feature.get(KillAura.class);
        if (killaura.getState()) {
            for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
                if (killaura.isValid(entity, false) && (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= killaura.autoblockDistance || Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= killaura.reach)) {
                    return killaura.autoBlock && Wrapper.INSTANCE.getLocalPlayer().getOffhandItem() != null && Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() instanceof ShieldItem;
                }
            }
        }
        if (Wrapper.INSTANCE.getLocalPlayer().isUsingItem())
            return Wrapper.INSTANCE.getLocalPlayer().getUseItem() != null && Wrapper.INSTANCE.getLocalPlayer().getUseItem().getItem() instanceof ShieldItem;
        return false;
    }

    public boolean isPassiveMob(Entity entity) {
        return !(entity instanceof Hoglin) && doesPlayerOwn(entity) || entity instanceof Allay || entity instanceof WanderingTrader || entity instanceof AbstractFish || entity instanceof Dolphin || entity instanceof Squid || entity instanceof Bat || entity instanceof Villager || entity instanceof Ocelot || entity instanceof Horse || entity instanceof Animal;
    }

    public boolean isNeutralMob(Entity entity) {
        return entity instanceof ZombifiedPiglin || entity instanceof Bee || entity instanceof Piglin || entity instanceof Panda || entity instanceof Wolf || entity instanceof PolarBear || entity instanceof IronGolem || entity instanceof EnderMan;
    }

    public boolean isHostileMob(Entity entity) {
        if (isNeutralMob(entity))
            return isAngryAtPlayer(entity);
        return entity instanceof Shulker || entity instanceof Hoglin || entity instanceof Ghast || entity instanceof Monster || entity instanceof Slime || entity instanceof EnderDragon || entity instanceof Phantom;
    }

    public boolean doesPlayerOwn(Entity entity) {
        return doesPlayerOwn(entity, Wrapper.INSTANCE.getLocalPlayer());
    }

    public boolean doesPlayerOwn(Entity entity, Player playerEntity) {
        if (entity instanceof LivingEntity)
            return getOwnerUUID((LivingEntity)entity) != null && getOwnerUUID((LivingEntity)entity).toString().equals(playerEntity.getUUID().toString());
        return false;
    }

    public UUID getOwnerUUID(LivingEntity livingEntity) {
        if (livingEntity instanceof TamableAnimal tameableEntity) {
            if (tameableEntity.isTame()) {
                return tameableEntity.getOwnerUUID();
            }
        }
        if (livingEntity instanceof AbstractHorse horseBaseEntity) {
            return horseBaseEntity.getOwnerUUID();
        }
        return null;
    }

    public boolean canBreed(Animal entity) {
        return !entity.isBaby() && entity.canFallInLove() && entity.isFood(Wrapper.INSTANCE.getLocalPlayer().getMainHandItem());
    }

    public boolean canPlayerSprint() {
        return Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getFoodData().getFoodLevel() > 6 && !Wrapper.INSTANCE.getLocalPlayer().horizontalCollision;
    }

    public boolean isOnSameTeam(Player player_1, Player player_2, boolean armor) {
        String all = "0123456789abcdef";
        for (int i = 0; i < all.length(); i++) {
            char s = all.charAt(i);
            if (player_1.getDisplayName().getString().toLowerCase().startsWith("§" + s) && player_2.getDisplayName().getString().toLowerCase().startsWith("§" + s)) {
                return true;
            }
        }
        if (armor) {
            ItemStack player_1Armor = InventoryHelper.INSTANCE.getInventory(player_1).getArmor(3);
            ItemStack player_2Armor = InventoryHelper.INSTANCE.getInventory(player_2).getArmor(3);
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

    public boolean canSee(Entity entity, BlockPos blockPos) {
        Vec3 vec3d = new Vec3(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3 vec3d2 = new Vec3(blockPos.getX(), blockPos.getY() + 0.5f, blockPos.getZ());
        return Wrapper.INSTANCE.getWorld().clip(new ClipContext(vec3d, vec3d2, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
    }

    public float distanceFromGround(Entity entity) {
        float dist = 9999;
        float pitch = getPitch(entity);
        setPitch(entity, 90);
        HitResult result = Wrapper.INSTANCE.getLocalPlayer().pick(256, 1, false);// Wrapper.clientWorld().rayTraceBlock(getVec(entity), getVec(entity).add(0, -256, 0), false, true, false);
        if (result != null)
            dist = ClientMathHelper.INSTANCE.getDistance(ClientMathHelper.INSTANCE.getVec(entity), result.getLocation());
        setPitch(entity, pitch);
        if (dist > 256 || dist < 0)
            dist = 0;
        return dist;
    }

    public boolean isNPC(Player player) {
        if (player instanceof FakePlayerEntity)
            return false;
        try {
            PlayerInfo p = Wrapper.INSTANCE.getLocalPlayer().connection.getPlayerInfo(player.getUUID());
            if (p.getGameMode().isSurvival() || p.getGameMode().isCreative()) {
                return false;
            }
        } catch (Exception e) {
            return true;
        }
        return true;
    }

    public boolean isAngryAtPlayer(Entity entity) {
        if (entity instanceof Bee bee && (bee.getPersistentAngerTarget() == Wrapper.INSTANCE.getLocalPlayer().getUUID() || (bee.getPersistentAngerTarget() == null && (bee.isAggressive()))))
            return true;
        if (entity instanceof Piglin piglinEntity && (piglinEntity.getArmPose() == PiglinArmPose.ATTACKING_WITH_MELEE_WEAPON || piglinEntity.getArmPose() == PiglinArmPose.CROSSBOW_CHARGE || piglinEntity.getArmPose() == PiglinArmPose.CROSSBOW_HOLD))
            return true;
        if (entity instanceof ZombifiedPiglin zombifiedPiglinEntity && (zombifiedPiglinEntity.getPersistentAngerTarget() == Wrapper.INSTANCE.getLocalPlayer().getUUID() || (zombifiedPiglinEntity.getPersistentAngerTarget() == null && (zombifiedPiglinEntity.getRemainingPersistentAngerTime() > 0))))
            return true;
        if (entity instanceof Panda pandaEntity && pandaEntity.isAggressive())
            return true;
        if (entity instanceof PolarBear polarBearEntity && (polarBearEntity.getPersistentAngerTarget() == Wrapper.INSTANCE.getLocalPlayer().getUUID() || (polarBearEntity.getPersistentAngerTarget() == null && polarBearEntity.isAggressive())))
            return true;
        if (entity instanceof EnderMan endermanEntity && (endermanEntity.getPersistentAngerTarget() == Wrapper.INSTANCE.getLocalPlayer().getUUID() || (endermanEntity.getPersistentAngerTarget() == null && (endermanEntity.isCreepy()))))
            return true;
        if (entity instanceof IronGolem ironGolemEntity && ironGolemEntity.getPersistentAngerTarget() == Wrapper.INSTANCE.getLocalPlayer().getUUID())
            return true;
        if (entity instanceof Wolf wolf && (wolf.isAggressive() && !doesPlayerOwn(wolf)))
            return true;
        return false;
    }
    
    public float getYaw(Entity entity) {
        return entity.getViewYRot(Wrapper.INSTANCE.getMinecraft().getFrameTime());
    }

    public float getPitch(Entity entity) {
        return entity.getViewXRot(Wrapper.INSTANCE.getMinecraft().getFrameTime());
    }

    public void setYaw(Entity entity, float yaw) {
        entity.setYRot(yaw);
    }

    public void setPitch(Entity entity, float pitch) {
        entity.setXRot(pitch);
    }

    public void addYaw(Entity entity, float add) {
        setYaw(entity, getYaw(entity) + add);
    }

    public void addPitch(Entity entity, float add) {
        setPitch(entity,getPitch(entity) + add);
    }

}
