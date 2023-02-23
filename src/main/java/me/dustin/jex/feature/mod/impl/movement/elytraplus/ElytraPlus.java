package me.dustin.jex.feature.mod.impl.movement.elytraplus;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.impl.AlwaysBoostElytraFly;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.impl.BoostElytraFly;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.impl.ECMEElytraFly;
import me.dustin.jex.feature.mod.impl.movement.elytraplus.impl.HoverElytraFly;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.lwjgl.glfw.GLFW;

public class ElytraPlus extends Feature {

    public final Property<Boolean> autoElytraProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Auto Elytra")
            .description("Automatically activate the elytra.")
            .value(true)
            .build();
    public final Property<Float> fallDistanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Fall Distance")
            .value(3f)
            .max(10)
            .inc(0.5f)
            .parent(autoElytraProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public Property<Boolean> elytraFlyProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Fly")
            .value(true)
            .build();
    public Property<Mode> modeProperty = new Property.PropertyBuilder<Mode>(this.getClass())
            .name("Mode")
            .value(Mode.BOOST)
            .parent(elytraFlyProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public Property<Float> flySpeedProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Fly Speed")
            .value(0.5f)
            .min(0.1f)
            .max(2)
            .inc(0.1f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.HOVER)
            .build();
    public Property<Boolean> slowGlideProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Slow Glide")
            .description("Glide down very slowly.")
            .value(false)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.HOVER)
            .build();
    public Property<Float> boostProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Boost")
            .value(0.01f)
            .max(1f)
            .inc(0.01f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.BOOST)
            .build();
    public Property<Float> maxBoostProperty = new Property.PropertyBuilder<Float>(this.getClass())
            .name("Max Boost")
            .value(2.5f)
            .max(5)
            .inc(0.1f)
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.BOOST)
            .build();
    public Property<Integer> boostKeyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Boost Key")
            .value(GLFW.GLFW_KEY_W)
            .isKey()
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.BOOST)
            .build();
    public Property<Integer> slowKeyProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Slowdown Key")
            .value(GLFW.GLFW_KEY_S)
            .isKey()
            .parent(modeProperty)
            .depends(parent -> parent.value() == Mode.BOOST)
            .build();

    private Mode lastMode;

    public ElytraPlus() {
        super(Category.MOVEMENT, "Change how the Elytra flies.");
        new AlwaysBoostElytraFly();
        new BoostElytraFly();
        new ECMEElytraFly();
        new HoverElytraFly();
    }

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        this.setSuffix(modeProperty.value());
        if (wearingElytra() && (autoElytraProperty.value() && Wrapper.INSTANCE.getLocalPlayer().fallDistance >= fallDistanceProperty.value() && !Wrapper.INSTANCE.getLocalPlayer().isOnGround() && !Wrapper.INSTANCE.getLocalPlayer().isFallFlying())) {
            if (Wrapper.INSTANCE.getLocalPlayer().age % 5 == 0)
                NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (elytraFlyProperty.value()) {
            sendEvent(event);
        }
    });

    private void sendEvent(Event event) {
        if (modeProperty.value() != lastMode && lastMode != null) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(modeProperty.value(), this).enable();
        }
        FeatureExtension.get(modeProperty.value(), this).pass(event);
        lastMode = modeProperty.value();
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(modeProperty.value(), this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(modeProperty.value(), this).disable();
        super.onDisable();
    }

    private boolean wearingElytra() {
        ItemStack equippedStack = Wrapper.INSTANCE.getLocalPlayer().getEquippedStack(EquipmentSlot.CHEST);
        return equippedStack != null && equippedStack.getItem() == Items.ELYTRA;
    }

    public enum Mode {
        BOOST, ALWAYS_BOOST, HOVER, ECME
    }
}
