package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.impl.player.AutoEat;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.feature.mod.core.Feature;
import java.awt.*;
import java.util.List;

public class CrystalAura extends Feature {
	public final Property<TargetMode> modeProperty = new Property.PropertyBuilder<TargetMode>(this.getClass())
			.name("Mode")
			.value(TargetMode.SUICIDAL)
			.build();
	public final Property<AttackMode> attackModeProperty = new Property.PropertyBuilder<AttackMode>(this.getClass())
			.name("Explode")
			.value(AttackMode.ANY)
			.build();
	public final Property<Float> attackDistanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
			.name("Max Attack Distance")
			.value(5f)
			.min(1)
			.max(6)
			.inc(0.1f)
			.build();
	public final Property<Long> attackDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
			.name("Attack Delay")
			.value(200L)
		    .min(0)
			.max(2000)
		    .inc(20)
			.build();
	public final Property<Boolean> autoPlaceProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Auto Place")
			.value(false)
			.build();
	public final Property<Boolean> visualizeProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Visualize")
			.value(true)
			.parent(autoPlaceProperty)
			.depends(parent -> (boolean) parent.value())
			.build();
	public final Property<Boolean> onlyShowPlacementsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Only show placements")
			.value(false)
			.parent(visualizeProperty)
			.depends(parent -> (boolean) parent.value())
			.build();
	public final Property<Color> thinkingColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Thinking Color")
			.value(new Color(0, 150, 255))
			.parent(visualizeProperty)
			.depends(parent -> (boolean)parent.value())
			.build();
	public final Property<Color> placingColorProperty = new Property.PropertyBuilder<Color>(this.getClass())
			.name("Placing Color")
			.value(new Color(255, 0, 0))
			.parent(visualizeProperty)
			.depends(parent -> (boolean)parent.value())
			.build();
	public final Property<Long> placeDelayProperty = new Property.PropertyBuilder<Long>(this.getClass())
			.name("Place Delay")
			.value(0L)
		    .inc(10L)
		    .min(0L)
			.max(1000)
			.parent(autoPlaceProperty)
			.depends(parent -> (boolean) parent.value())
			.build();
	public final Property<Float> placeDistanceProperty = new Property.PropertyBuilder<Float>(this.getClass())
			.name("Place Distance")
			.value(3.5f)
			.min(1)
			.max(6)
			.inc(0.1f)
			.parent(autoPlaceProperty)
			.depends(parent -> (boolean) parent.value())
			.build();
	public final Property<Boolean> swingProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Swing")
            .value(true)
            .build();

	private final StopWatch stopWatch = new StopWatch();
	private BlockPos placePos;

	public CrystalAura() {
		super(Category.COMBAT);
	}

	@EventPointer
	private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
		if (AutoEat.isEating || Feature.get(AutoGapple.class).isEating())
			return;
		boolean offhand = Wrapper.INSTANCE.getLocalPlayer() != null && Wrapper.INSTANCE.getLocalPlayer().getOffHandStack().getItem() == Items.END_CRYSTAL;
		if (event.getMode() == EventPlayerPackets.Mode.PRE) {
			this.setSuffix(modeProperty.value());
			if (placePos != null) {
				RotationVector rotation = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()).add(new Vec3d(0.5, 0.5, 0.5)));
				event.setRotation(rotation);
			}

			if (stopWatch.hasPassed(placeDelayProperty.value()))
				if (autoPlaceProperty.value() && ((Wrapper.INSTANCE.getLocalPlayer().getMainHandStack() != null && Wrapper.INSTANCE.getLocalPlayer().getMainHandStack().getItem() == Items.END_CRYSTAL) || offhand)) {
					Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
						if (entity instanceof PlayerEntity entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer() && !FriendHelper.INSTANCE.isFriend(entity.getDisplayName().getString())) {
							BlockPos placingPos = getOpenBlockPos(entityPlayer);
							if (placingPos != null) {
								EndCrystalEntity crystal = new EndCrystalEntity(Wrapper.INSTANCE.getWorld(), placingPos.getX(), placingPos.getY(), placingPos.getZ());
								if (entityPlayer.distanceTo(crystal) <= 6 && Wrapper.INSTANCE.getLocalPlayer().distanceTo(crystal) <= 6 && !FriendHelper.INSTANCE.isFriend(entityPlayer.getName().getString()) && entityPlayer.getHealth() > 0 && shouldAttack(crystal)) {
									RotationVector rotation = PlayerHelper.INSTANCE.rotateToVec(Wrapper.INSTANCE.getLocalPlayer(), new Vec3d(getOpenBlockPos(entityPlayer).down().getX(), getOpenBlockPos(entityPlayer).down().getY(), getOpenBlockPos(entityPlayer).down().getZ()).add(new Vec3d(0.5, 0.5, 0.5)));
									event.setRotation(rotation);
									placePos = placingPos.down();
									stopWatch.reset();
									return;
								}
							}
						}
					});
				}
			Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
				if (stopWatch.hasPassed(attackDelayProperty.value()))
				if (entity instanceof EndCrystalEntity enderCrystalEntity) {
					if (shouldAttack(enderCrystalEntity)) {
						RotationVector rotation = PlayerHelper.INSTANCE.rotateToEntity(enderCrystalEntity);
						event.setRotation(rotation);
						Wrapper.INSTANCE.getClientPlayerInteractionManager().attackEntity(Wrapper.INSTANCE.getLocalPlayer(), enderCrystalEntity);
						if (swingProperty.value()) { 
                                                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
						}
					}
				  }
			});
		} else {
			if (placePos != null) {
				BlockHitResult blockHitResult = new BlockHitResult(new Vec3d(placePos.getX(), placePos.getY(), placePos.getZ()), Direction.UP, placePos, false);
				Wrapper.INSTANCE.getClientPlayerInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, blockHitResult);
				placePos = null;
			}
		}
	});

	@EventPointer
	private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
		if (autoPlaceProperty.value() && visualizeProperty.value())
			Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
				if (entity instanceof PlayerEntity entityPlayer && entity != Wrapper.INSTANCE.getLocalPlayer()) {
					BlockPos placingPos = getOpenBlockPos(entityPlayer);
					if (placingPos != null && !FriendHelper.INSTANCE.isFriend(entityPlayer.getDisplayName().getString())) {
						EndCrystalEntity crystal = new EndCrystalEntity(Wrapper.INSTANCE.getWorld(), placingPos.getX(), placingPos.getY(), placingPos.getZ());
						Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(placingPos.getX(), placingPos.getY(), placingPos.getZ());
						Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
						Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, shouldAttack(crystal) ? placingColorProperty.value().getRGB() : thinkingColorProperty.value().getRGB());
					}
				}
			});
	});

	public BlockPos getOpenBlockPos(PlayerEntity entityPlayer) {
		double d = 0;
		BlockPos closest = null;
		for (int x = -6; x < 6; x++) {
			for (int y = -6; y < 6; y++) {
				for (int z = -6; z < 6; z++) {
					BlockPos pos = new BlockPos(entityPlayer.getX() + x, (int) entityPlayer.getY() - y, entityPlayer.getZ() + z);
					EndCrystalEntity fakeCrystal = new EndCrystalEntity(Wrapper.INSTANCE.getWorld(), pos.getX(), pos.getY(), pos.getZ());

					List<Entity> list = Wrapper.INSTANCE.getWorld().getOtherEntities((Entity) null, new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, pos.getY() + 2.0D, pos.getZ() + 1.0D));
					boolean collides = !list.isEmpty();
					BlockPos above = pos.up();
					if (WorldHelper.INSTANCE.getBlock(pos) == Blocks.AIR && WorldHelper.INSTANCE.getBlock(above) == Blocks.AIR && !collides) {
						BlockPos below = pos.down();
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

	private boolean isBlocking(BlockPos blockPos, PlayerEntity EntityPlayer) {
		Box box = new Box(blockPos.up());
		if (EntityPlayer.getBoundingBox().intersects(box))
			return true;
		return false;
	}

	public boolean shouldAttack(EndCrystalEntity enderCrystalEntity) {
		if (Wrapper.INSTANCE.getLocalPlayer().distanceTo(enderCrystalEntity) > attackDistanceProperty.value())
			return false;
		boolean hasTarget = attackModeProperty.value() == AttackMode.ANY;
		if (!hasTarget)
			for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
				if (entity instanceof LivingEntity livingEntity && isTarget(livingEntity, enderCrystalEntity)) {
					hasTarget = true;
					break;
				}
			}
		if (!hasTarget)
			return false;
		float damage = WorldHelper.INSTANCE.calcExplosionDamage(6, Wrapper.INSTANCE.getLocalPlayer(), enderCrystalEntity.getBlockPos());
		if (modeProperty.value() == TargetMode.RISKY)
			return damage <= 65;
		if (modeProperty.value() == TargetMode.SAFE)
			return damage < 65;
		return true;
	}

	public boolean isTarget(LivingEntity livingEntity, EndCrystalEntity enderCrystalEntity) {
		if (livingEntity instanceof PlayerEntity && livingEntity != Wrapper.INSTANCE.getLocalPlayer()) {
			return !FriendHelper.INSTANCE.isFriend(livingEntity.getName().getString()) && livingEntity.distanceTo(enderCrystalEntity) <= 6 && livingEntity.getHealth() > 0;
		}
		return false;
	}

	public enum TargetMode {
		SUICIDAL, RISKY, SAFE
	}

	public enum AttackMode {
		ANY, NEAR_TARGET
	}
}
