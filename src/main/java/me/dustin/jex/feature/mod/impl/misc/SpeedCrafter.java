package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.RecipeBookCategories;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import me.dustin.jex.feature.option.annotate.Op;
import java.util.List;

@Feature.Manifest(category = Feature.Category.MISC, description = "Automatically turn ingots into blocks by opening a crafting table.")
public class SpeedCrafter extends Feature {

    @Op(name = "Delay", max = 500)
    public int delay = 0;

    public Item craftingItem;
    private boolean alerted;
    private StopWatch stopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getLocalPlayer().containerMenu instanceof CraftingMenu craftingScreenHandler) {
            if (InventoryHelper.INSTANCE.isInventoryFull(new ItemStack(craftingItem))) {
                if (!alerted) {
                    ChatHelper.INSTANCE.addClientMessage("Inventory is full! Speedcrafter can not craft!");
                    alerted = true;
                }
                return;
            }
            alerted = false;
            if (!stopWatch.hasPassed(delay))
                return;
            List<RecipeCollection> recipeResultCollectionList = Wrapper.INSTANCE.getLocalPlayer().getRecipeBook().getCollection(RecipeBookCategories.CRAFTING_BUILDING_BLOCKS);
            for (RecipeCollection recipeResultCollection : recipeResultCollectionList) {
                for (Recipe<?> recipe : recipeResultCollection.getDisplayRecipes(true)) {
                    if (recipe.getResultItem().getItem() == craftingItem) {
                        Wrapper.INSTANCE.getMultiPlayerGameMode().handlePlaceRecipe(craftingScreenHandler.containerId, recipe, true);
                        InventoryHelper.INSTANCE.windowClick(craftingScreenHandler, 0, ClickType.QUICK_MOVE, 1);
                        stopWatch.reset();
                        if (delay > 0)
                            return;
                    }
                }
            }
        }
        setSuffix(craftingItem == null ? "None" : craftingItem.getDescription().getString());
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
