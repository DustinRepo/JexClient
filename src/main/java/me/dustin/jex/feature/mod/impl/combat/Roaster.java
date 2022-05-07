package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;

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


    InteractionHand hand = null;
    BlockPos blockPos = null;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (event.getMode() == EventPlayerPackets.Mode.PRE) {
            ItemStack mainHandStack = Wrapper.INSTANCE.getLocalPlayer().getMainHandItem();
            ItemStack offHandStack = Wrapper.INSTANCE.getLocalPlayer().getOffhandItem();
            hand = null;
            if (mainHandStack != null && (mainHandStack.getItem() == Items.FLINT_AND_STEEL || mainHandStack.getItem() == Items.FIRE_CHARGE))
                hand = InteractionHand.MAIN_HAND;
            if (offHandStack != null && (offHandStack.getItem() == Items.FLINT_AND_STEEL || offHandStack.getItem() == Items.FIRE_CHARGE))
                hand = InteractionHand.OFF_HAND;
            if (hand == null)
                return;
            Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
                if (entity instanceof LivingEntity livingEntity) {
                    if (isValid(livingEntity)) {
                        Block footBlock = Wrapper.INSTANCE.getWorld().getBlockState(livingEntity.blockPosition()).getBlock();
                        if (footBlock == Blocks.AIR) {
                            blockPos = livingEntity.blockPosition().below();
                            if (rotate) {
                                RotationVector rotations = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
                                event.setRotation(rotations);
                            }
                        }
                    }
                }
            });
        } else {
            if (blockPos != null) {
                Vec3 pos = new Vec3(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                BlockHitResult hitResult = new BlockHitResult(pos, Direction.UP, blockPos, false);
                Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), hand, hitResult);
                if (swing)
                    Wrapper.INSTANCE.getLocalPlayer().swing(hand);
            }
            blockPos = null;
        }
    });

    private boolean isValid(LivingEntity livingEntity) {
        if (livingEntity instanceof LocalPlayer)
            return false;
        if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(livingEntity) > 4)
            return false;
        if (livingEntity.isOnFire() && !onFire)
            return false;
        if (livingEntity.fireImmune())
            return false;
        if (!livingEntity.isOnGround())
            return false;
        if (livingEntity instanceof Player) {
            if (FriendHelper.INSTANCE.isFriend(livingEntity.getName().getString()))
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
