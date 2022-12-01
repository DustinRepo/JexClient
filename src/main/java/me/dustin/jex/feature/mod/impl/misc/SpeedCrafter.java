package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.feature.command.CommandManager;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.client.gui.screen.recipebook.RecipeResultCollection;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import java.util.List;

public class SpeedCrafter extends Feature {

    public final Property<Long> delayProperty = new Property.PropertyBuilder<Long>(this.getClass())
            .name("Delay")
            .value(0L)
            .max(500)
            .build();

    public Item craftingItem;
    private boolean alerted;
    private final StopWatch stopWatch = new StopWatch();

    public SpeedCrafter() {
        super(Category.MISC, "Automatically craft by opening a crafting table.");
    }

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
            if (!stopWatch.hasPassed(delayProperty.value()))
                return;
            List<RecipeResultCollection> recipeResultCollectionList = Wrapper.INSTANCE.getLocalPlayer().getRecipeBook().getResultsForGroup(RecipeBookGroup.CRAFTING_SEARCH);
            for (RecipeResultCollection recipeResultCollection : recipeResultCollectionList) {
                for (Recipe<?> recipe : recipeResultCollection.getRecipes(true)) {
                    if (recipe.getOutput().getItem() == craftingItem) {
                        Wrapper.INSTANCE.getClientPlayerInteractionManager().clickRecipe(craftingScreenHandler.syncId, recipe, true);
                        InventoryHelper.INSTANCE.windowClick(craftingScreenHandler, 0, SlotActionType.QUICK_MOVE, 1);
                        stopWatch.reset();
                        if (delayProperty.value() > 0)
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
            ChatHelper.INSTANCE.addClientMessage("Hold the intended output item and use " + CommandManager.INSTANCE.getPrefix() + "sc set");
        }
        super.onEnable();
    }
}
