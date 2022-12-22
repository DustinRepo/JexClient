package me.dustin.jex.feature.mod.impl.world;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.block.*;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BonemealAura extends Feature {
    public static BonemealAura INSTANCE;
    private boolean isBonemealing;
	
public final Property<Boolean> checkgrowProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("CheckGrow")
            .value(true)
            .build(); 
public final Property<Boolean> cropProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Crop")
            .value(true)
            .build();
public final Property<Boolean> otherProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Other")
            .value(true)
            .build();

    public BonemealAura() {
        super(Category.WORLD, "Automatically bonemeal crops around the player");
        INSTANCE = this;
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Feature.getState(AutoFarm.class) && !Feature.get(AutoFarm.class).isPaused()) {
            isBonemealing = false;
            return;
        }

        BlockPos crop = getCrop();
        if (crop == null) {
            isBonemealing = false;
            return;
        }

        int bonemeal = InventoryHelper.INSTANCE.get(Items.BONE_MEAL);
        if (bonemeal == -1) {
            isBonemealing = false;
            return;
        }
        if (bonemeal > 8) {
            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, bonemeal, SlotActionType.SWAP, 8);
            bonemeal = 8;
        }
        InventoryHelper.INSTANCE.setSlot(bonemeal, true, true);

        isBonemealing = true;
        PlayerHelper.INSTANCE.rightClickBlock(crop, Hand.MAIN_HAND, false);
        Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        BlockPos crop = getCrop();
        if (crop != null && InventoryHelper.INSTANCE.getFromHotbar(Items.BONE_MEAL) != -1) {
            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(crop);
            Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
            Render3DHelper.INSTANCE.drawBoxOutline(event.getPoseStack(), box, 0xffffff00);
        }
    });

    @Override
    public void onDisable() {
        isBonemealing = false;
        super.onDisable();
    }

    public boolean isBonemealing() {
        return isBonemealing;
    }

    public BlockPos getCrop() {
        for (int x = -2; x < 4; x++) {
            for (int y = -1; y < 2; y++) {
                for (int z = -2; z < 4; z++) {
                    BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    Block block = WorldHelper.INSTANCE.getBlock(blockPos);
		if (checkgrowProperty.value()) {	
	 if(!(block instanceof Fertilizable) || block instanceof GrassBlock){
			return null;
	 }
	}
              if (cropProperty.value()) {
                   if (block instanceof CropBlock cropBlock) {
                       int age = Wrapper.INSTANCE.getWorld().getBlockState(blockPos).get(cropBlock.getAgeProperty());
                       if (age < cropBlock.getMaxAge())
                            return blockPos;
                    }
		}
		if (otherProperty.value()) {
	        if (block instanceof Fertilizable) {
		Wrapper.INSTANCE.getWorld().getBlockState(blockPos);
                 return blockPos;
		}
		}
              }
           }
       }
        return null;
    }
}
