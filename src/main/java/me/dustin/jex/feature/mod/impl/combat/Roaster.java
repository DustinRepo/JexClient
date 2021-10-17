package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Roast your friends.")
public class Roaster extends Feature {

    @Op(name = "Player")
    public boolean player = true;
    @OpChild(name = "Friend", parent = "Player")
    public boolean friends = false;

    @Op(name = "Hostile")
    public boolean hostile = true;
    @Op(name = "Passive")
    public boolean passive = true;
    @Op(name = "On Fire")
    public boolean onFire = false;
    @Op(name = "Rotate")
    public boolean rotate = true;
    @Op(name = "Swing")
    public boolean swing = true;


    Hand hand = null;
    BlockPos blockPos = null;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
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
                if (entity instanceof LivingEntity livingEntity) {
                    if (isValid(livingEntity)) {
                        Block footBlock = Wrapper.INSTANCE.getWorld().getBlockState(livingEntity.getBlockPos()).getBlock();
                        if (footBlock == Blocks.AIR) {
                            blockPos = livingEntity.getBlockPos().down();
                            if (rotate) {
                                RotationVector rotations = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                                eventPlayerPackets.setRotation(rotations);
                            }
                        }
                    }
                }
            });
        } else {
            if (blockPos != null) {
                Vec3d pos = new Vec3d(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                BlockHitResult hitResult = new BlockHitResult(pos, Direction.UP, blockPos, false);
                NetworkHelper.INSTANCE.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult));
                if (swing)
                    Wrapper.INSTANCE.getLocalPlayer().swingHand(hand);
            }
            blockPos = null;
        }
    }

    private boolean isValid(LivingEntity livingEntity) {
        if (livingEntity instanceof ClientPlayerEntity)
            return false;
        if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(livingEntity) > 4)
            return false;
        if (livingEntity.isOnFire() && !onFire)
            return false;
        if (livingEntity.isFireImmune())
            return false;
        if (!livingEntity.isOnGround())
            return false;
        if (livingEntity instanceof PlayerEntity) {
            if (FriendHelper.INSTANCE.isFriend(livingEntity.getName().asString()))
                return friends;
            return player;
        }
        if (EntityHelper.INSTANCE.isHostileMob(livingEntity))
            return hostile;
        if (EntityHelper.INSTANCE.isPassiveMob(livingEntity) && !EntityHelper.INSTANCE.doesPlayerOwn(livingEntity))
            return passive;
        return false;
    }
}
