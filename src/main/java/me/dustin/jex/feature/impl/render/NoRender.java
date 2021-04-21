package me.dustin.jex.feature.impl.render;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.render.EventRenderBlockEntity;
import me.dustin.jex.event.render.EventRenderEntity;
import me.dustin.jex.event.world.EventRenderFirework;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.entity.*;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.boss.WitherEntity;

@Feat(name = "NoRender", category = FeatureCategory.VISUAL, description = "Don't render specific entities/blocks to improve fps")
public class NoRender extends Feature {

    @Op(name = "Items")
    public boolean item = true;
    @Op(name = "Fireworks")
    public boolean fireworks = true;
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

    @EventListener(events = {EventRenderEntity.class, EventRenderBlockEntity.class})
    private void runMethod(Event event) {
        if (event instanceof EventRenderEntity) {
            EventRenderEntity eventRenderEntity = (EventRenderEntity) event;
            if (eventRenderEntity.getEntity() instanceof ItemEntity && item)
                eventRenderEntity.cancel();
            if (eventRenderEntity.getEntity() instanceof WitherEntity && withers)
                eventRenderEntity.cancel();
            if (eventRenderEntity.getEntity() instanceof FallingBlockEntity && fallingBlocks)
                eventRenderEntity.cancel();
        } else if (event instanceof EventRenderBlockEntity) {
            EventRenderBlockEntity eventRenderBlockEntity = (EventRenderBlockEntity) event;
            if (eventRenderBlockEntity.blockEntity instanceof SignBlockEntity && signs)
                event.cancel();
            if (eventRenderBlockEntity.blockEntity instanceof ChestBlockEntity && chests)
                event.cancel();
            if (eventRenderBlockEntity.blockEntity instanceof EnderChestBlockEntity && endchests)
                event.cancel();
            if (eventRenderBlockEntity.blockEntity instanceof EnchantingTableBlockEntity && enchantbooks)
                event.cancel();
            if (eventRenderBlockEntity.blockEntity instanceof BannerBlockEntity && banners)
                event.cancel();
            if (eventRenderBlockEntity.blockEntity instanceof HopperBlockEntity && hoppers)
                event.cancel();
            if (eventRenderBlockEntity.blockEntity instanceof CampfireBlockEntity && campfires)
                event.cancel();
        } else if (event instanceof EventRenderFirework) {
            if (fireworks)
                event.cancel();
        }
    }

}
