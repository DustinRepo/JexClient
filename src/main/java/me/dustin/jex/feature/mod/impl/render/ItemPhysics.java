package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRotateItemEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Items will rotate around in the air and flop down")
public class ItemPhysics extends Feature {//fancier version that's not just flat items on the ground like the fabric mod

    @Op(name = "Roll Spin Speed", max = 50)
    public int rollSpeed = 0;
    @Op(name = "Pitch Spin Speed", max = 50)
    public int pitchSpeed = 25;
    @Op(name = "Yaw Spin Speed", max = 50)
    public int yawSpeed = 25;

    //TODO: find a better way to track this stuff
    private final HashMap<ItemEntity, Float> itemRotationsRoll = new HashMap<>();
    private final HashMap<ItemEntity, Float> prevItemRotationsRoll = new HashMap<>();

    private final HashMap<ItemEntity, Float> itemRotationsPitch = new HashMap<>();
    private final HashMap<ItemEntity, Float> prevItemRotationsPitch = new HashMap<>();

    private final HashMap<ItemEntity, Float> itemRotationsYaw = new HashMap<>();
    private final HashMap<ItemEntity, Float> prevItemRotationsYaw = new HashMap<>();

    private final HashMap<ItemEntity, Boolean> itemPitchNeg = new HashMap<>();
    private final HashMap<ItemEntity, Boolean> itemRollNeg = new HashMap<>();
    private final HashMap<ItemEntity, Boolean> itemYawNeg = new HashMap<>();

    @EventPointer
    private final EventListener<EventRotateItemEntity> eventRotateItemEntityEventListener = new EventListener<>(event -> {
        ItemEntity itemEntity = event.getItemEntity();
        PoseStack matrixStack = event.getPoseStack();
        float g = event.getG();
        float n = itemEntity.getSpin(g);

        if (!prevItemRotationsRoll.containsKey(itemEntity))
            return;
        matrixStack.translate(0, itemEntity.getBbHeight() / 1.5f, 0);

        BakedModel bakedModel = Wrapper.INSTANCE.getMinecraft().getItemRenderer().getModel(itemEntity.getItem(), itemEntity.level, null, itemEntity.getId());
        float roll = Mth.lerp(Wrapper.INSTANCE.getMinecraft().getFrameTime(), prevItemRotationsRoll.get(itemEntity), itemRotationsRoll.get(itemEntity));
        float pitch = Mth.lerp(Wrapper.INSTANCE.getMinecraft().getFrameTime(), prevItemRotationsPitch.get(itemEntity), itemRotationsPitch.get(itemEntity));
        float yaw = Mth.lerp(Wrapper.INSTANCE.getMinecraft().getFrameTime(), prevItemRotationsYaw.get(itemEntity), itemRotationsYaw.get(itemEntity));

        if (itemEntity.isOnGround())
            matrixStack.translate(0, bakedModel.isGui3d() ? -0.04 : -0.151f, 0);

        matrixStack.mulPose(new Quaternion(new Vector3f(itemPitchNeg.get(itemEntity) ? -1 : 1, 0, 0), pitch, true));
        matrixStack.mulPose(new Quaternion(new Vector3f(0, 0, itemRollNeg.get(itemEntity) ? -1 : 1), roll, true));
        matrixStack.mulPose(new Quaternion(new Vector3f(0, itemYawNeg.get(itemEntity) ? -1 : 1, 0), yaw, true));

        matrixStack.translate(0, -(itemEntity.getBbHeight() / 1.5f), 0);

        matrixStack.mulPose(Vector3f.YN.rotation(n));
        float l = Mth.sin(((float)itemEntity.getAge() + g) / 10.0F + itemEntity.bobOffs) * 0.1F + 0.1F;
        float m = bakedModel.getTransforms().getTransform(ItemTransforms.TransformType.GROUND).scale.y();
        matrixStack.translate(0.0D, -(l + 0.25F * m), 0.0D);
    });

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() != null)
            Wrapper.INSTANCE.getWorld().entitiesForRendering().forEach(entity -> {
                if (entity instanceof ItemEntity itemEntity) {
                    if (!itemRotationsRoll.containsKey(itemEntity)) {
                        Random r = new Random();
                        float roll = r.nextFloat() * 360;
                        float yaw = r.nextFloat() * 360;
                        float pitch = r.nextFloat() * 360;
                        itemRotationsRoll.put(itemEntity, roll);
                        prevItemRotationsRoll.put(itemEntity, roll);
                        itemRotationsPitch.put(itemEntity, yaw);
                        prevItemRotationsPitch.put(itemEntity, yaw);
                        itemRotationsYaw.put(itemEntity, pitch);
                        prevItemRotationsYaw.put(itemEntity, pitch);
                        itemPitchNeg.put(itemEntity, r.nextBoolean());
                        itemRollNeg.put(itemEntity, r.nextBoolean());
                        itemYawNeg.put(itemEntity, r.nextBoolean());
                    }
                    prevItemRotationsRoll.replace(itemEntity, itemRotationsRoll.get(itemEntity));
                    if (!itemEntity.isOnGround()) {
                        itemRotationsRoll.replace(itemEntity, itemRotationsRoll.get(itemEntity) + rollSpeed);
                    }
                    prevItemRotationsPitch.replace(itemEntity, itemRotationsPitch.get(itemEntity));
                    if (!itemEntity.isOnGround()) {
                        itemRotationsPitch.replace(itemEntity, itemRotationsPitch.get(itemEntity) + pitchSpeed);
                    } else
                        itemRotationsPitch.replace(itemEntity, 90.f);
                    prevItemRotationsYaw.replace(itemEntity, itemRotationsYaw.get(itemEntity));
                    if (!itemEntity.isOnGround()) {
                        itemRotationsYaw.replace(itemEntity, itemRotationsYaw.get(itemEntity) + yawSpeed);
                    } else
                        itemRotationsYaw.replace(itemEntity, 0.f);
                }
            });
        itemRotationsRoll.keySet().removeIf(Objects::isNull);
        prevItemRotationsRoll.keySet().removeIf(Objects::isNull);
        itemRotationsPitch.keySet().removeIf(Objects::isNull);
        prevItemRotationsPitch.keySet().removeIf(Objects::isNull);
        itemRotationsYaw.keySet().removeIf(Objects::isNull);
        prevItemRotationsYaw.keySet().removeIf(Objects::isNull);
        itemPitchNeg.keySet().removeIf(Objects::isNull);
        itemRollNeg.keySet().removeIf(Objects::isNull);
        itemYawNeg.keySet().removeIf(Objects::isNull);
    }, new TickFilter(EventTick.Mode.PRE));
}
