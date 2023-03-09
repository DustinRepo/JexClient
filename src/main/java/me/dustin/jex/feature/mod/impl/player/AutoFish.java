package me.dustin.jex.feature.mod.impl.player;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.filters.ServerPacketFilter;
import me.dustin.jex.event.misc.EventSetLevel;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
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
import me.dustin.jex.feature.mod.core.Feature;
import java.util.ConcurrentModificationException;

public class AutoFish extends Feature {

    public final Property<Boolean> soundProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Sound")
            .value(true)
            .build();
    public final Property<Boolean> distanceCheckProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Distance Check")
            .value(false)
            .parent(soundProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> dontReelProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Don't Reel")
            .value(false)
            .build();
    public final Property<Boolean> recastProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Re-Reel")
            .value(true)
            .build();
    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Delay")
            .value(750L)
            .max(2000)
            .parent(recastProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> reelOnReconnectProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Reel on Reconnect")
            .value(true)
            .build();
    public final Property<Boolean> showIfOpenWaterProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Show If OpenWater")
            .value(true)
            .build();

    private double lastY = -1;
    private boolean hasReeled = false;
    private final StopWatch stopWatch = new StopWatch();
    private final StopWatch stopWatch1 = new StopWatch();
    private boolean hasReconnected;

    public AutoFish() {
        super(Category.PLAYER);
    }

    @EventPointer
    private final EventListener<EventPacketReceive> eventPacketReceiveEventListener = new EventListener<>(event -> {
        if (hasReeled)
            return;
        PlaySoundS2CPacket soundPacket = (PlaySoundS2CPacket) event.getPacket();
        if (soundPacket.getSound().getId().toString().equalsIgnoreCase("minecraft:entity.fishing_bobber.splash")) {
            if (Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().fishHook == null)
                Wrapper.INSTANCE.getLocalPlayer().fishHook = getClosest();
            if (Wrapper.INSTANCE.getLocalPlayer() == null || Wrapper.INSTANCE.getLocalPlayer().fishHook == null)
                return;
            Vec3d vec3d = new Vec3d(soundPacket.getX(), soundPacket.getY(), soundPacket.getZ());
            if (distanceTo(Wrapper.INSTANCE.getLocalPlayer().fishHook, vec3d) < 3 || !distanceCheckProperty.value()) {
                reel();
                hasReeled = true;
                stopWatch.reset();
            }
        }
    }, new ServerPacketFilter(EventPacketReceive.Mode.PRE, PlaySoundS2CPacket.class));

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (AutoEat.isEating && recastProperty.value()) {
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

        FishingBobberEntity hook = getHook();

        if (hook != null && hook.age > 100 && !soundProperty.value()) {
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

        if (hasReeled && recastProperty.value()) {
            if (stopWatch.hasPassed(delayProperty.value())) {
                reel();
                hasReeled = false;
                stopWatch.reset();
            }
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventSetLevel> eventJoinWorldEventListener = new EventListener<>(event -> {
        if (reelOnReconnectProperty.value())
            hasReconnected = true;
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (!showIfOpenWaterProperty.value())
            return;
            FishingBobberEntity hook = getHook();
        if (hook != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.FISHING_ROD) {
            Vec3d renderPos = Render3DHelper.INSTANCE.getEntityRenderPosition(hook, event.getPartialTicks());
            Box box = new Box(renderPos.x - 0.2f, renderPos.y - 0.2f, renderPos.z - 0.2f, renderPos.x + 0.2f, renderPos.y + 0.2f, renderPos.z + 0.2f);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, isOpenOrWaterAround(hook.getBlockPos()) ? 0xff0000ff : 0xffff0000);
        }
    });

    void reel() {
        if (dontReelProperty.value()) return;
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
        } catch (ConcurrentModificationException ignored) {
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
