package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Change how the Elytra flies.")
public class ElytraPlus extends Feature {

    @Op(name = "Auto Elytra")
    public boolean autoElytra = true;
    @OpChild(name = "Fall Distance", min = 0, max = 10, inc = 0.5f, parent = "Auto Elytra")
    public float fallDistance = 3;

    @Op(name = "Fly")
    public boolean elytraFly = true;
    @OpChild(name = "Mode", all = {"Vanilla", "Boost", "AlwaysBoost", "Hover"}, parent = "Fly")
    public String mode = "Vanilla";

    @OpChild(name = "Fly Speed", min = 0.1f, max = 2, inc = 0.1f, parent = "Mode", dependency = "Hover")
    public float flySpeed = 0.5f;

    @OpChild(name = "Slow Glide", parent = "Mode", dependency = "Hover")
    public boolean slowGlide = false;

    @OpChild(name = "Boost", min=0, max=0.15f, inc = 0.01f, parent = "Mode", dependency = "Boost")
    public float boost = 0.05f;

    @OpChild(name = "Max Boost", min=0, max=5, inc = 0.1f, parent = "Mode", dependency = "Boost")
    public float maxBoost = 2.5f;

    @OpChild(name = "Boost Key", isKeybind = true, parent = "Mode", dependency = "Boost")
    public int boostKey = GLFW.GLFW_KEY_W;

    @OpChild(name = "Slowdown Key", isKeybind = true, parent = "Mode", dependency = "Boost")
    public int slowKey = GLFW.GLFW_KEY_S;

    @EventPointer
    private final EventListener<EventMove> eventMoveEventListener = new EventListener<>(event -> {
        this.setSuffix(mode);
        if (wearingElytra() && (autoElytra && Wrapper.INSTANCE.getLocalPlayer().fallDistance >= fallDistance && !Wrapper.INSTANCE.getLocalPlayer().isOnGround() && !Wrapper.INSTANCE.getLocalPlayer().isFallFlying())) {
            if (Wrapper.INSTANCE.getLocalPlayer().age % 5 == 0)
                NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying() && elytraFly) {
            if (mode.equalsIgnoreCase("Boost")) {
                ClientPlayerEntity player = Wrapper.INSTANCE.getLocalPlayer();
                double currentVel = Math.abs(player.getVelocity().x) + Math.abs(player.getVelocity().y) + Math.abs(player.getVelocity().z);
                float radianYaw = (float) Math.toRadians(player.getYaw());
                if (currentVel <= maxBoost) {
                    if (KeyboardHelper.INSTANCE.isPressed(boostKey)) {
                        player.addVelocity(MathHelper.sin(radianYaw) * -boost, 0, MathHelper.cos(radianYaw) * boost);
                    } else if (KeyboardHelper.INSTANCE.isPressed(slowKey)) {
                        player.addVelocity(MathHelper.sin(radianYaw) * boost, 0, MathHelper.cos(radianYaw) * -boost);
                    }
                }
            } else if (mode.equalsIgnoreCase("AlwaysBoost")) {
                Vec3d vec3d_1 = Wrapper.INSTANCE.getLocalPlayer().getRotationVector();
                Vec3d vec3d_2 = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                Wrapper.INSTANCE.getLocalPlayer().setVelocity(vec3d_2.add(vec3d_1.x * 0.1D + (vec3d_1.x * 1.5D - vec3d_2.x) * 0.5D, vec3d_1.y * 0.1D + (vec3d_1.y * 1.5D - vec3d_2.y) * 0.5D, vec3d_1.z * 0.1D + (vec3d_1.z * 1.5D - vec3d_2.z) * 0.5D));
            } else if (mode.equalsIgnoreCase("Hover")) {
                    PlayerHelper.INSTANCE.setMoveSpeed(event, flySpeed);
                    if (event.getY() <= 0)
                        event.setY(Wrapper.INSTANCE.getOptions().jumpKey.isPressed() ? flySpeed : (Wrapper.INSTANCE.getLocalPlayer().isSneaking() ? -flySpeed : (slowGlide ? -0.0001 : 0)));
            }
        }
    });

    private boolean wearingElytra() {
        ItemStack equippedStack = Wrapper.INSTANCE.getLocalPlayer().getEquippedStack(EquipmentSlot.CHEST);
        return equippedStack != null && equippedStack.getItem() == Items.ELYTRA;
    }

}
