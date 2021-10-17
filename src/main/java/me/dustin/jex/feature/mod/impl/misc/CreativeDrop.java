package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.LiteralText;
import net.minecraft.util.registry.Registry;

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
    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            Random random = new Random();
            String[] names = new String[]{"https://jexclient.com", "Download Jex Client to do this", "Nice FPS", "Oh look a shiny item", "Copper pants", "How do I stop dropping items?", "Can you hear me?", "Please help I am stuck in this item"};
            if (timer.hasPassed(delay) && Wrapper.INSTANCE.getLocalPlayer().isCreative()) {
                for (int i = 0; i < speed; i++) {
                    ItemStack itemStack = new ItemStack(Item.byRawId(slot));
                    if (itemStack.getItem() != null && itemStack.getItem() != Items.AIR) {
                        String name = "§" + (slot % 9) + names[(int) (random.nextFloat() * (names.length))];
                        if (this.name)
                            itemStack.setCustomName(new LiteralText(name));
                        if (enchant)
                            Registry.ENCHANTMENT.forEach(enchantment -> {
                                if (!newEnchants) {
                                    if (enchantment == Enchantments.SOUL_SPEED || enchantment == Enchantments.LOYALTY || enchantment == Enchantments.MULTISHOT || enchantment == Enchantments.PIERCING || enchantment == Enchantments.RIPTIDE || enchantment == Enchantments.IMPALING || enchantment == Enchantments.CHANNELING || enchantment == Enchantments.QUICK_CHARGE)
                                        return;
                                }
                                itemStack.addEnchantment(enchantment, 127);
                            });

                        NetworkHelper.INSTANCE.sendPacket(new CreativeInventoryActionC2SPacket(36, itemStack));
                        InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 36, SlotActionType.THROW, 0);
                        slot += 1;
                    } else
                        slot = 1;
                }
                timer.reset();
            }
        }
    }

}
