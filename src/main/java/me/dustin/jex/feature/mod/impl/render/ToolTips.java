package me.dustin.jex.feature.mod.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.filters.ToolTipFilter;
import me.dustin.jex.event.misc.EventGetToolTipFromItem;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRenderToolTip;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.misc.KeyboardHelper;
import me.dustin.jex.helper.misc.MouseHelper;
import me.dustin.jex.helper.misc.PrettyPrintTextFormatter;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringHelper;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;

public class ToolTips extends Feature {

    @Op(name = "Repair Cost")
    public boolean repairCost = true;
    @Op(name = "ShulkerToolTip")
    public boolean shulkerToolTip = true;
    @OpChild(name = "Inspect Key", isKeybind = true, parent = "ShulkerToolTip")
    public int inspectKey = GLFW.GLFW_KEY_LEFT_CONTROL;
    @Op(name = "MapToolTip")
    public boolean mapToolTip = true;
    @Op(name = "StewToolTip")
    public boolean stewToolTip = true;
    @Op(name = "HiveToolTip")
    public boolean hiveToolTip = true;
    @Op(name = "NBTToolTip")
    public boolean nbtToolTip = true;
    @OpChild(name = "Show NBT Key", isKeybind = true, parent = "NBTToolTip")
    public int nbtKey = GLFW.GLFW_KEY_LEFT_SHIFT;

    private final PrettyPrintTextFormatter formatter = new PrettyPrintTextFormatter();
    private final Identifier SHULKER_GUI = new Identifier("jex", "gui/mc/shulker_background.png");
    private float inspectX = -99999, inspectY = -99999;
    private ItemStack inspectStack;

    public ToolTips() {
        super(Category.VISUAL, "Show extra tooltip info including seeing inside of shulkers and viewing maps and other NBT data");
    }

    //ShulkerToolTip, also with the ability to hover over and inspect the items inside
    @EventPointer
    private final EventListener<EventRenderToolTip> shulkerToolTipListener = new EventListener<>(event -> {
        if (shulkerToolTip) {
            ItemStack stack = event.getItemStack();
            if (inspectStack != null) {
                if (KeyboardHelper.INSTANCE.isPressed(inspectKey)) {
                    stack = inspectStack;
                    event.setItemStack(stack);
                } else {
                    inspectStack = null;
                }
            }
            if (InventoryHelper.INSTANCE.isShulker(stack)) {
                BlockItem shulkerBoxItem = (BlockItem) stack.getItem();
                HashMap<Integer, ItemStack> stacks = InventoryHelper.INSTANCE.getStacksFromShulker(stack);
                float x = inspectX == -99999 ? MouseHelper.INSTANCE.getMouseX() + 8 : inspectX;
                if (x + 180 > Render2DHelper.INSTANCE.getScaledWidth())
                    x -= (Render2DHelper.INSTANCE.getScaledWidth() - x);
                float y = inspectY == -99999 ? MouseHelper.INSTANCE.getMouseY() - 85 : inspectY;
                if (y + 69 > Render2DHelper.INSTANCE.getScaledHeight())
                    y -= (Render2DHelper.INSTANCE.getScaledHeight() - y);

                if (KeyboardHelper.INSTANCE.isPressed(inspectKey)) {
                    if (inspectStack == null)
                        inspectStack = stack;
                    if (inspectX == -99999 || inspectY == -99999) {
                        inspectX = x;
                        inspectY = y;
                    }
                    event.setX((int) inspectX - 8);
                    event.setY((int) inspectY + 84);
                } else {
                    inspectX = -99999;
                    inspectY = -99999;
                    inspectStack = null;
                }

                MatrixStack matrixStack = event.getPoseStack();
                matrixStack.push();

                RenderSystem.disableDepthTest();
                Render2DHelper.INSTANCE.bindTexture(SHULKER_GUI);
                if (shulkerBoxItem != Items.SHULKER_BOX)
                    Render2DHelper.INSTANCE.shaderColor(shulkerBoxItem.getBlock().getDefaultMapColor().color);
                matrixStack.translate(0.0F, 0.0F, 32);
                DrawableHelper.drawTexture(event.getPoseStack(), (int) x, (int) y, 0, 0, 180, 69, 180, 69);
                Render2DHelper.INSTANCE.shaderColor(0xffffffff);

                int xCount = 0;
                int yCount = 0;

                float hoverX = 0, hoverY = 0;
                for (int i = 0; i < 27; i++) {
                    float xPos = x + 8 + (18.5f * xCount);
                    float yPos = y + 8 + (18.5f * yCount);
                    if (stacks.containsKey(i)) {
                        ItemStack itemStack = stacks.get(i);
                        Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 199;
                        Render2DHelper.INSTANCE.drawItem(itemStack, (int) xPos, (int) yPos);
                        Wrapper.INSTANCE.getMinecraft().getItemRenderer().zOffset = 0;

                        if (KeyboardHelper.INSTANCE.isPressed(inspectKey) && Render2DHelper.INSTANCE.isHovered(xPos - 1, yPos - 1, 20, 20)) {
                            event.setOther(new EventRenderToolTip.ToolTipData(itemStack, MouseHelper.INSTANCE.getMouseX(), MouseHelper.INSTANCE.getMouseY()));
                            hoverX = xPos;
                            hoverY = yPos;
                        }
                    }
                    xCount++;
                    if (xCount > 8) {
                        xCount = 0;
                        yCount++;
                    }
                }

                if (hoverX != 0 || hoverY != 0) {
                    Render2DHelper.INSTANCE.fill(matrixStack, hoverX - 1, hoverY - 0.5f, hoverX + 16, hoverY + 16, 0x60ffffff);
                }
                matrixStack.translate(0.0F, 0.0F, -32);

                RenderSystem.enableDepthTest();
                matrixStack.pop();
            }
        }
    }, new ToolTipFilter(EventRenderToolTip.Mode.PRE));

    //MapToolTip
    @EventPointer
    private final EventListener<EventRenderToolTip> mapToolTipListener = new EventListener<>(event -> {
        if (mapToolTip)
            Render2DHelper.INSTANCE.drawMap(event.getPoseStack(), event.getX() + 8, event.getY() - 165, event.getItemStack());
    }, new ToolTipFilter(EventRenderToolTip.Mode.POST, Items.FILLED_MAP));

    @EventPointer
    private final EventListener<EventGetToolTipFromItem> eventGetToolTipFromItemEventListener = new EventListener<>(event -> {
        ItemStack stack = event.getItemStack();
        if (InventoryHelper.INSTANCE.isShulker(stack) && shulkerToolTip) {
            event.getTextList().add(Text.of("Hold " + KeyboardHelper.INSTANCE.getKeyName(inspectKey) + " to inspect"));
            return;
        }
        if (repairCost) {
            NbtCompound nbtCompound = stack.getNbt();
            if (nbtCompound != null) {
                NbtElement repairCost = nbtCompound.get("RepairCost");
                if (repairCost != null) {
                    event.getTextList().add(Text.of(Formatting.GREEN  + "Repair Cost" + Formatting.WHITE + ": " + Formatting.GRAY + repairCost.asString()));
                }
            }
        }
        if (hiveToolTip && stack.getItem() == Items.BEEHIVE || stack.getItem() == Items.BEE_NEST){
            NbtCompound nbtCompound = stack.getNbt();
            if (nbtCompound != null) {
                NbtCompound blockEntityTag = nbtCompound.getCompound("BlockEntityTag");
                NbtList beesList = blockEntityTag.getList("Bees", 10);
                if (beesList != null) {
                    event.getTextList().add(Text.of("Bees: " + Render2DHelper.INSTANCE.getPercentFormatting(((beesList.size() / 3.f) * 100)) + beesList.size()));
                    event.getTextList().add(Text.of("---------------"));
                    for (int i = 0; i < beesList.size(); i++) {
                        NbtCompound beeData = beesList.getCompound(i).getCompound("EntityData");
                        int health = beeData.getInt("Health");
                        String customName = beeData.getString("CustomName");
                        if (customName == null || customName.isEmpty())
                            customName = "Bee";
                        event.getTextList().add(Text.of(Formatting.AQUA + customName + " " + Render2DHelper.INSTANCE.getPercentFormatting((health / 10.f) * 100) + health + Formatting.WHITE + "/" + Formatting.GREEN + "10"));
                    }
                    event.getTextList().add(Text.of("---------------"));
                } else {
                    event.getTextList().add(Text.of("Bees: " + Formatting.DARK_RED + "0"));
                }
                NbtCompound blockStateTag = nbtCompound.getCompound("BlockStateTag");
                if (blockStateTag != null) {
                    String honeyLevel = blockStateTag.getString("honey_level");
                    int honeyLevelInt = 0;
                    try {
                        honeyLevelInt = Integer.parseInt(honeyLevel);
                    } catch (Exception e) {
                    }
                    event.getTextList().add(Text.of("Honey Level: " + Render2DHelper.INSTANCE.getPercentFormatting((honeyLevelInt / 5.f) * 100) + honeyLevel));
                } else {
                    event.getTextList().add(Text.of("Honey Level: " + Formatting.DARK_RED + "0"));
                }
            } else {
                event.getTextList().add(Text.of("Bees: " + Formatting.DARK_RED + "0"));
                event.getTextList().add(Text.of("Honey Level: " + Formatting.DARK_RED + "0"));
            }
        }
        if (stewToolTip && stack.getItem() == Items.SUSPICIOUS_STEW) {
            NbtCompound nbtCompound = stack.getNbt();
            if (nbtCompound != null) {
                NbtList nbtList = (NbtList) nbtCompound.get("Effects");
                if (nbtList != null)
                for (NbtElement effectElement : nbtList) {
                    if (effectElement instanceof NbtCompound effectCompound) {
                        int id = effectCompound.getInt("EffectId");
                        int durationTicks = effectCompound.getInt("EffectDuration");
                        event.getTextList().add(Text.of(Formatting.AQUA + StatusEffect.byRawId(id).getName().getString() + " " + Formatting.GRAY + StringHelper.formatTicks(durationTicks)));
                    }
                }
            }
        }
        if (nbtToolTip && !InventoryHelper.INSTANCE.isShulker(stack) && stack.getNbt() != null) {
            if (KeyboardHelper.INSTANCE.isPressed(nbtKey)) {
                PrettyPrintTextFormatter.RGBColorText formatted = formatter.apply(stack.getNbt());
                event.getTextList().add(Text.of(Formatting.GRAY + "-------------------"));
                event.getTextList().add(Text.of("NBT:"));
                event.getTextList().addAll(formatted.entriesAsText());
            } else {
                event.getTextList().add(Text.of("Hold " + KeyboardHelper.INSTANCE.getKeyName(nbtKey) + " to see NBT"));
            }
        }
    });

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (!KeyboardHelper.INSTANCE.isPressed(inspectKey)) {
            inspectX = -99999;
            inspectY = -99999;
            inspectStack = null;
        }
    }, new TickFilter(EventTick.Mode.PRE));
}
