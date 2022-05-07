package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import java.awt.*;
import java.util.List;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Auto place/destroy End Crystals")
public class CrystalAura extends Feature {

	@Op(name = "Mode", all = { "Suicidal", "Risky" })
	public String mode = "Suicidal";

	@Op(name = "Attack", all = { "Any", "Near Target" })
	public String attackMode = "Any";

	@OpChild(name = "Max Attack Distance", min = 1, max = 6, inc = 0.1f, parent = "Attack")
	public float attackDistance = 4;

	@Op(name = "Auto Place")
	public boolean autoPlace = false;

	@OpChild(name = "Visualize", parent = "Auto Place")
	public boolean visualize = true;
	@OpChild(name = "Only show placements", parent = "Visualize")
	public boolean onlyShowPlacements;
	@OpChild(name = "Thinking Color", isColor = true, parent = "Visualize")
	public int thinkingColor = new Color(0, 150, 255).getRGB();
	@OpChild(name = "Placing Color", isColor = true, parent = "Visualize")
	public int placingColor = new Color(255, 0, 0).getRGB();

	@OpChild(name = "Place Delay", min = 0, max = 2000, parent = "Auto Place")
	public int delay = 200;

	private final StopWatch stopWatch = new StopWatch();
	private BlockPos placePos;

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (AutoEat.isEating || Feature.get(AutoGapple.class).isEating())
			return;
		boolean offhand = Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getOffhandItem().getItem() == Items.END_CRYSTAL;
		if (event.getMode() == EventPlayerPackets.Mode.PRE) {
			this.setSuffix(mode);
			if (placePos != null) {
				RotationVector rotation = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3(placePos.getX(), placePos.getY(), placePos.getZ()).add(new Vec3(0.5, 0.5, 0.5)));
				event.setRotation(rotation);
			}

			if (stopWatch.hasPassed(delay))
				if (autoPlace && ((Wrapper.INSTANCE.getLocalPlayer().getMainHandItem() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandItem().getItem() == Items.END_CRYSTAL) || offhand)) {
					Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
						if (entity instanceof Player entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer() && !FriendHelper.INSTANCE.isFriend(entity.getDisplayName().getString())) {
							BlockPos placingPos = getOpenBlockPos(entityPlayer);
							if (placingPos != null) {
								EndCrystal crystal = new EndCrystal(Wrapper.INSTANCE.getWorld(), placingPos.getX(), placingPos.getY(), placingPos.getZ());
								if (entityPlayer.distanceTo(crystal) <= 6 && Wrapper.INSTANCE.getLocalPlayer().distanceTo(crystal) <= 6 && !FriendHelper.INSTANCE.isFriend(entityPlayer.getName().getString()) && entityPlayer.getHealth() > 0 && shouldAttack(crystal)) {
									RotationVector rotation = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3(getOpenBlockPos(entityPlayer).below().getX(), getOpenBlockPos(entityPlayer).below().getY(), getOpenBlockPos(entityPlayer).below().getZ()).add(new Vec3(0.5, 0.5, 0.5)));
									event.setRotation(rotation);
									placePos = placingPos.below();
									stopWatch.reset();
									return;
								}
							}
						}
					});
				}
			Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
				if (entity instanceof EndCrystal enderCrystalEntity) {
					if (shouldAttack(enderCrystalEntity)) {
						RotationVector rotation = PlayerHelper.INSTANCE.rotateToEntity(enderCrystalEntity);
						event.setRotation(rotation);
						Wrapper.INSTANCE.getMultiPlayerGameMode().attack(Wrapper.INSTANCE.getLocalPlayer(), enderCrystalEntity);
						Wrapper.INSTANCE.getLocalPlayer().swing(InteractionHand.MAIN_HAND);
					}
				}
			});
		} else {
			if (placePos != null) {
				BlockHitResult blockHitResult = new BlockHitResult(new Vec3(placePos.getX(), placePos.getY(), placePos.getZ()), Direction.UP, placePos, false);
				Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND, blockHitResult);
				Wrapper.INSTANCE.getLocalPlayer().swing(offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
				placePos = null;
			}
		}
	});

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		if (autoPlace && visualize)
			Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
				if (entity instanceof Player entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer()) {
					BlockPos placingPos = getOpenBlockPos(entityPlayer);
					if (placingPos != null && !FriendHelper.INSTANCE.isFriend(entityPlayer.getDisplayName().getString())) {
						EndCrystal crystal = new EndCrystal(Wrapper.INSTANCE.getWorld(), placingPos.getX(), placingPos.getY(), placingPos.getZ());
						Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(placingPos.getX(), placingPos.getY(), placingPos.getZ());
						AABB box = new AABB(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
						Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, shouldAttack(crystal) ? placingColor : thinkingColor);
					}
				}
			});
	});

	public BlockPos getOpenBlockPos(Player entityPlayer) {
		double d = 0;
		BlockPos closest = null;
		for (int x = -4; x < 4; x++) {
			for (int y = -1; y < 4; y++) {
				for (int z = -4; z < 4; z++) {
					BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);
					EndCrystal fakeCrystal = new EndCrystal(Wrapper.INSTANCE.getWorld(), pos.getX(), pos.getY(), pos.getZ());

					List<Entity> list = Wrapper.INSTANCE.getWorld().getEntities((Entity) null, new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 2.0D, pos.getZ() + 1.0D));
					boolean collides = !list.isEmpty();
					BlockPos above = pos.above();
					if (WorldHelper.INSTANCE.getBlock(pos) == Blocks.AIR && WorldHelper.INSTANCE.getBlock(above) == Blocks.AIR && !collides) {
						BlockPos below = pos.below();
						Block belowBlock = WorldHelper.INSTANCE.getBlock(below);
						if (belowBlock == Blocks.OBSIDIAN || belowBlock == Blocks.BEDROCK) {
							if (!shouldAttack(fakeCrystal))
								continue;
							double playerdist = entityPlayer.distanceTo(fakeCrystal) - (pos.getY() - entityPlayer.getY());
							double damage = WorldHelper.INSTANCE.calcExplosionDamage(6, entityPlayer, pos);
							double damageToMe = WorldHelper.INSTANCE.calcExplosionDamage(6, Wrapper.INSTANCE.getLocalPlayer(), pos);
							double eff = damage - playerdist - damageToMe;
							if (eff > d) {
								closest = pos;
								d = eff;
							}
						}
					}
				}
			}
		}
		return closest;
	}

	private boolean isBlocking(BlockPos blockPos, Player EntityPlayer) {
		AABB box = new AABB(blockPos.above());
		if (EntityPlayer.getBoundingBox().intersects(box))
			return true;
		return false;
	}

	public boolean shouldAttack(EndCrystal enderCrystalEntity) {
		if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(enderCrystalEntity) > attackDistance)
			return false;
		boolean hasTarget = mode.equalsIgnoreCase("Any");
		if (!hasTarget)
			for (Entity entity : Wrapper.INSTANCE.getWorld().entitiesForRendering()) {
				if (entity instanceof LivingEntity livingEntity && isTarget(livingEntity, enderCrystalEntity)) {
					hasTarget = true;
					break;
				}
			}
		if (!hasTarget)
			return false;
		float damage = WorldHelper.INSTANCE.calcExplosionDamage(6, Wrapper.INSTANCE.getLocalPlayer(), enderCrystalEntity.blockPosition());
		if (mode.equalsIgnoreCase("Risky"))
			return damage <= 65;
		return true;
	}

	public boolean isTarget(LivingEntity livingEntity, EndCrystal enderCrystalEntity) {
		if (livingEntity instanceof Player && livingEntity != Wrapper.INSTANCE.getLocalPlayer()) {
			return !FriendHelper.INSTANCE.isFriend(livingEntity.getName().getString()) && livingEntity.distanceTo(enderCrystalEntity) <= 6 && livingEntity.getHealth() > 0;
		}
		return false;
	}

}
