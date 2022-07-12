package me.dustin.jex.helper.addon.hat;

import java.util.ArrayList;
import java.util.HashMap;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.render.EventInitPlayerModel;
import me.dustin.jex.event.render.EventPlayerEntityTexturedModelData;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public enum HatHelper {
    INSTANCE;
    private final HashMap<String, HatType> hats = new HashMap<>();
    private final Identifier TOP_HAT = new Identifier("jex", "hats/top_hat.png");
    private final Identifier HALO = new Identifier("jex", "hats/halo.png");
    private final Identifier MOHAWK = new Identifier("jex", "hats/mohawk.png");
    private final Identifier SANTA = new Identifier("jex", "hats/santa.png");
    private final Identifier TECHNO = new Identifier("jex", "hats/techno.png");

    private ModelPart top_hat;
    private ModelPart halo;
    private ModelPart mohawk;
    private ModelPart santa;
    private ModelPart techno;

    @EventPointer
    private final EventListener<EventPlayerEntityTexturedModelData> eventPlayerEntityTexturedModelDataEventListener = new EventListener<>(event -> {
        event.getModelData().getRoot().addChild("top_hat",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F, event.getDilation(), 38.f / 64.f, 22.f / 64.f)
                        .uv(0, 9).cuboid(-2.5F, -8.0F, -2.5F, 5.0F, 7.0F, 5.0F, event.getDilation(), 38.f / 64.f, 22.f / 64.f)
                        .uv(14, 15).cuboid(-3.0F, -2.0F, -3.0F, 6.0F, 1.0F, 6.0F, event.getDilation(), 38.f / 64.f, 22.f / 64.f),
                ModelTransform.NONE);
        event.getModelData().getRoot().addChild("halo",
                ModelPartBuilder.create()
                        .uv(0, 9).cuboid(-4.5F, -3.0F, -4.5F, 1.0F, 1.0F, 8.0F, event.getDilation(), 28.f / 64.f, 18.f / 64.f)
                        .uv(0, 0).cuboid(3.5F, -3.0F, -3.5F, 1.0F, 1.0F, 8.0F, event.getDilation(), 28.f / 64.f, 18.f / 64.f)
                        .uv(10, 2).cuboid(-4.5F, -3.0F, 3.5F, 8.0F, 1.0F, 1.0F, event.getDilation(), 28.f / 64.f, 18.f / 64.f)
                        .uv(10, 0).cuboid(-3.5F, -3.0F, -4.5F, 8.0F, 1.0F, 1.0F, event.getDilation(), 28.f / 64.f, 18.f / 64.f),
                ModelTransform.NONE);
        event.getModelData().getRoot().addChild("mohawk",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-1.0F, -2.0F, -4.0F, 2.0F, 2.0F, 8.0F, event.getDilation(), 20.f / 64.f, 13.f / 64.f)
                        .uv(6, 10).cuboid(-1.0F, -4.0F, 2.5F, 2.0F, 2.0F, 1.0F, event.getDilation(), 20.f / 64.f, 13.f / 64.f)
                        .uv(0, 10).cuboid(-1.0F, -4.0F, 0.5F, 2.0F, 2.0F, 1.0F, event.getDilation(), 20.f / 64.f, 13.f / 64.f)
                        .uv(0, 3).cuboid(-1.0F, -4.0F, -1.5F, 2.0F, 2.0F, 1.0F, event.getDilation(), 20.f / 64.f, 13.f / 64.f)
                        .uv(0, 0).cuboid(-1.0F, -4.0F, -3.5F, 2.0F, 2.0F, 1.0F, event.getDilation(), 20.f / 64.f, 13.f / 64.f),
                ModelTransform.NONE);
        event.getModelData().getRoot().addChild("santa",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F, event.getDilation(), 36.f / 64.f, 26.f / 64.f)
                        .uv(0, 9).cuboid(-3.0F, -3.0F, -3.0F, 6.0F, 2.0F, 6.0F, event.getDilation(), 36.f / 64.f, 26.f / 64.f)
                        .uv(18, 9).cuboid(-2.0F, -4.0F, -2.0F, 4.0F, 1.0F, 5.0F, event.getDilation(), 36.f / 64.f, 26.f / 64.f)
                        .uv(13, 17).cuboid(-1.5F, -6.0F, 0.5F, 3.0F, 1.0F, 4.0F, event.getDilation(), 36.f / 64.f, 26.f / 64.f)
                        .uv(0, 17).cuboid(-2.0F, -5.0F, -1.0F, 4.0F, 1.0F, 5.0F, event.getDilation(), 36.f / 64.f, 26.f / 64.f)
                        .uv(15, 22).cuboid(-1.0F, -7.0F, 2.5F, 2.0F, 1.0F, 3.0F, event.getDilation(), 36.f / 64.f, 26.f / 64.f)
                        .uv(0, 0).cuboid(-1.0F, -7.5F, 5.5F, 2.0F, 2.0F, 2.0F, event.getDilation(), 36.f / 64.f, 26.f / 64.f),
                ModelTransform.NONE);
        event.getModelData().getRoot().addChild("techno",
                ModelPartBuilder.create()
                        .uv(0, 0).cuboid(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(16, 11).cuboid(-4.0F, -2.0F, -4.0F, 8.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(16, 9).cuboid(-4.0F, -2.0F, 3.0F, 8.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(8, 10).cuboid(3.0F, -2.0F, -3.0F, 1.0F, 1.0F, 6.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(0, 9).cuboid(-4.0F, -2.0F, -3.0F, 1.0F, 1.0F, 6.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(0, 13).cuboid(3.0F, -3.0F, -4.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(8, 11).cuboid(-4.0F, -3.0F, -4.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(0, 11).cuboid(-2.0F, -3.0F, -4.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(8, 9).cuboid(1.0F, -3.0F, -4.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(0, 9).cuboid(-4.0F, -3.0F, -2.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(0, 6).cuboid(-4.0F, -3.0F, 3.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(3, 5).cuboid(-4.0F, -3.0F, 1.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(0, 4).cuboid(-2.0F, -3.0F, 3.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(3, 3).cuboid(1.0F, -3.0F, 3.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(3, 1).cuboid(3.0F, -3.0F, 3.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(0, 2).cuboid(3.0F, -3.0F, 1.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f)
                        .uv(0, 0).cuboid(3.0F, -3.0F, -2.0F, 1.0F, 1.0F, 1.0F, event.getDilation(), 34.f / 64.f, 17.f / 64.f),
                ModelTransform.NONE);
    });

    @EventPointer
    private final EventListener<EventInitPlayerModel> eventInitPlayerModelEventListener = new EventListener<>(event -> {
        top_hat = event.getRoot().getChild("top_hat");
        halo = event.getRoot().getChild("halo");
        mohawk = event.getRoot().getChild("mohawk");
        santa = event.getRoot().getChild("santa");
        techno = event.getRoot().getChild("techno");
    });
    
    public void setHat(String uuid, String hat) {
        hat = hat.toLowerCase().replace(" ", "_");
        if (!hasHat(uuid)) {
            hats.put(uuid, getHatType(hat));
        } else {
            hats.replace(uuid, getHatType(hat));
        }
    }

    public void clearHat(String uuid) {
        hats.remove(uuid);
    }

    public HatType getHatType(String type) {
        for (HatType hat : HatType.values()) {
            if (hat.name().equalsIgnoreCase(type))
                return hat;
        }
        return HatType.TOP_HAT;
    }

    public HatType getType(PlayerEntity playerEntity) {
        String uuid = playerEntity.getUuidAsString().replace("-", "");
        if (!hats.containsKey(uuid))
            return null;
        return hats.get(uuid);
    }

    public HatType getType(String uuid) {
        if (!hats.containsKey(uuid))
            return null;
        return hats.get(uuid);
    }

    public boolean hasHat(PlayerEntity playerEntity) {
        return hasHat(playerEntity.getUuid().toString().replace("-", ""));
    }

    public boolean hasHat(String uuid) {
        return hats.containsKey(uuid);
    }

    public Identifier getHatTexture(String uuid) {
        HatType type = hats.get(uuid);
        switch (type) {
            case HALO -> {
                return HALO;
            }
            case MOHAWK -> {
                return MOHAWK;
            }
            case SANTA -> {
                return SANTA;
            }
            case TECHNO -> {
                return TECHNO;
            }
            default -> {
                return TOP_HAT;
            }
        }
    }

    public void renderHat(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, HatHelper.HatType hatType) {
        switch (hatType) {
            case TOP_HAT -> top_hat.render(matrices, vertices, light, overlay);
            case HALO -> halo.render(matrices, vertices, light, overlay);
            case MOHAWK -> mohawk.render(matrices, vertices, light, overlay);
            case SANTA -> santa.render(matrices, vertices, light, overlay);
            case TECHNO -> techno.render(matrices, vertices, light, overlay);
        }
    }

    public enum HatType {
        TOP_HAT, HALO, MOHAWK, SANTA, TECHNO
    }

    public void clear() {
        hats.clear();
    }
}
