package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.option.annotate.Op;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

@Feature.Manifest(category = Feature.Category.MISC, description = "Automatically turn ingots into blocks by opening a crafting table.")
public class SpeedCrafter extends Feature {

    @Op(name = "Delay", max = 500)
    public int delay = 0;

    public Item craftingItem;
    private boolean alerted;
    private Timer timer = new Timer();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler instanceof CraftingScreenHandler craftingScreenHandler) {
            if (InventoryHelper.INSTANCE.isInventoryFull(new ItemStack(craftingItem))) {
                if (!alerted) {
                    ChatHelper.INSTANCE.addClientMessage("Inventory is full! Speedcrafter can not craft!");
                    alerted = true;
                }
                return;
            }
            alerted = false;
            if (!timer.hasPassed(delay))
                return;
            List<RecipeResultCollection> recipeResultCollectionList = Wrapper.INSTANCE.getLocalPlayer().getRecipeBook().getResultsForGroup(RecipeBookGroup.CRAFTING_BUILDING_BLOCKS);
            for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
                for (Recipe<?> recipe : recipeResultCollection.getRecipes(true)) {
                    if (recipe.getOutput().getItem() == craftingItem) {
                        Wrapper.INSTANCE.getInteractionManager().clickRecipe(craftingScreenHandler.syncId, recipe, true);
                        InventoryHelper.INSTANCE.windowClick(craftingScreenHandler, 0, SlotActionType.QUICK_MOVE, 1);
                        timer.reset();
                        if (delay > 0)
                            return;
                    }
                }
            }
        }
        setSuffix(craftingItem == null ? "None" : craftingItem.getName().getString());
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onEnable() {
        if (craftingItem == null) {
            ChatHelper.INSTANCE.addClientMessage("Crafting item not set!");
            ChatHelper.INSTANCE.addClientMessage("Hold the intended output item and use " + CommandManagerJex.INSTANCE.getPrefix() + "sc set");
        }
        super.onEnable();
    }
}
