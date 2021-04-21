package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Feat(name = "AutoWither", category = FeatureCategory.MISC, description = "Automatically create withers by just placing soul sand")
public class AutoWither extends Feature {

    boolean creatingWither = false;
    PlayerInteractBlockC2SPacket packet = null;
    
    @EventListener(events = {EventPlayerPackets.class, EventPacketSent.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets) {
            EventPlayerPackets eventPlayerPackets = (EventPlayerPackets)event;
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (creatingWither && packet != null) {
                    if (getSkulls() == -1 || getSoulSand() == -1) {
                        creatingWither = false;
                        packet = null;
                        return;
                    }
                    BlockPos originPos = packet.getBlockHitResult().getBlockPos();
                    if (packet.getBlockHitResult().getSide() == Direction.UP || packet.getBlockHitResult().getSide() == Direction.DOWN)
                        originPos = originPos.up();
                    if (packet.getBlockHitResult().getSide() == Direction.NORTH)
                        originPos = originPos.north();
                    if (packet.getBlockHitResult().getSide() == Direction.SOUTH)
                        originPos = originPos.south();
                    if (packet.getBlockHitResult().getSide() == Direction.WEST)
                        originPos = originPos.west();
                    if (packet.getBlockHitResult().getSide() == Direction.EAST)
                        originPos = originPos.east();

                    Vec3d originVec = new Vec3d(originPos.getX(), originPos.getY(), originPos.getZ());
                    boolean northSouth = Direction.fromRotation((double) Wrapper.INSTANCE.getLocalPlayer().yaw) == Direction.NORTH || Direction.fromRotation((double) Wrapper.INSTANCE.getLocalPlayer().yaw) == Direction.SOUTH;
                    int savedSlot = InventoryHelper.INSTANCE.getInventory().selectedSlot;
                    BlockHitResult blockHitResult = new BlockHitResult(originVec.add(0, 1, 0), Direction.DOWN, originPos.up(), false);
                    NetworkHelper.INSTANCE.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult));
                    blockHitResult = new BlockHitResult(originVec.add(1, 1, 0), northSouth ? Direction.EAST : Direction.SOUTH, northSouth ? originPos.up().east() : originPos.up().north(), false);
                    NetworkHelper.INSTANCE.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult));
                    blockHitResult = new BlockHitResult(originVec.add(-1, 1, 0), northSouth ? Direction.WEST : Direction.NORTH, northSouth ? originPos.up().west() : originPos.up().south(), false);
                    NetworkHelper.INSTANCE.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult));
                    NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(getSkulls()));
                    blockHitResult = new BlockHitResult(originVec.add(0, 1, 0), Direction.UP, originPos.up(2), false);
                    NetworkHelper.INSTANCE.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult));
                    blockHitResult = new BlockHitResult(originVec.add(1, 1, 0), Direction.UP, northSouth ? originPos.up().east() : originPos.up().north(), false);
                    NetworkHelper.INSTANCE.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult));
                    blockHitResult = new BlockHitResult(originVec.add(-1, 1, 0), Direction.UP, northSouth ? originPos.up().west() : originPos.up().south(), false);
                    NetworkHelper.INSTANCE.sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, blockHitResult));
                    NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(savedSlot));
                    creatingWither = false;
                    packet = null;
                }
            }
        } else if (event instanceof EventPacketSent) {
            EventPacketSent eventPacketSent = (EventPacketSent)event;
            if(eventPacketSent.getPacket() instanceof PlayerInteractBlockC2SPacket && !creatingWither && InventoryHelper.INSTANCE.getFromHotbar(Items.WITHER_SKELETON_SKULL) != -1)
            {
                packet = (PlayerInteractBlockC2SPacket)eventPacketSent.getPacket();
                if(Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.SOUL_SAND)
                {
                    creatingWither = true;

                }
            }
        }
    }
    
    public int getSkulls()
    {
        for(int i = 0; i < 9; i++)
        {
            if(InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getCount() > 2 && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() == Items.WITHER_SKELETON_SKULL)
            {
                return i;
            }
        }
        return -1;
    }

    public int getSoulSand()
    {
        for(int i = 0; i < 9; i++)
        {
            if(InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getCount() > 3 && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() == Items.SOUL_SAND)
            {
                return i;
            }
        }
        return -1;
    }

}
