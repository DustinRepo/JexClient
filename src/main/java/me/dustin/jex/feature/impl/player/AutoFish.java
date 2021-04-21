package me.dustin.jex.feature.impl.player;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ConcurrentModificationException;

@Feat(name = "AutoFish", category = FeatureCategory.PLAYER, description = "Automatically detect a fish on the hook")
public class AutoFish extends Feature {

    @Op(name = "Sound")
    public boolean sound = true;
    @OpChild(name = "Distance Check", parent = "Sound")
    public boolean distanceCheck = false;
    @Op(name = "Re-Reel")
    public boolean recast = true;
    @OpChild(name = "Delay", max = 2000, parent = "Re-Reel")
    public int delay = 750;
    @Op(name = "Reel on Reconnect")
    public boolean reelOnReconnect = false;
    @Op(name = "Show If OpenWater")
    public boolean showIfOpenWater = true;


    double lastY = -1;
    boolean hasReeled = false;
    Timer timer = new Timer();
    Timer timer1 = new Timer();
    boolean hasReconnected;

    @EventListener(events = {EventPacketReceive.class, EventPlayerPackets.class, EventJoinWorld.class, EventRender3D.class})
    public void run(Event event) {
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D) event;
            FishingBobberEntity hook = getHook();
            if (hook != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.FISHING_ROD) {
                Vec3d renderPos = Render3DHelper.INSTANCE.getEntityRenderPosition(hook, eventRender3D.getPartialTicks());
                Box box = new Box(renderPos.x - 0.2f, renderPos.y - 0.2f, renderPos.z - 0.2f, renderPos.x + 0.2f, renderPos.y + 0.2f, renderPos.z + 0.2f);
                Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), box, isOpenOrWaterAround(hook.getBlockPos()) ? 0xff0000ff : 0xffff0000);
            }
        }
        if (AutoEat.isEating && recast) {
            this.hasReeled = true;
            timer.reset();
            return;
        }
        if (event instanceof EventPlayerPackets) {
            if (((EventPlayerPackets) event).getMode() == EventPlayerPackets.Mode.PRE) {
                if (hasReconnected) {
                    if (timer1.hasPassed(5000)) {
                        reel();
                        timer1.reset();
                        hasReconnected = false;
                    }
                }


                FishingBobberEntity hook = getHook();

                if (hook != null && hook.age > 100 && !sound) {
                    if (lastY == -1)
                        lastY = hook.getY();
                    double difference = Math.abs(hook.getY() - lastY);
                    if (difference > 0.11) {
                        reel();
                        hasReeled = true;
                        timer.reset();
                    }
                    lastY = hook.getY();
                }

                if (hasReeled && recast) {
                    if (timer.hasPassed(delay)) {
                        reel();
                        hasReeled = false;
                        timer.reset();
                    }
                }
            }
        }
        if (!hasReeled && event instanceof EventPacketReceive) {

            EventPacketReceive receive = (EventPacketReceive) event;
            if (receive.getPacket() instanceof PlaySoundS2CPacket) {
                PlaySoundS2CPacket soundPacket = (PlaySoundS2CPacket) receive.getPacket();

                if (soundPacket.getSound().getId().toString().equalsIgnoreCase("minecraft:entity.fishing_bobber.splash")) {
                    if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().fishHook == null)
                        Wrapper.INSTANCE.getLocalPlayer().fishHook = getClosest();
                    if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getLocalPlayer().fishHook == null)
                        return;
                    Vec3d vec3d = new Vec3d(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ());
                    if (distanceTo(Wrapper.INSTANCE.getLocalPlayer().fishHook, vec3d) < 3 || !distanceCheck) {
                        reel();
                        hasReeled = true;
                        timer.reset();
                    }
                }
            }
        }
        if (event.equals(EventJoinWorld.class)) {
            if (reelOnReconnect)
                hasReconnected = true;
        }
    }

    void reel() {
        if (PlayerHelper.INSTANCE.mainHandStack() != null && PlayerHelper.INSTANCE.mainHandStack().getItem() == Items.FISHING_ROD) {
            PlayerHelper.INSTANCE.useItem(Hand.MAIN_HAND);
            PlayerHelper.INSTANCE.swing(Hand.MAIN_HAND);
        } else if (PlayerHelper.INSTANCE.offHandStack() != null && PlayerHelper.INSTANCE.offHandStack().getItem() == Items.FISHING_ROD) {
            PlayerHelper.INSTANCE.useItem(Hand.OFF_HAND);
            PlayerHelper.INSTANCE.swing(Hand.OFF_HAND);
        }
    }

    public float distanceTo(Entity entity, Vec3d vec3d) {
        float float_1 = (float) (entity.getX() - vec3d.x);
        float float_2 = (float) (entity.getY() - vec3d.y);
        float float_3 = (float) (entity.getZ() - vec3d.z);
        return MathHelper.sqrt(float_1 * float_1 + float_2 * float_2 + float_3 * float_3);
    }

    public FishingBobberEntity getHook() {
        FishingBobberEntity hook = Wrapper.INSTANCE.getLocalPlayer().fishHook;
        if (hook == null) {
            if (InventoryHelper.INSTANCE.getInventory().getStack(InventoryHelper.INSTANCE.getInventory().selectedSlot) != null && InventoryHelper.INSTANCE.getInventory().getStack(InventoryHelper.INSTANCE.getInventory().selectedSlot).getItem() == Items.FISHING_ROD) {
                hook = getClosest();
            }
        }
        return hook;
    }

    public FishingBobberEntity getClosest() {
        FishingBobberEntity hook = null;
        if (Wrapper.INSTANCE.getWorld() == null || Wrapper.INSTANCE.getLocalPlayer() == null)
            return null;
        try {
            for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
                if (entity instanceof FishingBobberEntity) {
                    if (((FishingBobberEntity) entity).getOwner() == Wrapper.INSTANCE.getLocalPlayer()) {
                        hook = (FishingBobberEntity) entity;
                    }
                }
            }
        } catch (ConcurrentModificationException e) {

        }
        return hook;
    }

    private boolean isOpenOrWaterAround(BlockPos pos) {
        for (int x = -2; x < 2; x++)
            for (int y = -2; y < 2; y++)
                for (int z = -2; z < 2; z++) {
                    BlockPos blockPos = pos.add(x, y, z);
                    if (WorldHelper.INSTANCE.getBlock(blockPos) != Blocks.AIR && WorldHelper.INSTANCE.getBlock(blockPos) != Blocks.WATER)
                        return false;
                }

        return true;
    }
}
