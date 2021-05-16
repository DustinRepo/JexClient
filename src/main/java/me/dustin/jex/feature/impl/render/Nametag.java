package me.dustin.jex.feature.impl.render;

import com.google.common.collect.Maps;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.addon.hat.Hat;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRenderGetPos;
import me.dustin.jex.event.render.EventRenderNametags;
import me.dustin.jex.friend.Friend;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.math.ClientMathHelper;
import me.dustin.jex.helper.math.ColorHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.FontHelper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.core.annotate.Feat;
import me.dustin.jex.feature.core.enums.FeatureCategory;
import me.dustin.jex.feature.impl.render.esp.ESP;
import me.dustin.jex.option.annotate.Op;
import me.dustin.jex.option.annotate.OpChild;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

@Feat(name = "Nametags", category = FeatureCategory.VISUAL, description = "Render names above players with more info.")
public class Nametag extends Feature {

    @Op(name = "Players")
    public boolean players = true;
    @OpChild(name = "Show Face", parent = "Players")
    public boolean showPlayerFace = true;
    @Op(name = "Hostiles")
    public boolean hostiles = false;
    @Op(name = "Passives")
    public boolean passives = false;
    @Op(name = "Items")
    public boolean items = false;
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
    private HashMap<Entity, Vec3d> positions = Maps.newHashMap();

    @EventListener(events = {EventRender2D.class, EventRenderNametags.class, EventRenderGetPos.class})
    private void runMethod(Event event) {
        if (event instanceof EventRenderNametags) {
            EventRenderNametags eventRenderNametags = (EventRenderNametags) event;
            if (isValid(eventRenderNametags.getEntity())) {
                eventRenderNametags.cancel();
            }
        } else if (event instanceof EventRender2D) {
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (isValid(entity)) {
                    drawNametags(entity, (EventRender2D) event);
                }
            });
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof LivingEntity && isValid(entity)) {
                    drawNametagInv((LivingEntity) entity, (EventRender2D) event);
                }
            });
            if (showself && Wrapper.INSTANCE.getOptions().getPerspective() != Perspective.FIRST_PERSON) {
                drawNametags(Wrapper.INSTANCE.getLocalPlayer(), (EventRender2D) event);
                drawNametagInv(Wrapper.INSTANCE.getLocalPlayer(), (EventRender2D) event);
            }
        } else if (event instanceof EventRenderGetPos) {
            this.positions.clear();
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (isValid(entity)) {
                    float offset = entity.getHeight() + 0.2f;
                    if (entity instanceof PlayerEntity) {
                        if (Hat.hasHat((PlayerEntity) entity)) {
                            if (Hat.getType((PlayerEntity) entity) == Hat.HatType.TOP_HAT)
                                offset = entity.getHeight() + 0.7f;
                            else
                                offset = entity.getHeight() + 0.4f;
                        }
                    }
                    Vec3d vec = Render2DHelper.INSTANCE.getPos(entity, offset, ((EventRenderGetPos) event).getPartialTicks());
                    this.positions.put(entity, vec);
                }
            });
        }
    }

    private void drawInv(LivingEntity player, float posX, float posY, EventRender2D eventRender2D) {
        int itemWidth = 16;
        int totalCount = getItems(player).size();
        float startX = (posX - ((totalCount * itemWidth) / 2));
        posY = (posY - 28);
        count = 0;
        for (ItemStack itemStack : getItems(player)) {
            if (!(itemStack.getItem() instanceof AirBlockItem)) {
                float newX = startX + (count * 16);
                if (itemBackgrounds)
                    Render2DHelper.INSTANCE.fill(eventRender2D.getMatrixStack(), newX, posY, newX + 16, posY + 16, 0x35000000);
                Render2DHelper.INSTANCE.drawItem(itemStack, newX, posY);
                if (itemStack.hasEnchantments()) {
                    float scale = 0.5f;
                    MatrixStack matrixStack = eventRender2D.getMatrixStack();
                    matrixStack.push();
                    matrixStack.scale(scale, scale, 1);
                    int enchCount = 1;
                    for (NbtElement tag : itemStack.getEnchantments()) {
                        try {
                            NbtCompound compoundTag = (NbtCompound) tag;
                            float newY = ((posY - ((10 * scale) * enchCount) + 0.5f) / scale);
                            float newerX = (newX / scale);
                            String name = getEnchantName(compoundTag);
                            float nameWidth = FontHelper.INSTANCE.getStringWidth(name);
                            Render2DHelper.INSTANCE.fill(eventRender2D.getMatrixStack(), newerX, newY - 1, newerX + nameWidth, newY + 9, 0x35000000);
                            FontHelper.INSTANCE.draw(eventRender2D.getMatrixStack(), name, newerX, newY, enchantColor);
                            enchCount++;
                        } catch (Exception e) {}
                    }
                    matrixStack.pop();
                }
                count++;
            }
        }
    }

    private String getEnchantName(NbtCompound compoundTag) {
        int level = compoundTag.getShort("lvl");
        String name = compoundTag.getString("id").split(":")[1];
        if (name.contains("_")) {
            String s[] = name.split("_");
            name = s[0].substring(0, 1).toUpperCase() + s[0].substring(1, 3) + s[1].substring(0, 1).toUpperCase();
        } else {
            name = name.substring(0, 1).toUpperCase() + name.substring(1, 3);
        }
        name += level;
        return name;
    }

    public void drawNametags(Entity playerEntity, EventRender2D eventRender2D) {
        Vec3d vec = positions.get(playerEntity);
        if (isOnScreen(vec)) {
            float x = (float) vec.x;
            float y = (float) vec.y - (showPlayerFace && playerEntity instanceof PlayerEntity ? 18 : 0);
            String nameString = getNameString(playerEntity);
            float length = FontHelper.INSTANCE.getStringWidth(nameString);

            if (showPlayerFace && playerEntity instanceof PlayerEntity) {
                PlayerListEntry playerListEntry = Wrapper.INSTANCE.getMinecraft().getNetworkHandler().getPlayerListEntry(playerEntity.getUuid());
                if (playerListEntry != null)
                    Render2DHelper.INSTANCE.drawFace(eventRender2D.getMatrixStack(), x - 8, y + 2, 2, playerListEntry.getSkinTexture());
            }

            if (health && healthMode.equalsIgnoreCase("Bar") && playerEntity instanceof LivingEntity) {
                float percent = ((LivingEntity) playerEntity).getHealth() / ((LivingEntity) playerEntity).getMaxHealth();
                float barLength = (int) ((length + 4) * percent);
                Render2DHelper.INSTANCE.fill(eventRender2D.getMatrixStack(), x - (length / 2) - 2, y - 1, (x - (length / 2) - 2) + barLength, y, getHealthColor(((LivingEntity) playerEntity)));
            }
            Render2DHelper.INSTANCE.fill(eventRender2D.getMatrixStack(), x - (length / 2) - 2, y - 12, x + (length / 2) + 2, y - 1, 0x35000000);
            FontHelper.INSTANCE.drawCenteredString(eventRender2D.getMatrixStack(), nameString, x, y - 10, getColor(playerEntity));
        }
    }

    public void drawNametagInv(LivingEntity playerEntity, EventRender2D eventRender2D) {
        Vec3d vec = positions.get(playerEntity);
        if (isOnScreen(vec)) {
            float x = (float) vec.x;
            float y = (float) vec.y - (showPlayerFace && playerEntity instanceof PlayerEntity ? 18 : 0);
            if (showInv)
                drawInv(playerEntity, x, y, eventRender2D);
        }
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
        String name = entity.getDisplayName().asString();
        if (name.trim().isEmpty())
            name = entity.getName().getString();
        if (entity instanceof ItemEntity) {
            ItemEntity itemEntity = (ItemEntity) entity;
            if (itemEntity.getStack().getCount() > 1)
                name += " \247fx" + itemEntity.getStack().getCount();
        }
        if (entity instanceof LivingEntity)
            if (health && !healthMode.equalsIgnoreCase("Bar")) {
                name += " " + getHealthString((LivingEntity) entity);
            }
        return name;
    }

    private int getColor(Entity player) {
        if ((player instanceof ItemEntity))
            return ESP.INSTANCE.itemColor;
        if (EntityHelper.INSTANCE.isHostileMob(player))
            return ESP.INSTANCE.hostileColor;
        if (EntityHelper.INSTANCE.isPassiveMob(player))
            return ESP.INSTANCE.passiveColor;
        if (player instanceof PlayerEntity) {
            if (Friend.isFriend(player.getName().asString()))
                return ColorHelper.INSTANCE.getClientColor();
            else if (player.isInvisible())
                return new Color(200, 70, 0).getRGB();
            else if (player.isSneaking())
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
        switch (healthMode) {
            case "Hearts":
                return player.getHealth() / 2;
            case "HP":
                return java.lang.Math.round(player.getHealth());
            case "Percent":
                return player.getHealth() * 5;
        }
        return player.getHealth();
    }

    private int getHealthColor(LivingEntity player) {
        float percent = (player.getHealth() / player.getMaxHealth()) * 100;
        return Render2DHelper.INSTANCE.getPercentColor(percent);
    }

    private boolean isValid(Entity entity) {
        if (entity instanceof ItemEntity) {
            return items;
        } else if (entity instanceof LivingEntity && ((LivingEntity) entity).isSleeping()) {
            return false;
        } else if (EntityHelper.INSTANCE.isHostileMob(entity)) {
            return hostiles;
        } else if (EntityHelper.INSTANCE.isPassiveMob(entity)) {
            return passives;
        } else if (entity instanceof PlayerEntity) {
            if (entity != Wrapper.INSTANCE.getLocalPlayer() && !EntityHelper.INSTANCE.isNPC((PlayerEntity) entity))
                return players;
        }
        return false;
    }

    public boolean isOnScreen(Vec3d pos) {
        return pos != null && (pos.z > -1 && pos.z < 1);
    }

}
