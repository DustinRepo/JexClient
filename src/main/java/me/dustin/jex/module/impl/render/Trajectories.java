package me.dustin.jex.module.impl.render;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.load.impl.IPersistentProjectileEntity;
import me.dustin.jex.load.impl.IProjectileEntity;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.client.render.*;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.awt.*;
import java.util.ArrayList;

@ModClass(name = "Trajectories", category = ModCategory.VISUAL, description = "Show a trajectory line for things like bows and snowballs")
public class Trajectories extends Module {

    @Op(name = "Miss Color", isColor = true)
    public int missColor = new Color(0, 255, 0).getRGB();
    @Op(name = "Hit Color", isColor = true)
    public int hitColor = new Color(255, 0, 0).getRGB();

    private static float getSpeed(ItemStack stack) {
        return stack.getItem() == Items.CROSSBOW && CrossbowItem.hasProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
    }

    public static boolean isCharged(ItemStack stack) {
        NbtCompound compoundTag = stack.getTag();
        return compoundTag != null && compoundTag.getBoolean("Charged");
    }

    @EventListener(events = {EventRender3D.class})
    private void runMethod(EventRender3D eventRender3D) {
        ArrayList<Vec3d> positions = new ArrayList<>();
        ItemStack mainStack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
        Entity hitEntity = null;
        if (isGoodItem(mainStack)) {
            if (mainStack.getItem() instanceof BowItem) {
                BowItem bowItem = (BowItem) mainStack.getItem();
                int i = bowItem.getMaxUseTime(mainStack) - Wrapper.INSTANCE.getLocalPlayer().getItemUseTimeLeft();
                float f = bowItem.getPullProgress(i);
                if (f == 0)
                    f = 1;
                ItemStack itemStack = new ItemStack(Items.ARROW);
                ArrowItem arrowItem = (ArrowItem) itemStack.getItem();
                PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(Wrapper.INSTANCE.getWorld(), itemStack, Wrapper.INSTANCE.getLocalPlayer());
                persistentProjectileEntity.setProperties(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getLocalPlayer().pitch, Wrapper.INSTANCE.getLocalPlayer().yaw, 0.0F, f * 3.0F, 0);
                for (int j = 0; j < 200; j++) {
                    persistentProjectileEntity.tick();
                    positions.add(persistentProjectileEntity.getPos());
                    hitEntity = getHitEntity(persistentProjectileEntity);
                    if (hitEntity != null) {
                        break;
                    }
                }
            } else if (mainStack.getItem() instanceof CrossbowItem) {
                if (isCharged(mainStack)) {
                    ItemStack itemStack = new ItemStack(Items.ARROW);
                    ArrowItem arrowItem = (ArrowItem) itemStack.getItem();
                    PersistentProjectileEntity persistentProjectileEntity = arrowItem.createArrow(Wrapper.INSTANCE.getWorld(), itemStack, Wrapper.INSTANCE.getLocalPlayer());

                    Vec3d vec3d = Wrapper.INSTANCE.getLocalPlayer().getOppositeRotationVector(1.0F);
                    Quaternion quaternion = new Quaternion(new Vec3f(vec3d), 0, true);
                    Vec3d vec3d2 = Wrapper.INSTANCE.getLocalPlayer().getRotationVec(1.0F);
                    Vec3f vector3f = new Vec3f(vec3d2);
                    vector3f.rotate(quaternion);
                    ((ProjectileEntity) persistentProjectileEntity).setVelocity((double) vector3f.getX(), (double) vector3f.getY(), (double) vector3f.getZ(), getSpeed(mainStack), 0);
                    for (int j = 0; j < 200; j++) {
                        persistentProjectileEntity.tick();
                        positions.add(persistentProjectileEntity.getPos());
                        hitEntity = getHitEntity(persistentProjectileEntity);
                        if (hitEntity != null) {
                            break;
                        }
                    }
                }
            } else if (mainStack.getItem() instanceof SnowballItem) {
                SnowballEntity snowballEntity = new SnowballEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                snowballEntity.setItem(mainStack);
                snowballEntity.setProperties(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getLocalPlayer().pitch, Wrapper.INSTANCE.getLocalPlayer().yaw, 0.0F, 1.5F, 0);
                IProjectileEntity iProjectileEntity = (IProjectileEntity) (ProjectileEntity) snowballEntity;
                for (int j = 0; j < 200; j++) {
                    snowballEntity.tick();
                    positions.add(snowballEntity.getPos());
                    HitResult hitResult = ProjectileUtil.getCollision(snowballEntity, iProjectileEntity::method);
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
            } else if (mainStack.getItem() instanceof EnderPearlItem) {
                EnderPearlEntity enderPearlEntity = new EnderPearlEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                enderPearlEntity.setItem(mainStack);
                enderPearlEntity.setProperties(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getLocalPlayer().pitch, Wrapper.INSTANCE.getLocalPlayer().yaw, 0.0F, 1.5F, 0);
                IProjectileEntity iProjectileEntity = (IProjectileEntity) (ProjectileEntity) enderPearlEntity;
                for (int j = 0; j < 200; j++) {
                    enderPearlEntity.tick();
                    positions.add(enderPearlEntity.getPos());
                    HitResult hitResult = ProjectileUtil.getCollision(enderPearlEntity, iProjectileEntity::method);
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
            } else if (mainStack.getItem() instanceof ThrowablePotionItem) {
                PotionEntity potionEntity = new PotionEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer());
                potionEntity.setItem(mainStack);
                potionEntity.setProperties(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getLocalPlayer().pitch, Wrapper.INSTANCE.getLocalPlayer().yaw, -20.0F, 0.5F, 0);
                IProjectileEntity iProjectileEntity = (IProjectileEntity) (ProjectileEntity) potionEntity;
                for (int j = 0; j < 200; j++) {
                    potionEntity.tick();
                    positions.add(potionEntity.getPos());
                    HitResult hitResult = ProjectileUtil.getCollision(potionEntity, iProjectileEntity::method);
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
                TridentEntity tridentEntity = new TridentEntity(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), mainStack);
                tridentEntity.setProperties(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getLocalPlayer().pitch, Wrapper.INSTANCE.getLocalPlayer().yaw, 0.0F, 2.5F + (float) j1 * 0.5F, 0);
                for (int j = 0; j < 200; j++) {
                    tridentEntity.tick();
                    positions.add(tridentEntity.getPos());
                    hitEntity = getHitEntity(tridentEntity);
                    if (hitEntity != null) {
                        break;
                    }
                }
            }

            if (!positions.isEmpty()) {
                for (int i = 0; i < positions.size(); i++) {
                    if (i != positions.size() - 1) {

                        int color = hitEntity == null ? missColor : hitColor;
                        Color color1 = ColorHelper.INSTANCE.getColor(color);

                        Vec3d vec = positions.get(i);
                        Vec3d vec1 = positions.get(i + 1);
                        double x = vec.x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
                        double y = vec.y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
                        double z = vec.z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;

                        double x1 = vec1.x - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().x;
                        double y1 = vec1.y - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().y;
                        double z1 = vec1.z - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().z;

                        Render3DHelper.INSTANCE.setup3DRender(true);

                        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                        bufferBuilder.begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);
                        bufferBuilder.vertex(x, y, z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.vertex(x1, y1, z1).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).next();
                        bufferBuilder.end();
                        BufferRenderer.draw(bufferBuilder);
                        Render3DHelper.INSTANCE.end3DRender();
                    } else {
                        Vec3d vec = Render3DHelper.INSTANCE.getRenderPosition(positions.get(i).x, positions.get(i).y, positions.get(i).z);
                        if (hitEntity != null) {
                            Vec3d vec2 = Render3DHelper.INSTANCE.getEntityRenderPosition(hitEntity, eventRender3D.getPartialTicks());
                            Render3DHelper.INSTANCE.drawEntityBox(eventRender3D.getMatrixStack(), hitEntity, vec2.x, vec2.y, vec2.z, hitColor);
                        } else {
                            Box bb1 = new Box(vec.x - 0.2f, vec.y - 0.2f, vec.z - 0.2f, vec.x + 0.2f, vec.y + 0.2f, vec.z + 0.2f);
                            Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), bb1, missColor);
                        }
                    }
                }
            }
        }
    }

    private Entity getHitEntity(PersistentProjectileEntity persistentProjectileEntity) {
        EntityHitResult entityHitResult = getEntityCollision(persistentProjectileEntity, persistentProjectileEntity.getPos(), persistentProjectileEntity.getPos().add(persistentProjectileEntity.getVelocity()));
        if (entityHitResult != null)
            return entityHitResult.getEntity();
        return null;
    }

    protected EntityHitResult getEntityCollision(PersistentProjectileEntity persistentProjectileEntity, Vec3d currentPosition, Vec3d nextPosition) {
        IPersistentProjectileEntity iPersistentProjectileEntity = (IPersistentProjectileEntity) persistentProjectileEntity;
        return ProjectileUtil.getEntityCollision(persistentProjectileEntity.world, persistentProjectileEntity, currentPosition, nextPosition, persistentProjectileEntity.getBoundingBox().stretch(persistentProjectileEntity.getVelocity()).expand(1.0D), iPersistentProjectileEntity::method);
    }

    private boolean isGoodItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getItem() == Items.AIR)
            return false;
        else
            return itemStack.getItem() == Items.BOW || itemStack.getItem() == Items.CROSSBOW || itemStack.getItem() == Items.SNOWBALL || itemStack.getItem() == Items.ENDER_PEARL || itemStack.getItem() == Items.TRIDENT || itemStack.getItem() instanceof ThrowablePotionItem;
    }

}
