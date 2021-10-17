package me.dustin.jex.feature.mod.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

@Feature.Manifest(category = Feature.Category.MOVEMENT, description = "Change how the Elytra flies.")
public class ElytraPlus extends Feature {

    @Op(name = "Auto Elytra")
    public boolean autoElytra = true;
    @OpChild(name = "Fall Distance", min = 0, max = 10, inc = 0.5f, parent = "Auto Elytra")
    public float fallDistance = 3;

    @Op(name = "Fly")
    public boolean elytraFly = true;
    @OpChild(name = "Mode", all = {"Vanilla", "Firework", "Hover"}, parent = "Fly")
    public String mode = "Vanilla";

    @OpChild(name = "Fly Speed", min = 0.1f, max = 2, inc = 0.1f, parent = "Mode", dependency = "Hover")
    public float flySpeed = 0.5f;

    @OpChild(name = "Slow Glide", parent = "Mode", dependency = "Hover")
    public boolean slowGlide = false;

    @EventListener(events = {EventMove.class})
    private void move(EventMove event) {
        this.setSuffix(mode);
        if (wearingElytra() && (autoElytra && Wrapper.INSTANCE.getLocalPlayer().fallDistance >= fallDistance && !Wrapper.INSTANCE.getLocalPlayer().isOnGround() && !Wrapper.INSTANCE.getLocalPlayer().isFallFlying())) {
            if (Wrapper.INSTANCE.getLocalPlayer().age % 5 == 0)
                NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }

        if (Wrapper.INSTANCE.getLocalPlayer().isFallFlying() && elytraFly) {
            if (mode.equalsIgnoreCase("Firework")) {

                Vec3d vec3d_1 = Wrapper.INSTANCE.getLocalPlayer().getRotationVector();
                double double_1 = 1.5D;
                double double_2 = 0.1D;
                Vec3d vec3d_2 = Wrapper.INSTANCE.getLocalPlayer().getVelocity();
                Wrapper.INSTANCE.getLocalPlayer().setVelocity(vec3d_2.add(vec3d_1.x * 0.1D + (vec3d_1.x * 1.5D - vec3d_2.x) * 0.5D, vec3d_1.y * 0.1D + (vec3d_1.y * 1.5D - vec3d_2.y) * 0.5D, vec3d_1.z * 0.1D + (vec3d_1.z * 1.5D - vec3d_2.z) * 0.5D));
            } else {
                if (mode.equalsIgnoreCase("Hover")) {
                    PlayerHelper.INSTANCE.setMoveSpeed(event, flySpeed);
                    if (event.getY() <= 0)
                        event.setY(Wrapper.INSTANCE.getOptions().keyJump.isPressed() ? flySpeed : (Wrapper.INSTANCE.getLocalPlayer().isSneaking() ? -flySpeed : (slowGlide ? -0.0001 : 0)));
                }

            }
        }
    }

    private boolean wearingElytra() {
        ItemStack equippedStack = Wrapper.INSTANCE.getLocalPlayer().getEquippedStack(EquipmentSlot.CHEST);
        return equippedStack != null && equippedStack.getItem() == Items.ELYTRA;
    }

}
