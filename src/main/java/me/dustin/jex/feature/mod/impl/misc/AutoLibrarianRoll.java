package me.dustin.jex.feature.mod.impl.misc;

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
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
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

    private Villager villager;
    private VillagerProfession lastProfession;
    private BlockPos lecternPos;
    private boolean checkedTrades;
    private boolean tradeFound;

    private Vec3 villagerPos = Vec3.ZERO;

    // Map of Enchantments to their accepted Levels
    public static Map<Enchantment, ArrayList<Integer>> enchantments = new HashMap<>();

    private static final Map<Villager, BlockPos> doneVillagers = new HashMap<>();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        if (enchantments.isEmpty())
            return;
        if (lecternPos == null) {
            lecternPos = getLectern();
        }
        if (Wrapper.INSTANCE.getMinecraft().screen instanceof MerchantScreen merchantScreen) {
            MerchantMenu merchantScreenHandler = merchantScreen.getMenu();
            if (merchantScreenHandler.getTraderXp() > 0) {
                doneVillagers.put(villager, lecternPos);
                lecternPos = null;
                villager = null;
                ChatHelper.INSTANCE.addClientMessage("Villager has been traded with, ignoring");
            }
            MerchantOffers tradeOfferList = merchantScreenHandler.getOffers();
            if (!tradeOfferList.isEmpty()) {
                tradeOfferList.forEach(tradeOffer -> {
                    if (tradeOffer.getResult().getItem() instanceof EnchantedBookItem) {
                        Map<Enchantment, Integer> enchants = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(tradeOffer.getResult()));
                        for (Map.Entry<Enchantment, ArrayList<Integer>> entry : enchantments.entrySet()) {
                            Enchantment enchantment = entry.getKey();
                            String enchantName = Component.translatable(enchantment.getDescriptionId()).getString();
                            if (enchants.containsKey(enchantment) && entry.getValue().contains(enchants.get(enchantment))) {
                                int count = priceMode.equalsIgnoreCase("Normal") ? tradeOffer.getBaseCostA().getCount() : tradeOffer.getCostA().getCount();
                                if (count <= price) {
                                    ChatHelper.INSTANCE.addClientMessage(enchantName + " " + enchants.get(enchantment) + " found at price " + count + " emeralds");
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
                                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, emerald, ClickType.PICKUP, 0);
                                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, 0, ClickType.PICKUP, 0);

                                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, book, ClickType.PICKUP, 0);
                                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, 1, ClickType.PICKUP, 0);

                                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, 2, ClickType.PICKUP, 0);
                                                InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, book, ClickType.PICKUP, 0);
                                            } else {
                                                ChatHelper.INSTANCE.addClientMessage("No emeralds in inventory! Can not trade.");
                                            }
                                        } else {
                                            ChatHelper.INSTANCE.addClientMessage("No books in inventory! Can not trade.");
                                        }
                                    }
                                } else {
                                    ChatHelper.INSTANCE.addClientMessage(enchantName + " " + enchants.get(enchantment) + " found, but price is too high: " + count);
                                }
                            }
                        }

                    }
                });
                if (!tradeFound) {
                    NetworkHelper.INSTANCE.sendPacket(new ServerboundContainerClosePacket(merchantScreenHandler.containerId));
                    Wrapper.INSTANCE.getMinecraft().setScreen(null);
                    checkedTrades = true;
                }
            }
        }
        if (checkedTrades) {
            if (lecternPos == null || tradeFound) {
                checkedTrades = false;
            } else if (WorldHelper.INSTANCE.getBlock(lecternPos) == Blocks.LECTERN) {
                Wrapper.INSTANCE.getMultiPlayerGameMode().continueDestroyBlock(lecternPos, Direction.UP);
                Wrapper.INSTANCE.getLocalPlayer().swing(InteractionHand.MAIN_HAND);
            } else if (WorldHelper.INSTANCE.getBlock(lecternPos) == Blocks.AIR) {
                checkedTrades = false;
            }
        }
        if (villager == null || Wrapper.INSTANCE.getLocalPlayer().distanceTo(villager) > 6) {
            tradeFound = false;
            Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
                if (entity instanceof Villager villagerEntity) {
                    if (villagerEntity.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN && Wrapper.INSTANCE.getLocalPlayer().distanceTo(villagerEntity) < 6) {
                        if (villager == null || Wrapper.INSTANCE.getLocalPlayer().distanceTo(villagerEntity) < Wrapper.INSTANCE.getLocalPlayer().distanceTo(villager))
                            if (!doneVillagers.containsKey(villagerEntity))
                                villager = villagerEntity;
                    }
                }
            });
        } else if (villager.getVillagerData().getProfession() != lastProfession && lecternPos != null) {
            if (villager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN) {
                NetworkHelper.INSTANCE.sendPacket(ServerboundInteractPacket.createInteractionPacket(villager, false, InteractionHand.MAIN_HAND));
            } else if (villager.getVillagerData().getProfession() == VillagerProfession.NONE) {
                int lecternHotbarSlot = InventoryHelper.INSTANCE.getFromHotbar(Items.LECTERN);
                if (lecternHotbarSlot == -1) {
                    int lecternInvSlot = InventoryHelper.INSTANCE.getFromInv(Items.LECTERN);
                    if (lecternInvSlot == -1)
                        return;
                    InventoryHelper.INSTANCE.windowClick(Wrapper.INSTANCE.getLocalPlayer().containerMenu, lecternInvSlot < 9 ? lecternInvSlot + 36 : lecternInvSlot, ClickType.SWAP, 8);
                    return;
                } else {
                    InventoryHelper.INSTANCE.setSlot(lecternHotbarSlot, true, true);
                    NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerCommandPacket(Wrapper.INSTANCE.getLocalPlayer(), ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
                    PlayerHelper.INSTANCE.placeBlockInPos(lecternPos, InteractionHand.MAIN_HAND, false);
                    NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerCommandPacket(Wrapper.INSTANCE.getLocalPlayer(), ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
                }
            }
            lastProfession = villager.getVillagerData().getProfession();
        }
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        if (lecternPos != null) {
            Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(lecternPos);
            AABB box = new AABB(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
            Render3DHelper.INSTANCE.drawBox(event.getPoseStack(), box, 0xff00ff00);
        }
        if (villager != null) {
            villagerPos = Render2DHelper.INSTANCE.getHeadPos(villager, event.getPartialTicks(), event.getPoseStack());
        }
    });

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        if (villager != null && Render2DHelper.INSTANCE.isOnScreen(villagerPos)) {
            Nametag nametag = Feature.get(Nametag.class);
            float x = (float) villagerPos.x;
            float y = (float) villagerPos.y - (nametag.getState() && nametag.passives ? 15 : 0);
            String string1 = "Searching:";
            StringBuilder sb = new StringBuilder();
            for (Enchantment enchantment : enchantments.keySet()) {
                sb.append(Component.translatable(enchantment.getDescriptionId()).getString()).append(": ");
                for (int level : enchantments.get(enchantment)) {
                    sb.append(level).append(", ");
                }
                sb.setLength(sb.length() - 2);
                sb.append(" | ");
            }
            sb.setLength(sb.length() - 3);
            String string2 = sb.toString();

            String string3 = price + " Emeralds";
            float length1 = FontHelper.INSTANCE.getStringWidth(string1);
            float length2 = FontHelper.INSTANCE.getStringWidth(string2);
            float length3 = FontHelper.INSTANCE.getStringWidth(string3);

            Render2DHelper.INSTANCE.fill(event.getPoseStack(), x - (length1 / 2) - 2, y - 34, x + (length1 / 2) + 2, y - 23, 0x35000000);
            FontHelper.INSTANCE.drawCenteredString(event.getPoseStack(), string1, x, y - 32, -1);

            Render2DHelper.INSTANCE.fill(event.getPoseStack(), x - (length2 / 2) - 2, y - 23, x + (length2 / 2) + 2, y - 12, 0x35000000);
            FontHelper.INSTANCE.drawCenteredString(event.getPoseStack(), string2, x, y - 21, ColorHelper.INSTANCE.getClientColor());

            Render2DHelper.INSTANCE.fill(event.getPoseStack(), x - (length3 / 2) - 2, y - 12, x + (length3 / 2) + 2, y - 1, 0x35000000);
            FontHelper.INSTANCE.drawCenteredString(event.getPoseStack(), string3, x, y - 10, 0xff00ff00);
        }
    });

    @Override
    public void onEnable() {
        if ((enchantments.isEmpty()) && Wrapper.INSTANCE.getLocalPlayer() != null) {
            ChatHelper.INSTANCE.addClientMessage("Enchantment not set! Set enchantment with " + CommandManagerJex.INSTANCE.getPrefix() + "librarianroll add <enchant> <level>");
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
            if (InventoryHelper.INSTANCE.getInventory().getItem(i) != null && InventoryHelper.INSTANCE.getInventory().getItem(i).getItem() == item)
                return i;
        }
        return -1;
    }

    private BlockPos getLectern() {
        BlockPos lectern = null;
        for (int x = -4; x < 4; x++) {
            for (int y = -2; y < 2; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockPos pos = Wrapper.INSTANCE.getLocalPlayer().blockPosition().offset(x, y, z);
                    if (WorldHelper.INSTANCE.getBlock(pos) == Blocks.LECTERN) {
                        if (lectern == null || ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), ClientMathHelper.INSTANCE.getVec(lectern)) > ClientMathHelper.INSTANCE.getDistance(Wrapper.INSTANCE.getLocalPlayer().position(), ClientMathHelper.INSTANCE.getVec(pos)))
                            lectern = pos;
                    }
                }
            }
        }
        return lectern;
    }

}
