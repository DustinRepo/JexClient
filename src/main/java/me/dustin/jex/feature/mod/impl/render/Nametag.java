package me.dustin.jex.feature.mod.impl.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.render.EventRenderNametags;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BowItem;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.TridentItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Nametag extends Feature {

    @Op(name = "Custom Font")
    public boolean customFont = false;
    @Op(name = "Players")
    public boolean players = true;
    @OpChild(name = "Show Face", parent = "Players")
    public boolean showPlayerFace = true;
    @Op(name = "Mobs")
    public boolean mobs = false;
    @OpChild(name = "Special Mobs Only", parent = "Mobs")
    public boolean specialMobsOnly = false;
    @OpChild(name = "Hostiles", parent = "Mobs")
    public boolean hostiles = true;
    @OpChild(name = "Neutrals", parent = "Mobs")
    public boolean neutrals = true;
    @OpChild(name = "Passives", parent = "Mobs")
    public boolean passives = true;
    @Op(name = "Items")
    public boolean items = false;
    @OpChild(name = "Group Range", max = 10, parent = "Items")
    public int groupRange = 5;
    @Op(name = "Ping")
    public boolean ping = true;
    @Op(name = "Distance")
    public boolean distance = true;
    @Op(name = "Show Inv")
    public boolean showInv = true;
    @OpChild(name = "Backgrounds", parent = "Show Inv")
    public boolean itemBackgrounds = true;
    @OpChild(name = "Enchant Text Color", isColor = true, parent = "Show Inv")
    public int enchantColor = new Color(0, 181, 255).getRGB();
    @Op(name = "Health")
    public boolean health = true;
    @Op(name = "Show on Self")
    public boolean showself = true;

    @OpChild(name = "Health Mode", all = {"Bar", "Hearts", "HP", "Percent"}, parent = "Health")
    public String healthMode = "Bar";
    int count = 0;
    private final HashMap<Entity, Vec3d> positions = Maps.newHashMap();

    public Nametag() {
        super(Category.VISUAL, "Render names above players with more info.");
    }

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        drawNametags(event.getPoseStack());
        drawPlayerFaces(event.getPoseStack());
    });

    @EventPointer
    private final EventListener<EventRenderNametags> eventRenderNametagsEventListener = new EventListener<>(event -> {
        if (isValid(event.getEntity())) {
            event.cancel();
        }
    });

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        this.positions.clear();
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (isValid(entity) && !positions.containsKey(entity)) {
                float offset = entity.getHeight() + 0.2f;
                if (entity instanceof PlayerEntity playerEntity) {
                    if (Hat.hasHat(playerEntity)) {
                        if (Hat.getType(playerEntity) == Hat.HatType.TOP_HAT || playerEntity.getEquippedStack(EquipmentSlot.HEAD).getItem() == Items.DRAGON_HEAD)
                            offset = entity.getHeight() + 0.7f;
                        else
                            offset = entity.getHeight() + 0.4f;
                    }
                }
                Vec3d vec = Render2DHelper.INSTANCE.getPos(entity, offset, event.getPartialTicks(), event.getPoseStack());
                if (entity instanceof ItemEntity itemEntity) {
                    Wrapper.INSTANCE.getWorld().getEntities().forEach(entity1 -> {
                        if (entity1 instanceof ItemEntity itemEntity1 && entity.distanceTo(entity1) <= groupRange && entity.getBlockY() == entity1.getBlockY()) {
                            if (itemEntity1.getStack().getItem() == itemEntity.getStack().getItem())
                                this.positions.put(entity1, vec);
                        }
                    });
                }
                this.positions.put(entity, vec);
            }
        });
    });

    public void drawNametags(MatrixStack matrixStack) {
        ArrayList<Entity> exceptions = new ArrayList<>();
        //draw all backgrounds then render all at once
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tessellator.getInstance().getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (!exceptions.contains(entity) && isValid(entity)) {
                Vec3d vec = positions.get(entity);
                if (isOnScreen(vec)) {
                    float x = (float) vec.x;
                    float y = (float) vec.y - (showPlayerFace && entity instanceof PlayerEntity ? 18 : 0);
                    String nameString = getNameString(entity);
                    if (entity instanceof ItemEntity itemEntity) {
                        AtomicInteger stackCount = new AtomicInteger(itemEntity.getStack().getCount());
                        positions.forEach((entity1, vec3d) -> {
                            if (!exceptions.contains(entity1) && entity != entity1 && vec3d.equals(vec) && entity1 instanceof ItemEntity itemEntity1) {
                                if (itemEntity1.getStack().getItem() == itemEntity.getStack().getItem()) {
                                    stackCount.addAndGet(itemEntity1.getStack().getCount());
                                    exceptions.add(entity1);
                                }
                            }
                        });

                        nameString = entity.getDisplayName().getString();
                        if (stackCount.get() > 1)
                            nameString += " \247fx" + stackCount.get();
                    }
                    float length = FontHelper.INSTANCE.getStringWidth(nameString, customFont || CustomFont.INSTANCE.getState());
                    //health bar
                    if (health && healthMode.equalsIgnoreCase("Bar") && entity instanceof LivingEntity) {
                        float percent = ((LivingEntity) entity).getHealth() / ((LivingEntity) entity).getMaxHealth();
                        float barLength = (int) ((length + 4) * percent);
                        Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x - (length / 2) - 2, y - 1, (x - (length / 2) - 2) + barLength, y, getHealthColor(((LivingEntity) entity)));
                    }
                    //name background
                    Render2DHelper.INSTANCE.fillNoDraw(matrixStack, x - (length / 2) - 2, y - 12, x + (length / 2) + 2, y - 1, 0x35000000);
                    if (showInv && itemBackgrounds && entity instanceof LivingEntity livingEntity) {
                        drawInventoryBackgrounds(matrixStack, vec, livingEntity);
                    }
                }
            }
        });
        bufferBuilder.clear();
        BufferRenderer.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        //draw all text since we can't also do that while rendering the boxes
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (!exceptions.contains(entity) && isValid(entity)) {
                Vec3d vec = positions.get(entity);
                if (isOnScreen(vec)) {
                    float x = (float) vec.x;
                    float y = (float) vec.y - (showPlayerFace && entity instanceof PlayerEntity ? 18 : 0);
                    String nameString = getNameString(entity);

                    if (entity instanceof ItemEntity itemEntity) {
                        int stackCount = itemEntity.getStack().getCount();
                        for (Entity entity1 : exceptions) {
                            Vec3d vec3d = positions.get(entity1);
                            if (vec3d.equals(vec) && entity1 instanceof ItemEntity itemEntity1) {
                                if (itemEntity1.getStack().getItem() == itemEntity.getStack().getItem()) {
                                    stackCount += itemEntity1.getStack().getCount();
                                }
                            }
                        }
                        nameString = entity.getDisplayName().getString();
                        if (stackCount > 1)
                            nameString += " \247fx" + stackCount;
                    }
                    
                    FontHelper.INSTANCE.drawCenteredString(matrixStack, nameString, x, y - 10, getColor(entity), customFont);

                    if (showInv && entity instanceof LivingEntity livingEntity) {
                        drawInventoryItems(matrixStack, vec, livingEntity);
                    }
                }
            }
        });
    }

    public void drawInventoryItems(MatrixStack matrixStack, Vec3d vec, LivingEntity livingEntity) {
        float x = (float) vec.x;
        float y = (float) vec.y - (showPlayerFace && livingEntity instanceof PlayerEntity ? 18 : 0);
        int itemWidth = 16;
        int totalCount = getItems(livingEntity).size();
        float startX = (x - ((totalCount * itemWidth) / 2.f));
        y -= 28;
        count = 0;
        for (ItemStack itemStack : getItems(livingEntity)) {
            if (!(itemStack.getItem() instanceof AirBlockItem)) {
                float newX = startX + (count * 16);
                Render2DHelper.INSTANCE.drawItem(itemStack, newX, y);
                if (itemStack.hasEnchantments()) {
                    float scale = 0.5f;
                    matrixStack.push();
                    matrixStack.scale(scale, scale, 1);
                    int enchCount = 1;
                    for (NbtElement tag : itemStack.getEnchantments()) {
                        try {
                            NbtCompound compoundTag = (NbtCompound) tag;
                            float newY = ((y - ((10 * scale) * enchCount) + 0.5f) / scale);
                            float newerX = (newX / scale);
                            String name = getEnchantName(compoundTag);
                            if (compoundTag.getString("id").equalsIgnoreCase("minecraft:binding_curse") || compoundTag.getString("id").equalsIgnoreCase("minecraft:vanishing_curse"))
                                name = "\247c" + name;
                            FontHelper.INSTANCE.draw(matrixStack, name, newerX + 1.5f, newY, enchantColor, customFont);
                            enchCount++;
                        } catch (Exception ignored) {}
                    }
                    matrixStack.pop();
                }
                count++;
            }
        }
    }

    public void drawInventoryBackgrounds(MatrixStack matrixStack, Vec3d vec, LivingEntity livingEntity) {
        float x = (float) vec.x;
        float y = (float) vec.y - (showPlayerFace && livingEntity instanceof PlayerEntity ? 18 : 0);
        int itemWidth = 16;
        int totalCount = getItems(livingEntity).size();
        float startX = (x - ((totalCount * itemWidth) / 2.f));
        y -= 28;
        count = 0;
        for (ItemStack itemStack : getItems(livingEntity)) {
            if (!(itemStack.getItem() instanceof AirBlockItem)) {
                float newX = startX + (count * 16);
                Render2DHelper.INSTANCE.fillNoDraw(matrixStack, newX, y, newX + 16, y + 16, 0x35000000);
                if (itemStack.hasEnchantments()) {
                    float scale = 0.5f;
                    matrixStack.push();
                    matrixStack.scale(scale, scale, 1);
                    int enchCount = 1;
                    for (NbtElement tag : itemStack.getEnchantments()) {
                        try {
                            NbtCompound compoundTag = (NbtCompound) tag;
                            float newY = ((y - ((10 * scale) * enchCount) + 0.5f) / scale);
                            float newerX = (newX / scale);
                            String name = getEnchantName(compoundTag);
                            if (compoundTag.getString("id").equalsIgnoreCase("minecraft:binding_curse") || compoundTag.getString("id").equalsIgnoreCase("minecraft:vanishing_curse"))
                                name = "\247c" + name;
                            float nameWidth = FontHelper.INSTANCE.getStringWidth(name, customFont || CustomFont.INSTANCE.getState());
                            Render2DHelper.INSTANCE.fillNoDraw(matrixStack, newerX, newY - 1, newerX + nameWidth, newY + 9, 0x35000000);
                            enchCount++;
                        } catch (Exception ignored) {}
                    }
                    matrixStack.pop();
                }
                count++;
            }
        }
    }

    public void drawPlayerFaces(MatrixStack matrixStack) {
        Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
            if (isValid(entity)) {
                Vec3d vec = positions.get(entity);
                if (isOnScreen(vec)) {
                    float x = (float) vec.x - 8;
                    float y = (float) vec.y - 16;

                    if (showPlayerFace && entity instanceof PlayerEntity) {
                        PlayerListEntry playerListEntry = Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerListEntry(entity.getUuid());
                        if (playerListEntry != null) {
                            Render2DHelper.INSTANCE.drawFace(matrixStack, x, y, 2, playerListEntry.getSkinTexture());
                            RenderSystem.setShaderTexture(0, 0);
                        }
                    }
                }
            }
        });
    }

    private String getEnchantName(NbtCompound compoundTag) {
        int level = compoundTag.getShort("lvl");
        String name = compoundTag.getString("id").split(":")[1];
        if (name.contains("_")) {
            String[] s = name.split("_");
            name = s[0].substring(0, 1).toUpperCase() + s[0].substring(1, 3) + s[1].substring(0, 1).toUpperCase();
        } else {
            name = name.substring(0, 1).toUpperCase() + name.substring(1, 3);
        }
        name += level;
        return name;
    }

    private ArrayList<ItemStack> getItems(LivingEntity player) {
        ArrayList<ItemStack> stackList = new ArrayList<>();
        player.getArmorItems().forEach(itemStack -> {
            if (itemStack != null) {
                if (!(itemStack.getItem() instanceof AirBlockItem)) {
                    stackList.add(itemStack);
                }
            }
        });
        ItemStack itemStack = player.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!(itemStack.getItem() instanceof AirBlockItem)) {
            stackList.add(itemStack);
        }
        itemStack = player.getEquippedStack(EquipmentSlot.OFFHAND);
        if (!(itemStack.getItem() instanceof AirBlockItem)) {
            stackList.add(itemStack);
        }
        return stackList;
    }

    public String getNameString(Entity entity) {
        String name = entity.getDisplayName().getString();
        if (name.trim().isEmpty())
            name = entity.getName().getString();
        if (entity instanceof PlayerEntity && FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
            name = FriendHelper.INSTANCE.getFriendViaName(entity.getName().getString()).alias();
        if (entity instanceof ItemEntity itemEntity) {
            name = entity.getDisplayName().getString();
            if (itemEntity.getStack().getCount() > 1)
                name += " \247fx" + itemEntity.getStack().getCount();
        }
        if (ping && entity instanceof PlayerEntity playerEntity && Wrapper.INSTANCE.getLocalPlayer().networkHandler != null) {
            PlayerListEntry entry = Wrapper.INSTANCE.getLocalPlayer().networkHandler.getPlayerListEntry(playerEntity.getUuid());
            if (entry != null) {
                int ping = entry.getLatency();
                name += String.format(" %s[%s%dms%s]%s", Formatting.GRAY, getPingFormatting(ping), ping, Formatting.GRAY, Formatting.RESET);
            }
        }
        if (distance) {
            name += String.format(" %s[%s%.1f%s]%s", Formatting.GRAY, Formatting.WHITE, Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity), Formatting.GRAY, Formatting.RESET);
        }
        if (entity instanceof LivingEntity)
            if (health && !healthMode.equalsIgnoreCase("Bar")) {
                name += " " + getHealthString((LivingEntity) entity);
            }
        return name;
    }

    public Formatting getPingFormatting(int ping) {
        if (ping <= 50)
            return Formatting.GREEN;
        else if (ping <= 75)
            return Formatting.YELLOW;
        else if (ping <= 100)
            return Formatting.RED;
        return Formatting.DARK_RED;
    }

    private int getColor(Entity entity) {
        if ((entity instanceof ItemEntity))
            return ESP.INSTANCE.itemColor;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return ESP.INSTANCE.hostileColor;
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return ESP.INSTANCE.passiveColor;
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return ESP.INSTANCE.neutralColor;
        if (entity instanceof PlayerEntity playerEntity) {
            if (FriendHelper.INSTANCE.isFriend(playerEntity))
                return ESP.INSTANCE.friendColor;
            else if (entity.isInvisible())
                return new Color(200, 70, 0).getRGB();
            else if (entity.isSneaking())
                return Color.red.getRGB();
        }
        return -1;
    }

    private String getHealthString(LivingEntity player) {
        String health = "§2";
        if (player.getHealth() < 15)
            health = "§e";
        if (player.getHealth() < 10)
            health = "§6";
        if (player.getHealth() < 5)
            health = "§4";
        if (!Float.isNaN(getHealth(player)) && !Float.isInfinite(getHealth(player)))
            health += ClientMathHelper.INSTANCE.round(getHealth(player), 1);
        else
            health += "NaN";
        return health;
    }

    private float getHealth(LivingEntity player) {
        return switch (healthMode) {
            case "Hearts" -> player.getHealth() / 2;
            case "HP" -> Math.round(player.getHealth());
            case "Percent" -> player.getHealth() * 5;
            default -> player.getHealth();
        };
    }

    private int getHealthColor(LivingEntity player) {
        float percent = (player.getHealth() / player.getMaxHealth()) * 100;
        return Render2DHelper.INSTANCE.getPercentColor(percent);
    }

    private boolean isValid(Entity entity) {
        if (entity instanceof ItemEntity)
            return items;
        else if (entity instanceof LivingEntity livingEntity && livingEntity.isSleeping())
            return false;
        else if (mobs && EntityHelper.INSTANCE.isHostileMob(entity)) {
            if (specialMobsOnly && !isSpecialMob(entity))
                return false;
            return hostiles;
        } else if (mobs && EntityHelper.INSTANCE.isPassiveMob(entity)) {
            if (specialMobsOnly && !isSpecialMob(entity))
                return false;
            return passives;
        } else if (mobs && EntityHelper.INSTANCE.isNeutralMob(entity)) {
            if (specialMobsOnly && !isSpecialMob(entity))
                return false;
            return neutrals;
        } else if (entity instanceof PlayerEntity)
            if (!EntityHelper.INSTANCE.isNPC((PlayerEntity) entity)) {
                if (entity == Wrapper.INSTANCE.getLocalPlayer())
                    return showself && Wrapper.INSTANCE.getOptions().getPerspective() != Perspective.FIRST_PERSON;
                return players;
            }
        return false;
    }

    private boolean isSpecialMob(Entity entity) {
        if (entity instanceof LivingEntity livingEntity) {
            if (entity.hasCustomName())
                return true;
            return hasAbnormalItems(livingEntity);
        }
        return false;
    }

    public boolean hasAbnormalItems(LivingEntity livingEntity) {
        ItemStack mainHandStack = livingEntity.getMainHandStack();
        ItemStack offhandStack = livingEntity.getMainHandStack();

        if (mainHandStack.getItem() instanceof TridentItem || offhandStack.getItem() instanceof TridentItem)
            return true;
        if (!(mainHandStack.getItem() instanceof BowItem) && !(mainHandStack.getItem() instanceof FishingRodItem) && mainHandStack.getItem() != Items.CROSSBOW && mainHandStack.getItem() != Items.IRON_AXE && mainHandStack.getItem() != Items.IRON_SWORD && mainHandStack.getItem() != Items.IRON_SHOVEL && mainHandStack.getItem() != Items.STONE_SWORD && mainHandStack.getItem() != Items.GOLDEN_SWORD) {
            return mainHandStack.getItem() != Items.AIR || offhandStack.getItem() != Items.AIR;
        }
        boolean hasArmor = false;
        for (ItemStack armorItem : livingEntity.getArmorItems()) {
            if (armorItem.getItem() != Items.AIR) {
                hasArmor = true;
                break;
            }
        }
        return hasArmor;
    }

    private boolean isOnScreen(Vec3d pos) {
        return pos != null && (pos.z > -1 && pos.z < 1);
    }

}
