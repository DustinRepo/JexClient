package me.dustin.jex.feature.mod.impl.world;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

@Feature.Manifest(category = Feature.Category.WORLD, description = "Display Waypoints to mark areas.")
public class Waypoints extends Feature {

	public static ArrayList<String> servers = new ArrayList<>();
	public static ArrayList<Waypoint> waypoints = new ArrayList<>();
	private static final Map<Waypoint, Vec3> waypointPositions = Maps.newHashMap();
	@Op(name = "FOV based Tag")
	public boolean fovBasedTag = true;
	@Op(name = "Distance", min = 20, max = 150)
	public int fovDistance = 35;
	@Op(name = "Distance on Tag")
	public boolean distance = true;
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

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {

		String server = WorldHelper.INSTANCE.getCurrentServerName();
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
				if (waypoint.hidden || !waypoint.drawBeacon)
					continue;
				float x = waypoint.getX();
				float y = waypoint.getY();
				float z = waypoint.getZ();
				float distance = ClientMathHelper.INSTANCE.getDistance2D(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(x, y, z));
				Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3(x, waypoint.getY(), z));
				if (distance < 270) {
					AABB box = new AABB(renderPos.x - 0.2f, renderPos.y, renderPos.z - 0.2f, renderPos.x + 0.2f, (256 - waypoint.y), renderPos.z + 0.2f);
					Render3DHelper.INSTANCE.drawBox(((EventRender3D) event).getPoseStack(), box, waypoint.getColor());
				} else {
					float yaw = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3(x, y, z)).getYaw();
					x = (float) Wrapper.INSTANCE.getLocalPlayer().getX() + 250 * (float) Math.cos(Math.toRadians(yaw + 90));
					z = (float) Wrapper.INSTANCE.getLocalPlayer().getZ() + 250 * (float) Math.sin(Math.toRadians(yaw + 90));
				}
				Vec3 screenPos = Render2DHelper.INSTANCE.to2D(new Vec3(x, waypoint.getY() + Wrapper.INSTANCE.getLocalPlayer().getEyeHeight(Pose.STANDING), z), event.getPoseStack());
				waypointPositions.put(waypoint, screenPos);
			}
		}
	});

	@EventPointer
	private final EventListener<EventRender3D.EventRender3DNoBob> eventRender3DNoBobEventListener = new EventListener<>(event -> {
		String server = WorldHelper.INSTANCE.getCurrentServerName();
		for (Waypoint waypoint : getWaypoints(server)) {
			if (waypoint.hidden || !waypoint.drawTracer)
				continue;
			if (waypoint.isHidden())
				continue;
			float x = waypoint.getX();
			float y = waypoint.getY();
			float z = waypoint.getZ();
			float distance = ClientMathHelper.INSTANCE.getDistance2D(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(x, y, z));
			if (distance > 270) {
				float yaw = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3(x, y, z)).getYaw();
				x = (float) Wrapper.INSTANCE.getLocalPlayer().getX() + 250 * (float) Math.cos(Math.toRadians(yaw + 90));
				z = (float) Wrapper.INSTANCE.getLocalPlayer().getZ() + 250 * (float) Math.sin(Math.toRadians(yaw + 90));
			}
			Vec3 pos = new Vec3(x, y, z);
			Entity cameraEntity = Wrapper.INSTANCE.getMinecraft().getCameraEntity();
			assert cameraEntity != null;
			Vec3 entityPos = Render3DHelper.INSTANCE.getRenderPosition(new Vec3(pos.x() + 0.5f, pos.y(), pos.z() + 0.5f));

			Color color1 = ColorHelper.INSTANCE.getColor(waypoint.getColor());

			Render3DHelper.INSTANCE.setup3DRender(true);
			RenderSystem.lineWidth(1.2f);
			RenderSystem.setShader(GameRenderer::getPositionColorShader);

			Vec3 eyes = new Vec3(0, 0, 1).xRot(-(float) Math.toRadians(PlayerHelper.INSTANCE.getPitch())).yRot(-(float) Math.toRadians(PlayerHelper.INSTANCE.getYaw()));

			BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
			bufferBuilder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex(eyes.x, eyes.y, eyes.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.vertex(entityPos.x, entityPos.y, entityPos.z).color(color1.getRed(), color1.getGreen(), color1.getBlue(), color1.getAlpha()).endVertex();
			bufferBuilder.clear();
			BufferUploader.drawWithShader(bufferBuilder.end());
			Render3DHelper.INSTANCE.end3DRender();
		}
	});

	@EventPointer
	private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
		waypointPositions.keySet().forEach(waypoint -> {
			if (waypoint.hidden || !waypoint.drawNametag)
				return;
			Vec3 renderPos = waypointPositions.get(waypoint);
			if (shouldRender(renderPos)) {
				String name = waypoint.getName();
				if (this.distance) {
					name = String.format("%s [%.1f]", waypoint.getName(), ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), new Vec3(waypoint.getX(), waypoint.getY(), waypoint.getZ())));
				}
				float x = (float) renderPos.x;
				float y = (float) renderPos.y;
				float crosshairFOV = ClientMathHelper.INSTANCE.getDistance2D(new Vec2(x, y), new Vec2(Render2DHelper.INSTANCE.getScaledWidth() / 2.f, Render2DHelper.INSTANCE.getScaledHeight() / 2.f));
				if (fovBasedTag && crosshairFOV > fovDistance)
					name = "[]";
				float width = FontHelper.INSTANCE.getStringWidth(name);
				Render2DHelper.INSTANCE.fill(((EventRender2D) event).getPoseStack(), x - (width / 2) - 2, y - 11, x + (width / 2.f) + 2, y, 0x50000000);
				FontHelper.INSTANCE.drawCenteredString(((EventRender2D) event).getPoseStack(), name, x, y - 9, waypoint.color);
			}
		});
	});

	@EventPointer
	private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
		spin++;
	}, new TickFilter(EventTick.Mode.PRE));

	public boolean shouldRender(Vec3 pos) {
		return pos != null && (pos.z() > -1 && pos.z() < 1);
	}

	public static class Waypoint {
		private String name;
		private String server;
		private String dimension;
		private float x, y, z;
		private int color;

		private boolean hidden = false;
		private boolean drawNametag = true;
		private boolean drawBeacon = true;
		private boolean drawTracer = false;

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

		public boolean isDrawNametag() {
			return drawNametag;
		}

		public void setDrawNametag(boolean drawNametag) {
			this.drawNametag = drawNametag;
		}

		public boolean isDrawBeacon() {
			return drawBeacon;
		}

		public void setDrawBeacon(boolean drawBeacon) {
			this.drawBeacon = drawBeacon;
		}

		public boolean isDrawTracer() {
			return drawTracer;
		}

		public void setDrawTracer(boolean drawTracer) {
			this.drawTracer = drawTracer;
		}
	}
}
