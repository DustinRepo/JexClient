package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.StopWatch;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import java.util.Random;

@Feature.Manifest(category = Feature.Category.MISC, description = "Drop all items from your inventory in creative.")
public class CreativeDrop extends Feature {

    @Op(name = "Drop Delay (MS)", max = 1000, inc = 10)
    public int delay = 0;

    @Op(name = "Speed", min = 1, max = 10)
    public int speed = 1;

    @Op(name = "Name")
    public boolean name = true;

    @Op(name = "Enchant")
    public boolean enchant = true;

    @OpChild(name = "1.14+", parent = "Enchant")
    public boolean newEnchants = true;

    private int slot = 1;
    private final StopWatch stopWatch = new StopWatch();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        Random random = new Random();
        String[] names = new String[]{JexClient.INSTANCE.getBaseUrl(), "Download Jex Client to do this", "Nice FPS", "Oh look a shiny item", "Copper pants", "How do I stop dropping items?", "Can you hear me?", "Please help I am stuck in this item"};
        if (stopWatch.hasPassed(delay) && Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
            for (int i = 0; i < speed; i++) {
                ItemStack itemStack = new ItemStack(Item.byId(slot));
                if (itemStack.getItem() != null && itemStack.getItem() != Items.AIR) {
                    String name = "§" + (slot % 9) + names[(int) (random.nextFloat() * (names.length))];
                    if (this.name)
                        itemStack.setHoverName(Component.nullToEmpty(name));
                    if (enchant)
                        Registry.ENCHANTMENT.forEach(enchantment -> {
                            if (!newEnchants) {
                                if (enchantment == Enchantments.SOUL_SPEED || enchantment == Enchantments.LOYALTY || enchantment == Enchantments.MULTISHOT || enchantment == Enchantments.PIERCING || enchantment == Enchantments.RIPTIDE || enchantment == Enchantments.IMPALING || enchantment == Enchantments.CHANNELING || enchantment == Enchantments.QUICK_CHARGE)
                                    return;
                            }
                            itemStack.enchant(enchantment, 127);
                        });

                    NetworkHelper.INSTANCE.sendPacket(new ServerboundSetCreativeModeSlotPacket(36, itemStack));
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, 36, ClickType.THROW, 0);
                    slot += 1;
                } else
                    slot = 1;
            }
            stopWatch.reset();
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));
}
