package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderBlockEntity;
import me.dustin.jex.event.render.EventRenderEntity;
import me.dustin.jex.event.world.EventTickParticle;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.particle.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;

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

    public NoRender() {
        super(Category.VISUAL, "Don't render specific entities/blocks to improve fps");
    }

    @EventPointer
    private final EventListener<EventRenderEntity> eventRenderEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof ItemEntity && item)
            event.cancel();
        if (event.getEntity() instanceof WitherEntity && withers)
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
        if (event.blockEntity instanceof EnchantingTableBlockEntity && enchantbooks)
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
        if (event.getParticle() instanceof ExplosionSmokeParticle || event.getParticle() instanceof FireSmokeParticle || event.getParticle() instanceof CampfireSmokeParticle && smoke) {
            event.cancel();
        }
        if (event.getParticle() instanceof ExplosionLargeParticle || event.getParticle() instanceof ExplosionEmitterParticle && explosions) {
            event.cancel();
        }
        if (event.getParticle() instanceof FireworksSparkParticle.FireworkParticle || event.getParticle() instanceof FireworksSparkParticle.FireworkParticle && fireworks) {
            event.cancel();
        }
        if (event.getParticle() instanceof BlockDustParticle && blockBreak) {
            event.cancel();
        }
    });
}
