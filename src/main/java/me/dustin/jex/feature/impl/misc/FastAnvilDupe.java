package me.dustin.jex.feature.impl.misc;

import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.event.render.EventDrawScreen;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.Timer;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.option.annotate.Op;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;
import net.minecraft.client.render.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.RenameItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

@Feat(name = "FastAnvilDupe", category = FeatureCategory.MISC, description = "Speeds up the current anvil dupe")
public class FastAnvilDupe extends Feature {

    @Op(name = "Mark Damaged")
    public boolean markedDamage = true;
    @Op(name = "Auto Open Next Anvil")
    public boolean openNext = true;
    @Op(name = "Auto Insert Item")
    public boolean autoInsert = true;
    @Op(name = "Auto Level")
    public boolean autoLevel = true;

    private boolean pickedUp;
    private Item dupingItem;

    private ArrayList<Box> renderBoxes = new ArrayList<>();
    private Timer timer = new Timer();

    @EventListener(events = {EventPlayerPackets.class, EventRender3D.class, EventKeyPressed.class, EventDrawScreen.class, EventDisplayScreen.class})
    private void runMethod(Event event) {
        if (event instanceof EventPlayerPackets eventPlayerPackets) {
            if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
                if (Wrapper.INSTANCE.getLocalPlayer().age % 2 != 0)
                    return;
                if (Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AnvilScreen anvilScreen) {
                    if (!InventoryHelper.INSTANCE.isInventoryFull()) {
                        return;
                    }
                    AnvilScreenHandler anvilScreenHandler = anvilScreen.getScreenHandler();
                    if (pickedUp) {
                        InventoryHelper.INSTANCE.windowClick(anvilScreenHandler, 0, SlotActionType.PICKUP);
                        pickedUp = false;
                    } else if (anvilScreenHandler.getSlot(2).getStack().getItem() != Items.AIR) {
                        InventoryHelper.INSTANCE.windowClick(anvilScreenHandler, 2, SlotActionType.PICKUP);
                        pickedUp = true;
                    } else if (anvilScreenHandler.getSlot(0).getStack().getItem() != Items.AIR) {
                        dupingItem = anvilScreenHandler.getSlot(0).getStack().getItem();
                        if (Wrapper.INSTANCE.getLocalPlayer().experienceLevel < 1) {
                            if (autoLevel) {
                                int xpBottleSlot = InventoryHelper.INSTANCE.getFromHotbar(Items.EXPERIENCE_BOTTLE);
                                if (xpBottleSlot == -1) {
                                    int fromInv = InventoryHelper.INSTANCE.getFromInv(Items.EXPERIENCE_BOTTLE);
                                    if (fromInv != -1) {
                                        InventoryHelper.INSTANCE.windowClick(anvilScreenHandler, fromInv - 6, SlotActionType.SWAP, 8);
                                    }
                                } else {
                                    PlayerHelper.INSTANCE.setPitch(90);
                                    NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(xpBottleSlot));
                                    Wrapper.INSTANCE.getInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND);
                                    NetworkHelper.INSTANCE.sendPacket(new UpdateSelectedSlotC2SPacket(InventoryHelper.INSTANCE.getInventory().selectedSlot));
                                }
                            }
                            pickedUp = false;
                            return;
                        }
                        String currentName = anvilScreenHandler.getSlot(0).getStack().getName().getString();
                        NetworkHelper.INSTANCE.sendPacket(new RenameItemC2SPacket(currentName.equalsIgnoreCase("dupe") ? "dupe-1" : "dupe"));
                        anvilScreenHandler.updateResult();
                    } else if (dupingItem != null && !pickedUp && autoInsert) {
                        int slot = -1;
                        for (int i = 0; i < 44; i++) {
                            ItemStack itemStack = InventoryHelper.INSTANCE.getInventory().getStack(i);
                            if (itemStack.getItem() == dupingItem) {
                                slot = i;
                                break;
                            }
                        }
                        if (slot != -1) {
                            InventoryHelper.INSTANCE.windowClick(anvilScreenHandler, slot < 9 ? slot + 30 : slot - 6, SlotActionType.QUICK_MOVE, 1);
                            dupingItem = null;
                        }
                    }
                }
            }
        } else if (event instanceof EventRender3D eventRender3D && markedDamage) {
            ArrayList<BlockPos> ignore = new ArrayList<>();
            int color = ColorHelper.INSTANCE.getRainbowColor();
            Render3DHelper.INSTANCE.setup3DRender(true);
            //I know this looks fucky but it's done for performance
            BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            renderBoxes.forEach(box -> {
                Vec3d vec3d = Render3DHelper.INSTANCE.getRenderPosition(box.minX, box.minY, box.minZ);
                box = new Box(vec3d.getX(), vec3d.getY(), vec3d.getZ(), vec3d.getX() + 1, vec3d.getY() + (box.maxY - box.minY), vec3d.getZ() + 1);
                Render3DHelper.INSTANCE.doFadeBoxNoDraw(eventRender3D.getMatrixStack(), box, color & 0xa9ffffff);
            });
            bufferBuilder.end();
            BufferRenderer.draw(bufferBuilder);
            Render3DHelper.INSTANCE.end3DRender();
            if (!timer.hasPassed(250))
                return;
            timer.reset();
            renderBoxes.clear();
            for (int x = -4; x < 4; x++) {
                for (int y = -4; y < 4; y++) {
                    for (int z = -4; z < 4; z++) {
                        BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                        if (ignore.contains(blockPos))
                            continue;
                        Box box;
                        if (WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.DAMAGED_ANVIL) {
                            box = new Box(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + 1, blockPos.getY() + 1.2f, blockPos.getZ() + 1);
                            while (WorldHelper.INSTANCE.getBlock(blockPos.up()) == Blocks.DAMAGED_ANVIL) {
                                box = box.withMaxY(box.maxY + 1);
                                blockPos = blockPos.up();
                                ignore.add(blockPos);
                            }
                            renderBoxes.add(box);
                        }
                    }
                }
            }
        } else if (event instanceof EventKeyPressed eventKeyPressed && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AnvilScreen anvilScreen) {
            if (eventKeyPressed.getKey() == GLFW.GLFW_KEY_BACKSPACE && anvilScreen.getScreenHandler().getSlot(2).hasStack() && anvilScreen.getScreenHandler().getSlot(2).getStack().getName().getString().length() <= 1) {
                NetworkHelper.INSTANCE.sendPacket(new RenameItemC2SPacket(""));
            }
        } else if (event instanceof EventDrawScreen eventDrawScreen) {
            if (eventDrawScreen.getMode() == EventDrawScreen.Mode.POST && eventDrawScreen.getScreen() instanceof AnvilScreen) {
                float midX = Render2DHelper.INSTANCE.getScaledWidth() / 2.f;
                float midY = Render2DHelper.INSTANCE.getScaledHeight() / 2.f;
                FontHelper.INSTANCE.drawCenteredString(eventDrawScreen.getMatrixStack(), "Tip: Open slots in your inventory and remove the name on the item to get original name.", midX, 2, ColorHelper.INSTANCE.getClientColor());
                FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), "Auto Level: " + (autoLevel ? "\247aON \247rBottles: \247f" + InventoryHelper.INSTANCE.countItems(Items.EXPERIENCE_BOTTLE) : "\2474OFF"), midX + 90, midY - 80, ColorHelper.INSTANCE.getClientColor());
                FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), "Enough XP?", midX + 90, midY - 70, Wrapper.INSTANCE.getLocalPlayer().experienceLevel < 1 ? 0xffff0000 : 0xff00ff00);
                FontHelper.INSTANCE.drawWithShadow(eventDrawScreen.getMatrixStack(), "Filled Inventory?", midX + 90, midY - 60, !InventoryHelper.INSTANCE.isInventoryFull() ? 0xffff0000 : 0xff00ff00);
            }
        } else if (event instanceof EventDisplayScreen eventDisplayScreen) {
            if (eventDisplayScreen.getScreen() == null && Wrapper.INSTANCE.getMinecraft().currentScreen instanceof AnvilScreen && !(KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_ESCAPE) || KeyboardHelper.INSTANCE.isPressed(GLFW.GLFW_KEY_E))) {
                pickedUp = false;
                if (!openNext)
                    return;
                for (int x = -4; x < 4; x++) {
                    for (int y = 4; y > -4; y--) {
                        for (int z = -4; z < 4; z++) {
                            BlockPos blockPos = Wrapper.INSTANCE.getLocalPlayer().getBlockPos().add(x, y, z);
                            if (WorldHelper.INSTANCE.getBlock(blockPos) == Blocks.DAMAGED_ANVIL) {
                                BlockHitResult blockHitResult = new BlockHitResult(ClientMathHelper.INSTANCE.getVec(blockPos), Direction.UP, blockPos, false);
                                Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, blockHitResult);
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDisable() {
        dupingItem = null;
        pickedUp = false;
        super.onDisable();
    }
}
