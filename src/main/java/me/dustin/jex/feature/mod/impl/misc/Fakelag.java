package me.dustin.jex.feature.mod.impl.misc;

import com.mojang.authlib.GameProfile;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.helper.entity.FakePlayerEntity;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;

import java.util.ArrayList;
import java.util.UUID;

@Feature.Manifest(category = Feature.Category.MISC, description = "Pretend to lag")
public class Fakelag extends Feature {

    @Op(name = "Catch when", all = {"Both", "OnGround", "In Air"})
    public String catchWhen = "Both";
    @Op(name = "Visualize")
    public boolean visualize = true;
    @Op(name = "Choke MS", min = 50, max = 2000, inc = 10)
    public int choke = 100;

    private final ArrayList<Packet<?>> packets = new ArrayList<>();
    private final Timer timer = new Timer();
    private boolean sending = false;

    private FakePlayerEntity fakePlayerEntity;

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        if (sending)
            return;
        if (Wrapper.INSTANCE.getLocalPlayer() == null) {
            packets.clear();
            timer.reset();
            fakePlayerEntity = null;
        }
        if (!timer.hasPassed(choke) && shouldCatchPackets()) {
            packets.add(event.getPacket());
            event.cancel();
        } else {
            sending = true;
            packets.forEach(Wrapper.INSTANCE.getLocalPlayer().networkHandler::sendPacket);
            sending = false;
            if (visualize) {
                if (fakePlayerEntity == null)
                    createFakePlayer();
                fakePlayerEntity.copyPositionAndRotation(Wrapper.INSTANCE.getLocalPlayer());
            } else {
                removeFakePlayer();
            }
            packets.clear();
            timer.reset();
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE));

    private boolean shouldCatchPackets() {
        return switch (catchWhen.toLowerCase()) {
            case "both" -> true;
            case "onground" -> Wrapper.INSTANCE.getLocalPlayer().isOnGround();
            case "in air" -> !Wrapper.INSTANCE.getLocalPlayer().isOnGround();
            default -> false;
        };
    }

    @Override
    public void onEnable() {
        if (Wrapper.INSTANCE.getWorld() != null)
            createFakePlayer();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if (Wrapper.INSTANCE.getLocalPlayer() != null) {
            try {
                packets.forEach(Wrapper.INSTANCE.getLocalPlayer().networkHandler::sendPacket);
            } catch (Exception e) {}
            packets.clear();
        }
        removeFakePlayer();
        fakePlayerEntity = null;
        super.onDisable();
    }

    public void removeFakePlayer() {
        if (fakePlayerEntity != null && Wrapper.INSTANCE.getWorld() != null) {
            fakePlayerEntity.setPos(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
            Wrapper.INSTANCE.getWorld().removeEntity(40000, Entity.RemovalReason.DISCARDED);
        }
    }

    public void createFakePlayer() {
        this.fakePlayerEntity = new FakePlayerEntity(Wrapper.INSTANCE.getWorld(), new GameProfile(UUID.randomUUID(), "Fakelag Player"));
        fakePlayerEntity.copyFrom(Wrapper.INSTANCE.getLocalPlayer());
        fakePlayerEntity.copyPositionAndRotation(Wrapper.INSTANCE.getLocalPlayer());
        Wrapper.INSTANCE.getWorld().addEntity(40000, fakePlayerEntity);
    }
}
