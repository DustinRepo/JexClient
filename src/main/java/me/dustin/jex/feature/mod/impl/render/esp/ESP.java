package me.dustin.jex.feature.mod.impl.render.esp;

import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.misc.EventJoinWorld;
import me.dustin.jex.event.render.*;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.impl.render.esp.impl.OutlineBox;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.esp.impl.BoxESP;
import me.dustin.jex.feature.mod.impl.render.esp.impl.ShaderESP;
import me.dustin.jex.feature.mod.impl.render.esp.impl.TwoDeeESP;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Mark entities/players through walls")
public class ESP extends Feature {
    public static ESP INSTANCE;
    @Op(name = "Mode", all = {"Shader", "2D", "Box Outline", "Box"})
    public String mode = "Shader";

    @OpChild(name = "Line Width", min = 1, max = 10, inc = 0.1f, parent = "Mode", dependency = "Box Outline")
    public float lineWidth = 2;

    @Op(name = "Player")
    public boolean player = true;
    @Op(name = "Neutral")
    public boolean neutral = true;
    @Op(name = "Hostile")
    public boolean hostile = true;
    @Op(name = "Passive")
    public boolean passive = true;

    @Op(name = "Item")
    public boolean item = false;

    @OpChild(name = "Player Color", isColor = true, parent = "Player")
    public int playerColor = 0xffff0000;
    @OpChild(name = "Color on Distance", parent = "Player Color")
    public boolean colorOnDistance;
    @OpChild(name = "Friend Color", isColor = true, parent = "Player Color")
    public int friendColor = 0xff0080ff;
    @OpChild(name = "Hostile Color", isColor = true, parent = "Hostile")
    public int hostileColor = 0xffff8000;
    @OpChild(name = "Neutral Color", isColor = true, parent = "Neutral")
    public int neutralColor = 0xffff00ff;
    @OpChild(name = "Passive Color", isColor = true, parent = "Passive")
    public int passiveColor = 0xff00ff00;
    @OpChild(name = "Pets Color", isColor = true, parent = "Passive Color")
    public int petColor = 0xff0000ff;
    @OpChild(name = "Item Color", isColor = true, parent = "Item")
    public int itemColor = 0xffffffff;
    String lastMode;

    public ESP() {
        new ShaderESP();
        new BoxESP();
        new OutlineBox();
        new TwoDeeESP();
        INSTANCE = this;
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventRender2DNoScale> eventRender2DNoScaleEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventOutlineColor> eventOutlineColorEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventJoinWorld> eventJoinWorldEventListener = new EventListener<>(event -> sendEvent(event));
    @EventPointer
    private final EventListener<EventHasOutline> eventHasOutlineEventListener = new EventListener<>(event -> sendEvent(event));

    private void sendEvent(Event event) {
        if (lastMode != null && !mode.equalsIgnoreCase(lastMode)) {
            FeatureExtension.get(lastMode, this).disable();
            FeatureExtension.get(mode, this).enable();
        }
        FeatureExtension.get(mode, this).pass(event);
        this.setSuffix(mode);
        lastMode = mode;
    }

    @Override
    public void onEnable() {
        FeatureExtension.get(mode, this).enable();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        FeatureExtension.get(mode, this).disable();
        super.onDisable();
    }

    public boolean isValid(Entity entity) {
        if (entity == null)
            return false;
        if (entity instanceof ItemEntity)
            return item;
        if (!(entity instanceof LivingEntity livingEntity))
            return false;
        if (livingEntity == Wrapper.INSTANCE.getLocalPlayer())
            return false;
        if (livingEntity instanceof PlayerEntity && EntityHelper.INSTANCE.isNPC((PlayerEntity) livingEntity))
            return false;
        if (livingEntity instanceof PlayerEntity)
            return player;
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutral;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostile;
        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            return passive;
        return false;
    }

    public int getColor(Entity entity) {
        if (entity instanceof ItemEntity)
            return itemColor;
        if (FriendHelper.INSTANCE.isFriend(entity.getName().getString()))
            return friendColor;
        if (entity instanceof PlayerEntity) {
            if (colorOnDistance) {
                return getColor(entity.distanceTo(Wrapper.INSTANCE.getLocalPlayer()) / 64).getRGB();
            }
            return playerColor;
        }

        if (EntityHelper.INSTANCE.isPassiveMob(entity))
            if (EntityHelper.INSTANCE.doesPlayerOwn(entity))
                return petColor;
            else
                return passiveColor;
        if (EntityHelper.INSTANCE.isHostileMob(entity))
            return hostileColor;
        if (EntityHelper.INSTANCE.isNeutralMob(entity))
            return neutralColor;
        return -1;
    }

    public Color getColor(double power) {
        if (power > 1)
            power = 1;
        double H = power * 0.35; // Hue (note 0.35 = Green, see huge chart below)
        double S = 0.9; // Saturation
        double B = 0.9; // Brightness

        return Color.getHSBColor((float) H, (float) S, (float) B);
    }



}
