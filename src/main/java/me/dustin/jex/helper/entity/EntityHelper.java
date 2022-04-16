package me.dustin.jex.helper.entity;

import me.dustin.jex.JexClient;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.combat.killaura.KillAura;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;

import java.util.UUID;

public enum EntityHelper {
    INSTANCE;

    public boolean isAuraBlocking() {
        if (AutoEat.isEating)
            return false;
        KillAura killaura = Feature.get(KillAura.class);
        if (killaura.getState()) {
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (killaura.isValid(entity, false) && (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= killaura.autoblockDistance || Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) <= killaura.reach)) {
                    return killaura.autoBlock && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() instanceof ShieldItem;
                }
            }
        }
        if (Wrapper.INSTANCE.getLocalPlayer().isUsingItem())
            return Wrapper.INSTANCE.getLocalPlayer().getActiveItem() != null && Wrapper.INSTANCE.getLocalPlayer().getActiveItem().getItem() instanceof ShieldItem;
        return false;
    }

    public boolean isPassiveMob(Entity entity) {
        return !(entity instanceof HoglinEntity) && doesPlayerOwn(entity) || entity instanceof AllayEntity || entity instanceof WanderingTraderEntity || entity instanceof FishEntity || entity instanceof DolphinEntity || entity instanceof SquidEntity || entity instanceof BatEntity || entity instanceof VillagerEntity || entity instanceof OcelotEntity || entity instanceof HorseEntity || entity instanceof AnimalEntity;
    }

    public boolean isNeutralMob(Entity entity) {
        return entity instanceof ZombifiedPiglinEntity || entity instanceof BeeEntity || entity instanceof PiglinEntity || entity instanceof PandaEntity || entity instanceof WolfEntity || entity instanceof PolarBearEntity || entity instanceof IronGolemEntity || entity instanceof EndermanEntity;
    }

    public boolean isHostileMob(Entity entity) {
        if (isNeutralMob(entity))
            return isAngryAtPlayer(entity);
        return entity instanceof ShulkerEntity || entity instanceof HoglinEntity || entity instanceof GhastEntity || entity instanceof HostileEntity || entity instanceof SlimeEntity || entity instanceof EnderDragonEntity || entity instanceof PhantomEntity;
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
        if (livingEntity instanceof TameableEntity tameableEntity) {
            if (tameableEntity.isTamed()) {
                return tameableEntity.getOwnerUuid();
            }
        }
        if (livingEntity instanceof HorseBaseEntity horseBaseEntity) {
            return horseBaseEntity.getOwnerUuid();
        }
        return null;
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
            ItemStack player_1Armor = InventoryHelper.INSTANCE.getInventory(player_1).getArmorStack(3);
            ItemStack player_2Armor = InventoryHelper.INSTANCE.getInventory(player_2).getArmorStack(3);
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
        Vec3d vec3d = new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
        Vec3d vec3d2 = new Vec3d(blockPos.getX(), blockPos.getY() + 0.5f, blockPos.getZ());
        return Wrapper.INSTANCE.getWorld().raycast(new RaycastContext(vec3d, vec3d2, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity)).getType() == HitResult.Type.MISS;
    }

    public float distanceFromGround(Entity entity) {
        float dist = 9999;
        float pitch = getPitch(entity);
        setPitch(entity, 90);
        HitResult result = Wrapper.INSTANCE.getLocalPlayer().raycast(256, 1, false);// Wrapper.clientWorld().rayTraceBlock(getVec(entity), getVec(entity).add(0, -256, 0), false, true, false);
        if (result != null)
            dist = ClientMathHelper.INSTANCE.getDistance(ClientMathHelper.INSTANCE.getVec(entity), result.getPos());
        setPitch(entity, pitch);
        if (dist > 256 || dist < 0)
            dist = 0;
        return dist;
    }

    public boolean isNPC(PlayerEntity player) {
        if (player instanceof FakePlayerEntity)
            return false;
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
        if (entity instanceof BeeEntity bee && (bee.getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid() || (bee.getAngryAt() == null && (bee.isAttacking()))))
            return true;
        if (entity instanceof PiglinEntity piglinEntity && (piglinEntity.getActivity() == PiglinActivity.ATTACKING_WITH_MELEE_WEAPON || piglinEntity.getActivity() == PiglinActivity.CROSSBOW_CHARGE || piglinEntity.getActivity() == PiglinActivity.CROSSBOW_HOLD))
            return true;
        if (entity instanceof ZombifiedPiglinEntity zombifiedPiglinEntity && (zombifiedPiglinEntity.getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid() || (zombifiedPiglinEntity.getAngryAt() == null && (zombifiedPiglinEntity.getAngerTime() > 0))))
            return true;
        if (entity instanceof PandaEntity pandaEntity && pandaEntity.isAttacking())
            return true;
        if (entity instanceof PolarBearEntity polarBearEntity && (polarBearEntity.getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid() || (polarBearEntity.getAngryAt() == null && polarBearEntity.isAttacking())))
            return true;
        if (entity instanceof EndermanEntity endermanEntity && (endermanEntity.getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid() || (endermanEntity.getAngryAt() == null && (endermanEntity.isAngry()))))
            return true;
        if (entity instanceof IronGolemEntity ironGolemEntity && ironGolemEntity.getAngryAt() == Wrapper.INSTANCE.getLocalPlayer().getUuid())
            return true;
        if (entity instanceof WolfEntity wolf && (wolf.isAttacking() && !doesPlayerOwn(wolf)))
            return true;
        return false;
    }
    
    public float getYaw(Entity entity) {
        return entity.getYaw(Wrapper.INSTANCE.getMinecraft().getTickDelta());
    }

    public float getPitch(Entity entity) {
        return entity.getPitch(Wrapper.INSTANCE.getMinecraft().getTickDelta());
    }

    public void setYaw(Entity entity, float yaw) {
        entity.setYaw(yaw);
    }

    public void setPitch(Entity entity, float pitch) {
        entity.setPitch(pitch);
    }

    public void addYaw(Entity entity, float add) {
        setYaw(entity, getYaw(entity) + add);
    }

    public void addPitch(Entity entity, float add) {
        setPitch(entity,getPitch(entity) + add);
    }

}
