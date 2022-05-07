package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderBlockEntity;
import me.dustin.jex.event.render.EventRenderEntity;
import me.dustin.jex.event.world.EventTickParticle;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.client.particle.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.EnchantmentTableBlockEntity;
import net.minecraft.world.level.block.entity.EnderChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Don't render specific entities/blocks to improve fps")
public class NoRender extends Feature {

    @Op(name = "Items")
    public boolean item = true;
    @Op(name = "Particles")
    public boolean particles = true;
    @OpChild(name = "Fireworks", parent = "Particles")
    public boolean fireworks = true;
    @OpChild(name = "Explosions", parent = "Particles")
    public boolean explosions = true;
    @OpChild(name = "Smoke", parent = "Particles")
    public boolean smoke = true;
    @OpChild(name = "Block Breaking", parent = "Particles")
    public boolean blockBreak = true;
    @Op(name = "Withers")
    public boolean withers = true;
    @Op(name = "Falling Blocks")
    public boolean fallingBlocks = true;
    @Op(name = "Signs")
    public boolean signs = true;
    @Op(name = "Chests")
    public boolean chests = true;
    @Op(name = "End Chests")
    public boolean endchests = true;
    @Op(name = "EnchantTable Books")
    public boolean enchantbooks = true;
    @Op(name = "Banners")
    public boolean banners = true;
    @Op(name = "Hoppers")
    public boolean hoppers = true;
    @Op(name = "Campfires")
    public boolean campfires = true;

    @EventPointer
    private final EventListener<EventRenderEntity> eventRenderEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof ItemEntity && item)
            event.cancel();
        if (event.getEntity() instanceof WitherBoss && withers)
            event.cancel();
        if (event.getEntity() instanceof FallingBlockEntity && fallingBlocks)
            event.cancel();
    });


    @EventPointer
    private final EventListener<EventRenderBlockEntity> eventRenderBlockEntityEventListener = new EventListener<>(event -> {
        if (event.blockEntity instanceof SignBlockEntity && signs)
            event.cancel();
        if (event.blockEntity instanceof ChestBlockEntity && chests)
            event.cancel();
        if (event.blockEntity instanceof EnderChestBlockEntity && endchests)
            event.cancel();
        if (event.blockEntity instanceof EnchantmentTableBlockEntity && enchantbooks)
            event.cancel();
        if (event.blockEntity instanceof BannerBlockEntity && banners)
            event.cancel();
        if (event.blockEntity instanceof HopperBlockEntity && hoppers)
            event.cancel();
        if (event.blockEntity instanceof CampfireBlockEntity && campfires)
            event.cancel();
    });

    @EventPointer
    private final EventListener<EventTickParticle> eventTickParticleEventListener = new EventListener<>(event -> {
        if (!particles)
            return;
        if (event.getParticle() instanceof ExplodeParticle || event.getParticle() instanceof SmokeParticle || event.getParticle() instanceof CampfireSmokeParticle && smoke) {
            event.cancel();
        }
        if (event.getParticle() instanceof HugeExplosionParticle || event.getParticle() instanceof HugeExplosionSeedParticle && explosions) {
            event.cancel();
        }
        if (event.getParticle() instanceof FireworkParticles.Starter || event.getParticle() instanceof FireworkParticles.Starter && fireworks) {
            event.cancel();
        }
        if (event.getParticle() instanceof TerrainParticle && blockBreak) {
            event.cancel();
        }
    });
}
