package me.dustin.jex.feature.mod.impl.movement.elytraplus;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.impl.AlwaysBoostElytraFly;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.impl.BoostElytraFly;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.impl.ECMEElytraFly;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.impl.HoverElytraFly;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Change how the Elytra flies.")
public class ElytraPlus extends Feature {

    @Op(name = "Auto Elytra")
    public boolean autoElytra = true;
    @OpChild(name = "Fall Distance", max = 10, inc = 0.5f, parent = "Auto Elytra")
    public float fallDistance = 3;

    @Op(name = "Fly")
    public boolean elytraFly = true;
    @OpChild(name = "Mode", all = {"Vanilla", "Boost", "AlwaysBoost", "Hover", "ECME"}, parent = "Fly")
    public String mode = "Vanilla";

    @OpChild(name = "Fly Speed", min = 0.1f, max = 2, inc = 0.1f, parent = "Mode", dependency = "Hover")
    public float flySpeed = 0.5f;
    @OpChild(name = "Slow Glide", parent = "Mode", dependency = "Hover")
    public boolean slowGlide = false;

    @OpChild(name = "Boost", max=0.15f, inc = 0.01f, parent = "Mode", dependency = "Boost")
    public float boost = 0.05f;
    @OpChild(name = "Max Boost", max=5, inc = 0.1f, parent = "Mode", dependency = "Boost")
    public float maxBoost = 2.5f;
    @OpChild(name = "Boost Key", isKeybind = true, parent = "Mode", dependency = "Boost")
    public int boostKey = GLFW.GLFW_KEY_W;
    @OpChild(name = "Slowdown Key", isKeybind = true, parent = "Mode", dependency = "Boost")
    public int slowKey = GLFW.GLFW_KEY_S;

    private String lastMode;

    public ElytraPlus() {
        new AlwaysBoostElytraFly();
        new BoostElytraFly();
        new ECMEElytraFly();
        new HoverElytraFly();
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        this.setSuffix(mode);
        if (wearingElytra() && (autoElytra && Wrapper.INSTANCE.getLocalPlayer().fallDistance >= fallDistance && !Wrapper.INSTANCE.getLocalPlayer().isOnGround() && !Wrapper.INSTANCE.getLocalPlayer().isFallFlying())) {
            if (Wrapper.INSTANCE.getLocalPlayer().age % 5 == 0)
                NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (elytraFly) {
            sendEvent(event);
        }
    });

    private void sendEvent(Event event) {
        if (!mode.equalsIgnoreCase(lastMode) && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(mode, this).enable();
        }
        FeatureExtension.get(mode, this).pass(event);
        lastMode = mode;
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(mode, this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(mode, this).disable();
        super.onDisable();
    }

    private boolean wearingElytra() {
        ItemStack equippedStack = Wrapper.INSTANCE.getLocalPlayer().getEquippedStack(EquipmentSlot.CHEST);
        return equippedStack != null && equippedStack.getItem() == Items.ELYTRA;
    }
}
