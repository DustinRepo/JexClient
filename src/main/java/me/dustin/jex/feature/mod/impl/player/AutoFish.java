package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import java.util.ConcurrentModificationException;

@Feature.Manifest(category = Feature.Category.PLAYER, description = "Automatically detect a fish on the hook")
public class AutoFish extends Feature {

    @Op(name = "Sound")
    public boolean sound = true;
    @OpChild(name = "Distance Check", parent = "Sound")
    public boolean distanceCheck = false;
    @Op(name = "Don't Reel")
    public boolean dontReel = false;
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
    StopWatch stopWatch = new StopWatch();
    StopWatch stopWatch1 = new StopWatch();
    boolean hasReconnected;

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (hasReeled)
            return;
        ClientboundSoundPacket soundPacket = (ClientboundSoundPacket) event.getPacket();
        if (soundPacket.getSound().getLocation().toString().equalsIgnoreCase("minecraft:entity.fishing_bobber.splash")) {
            if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().fishing == null)
                Wrapper.INSTANCE.getLocalPlayer().fishing = getClosest();
            if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getLocalPlayer().fishing == null)
                return;
            Vec3 vec3d = new Vec3(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ());
            if (distanceTo(Wrapper.INSTANCE.getLocalPlayer().fishing, vec3d) < 3 || !distanceCheck) {
                reel();
                hasReeled = true;
                stopWatch.reset();
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, ClientboundSoundPacket.class));

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating && recast) {
            this.hasReeled = true;
            stopWatch.reset();
            return;
        }
        if (hasReconnected) {
            if (stopWatch1.hasPassed(5000)) {
                reel();
                stopWatch1.reset();
                hasReconnected = false;
            }
        }


        FishingHook hook = getHook();

        if (hook != null && hook.tickCount > 100 && !sound) {
            if (lastY == -1)
                lastY = hook.getY();
            double difference = Math.abs(hook.getY() - lastY);
            if (difference > 0.11) {
                reel();
                hasReeled = true;
                stopWatch.reset();
            }
            lastY = hook.getY();
        }

        if (hasReeled && recast) {
            if (stopWatch.hasPassed(delay)) {
                reel();
                hasReeled = false;
                stopWatch.reset();
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        if (reelOnReconnect)
            hasReconnected = true;
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (!showIfOpenWater)
            return;
            FishingHook hook = getHook();
        if (hook != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandItem() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandItem().getItem() == Items.FISHING_ROD) {
            Vec3 renderPos = Render3DHelper.INSTANCE.getEntityRenderPosition(hook, event.getPartialTicks());
            AABB box = new AABB(renderPos.x - 0.2f, renderPos.y - 0.2f, renderPos.z - 0.2f, renderPos.x + 0.2f, renderPos.y + 0.2f, renderPos.z + 0.2f);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, isOpenOrWaterAround(hook.blockPosition()) ? 0xff0000ff : 0xffff0000);
        }
    });

    void reel() {
        if (dontReel) return;
        if (PlayerHelper.INSTANCE.mainHandStack() != null && PlayerHelper.INSTANCE.mainHandStack().getItem() == Items.FISHING_ROD) {
            PlayerHelper.INSTANCE.useItem(InteractionHand.MAIN_HAND);
            PlayerHelper.INSTANCE.swing(InteractionHand.MAIN_HAND);
        } else if (PlayerHelper.INSTANCE.offHandStack() != null && PlayerHelper.INSTANCE.offHandStack().getItem() == Items.FISHING_ROD) {
            PlayerHelper.INSTANCE.useItem(InteractionHand.OFF_HAND);
            PlayerHelper.INSTANCE.swing(InteractionHand.OFF_HAND);
        }
    }

    public float distanceTo(Entity entity, Vec3 vec3d) {
        float float_1 = (float) (entity.getX() - vec3d.x);
        float float_2 = (float) (entity.getY() - vec3d.y);
        float float_3 = (float) (entity.getZ() - vec3d.z);
        return Mth.sqrt(float_1 * float_1 + float_2 * float_2 + float_3 * float_3);
    }

    public FishingHook getHook() {
        FishingHook hook = Wrapper.INSTANCE.getLocalPlayer().fishing;
        if (hook == null) {
            if (InventoryHelper.INSTANCE.getInventory().getItem(InventoryHelper.INSTANCE.getInventory().selected) != null && InventoryHelper.INSTANCE.getInventory().getItem(InventoryHelper.INSTANCE.getInventory().selected).getItem() == Items.FISHING_ROD) {
                hook = getClosest();
            }
        }
        return hook;
    }

    public FishingHook getClosest() {
        FishingHook hook = null;
        if (Wrapper.INSTANCE.getWorld() == null || Wrapper.INSTANCE.getLocalPlayer() == null)
            return null;
        try {
            for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
                if (entity instanceof FishingHook) {
                    if (((FishingHook) entity).getOwner() == Wrapper.INSTANCE.getLocalPlayer()) {
                        hook = (FishingHook) entity;
                    }
                }
            }
        } catch (ConcurrentModificationException ignored) {

        }
        return hook;
    }

    private boolean isOpenOrWaterAround(BlockPos pos) {
        for (int x = -2; x < 2; x++)
            for (int y = -2; y < 2; y++)
                for (int z = -2; z < 2; z++) {
                    BlockPos blockPos = pos.offset(x, y, z);
                    if (WorldHelper.INSTANCE.getBlock(blockPos) != Blocks.AIR && WorldHelper.INSTANCE.getBlock(blockPos) != Blocks.WATER)
                        return false;
                }

        return true;
    }
}
