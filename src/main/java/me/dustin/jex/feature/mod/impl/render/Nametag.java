package me.dustin.jex.feature.mod.impl.render;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.event.render.EventRenderNametags;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.ESP;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.ChatFormatting;
import net.minecraft.client.CameraType;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.phys.Vec3;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Render names above players with more info.")
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
    private final HashMap<Entity, Vec3> positions = Maps.newHashMap();

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
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            if (isValid(entity) && !positions.containsKey(entity)) {
                float offset = entity.getBbHeight() + 0.2f;
                if (entity instanceof Player playerEntity) {
                    if (Hat.hasHat(playerEntity)) {
                        if (Hat.getType(playerEntity) == Hat.HatType.TOP_HAT || playerEntity.getItemBySlot(EquipmentSlot.HEAD).getItem() == Items.DRAGON_HEAD)
                            offset = entity.getBbHeight() + 0.7f;
                        else
                            offset = entity.getBbHeight() + 0.4f;
                    }
                }
                Vec3 vec = Render2DHelper.INSTANCE.getPos(entity, offset, event.getPartialTicks(), event.getPoseStack());
                if (entity instanceof ItemEntity itemEntity) {
                    Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity1 -> {
                        if (entity1 instanceof ItemEntity itemEntity1 && entity.distanceTo(entity1) <= groupRange && entity.getBlockY() == entity1.getBlockY()) {
                            if (itemEntity1.getItem().getItem() == itemEntity.getItem().getItem())
                                this.positions.put(entity1, vec);
                        }
                    });
                }
                this.positions.put(entity, vec);
            }
        });
    });

    public void drawNametags(PoseStack matrixStack) {
        ArrayList<Entity> exceptions = new ArrayList<>();
        //draw all backgrounds then render all at once
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            if (!exceptions.contains(entity) && isValid(entity)) {
                Vec3 vec = positions.get(entity);
                if (isOnScreen(vec)) {
                    float x = (float) vec.x;
                    float y = (float) vec.y - (showPlayerFace && entity instanceof Player ? 18 : 0);
                    String nameString = getNameString(entity);
                    if (entity instanceof ItemEntity itemEntity) {
                        AtomicInteger stackCount = new AtomicInteger(itemEntity.getItem().getCount());
                        positions.forEach((entity1, vec3d) -> {
                            if (!exceptions.contains(entity1) && entity != entity1 && vec3d.equals(vec) && entity1 instanceof ItemEntity itemEntity1) {
                                if (itemEntity1.getItem().getItem() == itemEntity.getItem().getItem()) {
                                    stackCount.addAndGet(itemEntity1.getItem().getCount());
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
        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        //draw all text since we can't also do that while rendering the boxes
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            if (!exceptions.contains(entity) && isValid(entity)) {
                Vec3 vec = positions.get(entity);
                if (isOnScreen(vec)) {
                    float x = (float) vec.x;
                    float y = (float) vec.y - (showPlayerFace && entity instanceof Player ? 18 : 0);
                    String nameString = getNameString(entity);

                    if (entity instanceof ItemEntity itemEntity) {
                        int stackCount = itemEntity.getItem().getCount();
                        for (Entity entity1 : exceptions) {
                            Vec3 vec3d = positions.get(entity1);
                            if (vec3d.equals(vec) && entity1 instanceof ItemEntity itemEntity1) {
                                if (itemEntity1.getItem().getItem() == itemEntity.getItem().getItem()) {
                                    stackCount += itemEntity1.getItem().getCount();
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

    public void drawInventoryItems(PoseStack matrixStack, Vec3 vec, LivingEntity livingEntity) {
        float x = (float) vec.x;
        float y = (float) vec.y - (showPlayerFace && livingEntity instanceof Player ? 18 : 0);
        int itemWidth = 16;
        int totalCount = getItems(livingEntity).size();
        float startX = (x - ((totalCount * itemWidth) / 2.f));
        y -= 28;
        count = 0;
        for (ItemStack itemStack : getItems(livingEntity)) {
            if (!(itemStack.getItem() instanceof AirItem)) {
                float newX = startX + (count * 16);
                Render2DHelper.INSTANCE.drawItem(itemStack, newX, y);
                if (itemStack.isEnchanted()) {
                    float scale = 0.5f;
                    matrixStack.pushPose();
                    matrixStack.scale(scale, scale, 1);
                    int enchCount = 1;
                    for (Tag tag : itemStack.getEnchantmentTags()) {
                        try {
                            CompoundTag compoundTag = (CompoundTag) tag;
                            float newY = ((y - ((10 * scale) * enchCount) + 0.5f) / scale);
                            float newerX = (newX / scale);
                            String name = getEnchantName(compoundTag);
                            if (compoundTag.getString("id").equalsIgnoreCase("minecraft:binding_curse") || compoundTag.getString("id").equalsIgnoreCase("minecraft:vanishing_curse"))
                                name = "\247c" + name;
                            FontHelper.INSTANCE.draw(matrixStack, name, newerX + 1.5f, newY, enchantColor, customFont);
                            enchCount++;
                        } catch (Exception ignored) {}
                    }
                    matrixStack.popPose();
                }
                count++;
            }
        }
    }

    public void drawInventoryBackgrounds(PoseStack matrixStack, Vec3 vec, LivingEntity livingEntity) {
        float x = (float) vec.x;
        float y = (float) vec.y - (showPlayerFace && livingEntity instanceof Player ? 18 : 0);
        int itemWidth = 16;
        int totalCount = getItems(livingEntity).size();
        float startX = (x - ((totalCount * itemWidth) / 2.f));
        y -= 28;
        count = 0;
        for (ItemStack itemStack : getItems(livingEntity)) {
            if (!(itemStack.getItem() instanceof AirItem)) {
                float newX = startX + (count * 16);
                Render2DHelper.INSTANCE.fillNoDraw(matrixStack, newX, y, newX + 16, y + 16, 0x35000000);
                if (itemStack.isEnchanted()) {
                    float scale = 0.5f;
                    matrixStack.pushPose();
                    matrixStack.scale(scale, scale, 1);
                    int enchCount = 1;
                    for (Tag tag : itemStack.getEnchantmentTags()) {
                        try {
                            CompoundTag compoundTag = (CompoundTag) tag;
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
                    matrixStack.popPose();
                }
                count++;
            }
        }
    }

    public void drawPlayerFaces(PoseStack matrixStack) {
        Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
            if (isValid(entity)) {
                Vec3 vec = positions.get(entity);
                if (isOnScreen(vec)) {
                    float x = (float) vec.x - 8;
                    float y = (float) vec.y - 16;

                    if (showPlayerFace && entity instanceof Player) {
                        PlayerInfo playerListEntry = Wrapper.INSTANCE.getLocalPlayer().connection.getPlayerInfo(entity.getUUID());
                        if (playerListEntry != null) {
                            Render2DHelper.INSTANCE.drawFace(matrixStack, x, y, 2, playerListEntry.getSkinLocation());
                            RenderSystem.setShaderTexture(0, 0);
                        }
                    }
                }
            }
        });
    }

    private String getEnchantName(CompoundTag compoundTag) {
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
        player.getArmorSlots().forEach(itemStack -> {
            if (itemStack != null) {
                if (!(itemStack.getItem() instanceof AirItem)) {
                    stackList.add(itemStack);
                }
            }
        });
        ItemStack itemStack = player.getItemBySlot(EquipmentSlot.MAINHAND);
        if (!(itemStack.getItem() instanceof AirItem)) {
            stackList.add(itemStack);
        }
        itemStack = player.getItemBySlot(EquipmentSlot.OFFHAND);
        if (!(itemStack.getItem() instanceof AirItem)) {
            stackList.add(itemStack);
        }
        return stackList;
    }

    public String getNameString(Entity entity) {
        String name = entity.getDisplayName().getString();
        if (name.trim().isEmpty())
            name = entity.getName().getString();
        if (entity instanceof Player && FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
            name = FriendHelper.INSTANCE.getFriendViaName(entity.getName().getString()).alias();
        if (entity instanceof ItemEntity itemEntity) {
            name = entity.getDisplayName().getString();
            if (itemEntity.getItem().getCount() > 1)
                name += " \247fx" + itemEntity.getItem().getCount();
        }
        if (ping && entity instanceof Player playerEntity && Wrapper.INSTANCE.getLocalPlayer().connection != null) {
            PlayerInfo entry = Wrapper.INSTANCE.getLocalPlayer().connection.getPlayerInfo(playerEntity.getUUID());
            if (entry != null) {
                int ping = entry.getLatency();
                name += String.format(" %s[%s%dms%s]%s", ChatFormatting.GRAY, getPingFormatting(ping), ping, ChatFormatting.GRAY, ChatFormatting.RESET);
            }
        }
        if (distance) {
            name += String.format(" %s[%s%.1f%s]%s", ChatFormatting.GRAY, ChatFormatting.WHITE, Wrapper.INSTANCE.getLocalPlayer().distanceTo(entity), ChatFormatting.GRAY, ChatFormatting.RESET);
        }
        if (entity instanceof LivingEntity)
            if (health && !healthMode.equalsIgnoreCase("Bar")) {
                name += " " + getHealthString((LivingEntity) entity);
            }
        return name;
    }

    public ChatFormatting getPingFormatting(int ping) {
        if (ping <= 50)
            return ChatFormatting.GREEN;
        else if (ping <= 75)
            return ChatFormatting.YELLOW;
        else if (ping <= 100)
            return ChatFormatting.RED;
        return ChatFormatting.DARK_RED;
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
        if (entity instanceof Player playerEntity) {
            if (FriendHelper.INSTANCE.isFriend(playerEntity))
                return ESP.INSTANCE.friendColor;
            else if (entity.isInvisible())
                return new Color(200, 70, 0).getRGB();
            else if (entity.isShiftKeyDown())
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
        } else if (entity instanceof Player)
            if (!EntityHelper.INSTANCE.isNPC((Player) entity)) {
                if (entity == Wrapper.INSTANCE.getLocalPlayer())
                    return showself && Wrapper.INSTANCE.getOptions().getCameraType() != CameraType.FIRST_PERSON;
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
        ItemStack mainHandStack = livingEntity.getMainHandItem();
        ItemStack offhandStack = livingEntity.getMainHandItem();

        if (mainHandStack.getItem() instanceof TridentItem || offhandStack.getItem() instanceof TridentItem)
            return true;
        if (!(mainHandStack.getItem() instanceof BowItem) && !(mainHandStack.getItem() instanceof FishingRodItem) && mainHandStack.getItem() != Items.CROSSBOW && mainHandStack.getItem() != Items.IRON_AXE && mainHandStack.getItem() != Items.IRON_SWORD && mainHandStack.getItem() != Items.IRON_SHOVEL && mainHandStack.getItem() != Items.STONE_SWORD && mainHandStack.getItem() != Items.GOLDEN_SWORD) {
            return mainHandStack.getItem() != Items.AIR || offhandStack.getItem() != Items.AIR;
        }
        boolean hasArmor = false;
        for (ItemStack armorItem : livingEntity.getArmorSlots()) {
            if (armorItem.getItem() != Items.AIR) {
                hasArmor = true;
                break;
            }
        }
        return hasArmor;
    }

    private boolean isOnScreen(Vec3 pos) {
        return pos != null && (pos.z > -1 && pos.z < 1);
    }

}
