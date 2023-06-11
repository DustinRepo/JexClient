package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;

public class Roaster extends Feature {
    
    public final Property<Integer> distanceProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Distance")
            .value(4)
            .min(2)
            .max(6)
            .inc(1)
            .build();
	public final Property<Boolean> playerProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Player")
            .value(true)
            .build();
    public final Property<Boolean> friendProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Friends")
            .value(true)
            .parent(playerProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> neutralProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Neutral")
            .value(false)
            .build();
    public final Property<Boolean> bossProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Boss")
            .value(true)
            .build();
    public final Property<Boolean> hostileProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hostile")
            .value(true)
            .build();
    public final Property<Boolean> passiveProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Passive")
            .value(true)
            .build();
    public final Property<Boolean> onFireProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("On Fire")
            .value(false)
            .build();
    public final Property<Boolean> rotateProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Rotate")
            .value(true)
            .build();
    public final Property<Boolean> lockviewProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Client")
            .value(false)
            .parent(rotateProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> groundProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("OutGround")
            .value(true)
            .build();
    public final Property<Boolean> fireimmuneProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("FireImmune")
            .value(true)
            .build();
public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();

    private Hand hand = null;
    private BlockPos blockPos = null;

    public Roaster() {
        super(Category.COMBAT);
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            ItemStack mainHandStack = Wrapper.INSTANCE.getLocalPlayer().getMainHandStack();
            ItemStack offHandStack = Wrapper.INSTANCE.getLocalPlayer().getOffHandStack();
            hand = null;
            if (mainHandStack != null && (mainHandStack.getItem() == Items.FLINT_AND_STEEL || mainHandStack.getItem() == Items.FIRE_CHARGE))
                hand = Hand.MAIN_HAND;
            if (offHandStack != null && (offHandStack.getItem() == Items.FLINT_AND_STEEL || offHandStack.getItem() == Items.FIRE_CHARGE))
                hand = Hand.OFF_HAND;
            if (hand == null)
                return;
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                    if (isValid(entity)) {
                        Block footBlock = Wrapper.INSTANCE.getWorld().getBlockState(entity.getBlockPos()).getBlock();
                        if (footBlock == Blocks.AIR) {
                            blockPos = entity.getBlockPos().down();
                            if (rotateProperty.value()) {
                                RotationVector rotations = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                                event.setRotation(rotations);
                            }
                            if (lockviewProperty.value()) {
                            PlayerHelper.INSTANCE.setRotation(event.getRotation());
                            }
                        }
                    }
            });
        } else {
            if (blockPos != null) {
                Vec3d pos = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                BlockHitResult hitResult = new BlockHitResult(pos, Direction.UP, blockPos, false);
                Wrapper.INSTANCE.getClientPlayerInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), hand, hitResult);
                if (swingProperty.value())
                    Wrapper.INSTANCE.getLocalPlayer().swingHand(hand);
            }
            blockPos = null;
        }
    });

    private boolean isValid(Entity entity) {
        if (entity instanceof ClientPlayerEntity)
            return false;
        if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity) > distanceProperty.value())
            return false;
        if (entity instanceof PlayerEntity) {
            if (FriendHelper.INSTANCE.isFriend(livingEntity.getName().getString()))
            return friendProperty.value();
            return playerProperty.value();
           }   
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostileProperty.value();
        if (EntityHelper.INSTANCE.isPassiveMob(entity) && !EntityHelper.INSTANCE.doesPlayerOwn(entity))
            return passiveProperty.value();
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutralProperty.value();
        if (entity.isFireImmune())
            return fireimmuneProperty.value();
        if (entity.isOnFire())
            return onFireProperty.value();
        if (!entity.isOnGround())
            return groundProperty.value();    
        return false;
    }
}
