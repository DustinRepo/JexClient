package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;

@Feature.Manifest(category = Feature.Category.MISC, description = "Automatically create withers by just placing soul sand")
public class AutoWither extends Feature {

    int stage = 0;
    boolean creatingWither = false;
    ServerboundUseItemOnPacket packet = null;

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (creatingWither && packet != null) {
            int skulls = getSkulls();
            int soulSand = getSoulSand();
            if (skulls == -1 || soulSand == -1) {
                stage = 0;
                creatingWither = false;
                packet = null;
                return;
            }
            BlockPos originPos = packet.getHitResult().getBlockPos();
            if (packet.getHitResult().getDirection() == Direction.UP || packet.getHitResult().getDirection() == Direction.DOWN)
                originPos = originPos.above();
            if (packet.getHitResult().getDirection() == Direction.NORTH)
                originPos = originPos.north();//north
            if (packet.getHitResult().getDirection() == Direction.SOUTH)
                originPos = originPos.south();//south
            if (packet.getHitResult().getDirection() == Direction.WEST)
                originPos = originPos.west();//west
            if (packet.getHitResult().getDirection() == Direction.EAST)
                originPos = originPos.east();//east
            if (stage >= 3) {
                if (InventoryHelper.INSTANCE.getInventory().selected != skulls) {
                    InventoryHelper.INSTANCE.setSlot(skulls, true, true);
                }
            } else {
                if (InventoryHelper.INSTANCE.getInventory().selected != soulSand) {
                    InventoryHelper.INSTANCE.setSlot(soulSand, true, true);
                }
            }
            PlayerHelper.INSTANCE.placeBlockInPos(getBlockPos(originPos, stage), InteractionHand.MAIN_HAND, true);

            stage++;
            if (stage == 6) {
                creatingWither = false;
                packet = null;
                stage = 0;
                InventoryHelper.INSTANCE.setSlot(soulSand, true, true);
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (!creatingWither && InventoryHelper.INSTANCE.getFromHotbar(Items.WITHER_SKELETON_SKULL) != -1) {
            packet = (ServerboundUseItemOnPacket)event.getPacket();
            if(Wrapper.INSTANCE.getLocalPlayer().getMainHandItem() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandItem().getItem() == Items.SOUL_SAND)
            {
                creatingWither = true;
            }
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundUseItemOnPacket.class));

    @Override
    public void onDisable() {
        stage = 0;
        super.onDisable();
    }

    public BlockPos getBlockPos(BlockPos origin, int stage) {
        switch (stage) {
            case 0 -> {//right above block we originally place
                return origin.offset(0, 1, 0);
            }
            case 1 -> {//to the side
                return origin.offset(1, 1, 0);
            }
            case 2 -> {//other side
                return origin.offset(-1, 1, 0);
            }
            case 3 -> {//side head
                return origin.offset(1, 2, 0);
            }
            case 4 -> {//middle head
                return origin.offset(0, 2, 0);
            }
            case 5 -> {//other side head
                return origin.offset(-1, 2, 0);
            }
        }
        return BlockPos.ZERO;
    }

    public int getSkulls()
    {
        for(int i = 0; i < 9; i++)
        {
            if(InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getCount() > 2 && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() == Items.WITHER_SKELETON_SKULL)
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
            if(InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getCount() > 3 && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() == Items.SOUL_SAND)
            {
                return i;
            }
        }
        return -1;
    }

}
