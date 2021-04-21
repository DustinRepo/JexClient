package me.dustin.jex.feature.impl.movement;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventMove;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.option.annotate.Op;

@Feat(name = "DepthStrider", category = FeatureCategory.MOVEMENT, description = "Move through water like you have Depth Strider boots on")
public class DepthStrider extends Feature {

    @Op(name = "Level", min = 1, max = 3)
    public int level = 3;
    @Op(name = "Allow Sprinting")
    public boolean allowSprinting;

    @EventListener(events = {EventMove.class})
    private void runMethod(EventMove eventMove) {
        int enchLevel = level;
        if (InventoryHelper.INSTANCE.getDepthStriderLevel() > enchLevel)
            enchLevel = InventoryHelper.INSTANCE.getDepthStriderLevel();
        if (WorldHelper.INSTANCE.isInLiquid(Wrapper.INSTANCE.getLocalPlayer())) {
            PlayerHelper.INSTANCE.setMoveSpeed(eventMove, PlayerHelper.INSTANCE.getWaterSpeed(enchLevel, allowSprinting));
        }
    }

}
