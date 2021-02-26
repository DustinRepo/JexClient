package me.dustin.jex.module.impl.world;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EyeOfEnderEntity;
import net.minecraft.util.math.Vec3d;

@ModClass(name = "EndPortalFinder", category = ModCategory.WORLD, description = "Find end portals with just two eye of ender. Math from https://www.omnicalculator.com/other/end-portal-finder")
public class EndPortalFinder extends Module {

    double[] portalPos = null;
    private Vec3d firstPos;
    private float firstYaw = -999;
    private Vec3d secondPos;
    private float secondYaw = -999;
    private int pearl = 0;
    private EyeOfEnderEntity trackedEye;

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            if (portalPos != null) {
                ChatHelper.INSTANCE.addClientMessage("End Portal should be near: x" + ((int) portalPos[0]) + " z" + ((int) portalPos[1]));
                firstPos = null;
                pearl = 0;
                portalPos = null;
            }
            if (pearl == 0) {
                if (findEye() != null) {
                    if (firstPos == null) {
                        firstPos = Wrapper.INSTANCE.getLocalPlayer().getPos();
                    }
                    firstYaw = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), trackedEye)[0];
                }
                if (trackedEye == null || Wrapper.INSTANCE.getWorld().getEntityById(trackedEye.getEntityId()) == null) {
                    if (firstPos != null) {
                        pearl = 1;
                        secondPos = null;
                        ChatHelper.INSTANCE.addClientMessage("First position set. Now please move about 100 blocks away and throw another.");
                        trackedEye = null;
                    }
                }
            } else if (pearl == 1) {
                if (findEye() != null) {
                    if (secondPos == null) {
                        secondPos = Wrapper.INSTANCE.getLocalPlayer().getPos();
                    }
                    secondYaw = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), trackedEye)[0];
                }
                if (trackedEye == null || Wrapper.INSTANCE.getWorld().getEntityById(trackedEye.getEntityId()) == null) {
                    if (firstPos != null && secondPos != null) {
                        portalPos = getPortalPosition();
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        firstPos = null;
        secondPos = null;
        pearl = 0;
        super.onDisable();
    }

    public double mca2deg(double mca) {
        if (mca < -90) {
            return Math.abs(mca) - 90;
        } else if (mca < 0) {
            return 270 + mca;
        } else {
            return 270 - mca;
        }
    }

    private double[] getPortalPosition() {
        double o1 = mca2deg(firstYaw);
        double o2 = mca2deg(secondYaw);
        double x1 = firstPos.x;
        double x2 = secondPos.x;
        double z1 = firstPos.z;
        double z2 = secondPos.z;
        double x = ((z1 - z2) + x2 * Math.tan(o2) - x1 * Math.tan(o1)) / (Math.tan(o2) - Math.tan(o1));
        double z = ((z1 * Math.tan(o2) - z2 * Math.tan(o1)) + (x2 - x1) * Math.tan(o2) * Math.tan(o1)) / (Math.tan(o2) - Math.tan(o1));
        return new double[]{x, z};
    }

    private EyeOfEnderEntity findEye() {
        if (trackedEye != null) {
            return trackedEye;
        }
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof EyeOfEnderEntity) {
                if (entity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) < 1)
                    return trackedEye = (EyeOfEnderEntity) entity;
            }
        }
        return null;
    }

}
