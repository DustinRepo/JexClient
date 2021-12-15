package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.command.CommandManagerJex;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Nametag;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.MerchantScreen;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.screen.MerchantScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.VillagerProfession;

import java.util.HashMap;
import java.util.Map;

@Feature.Manifest(category = Feature.Category.MISC, description = "Automatically break lecterns matched to a villager until it has the trade you want")
public class AutoLibrarianRoll extends Feature {

    @Op(name = "Price Mode", all = {"Normal", "Adjusted"})
    public String priceMode = "Normal";
    @Op(name = "Max Price", min = 1, max = 75)
    public int price = 20;
    @Op(name = "Auto Trade")
    public boolean autoTrade = true;

    private VillagerEntity villager;
    private VillagerProfession lastProfession;
    private BlockPos lecternPos;
    private boolean checkedTrades;
    private boolean tradeFound;

    private Vec3d villagerPos = Vec3d.ZERO;

    public static Enchantment enchantment = null;
    public static int enchantmentLevel = -1;

    private static final Map<VillagerEntity, BlockPos> doneVillagers = new HashMap<>();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (enchantment == null || enchantmentLevel == -1)
            return;
        if (lecternPos == null) {
            lecternPos = getLectern();
        }
        if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof MerchantScreen merchantScreen) {
            MerchantScreenHandler merchantScreenHandler = merchantScreen.getScreenHandler();
            if (merchantScreenHandler.getExperience() > 0) {
                doneVillagers.put(villager, lecternPos);
                lecternPos = null;
                villager = null;
                ChatHelper.INSTANCE.addClientMessage("Villager has been traded with, ignoring");
            }
            TradeOfferList tradeOfferList = merchantScreenHandler.getRecipes();
            if (!tradeOfferList.isEmpty()) {
                tradeOfferList.forEach(tradeOffer -> {
                    if (tradeOffer.getSellItem().getItem() instanceof EnchantedBookItem) {
                        Map<Enchantment, Integer> enchants = EnchantmentHelper.fromNbt(EnchantedBookItem.getEnchantmentNbt(tradeOffer.getSellItem()));
                        if (enchants.containsKey(enchantment) && enchants.get(enchantment) == enchantmentLevel) {
                            int count = priceMode.equalsIgnoreCase("Normal") ? tradeOffer.getOriginalFirstBuyItem().getCount() : tradeOffer.getAdjustedFirstBuyItem().getCount();
                            if (count <= price) {
                                ChatHelper.INSTANCE.addClientMessage("Enchantment found at price " + count + " emeralds");
                                tradeFound = true;
                                doneVillagers.put(villager, lecternPos);
                                this.setState(false);

                                if (autoTrade) {
                                    int book = getItem(Items.BOOK) - 6;
                                    if (book != -1) {
                                        int emerald = getItem(Items.EMERALD) - 6;
                                        if (emerald != -1) {
                                            JexClient.INSTANCE.getLogger().info("Autotrading");
                                            JexClient.INSTANCE.getLogger().info("Book slot: " + book + " Emerald slot: " + emerald);
                                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, emerald, SlotActionType.PICKUP, 0);
                                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 0, SlotActionType.PICKUP, 0);

                                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, book, SlotActionType.PICKUP, 0);
                                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 1, SlotActionType.PICKUP, 0);

                                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, 2, SlotActionType.PICKUP, 0);
                                            InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, book, SlotActionType.PICKUP, 0);
                                        } else {
                                            ChatHelper.INSTANCE.addClientMessage("No emeralds in inventory! Can not trade.");
                                        }
                                    } else {
                                        ChatHelper.INSTANCE.addClientMessage("No books in inventory! Can not trade.");
                                    }
                                }
                            } else {
                                ChatHelper.INSTANCE.addClientMessage("Enchantment found, but price is too high: " + count);
                            }
                        }
                    }
                });
                if (!tradeFound) {
                    NetworkHelper.INSTANCE.sendPacket(new CloseHandledScreenC2SPacket(merchantScreenHandler.syncId));
                    Wrapper.INSTANCE.getMinecraft().setScreen(null);
                    checkedTrades = true;
                }
            }
        }
        if (checkedTrades) {
            if (lecternPos == null || tradeFound) {
                checkedTrades = false;
            } else if (WorldHelper.INSTANCE.getBlock(lecternPos) == Blocks.LECTERN) {
                Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(lecternPos, Direction.UP);
                Wrapper.INSTANCE.getLocalPlayer().swingHand(Hand.MAIN_HAND);
            } else if (WorldHelper.INSTANCE.getBlock(lecternPos) == Blocks.AIR) {
                checkedTrades = false;
            }
        }
        if (villager == null || Wrapper.INSTANCE.getLocalPlayer().distanceTo(villager) > 6) {
            tradeFound = false;
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof VillagerEntity villagerEntity) {
                    if (villagerEntity.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN && Wrapper.INSTANCE.getLocalPlayer().distanceTo(villagerEntity) < 6) {
                        if (villager == null || Wrapper.INSTANCE.getLocalPlayer().distanceTo(villagerEntity) < Wrapper.INSTANCE.getLocalPlayer().distanceTo(villager))
                            if (!doneVillagers.containsKey(villagerEntity))
                                villager = villagerEntity;
                    }
                }
            });
        } else if (villager.getVillagerData().getProfession() != lastProfession && lecternPos != null) {
            if (villager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN) {
                NetworkHelper.INSTANCE.sendPacket(PlayerInteractEntityC2SPacket.interact(villager, false, Hand.MAIN_HAND));
            } else if (villager.getVillagerData().getProfession() == VillagerProfession.NONE) {
                int lecternHotbarSlot = InventoryHelper.INSTANCE.getFromHotbar(Items.LECTERN);
                if (lecternHotbarSlot == -1) {
                    int lecternInvSlot = InventoryHelper.INSTANCE.getFromInv(Items.LECTERN);
                    if (lecternInvSlot == -1)
                        return;
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler, lecternInvSlot < 9 ? lecternInvSlot + 36 : lecternInvSlot, SlotActionType.SWAP, 8);
                    return;
                } else {
                    InventoryHelper.INSTANCE.setSlot(lecternHotbarSlot, true, true);
                    NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
                    PlayerHelper.INSTANCE.placeBlockInPos(lecternPos, Hand.MAIN_HAND, false);
                    NetworkHelper.INSTANCE.sendPacket(new ClientCommandC2SPacket(Wrapper.INSTANCE.getLocalPlayer(), ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
                }
            }
            lastProfession = villager.getVillagerData().getProfession();
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (lecternPos != null) {
            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(lecternPos);
            Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getMatrixStack(), box, 0xff00ff00);
        }
        if (villager != null) {
            villagerPos = Render2DHelper.INSTANCE.getHeadPos(villager, event.getPartialTicks(), event.getMatrixStack());
        }
    });

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        if (villager != null && Render2DHelper.INSTANCE.isOnScreen(villagerPos)) {
            Nametag nametag = (Nametag)Feature.get(Nametag.class);
            float x = (float) villagerPos.x;
            float y = (float) villagerPos.y - (nametag.getState() && nametag.passives ? 15 : 0);
            String string1 = "Searching:";
            String string2 = enchantment.getName(enchantmentLevel).getString();
            String string3 = price + " Emeralds";
            float length1 = FontHelper.INSTANCE.getStringWidth(string1);
            float length2 = FontHelper.INSTANCE.getStringWidth(string2);
            float length3 = FontHelper.INSTANCE.getStringWidth(string3);

            Render2DHelper.INSTANCE.fill(event.getMatrixStack(), x - (length1 / 2) - 2, y - 34, x + (length1 / 2) + 2, y - 23, 0x35000000);
            FontHelper.INSTANCE.drawCenteredString(event.getMatrixStack(), string1, x, y - 32, -1);

            Render2DHelper.INSTANCE.fill(event.getMatrixStack(), x - (length2 / 2) - 2, y - 23, x + (length2 / 2) + 2, y - 12, 0x35000000);
            FontHelper.INSTANCE.drawCenteredString(event.getMatrixStack(), string2, x, y - 21, ColorHelper.INSTANCE.getClientColor());

            Render2DHelper.INSTANCE.fill(event.getMatrixStack(), x - (length3 / 2) - 2, y - 12, x + (length3 / 2) + 2, y - 1, 0x35000000);
            FontHelper.INSTANCE.drawCenteredString(event.getMatrixStack(), string3, x, y - 10, 0xff00ff00);
        }
    });

    @Override
    public void onEnable() {
        if ((enchantment == null || enchantmentLevel == -1) && Wrapper.INSTANCE.getLocalPlayer() != null) {
            ChatHelper.INSTANCE.addClientMessage("Enchantment not set! Set enchantment with " + CommandManagerJex.INSTANCE.getPrefix() + "librarianroll <enchant> <level>");
        }
        super.onEnable();
    }

    @Override
    public void onDisable() {
        tradeFound = false;
        villager = null;
        lecternPos = null;
        checkedTrades = false;
        lastProfession = null;
        super.onDisable();
    }

    private int getItem(Item item) {
        for (int i = 3; i < 38; i++) {
            if (InventoryHelper.INSTANCE.getInventory().getStack(i) != null && InventoryHelper.INSTANCE.getInventory().getStack(i).getItem() == item)
                return i;
        }
        return -1;
    }

    private BlockPos getLectern() {
        BlockPos lectern = null;
        for (int x = -4; x < 4; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                    if (WorldHelper.INSTANCE.getBlock(pos) == Blocks.LECTERN) {
                        if (lectern == null || ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), ClientMathHelper.INSTANCE.getVec(lectern)) > ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().getPos(), ClientMathHelper.INSTANCE.getVec(pos)))
                            lectern = pos;
                    }
                }
            }
        }
        return lectern;
    }

}
