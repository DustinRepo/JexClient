package me.dustin.jex.module.impl.world;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.core.annotate.ModClass;
import me.dustin.jex.module.core.enums.ModCategory;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

@ModClass(name = "Waypoints", category = ModCategory.WORLD, description = "Display Waypoints to mark areas.")
public class Waypoints extends Module {

    public static ArrayList<String> servers = new ArrayList<>();
    public static ArrayList<Waypoint> waypoints = new ArrayList<>();
    private static Map<Waypoint, Vec3d> waypointPositions = Maps.newHashMap();
    @Op(name = "Nametags")
    public boolean nametags = true;
    @OpChild(name = "Distance", parent = "Nametags")
    public boolean distance = true;
    @Op(name = "Beacon")
    public boolean beacon = true;
    @Op(name = "Tracer")
    public boolean tracer = false;
    @Op(name = "Last Death")
    public boolean lastDeath = true;
    int spin = 0;

    public static Waypoint get(String name, String server) {
        for (Waypoint waypoint : waypoints) {
            if (waypoint.getServer().equalsIgnoreCase(server) && waypoint.getName().equalsIgnoreCase(name))
                return waypoint;
        }
        return null;
    }

    public static ArrayList<Waypoint> getWaypoints(String server) {
        ArrayList<Waypoint> points = new ArrayList<>();
        for (Waypoint waypoint : waypoints) {
            if (waypoint.getServer().equalsIgnoreCase(server))
                points.add(waypoint);
        }
        return points;
    }

    public static ArrayList<String> getServers() {
        return servers;
    }

    @EventListener(events = {EventRender3D.class, EventRender3D.EventRender3DNoBob.class, EventRender2D.class, EventTick.class})
    private void runMethod(Event event) {
        if (event instanceof EventTick) {
            spin++;
        }
        if (event instanceof EventRender3D) {
            String server = Wrapper.INSTANCE.getMinecraft().isIntegratedServerRunning() ? Objects.requireNonNull(Wrapper.INSTANCE.getMinecraft().getServer()).getName() : Objects.requireNonNull(Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry()).address;
            if (!Wrapper.INSTANCE.getLocalPlayer().isAlive() && lastDeath) {
                Waypoint oldWaypoint = get("Last Death", server);
                if (oldWaypoint != null) {
                    waypoints.remove(oldWaypoint);
                }
                waypoints.add(new Waypoint("Last Death", server, (float) Wrapper.INSTANCE.getLocalPlayer().getX(), (float) Wrapper.INSTANCE.getLocalPlayer().getY(), (float) Wrapper.INSTANCE.getLocalPlayer().getZ(), WorldHelper.INSTANCE.getDimensionID().toString(), ColorHelper.INSTANCE.getColorViaHue(0).getRGB()));
            }
            waypointPositions.clear();
            for (Waypoint waypoint : getWaypoints(server)) {
                if (waypoint.getDimension().equalsIgnoreCase(WorldHelper.INSTANCE.getDimensionID().toString())) {
                    float x = waypoint.getX();
                    float y = waypoint.getY();
                    float z = waypoint.getZ();
                    float distance = ClientMathHelper.INSTANCE.getDistance2D(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(x, y, z));
                    if (distance > 270) {
                        float yaw = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(x, y, z)).getYaw();
                        x = (float) Wrapper.INSTANCE.getLocalPlayer().getX() + 250 * (float) Math.cos(Math.toRadians(yaw + 90));
                        z = (float) Wrapper.INSTANCE.getLocalPlayer().getZ() + 250 * (float) Math.sin(Math.toRadians(yaw + 90));
                    }
                    Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(x, waypoint.getY(), z));
                    Vec3d screenPos = Render2DHelper.INSTANCE.to2D(new Vec3d(x, waypoint.getY() + Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(EntityPose.STANDING), z));
                    waypointPositions.put(waypoint, screenPos);
                    if (beacon && distance < 270) {
                        Box box = new Box(renderPos.x - 0.2f, renderPos.y, renderPos.z - 0.2f, renderPos.x + 0.2f, (256 - waypoint.y), renderPos.z + 0.2f);
                        Render3DHelper.INSTANCE.drawBox(box, waypoint.getColor());
                    }
                }
            }
        }
        if (event instanceof EventRender3D.EventRender3DNoBob) {
            if (!tracer)
                return;
            String server = Wrapper.INSTANCE.getMinecraft().isIntegratedServerRunning() ? Objects.requireNonNull(Wrapper.INSTANCE.getMinecraft().getServer()).getName() : Objects.requireNonNull(Wrapper.INSTANCE.getMinecraft().getCurrentServerEntry()).address;
            EventRender3D.EventRender3DNoBob eventRender3D = (EventRender3D.EventRender3DNoBob) event;
            for (Waypoint waypoint : getWaypoints(server)) {
                if (waypoint.isHidden())
                    continue;
                float x = waypoint.getX();
                float y = waypoint.getY();
                float z = waypoint.getZ();
                float distance = ClientMathHelper.INSTANCE.getDistance2D(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(x, y, z));
                if (distance > 270) {
                    float yaw = PlayerHelper.INSTANCE.getRotations(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(x, y, z)).getYaw();
                    x = (float) Wrapper.INSTANCE.getLocalPlayer().getX() + 250 * (float) Math.cos(Math.toRadians(yaw + 90));
                    z = (float) Wrapper.INSTANCE.getLocalPlayer().getZ() + 250 * (float) Math.sin(Math.toRadians(yaw + 90));
                }
                Vec3d pos = new Vec3d(x, y, z);
                Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
                assert cameraEntity != null;
                Vec3d entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3d(pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f));

                boolean bobView = Wrapper.INSTANCE.getOptions().bobView;
                float lastNauseaStrength = Wrapper.INSTANCE.getLocalPlayer().lastNauseaStrength;
                float nextNauseStrength = Wrapper.INSTANCE.getLocalPlayer().nextNauseaStrength;
                float red = (waypoint.getColor() >> 16 & 0xFF) / 255.0F;
                float green = (waypoint.getColor() >> 8 & 0xFF) / 255.0F;
                float blue = (waypoint.getColor() & 0xFF) / 255.0F;

                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                RenderSystem.lineWidth(1.2f);

                Vec3d eyes = new Vec3d(0, 0, 1).rotateX(-(float) Math.toRadians(Wrapper.INSTANCE.getLocalPlayer().pitch)).rotateY(-(float) Math.toRadians(Wrapper.INSTANCE.getLocalPlayer().yaw));

                BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
                bufferBuilder.begin(1, VertexFormats.POSITION_COLOR);
                bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(red, green, blue, 1).next();
                bufferBuilder.vertex(entityPos.x, entityPos.y, entityPos.z).color(red, green, blue, 1).next();
                bufferBuilder.end();
                BufferRenderer.draw(bufferBuilder);

                RenderSystem.enableDepthTest();
                RenderSystem.enableTexture();

                Wrapper.INSTANCE.getOptions().bobView = bobView;
                Wrapper.INSTANCE.getLocalPlayer().lastNauseaStrength = lastNauseaStrength;
                Wrapper.INSTANCE.getLocalPlayer().nextNauseaStrength = nextNauseStrength;
            }
        }
        if (event instanceof EventRender2D) {
            waypointPositions.keySet().forEach(waypoint -> {
                if (waypoint.hidden)
                    return;
                Vec3d renderPos = waypointPositions.get(waypoint);
                if (shouldRender(renderPos)) {
                    if (nametags) {
                        String name = waypoint.getName();
                        if (this.distance) {
                            name = String.format("%s [%.1f]", waypoint.getName(), ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), new Vec3d(waypoint.getX(), waypoint.getY(), waypoint.getZ())));
                        }
                        float width = FontHelper.INSTANCE.getStringWidth(name);
                        float x = (float) renderPos.x;
                        float y = (float) renderPos.y;
                        Render2DHelper.INSTANCE.fill(((EventRender2D) event).getMatrixStack(), (float) x - (width / 2) - 2, (float) y - 11, (float) x + (width / 2) + 2, (float) y, 0x50000000);
                        FontHelper.INSTANCE.drawWithShadow(((EventRender2D) event).getMatrixStack(), name, (float) x - (width / 2), (float) y - 9, waypoint.color);
                    }
                }
            });
        }
    }

    public boolean shouldRender(Vec3d pos) {
        return pos != null && (pos.getZ() > -1 && pos.getZ() < 1);
    }

    public static class Waypoint {
        private String name;
        private String server;
        private String dimension;
        private boolean hidden;
        private float x, y, z;
        private int color;

        public Waypoint(String name, String server, float x, float y, float z, String dimension, int color) {
            if (servers.contains(server))
                servers.add(server);
            this.name = name;
            this.server = server;
            this.x = x;
            this.y = y;
            this.z = z;
            this.dimension = dimension;
            this.color = color;
        }


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getServer() {
            return server;
        }

        public void setServer(String server) {
            this.server = server;
        }

        public String getDimension() {
            return dimension;
        }

        public void setDimension(String dimension) {
            this.dimension = dimension;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getZ() {
            return z;
        }

        public void setZ(float z) {
            this.z = z;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }
    }
}
