package me.dustin.jex.helper.addon.pegleg;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventInitPlayerModel;
import me.dustin.jex.event.render.EventLivingEntityCallRender;
import me.dustin.jex.event.render.EventPlayerEntityGetBodyParts;
import me.dustin.jex.event.render.EventPlayerEntityTexturedModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;

public enum PeglegHelper {
    INSTANCE;
    private final HashMap<String, PeglegType> peglegs = new HashMap<>();
    private final Identifier STICK = new Identifier("jex", "legs/stick.png");
    private final Identifier LIGHTNING_ROD = new Identifier("jex", "legs/lightning_rod.png");
    private final Identifier ROBOT = new Identifier("jex", "legs/robot.png");
    private ModelPart cut_leg;
    private ModelPart cut_pants;
    private ModelPart stick_pegleg;
    private ModelPart lightning_rod_pegleg;
    private ModelPart robot_pegleg;
    private PlayerEntity currentRender;

    @EventPointer
    private final EventListener<EventLivingEntityCallRender> eventLivingEntityCallRenderEventListener = new EventListener<>(event -> {
        if (event.getLivingEntity() instanceof PlayerEntity playerEntity)
            currentRender = playerEntity;
    });

    @EventPointer
    private final EventListener<EventPlayerEntityGetBodyParts> eventPlayerEntityGetBodyPartsEventListener = new EventListener<>(event -> {
       if (hasPegleg(currentRender)) {
           event.getBodyParts().remove(event.getPlayerEntityModel().leftLeg);
           event.getBodyParts().remove(event.getPlayerEntityModel().leftPants);
           event.getBodyParts().add(cut_leg);
           event.getBodyParts().add(cut_pants);
       }
    });

    @EventPointer
    private final EventListener<EventPlayerEntityTexturedModelData> eventPlayerEntityTexturedModelDataEventListener = new EventListener<>(event -> {
        event.getModelData().getRoot().addChild("cut_leg",
                ModelPartBuilder.create()
                    .uv(0, 16).mirrored().cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, event.getDilation()),
                ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        event.getModelData().getRoot().addChild("cut_pants",
                ModelPartBuilder.create()
                    .uv(0, 48).cuboid(-2.0F, 0.0F, -2.0F, 4.0F, 4.0F, 4.0F, event.getDilation().add(0.25f)),
                ModelTransform.pivot(1.9f, 12.0f, 0.0f));
        event.getModelData().getRoot().addChild("stick_pegleg",
                ModelPartBuilder.create()
                    .uv(0, 0).cuboid(-0.5F, -8.0F, -0.5F, 1.0F, 8.0F, 1.0F, event.getDilation(), 4.F / 64.F, 9.F / 64.F),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F));
        event.getModelData().getRoot().addChild("lightning_rod_pegleg",
                ModelPartBuilder.create()
                    .uv(0, 0).cuboid(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, event.getDilation(), 8.F / 64.F, 11.F / 64.F)
                    .uv(0, 4).cuboid(-0.5F, -8.0F, -0.5F, 1.0F, 6.0F, 1.0F, event.getDilation(), 8.F / 64.F, 11.F / 64.F),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F));
        event.getModelData().getRoot().addChild("robot_pegleg",
                ModelPartBuilder.create()
                    .uv(0, 0).cuboid(-1.0F, -1.0F, -2.0F, 2.0F, 1.0F, 4.0F, event.getDilation(), 12.F / 64.F, 9.F / 64.F)
                    .uv(0, 5).cuboid(-0.5F, -4.0F, -0.5F, 1.0F, 3.0F, 1.0F, event.getDilation(), 12.F / 64.F, 9.F / 64.F)
                    .uv(4, 5).cuboid(-0.5F, -5.0F, -1.5F, 1.0F, 1.0F, 1.0F, event.getDilation(), 12.F / 64.F, 9.F / 64.F)
                    .uv(0, 0).cuboid(-0.5F, -8.0F, -0.5F, 1.0F, 3.0F, 1.0F, event.getDilation(), 12.F / 64.F, 9.F / 64.F),
                ModelTransform.pivot(1.9F, 12.0F, 0.0F));

    });

    @EventPointer
    private final EventListener<EventInitPlayerModel> eventInitPlayerModelEventListener = new EventListener<>(event -> {
        cut_leg = event.getRoot().getChild("cut_leg");
        cut_pants = event.getRoot().getChild("cut_pants");
        stick_pegleg = event.getRoot().getChild("stick_pegleg");
        lightning_rod_pegleg = event.getRoot().getChild("lightning_rod_pegleg");
        robot_pegleg = event.getRoot().getChild("robot_pegleg");
    });
    
    public void setPegleg(String uuid, String hat) {
        hat = hat.toLowerCase().replace(" ", "_");
        if (!hasPegleg(uuid)) {
            peglegs.put(uuid, getPeglegType(hat));
        } else {
            peglegs.replace(uuid, getPeglegType(hat));
        }
    }

    public void clearPegleg(String uuid) {
        peglegs.remove(uuid);
    }

    public PeglegType getPeglegType(String type) {
        for (PeglegType pegleg : PeglegType.values()) {
            if (pegleg.name().equalsIgnoreCase(type))
                return pegleg;
        }
        return PeglegType.STICK;
    }

    public PeglegType getType(PlayerEntity playerEntity) {
        String uuid = playerEntity.getUuidAsString().replace("-", "");
        if (!peglegs.containsKey(uuid))
            return null;
        return peglegs.get(uuid);
    }

    public boolean hasPegleg(PlayerEntity playerEntity) {
        return hasPegleg(playerEntity.getUuid().toString().replace("-", ""));
    }

    public boolean hasPegleg(String uuid) {
        return peglegs.containsKey(uuid);
    }

    public Identifier getPeglegTexture(String uuid) {
        PeglegType type = peglegs.get(uuid);
        switch (type) {
            case STICK -> {
                return STICK;
            }
            case LIGHTNING_ROD -> {
                return LIGHTNING_ROD;
            }
            case ROBOT -> {
                return ROBOT;
            }
        }
        return STICK;
    }

    public void renderPegleg(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, PeglegType peglegType) {
        switch (peglegType) {
            case STICK -> {
                stick_pegleg.render(matrices, vertices, light, overlay);
            }
            case LIGHTNING_ROD -> {
                lightning_rod_pegleg.render(matrices, vertices, light, overlay);
            }
            case ROBOT -> {
                robot_pegleg.render(matrices, vertices, light, overlay);
            }
        }
    }

    public ModelPart getCut_leg() {
        return cut_leg;
    }

    public ModelPart getCut_Pants() {
        return cut_pants;
    }

    public enum PeglegType {
        STICK, LIGHTNING_ROD, ROBOT
    }

    public void clear() {
        peglegs.clear();
    }
}
