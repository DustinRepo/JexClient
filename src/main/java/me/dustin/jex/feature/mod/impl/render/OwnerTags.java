package me.dustin.jex.feature.mod.impl.render;

import com.google.common.collect.Maps;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventRender2D;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.entity.EntityHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.MCAPIHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.font.FontHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.feature.mod.core.Feature;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class OwnerTags extends Feature {

    public final Property<Boolean> drawFacesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Draw Faces")
            .value(true)
            .build();

    private final HashMap<LivingEntity, Vec3d> positions = Maps.newHashMap();

    public OwnerTags() {
        super(Category.VISUAL);
    }

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
        positions.clear();
        for (Entity entity : Wrapper.INSTANCE.getWorld().getEntities()) {
            if (entity instanceof LivingEntity tameableEntity) {
                if (EntityHelper.INSTANCE.getOwnerUUID(tameableEntity) != null) {
                    positions.put(tameableEntity, Render2DHelper.INSTANCE.getHeadPos(entity, event.getPartialTicks(), event.getPoseStack()));
                }
            }
        }
    });

    @EventPointer
    private final EventListener<EventRender2D> eventRender2DEventListener = new EventListener<>(event -> {
        Nametag nametagModule = Feature.get(Nametag.class);
        positions.keySet().forEach(livingEntity -> {
            Vec3d pos = positions.get(livingEntity);
            if (isOnScreen(pos)) {
                float x = (float) pos.x;
                float y = (float) pos.y;
                if (nametagModule.getState() && nametagModule.passivesProperty.value()) {
                    y -= 12;
                }
                UUID uuid = EntityHelper.INSTANCE.getOwnerUUID(livingEntity);
                String nameString = PlayerHelper.INSTANCE.getName(uuid);
                if (nameString == null)
                    nameString = Objects.requireNonNull(uuid).toString();
                nameString = "\247o" + nameString.trim();
                float length = FontHelper.INSTANCE.getStringWidth(nameString);
                Render2DHelper.INSTANCE.fill(event.getPoseStack(), x - (length / 2) - 2, y - 12, x + (length / 2) + 2, y - 1, 0x35000000);
                FontHelper.INSTANCE.drawCenteredString(event.getPoseStack(), nameString, x, y - 10, -1);

                if (drawFacesProperty.value()) {
                    Render2DHelper.INSTANCE.drawFace(((EventRender2D) event).getPoseStack(), x - 8, y - 30, 2, MCAPIHelper.INSTANCE.getPlayerSkin(uuid));
                }
            }
        });
    });

    public boolean isOnScreen(Vec3d pos) {
        return pos != null && (pos.z > -1 && pos.z < 1);
    }
}
