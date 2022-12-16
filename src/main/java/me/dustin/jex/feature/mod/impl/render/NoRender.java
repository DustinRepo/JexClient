package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRenderBlockEntity;
import me.dustin.jex.event.render.EventWorldRenderEntity;
import me.dustin.jex.event.world.EventTickParticle;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import net.minecraft.block.entity.BannerBlockEntity;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.EnchantingTableBlockEntity;
import net.minecraft.block.entity.EnderChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.particle.*;
import net.minecraft.entity.projectile.thrown.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;

public class NoRender extends Feature {

    public final Property<Boolean> itemProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Items")
            .value(true)
            .build();
    public final Property<Boolean> particlesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Particles")
            .value(true)
            .build();
    public final Property<Boolean> fireworksProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Fireworks")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> explosionsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Explosions")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> smokeProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Smoke")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> blockBreakProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Block Breaking")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();  
    public final Property<Boolean> damageProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Damage")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> glowProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Glow")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> totemProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Totem")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> ashProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Ash")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
     public final Property<Boolean> cloudProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Cloud")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
     public final Property<Boolean> dragonbreathProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Dragon Breath")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();
    public final Property<Boolean> crackProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Crack")
            .value(true)
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
            .build();   
    public final Property<Boolean> withersProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Withers")
            .value(true)
            .build();
     public final Property<Boolean> eggProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Egg")
            .value(true)
            .build(); 
    public final Property<Boolean> snowballProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Snowball")
            .value(true)
            .build();
    public final Property<Boolean> fallingBlocksProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Falling Blocks")
            .value(true)
            .build();
    public final Property<Boolean> signsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Signs")
            .value(true)
            .build();
    public final Property<Boolean> chestsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Chests")
            .value(true)
            .build();
    public final Property<Boolean> endchestsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("End Chests")
            .value(true)
            .build();
    public final Property<Boolean> enchantbooksProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("EnchantTable Books")
            .value(true)
            .build();
    public final Property<Boolean> bannersProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Banners")
            .value(true)
            .build();
    public final Property<Boolean> hoppersProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Hoppers")
            .value(true)
            .build();
    public final Property<Boolean> campfiresProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Campfires")
            .value(true)
            .build();

    public NoRender() {
        super(Category.VISUAL, "Don't render specific entities/blocks to improve fps");
    }

    @EventPointer
    private final EventListener<EventWorldRenderEntity> eventRenderEntityEventListener = new EventListener<>(event -> {
        if (event.getEntity() instanceof ItemEntity && itemProperty.value())
            event.cancel();
        if (event.getEntity() instanceof WitherEntity && withersProperty.value())
            event.cancel();
        if (event.getEntity() instanceof FallingBlockEntity && fallingBlocksProperty.value())
            event.cancel();
        if (event.getEntity() instanceof EggEntity && eggProperty.value())
            event.cancel();
        if (event.getEntity() instanceof SnowballEntity && snowballProperty.value())
            event.cancel();
        
    });


    @EventPointer
    private final EventListener<EventRenderBlockEntity> eventRenderBlockEntityEventListener = new EventListener<>(event -> {
        if (event.blockEntity instanceof SignBlockEntity && signsProperty.value())
            event.cancel();
        if (event.blockEntity instanceof ChestBlockEntity && chestsProperty.value())
            event.cancel();
        if (event.blockEntity instanceof EnderChestBlockEntity && endchestsProperty.value())
            event.cancel();
        if (event.blockEntity instanceof EnchantingTableBlockEntity && enchantbooksProperty.value())
            event.cancel();
        if (event.blockEntity instanceof BannerBlockEntity && bannersProperty.value())
            event.cancel();
        if (event.blockEntity instanceof HopperBlockEntity && hoppersProperty.value())
            event.cancel();
        if (event.blockEntity instanceof CampfireBlockEntity && campfiresProperty.value())
            event.cancel();
    });

    @EventPointer
    private final EventListener<EventTickParticle> eventTickParticleEventListener = new EventListener<>(event -> {
        if (!particlesProperty.value())
            return;
        if (event.getParticle() instanceof ExplosionSmokeParticle || event.getParticle() instanceof FireSmokeParticle || event.getParticle() instanceof CampfireSmokeParticle && smokeProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof ExplosionLargeParticle || event.getParticle() instanceof ExplosionEmitterParticle && explosionsProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof FireworksSparkParticle.FireworkParticle || event.getParticle() instanceof FireworksSparkParticle.FireworkParticle && fireworksProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof BlockDustParticle && blockBreakProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof DamageParticle && damageProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof GlowParticle && glowProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof AshParticle && ashProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof TotemParticle && totemProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof DragonBreathParticle && dragonbreathProperty.value()) {
            event.cancel();
        }
        if (event.getParticle() instanceof CrackParticle && crackProperty.value()) {
            event.cancel();
        }
    });
}
