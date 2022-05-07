package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.load.impl.IPersistentProjectileEntity;
import me.dustin.jex.load.impl.IProjectile;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SnowballItem;
import net.minecraft.world.item.ThrowablePotionItem;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.awt.*;
import java.util.ArrayList;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Show a trajectory line for things like bows and snowballs")
public class Trajectories extends Feature {

    @Op(name = "Z-Clip")
    public boolean disableDepth = true;
    @Op(name = "Miss Color", isColor = true)
    public int missColor = new Color(0, 255, 0).getRGB();
    @Op(name = "Hit Color", isColor = true)
    public int hitColor = new Color(255, 0, 0).getRGB();

    private static float getSpeed(ItemStack stack) {
        return stack.getItem() == Items.CROSSBOW && CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    public static boolean isCharged(ItemStack stack) {
        CompoundTag compoundTag = stack.getTag();
        return compoundTag != null && compoundTag.getBoolean("Charged");
    }

    private Entity hitEntity = null;
    private final ArrayList<Vec3> positions = new ArrayList<>();

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (!positions.isEmpty()) {
            PoseStack matrixStack = event.getPoseStack();
            Matrix4f matrix4f = matrixStack.last().pose();
            for (int i = 0; i < positions.size(); i++) {
                if (i != positions.size() - 1) {

                    int color = hitEntity == null ? missColor : hitColor;
                    Color color1 = ColorHelper.INSTANCE.getColor(color);

                    Vec3 vec = positions.get(i);
                    Vec3 vec1 = positions.get(i + 1);
                    double x = vec.x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
                    double y = vec.y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
                    double z = vec.z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;

                    double x1 = vec1.x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().x;
                    double y1 = vec1.y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().y;
                    double z1 = vec1.z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPosition().z;

                    Render3DHelper.INSTANCE.setup3DRender(disableDepth);
                    BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                    bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                    bufferBuilder.vertex(matrix4f, (float) x, (float) y, (float) z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
                    bufferBuilder.vertex(matrix4f, (float) x1, (float) y1, (float) z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
                    bufferBuilder.clear();
                    BufferUploader.drawWithShader(bufferBuilder.end());
                    Render3DHelper.INSTANCE.end3DRender();
                } else {
                    Vec3 vec = Render3DHelper.INSTANCE.getRenderPosition(positions.get(i).x, positions.get(i).y, positions.get(i).z);
                    if (hitEntity != null) {
                        Vec3 vec2 = Render3DHelper.INSTANCE.getEntityRenderPosition(hitEntity, event.getPartialTicks());
                        Render3DHelper.INSTANCE.drawEntityBox(event.getPoseStack(), hitEntity, vec2.x, vec2.y, vec2.z, hitColor);
                    } else {
                        AABB bb1 = new AABB(vec.x - 0.2f, vec.y - 0.2f, vec.z - 0.2f, vec.x + 0.2f, vec.y + 0.2f, vec.z + 0.2f);
                        Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), bb1, missColor);
                    }
                }
            }
        }
    });

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        positions.clear();
        ItemStack mainStack = Wrapper.INSTANCE.getLocalPlayer().getMainHandItem();
        hitEntity = null;
        if (isGoodItem(mainStack)) {
            if (mainStack.getItem() instanceof BowItem) {
                BowItem bowItem = (BowItem) mainStack.getItem();
                int i = bowItem.getUseDuration(mainStack) - Wrapper.INSTANCE.getLocalPlayer().getUseItemRemainingTicks();
                float f = BowItem.getPowerForTime(i);
                if (f == 0)
                    f = 1;
                ItemStack itemStack = new ItemStack(Items.ARROW);
                ArrowItem arrowItem = (ArrowItem) itemStack.getItem();
                AbstractArrow persistentProjectileEntity = arrowItem.createArrow(Wrapper.INSTANCE.getWorld(), itemStack, Wrapper.INSTANCE.getLocalPlayer());
                persistentProjectileEntity.shootFromRotation(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), 0.0F, f * 3.0F, 0);
                for (int j = 0; j < 200; j++) {
                    persistentProjectileEntity.tick();
                    positions.add(persistentProjectileEntity.position());
                    hitEntity = getHitEntity(persistentProjectileEntity);
                    if (hitEntity != null) {
                        break;
                    }
                }
            } else if (mainStack.getItem() instanceof CrossbowItem) {
                if (isCharged(mainStack)) {
                    ItemStack itemStack = new ItemStack(Items.ARROW);
                    ArrowItem arrowItem = (ArrowItem) itemStack.getItem();
                    AbstractArrow persistentProjectileEntity = arrowItem.createArrow(Wrapper.INSTANCE.getWorld(), itemStack, Wrapper.INSTANCE.getLocalPlayer());

                    Vec3 vec3d = Wrapper.INSTANCE.getLocalPlayer().getUpVector(1.0F);
                    Quaternion quaternion = new Quaternion(new Vector3f(vec3d), 0, true);
                    Vec3 vec3d2 = Wrapper.INSTANCE.getLocalPlayer().getViewVector(1.0F);
                    Vector3f vector3f = new Vector3f(vec3d2);
                    vector3f.transform(quaternion);
                    ((Projectile) persistentProjectileEntity).shoot(vector3f.x(), vector3f.y(), vector3f.z(), getSpeed(mainStack), 0);
                    for (int j = 0; j < 200; j++) {
                        persistentProjectileEntity.tick();
                        positions.add(persistentProjectileEntity.position());
                        hitEntity = getHitEntity(persistentProjectileEntity);
                        if (hitEntity != null) {
                            break;
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof SnowballItem) {
                Snowball snowballEntity = new Snowball(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                snowballEntity.setItem(mainStack);
                snowballEntity.shootFromRotation(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), 0.0F, 1.5F, 0);
                IProjectile iProjectile = (IProjectile) (Projectile) snowballEntity;
                for (int j = 0; j < 200; j++) {
                    snowballEntity.tick();
                    positions.add(snowballEntity.position());
                    HitResult hitResult = ProjectileUtil.getHitResult(snowballEntity, iProjectile::callCanHit);
                    if (hitResult != null) {
                        if (hitResult.getType() == HitResult.Type.ENTITY) {
                            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                            hitEntity = entityHitResult.getEntity();
                            if (hitEntity != null)
                                break;
                        } else if (hitResult.getType() != HitResult.Type.MISS) {
                            break;
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof EnderpearlItem) {
                ThrownEnderpearl enderPearlEntity = new ThrownEnderpearl(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                enderPearlEntity.setItem(mainStack);
                enderPearlEntity.shootFromRotation(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), 0.0F, 1.5F, 0);
                IProjectile iProjectile = (IProjectile) (Projectile) enderPearlEntity;
                for (int j = 0; j < 200; j++) {
                    enderPearlEntity.tick();
                    if (WorldHelper.INSTANCE.getBlock(new BlockPos(enderPearlEntity.position())) == Blocks.END_GATEWAY) {
                        hitEntity = Wrapper.INSTANCE.getLocalPlayer();
                    } else {
                        positions.add(enderPearlEntity.position());
                        HitResult hitResult = ProjectileUtil.getHitResult(enderPearlEntity, iProjectile::callCanHit);
                        if (hitResult != null) {
                            if (hitResult.getType() == HitResult.Type.ENTITY) {
                                EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                                hitEntity = entityHitResult.getEntity();
                                if (hitEntity != null)
                                    break;
                            } else if (hitResult.getType() != HitResult.Type.MISS) {
                                break;
                            }
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof ThrowablePotionItem) {
                ThrownPotion potionEntity = new ThrownPotion(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                potionEntity.setItem(mainStack);
                potionEntity.shootFromRotation(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), -20.0F, 0.5F, 0);
                IProjectile iProjectile = (IProjectile) (Projectile) potionEntity;
                for (int j = 0; j < 200; j++) {
                    potionEntity.tick();
                    positions.add(potionEntity.position());
                    HitResult hitResult = ProjectileUtil.getHitResult(potionEntity, iProjectile::callCanHit);
                    if (hitResult != null) {
                        if (hitResult.getType() == HitResult.Type.ENTITY) {
                            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                            hitEntity = entityHitResult.getEntity();
                            if (hitEntity != null)
                                break;
                        } else if (hitResult.getType() != HitResult.Type.MISS) {
                            break;
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof TridentItem) {
                int j1 = EnchantmentHelper.getRiptide(mainStack);
                ThrownTrident tridentEntity = new ThrownTrident(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), mainStack);
                tridentEntity.shootFromRotation(Wrapper.INSTANCE.getLocalPlayer(), PlayerHelper.INSTANCE.getPitch(), PlayerHelper.INSTANCE.getYaw(), 0.0F, 2.5F + (float) j1 * 0.5F, 0);
                for (int j = 0; j < 200; j++) {
                    tridentEntity.tick();
                    positions.add(tridentEntity.position());
                    hitEntity = getHitEntity(tridentEntity);
                    if (hitEntity != null) {
                        break;
                    }
                }
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    private Entity getHitEntity(AbstractArrow persistentProjectileEntity) {
        EntityHitResult entityHitResult = getEntityCollision(persistentProjectileEntity, persistentProjectileEntity.position(), persistentProjectileEntity.position().add(persistentProjectileEntity.getDeltaMovement()));
        if (entityHitResult != null)
            return entityHitResult.getEntity();
        return null;
    }

    protected EntityHitResult getEntityCollision(AbstractArrow persistentProjectileEntity, Vec3 currentPosition, Vec3 nextPosition) {
        IPersistentProjectileEntity iPersistentProjectileEntity = (IPersistentProjectileEntity) persistentProjectileEntity;
        return ProjectileUtil.getEntityHitResult(persistentProjectileEntity.level, persistentProjectileEntity, currentPosition, nextPosition, persistentProjectileEntity.getBoundingBox().expandTowards(persistentProjectileEntity.getDeltaMovement()).inflate(1.0D), iPersistentProjectileEntity::callCanHit);
    }

    private boolean isGoodItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItem() == Items.AIR)
            return false;
        else
            return itemStack.getItem() == Items.BOW || itemStack.getItem() == Items.CROSSBOW || itemStack.getItem() == Items.SNOWBALL || itemStack.getItem() == Items.ENDER_PEARL || itemStack.getItem() == Items.TRIDENT || itemStack.getItem() instanceof ThrowablePotionItem;
    }

}
