package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

@Feature.Manifest(name = "AutoWither", category = Feature.Category.MISC, description = "Automatically create withers by just placing soul sand")
public class AutoWither extends Feature {

    int stage = 0;
    boolean creatingWither = false;
    PlayerInteractBlockC2SPacket packet = null;
    
    @EventListener(events = {EventPlayerPackets.class, EventPacketSent.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (creatingWither && packet != null) {
                    int skulls = getSkulls();
                    int soulSand = getSoulSand();
                    if (skulls == -1 || soulSand == -1) {
                        stage = 0;
                        creatingWither = false;
                        packet = null;
                        return;
                    }
                    BlockPos originPos = packet.getBlockHitResult().getBlockPos();
                    if (packet.getBlockHitResult().getSide() == Direction.UP || packet.getBlockHitResult().getSide() == Direction.DOWN)
                        originPos = originPos.up();
                    if (packet.getBlockHitResult().getSide() == Direction.NORTH)
                        originPos = originPos.north();//north
                    if (packet.getBlockHitResult().getSide() == Direction.SOUTH)
                        originPos = originPos.south();//south
                    if (packet.getBlockHitResult().getSide() == Direction.WEST)
                        originPos = originPos.west();//west
                    if (packet.getBlockHitResult().getSide() == Direction.EAST)
                        originPos = originPos.east();//east
                    if (stage >= 3) {
                        if (InventoryHelper.INSTANCE.getInventory().selectedSlot != skulls) {
                            InventoryHelper.INSTANCE.getInventory().selectedSlot = skulls;
                            NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(skulls));
                        }
                    } else {
                        if (InventoryHelper.INSTANCE.getInventory().selectedSlot != soulSand) {
                            InventoryHelper.INSTANCE.getInventory().selectedSlot = soulSand;
                            NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(soulSand));
                        }
                    }
                    PlayerHelper.INSTANCE.placeBlockInPos(getBlockPos(originPos, stage), Hand.MAIN_HAND, true);

                    stage++;
                    if (stage == 6) {
                        creatingWither = false;
                        packet = null;
                        stage = 0;
                        InventoryHelper.INSTANCE.getInventory().selectedSlot = soulSand;
                        NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(soulSand));
                    }
                }
            }
        } else if (event instanceof EventPacketSent eventPacketSent) {
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

    @Override
    public void onDisable() {
        stage = 0;
        super.onDisable();
    }

    public BlockPos getBlockPos(BlockPos origin, int stage) {
        switch (stage) {
            case 0 -> {//right above block we originally place
                return origin.add(0, 1, 0);
            }
            case 1 -> {//to the side
                return origin.add(1, 1, 0);
            }
            case 2 -> {//other side
                return origin.add(-1, 1, 0);
            }
            case 3 -> {//side head
                return origin.add(1, 2, 0);
            }
            case 4 -> {//middle head
                return origin.add(0, 2, 0);
            }
            case 5 -> {//other side head
                return origin.add(-1, 2, 0);
            }
        }
        return BlockPos.ORIGIN;
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
