package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.render.EventRotateItemEntity;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.helper.misc.Wrapper;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.HashMap;
import java.util.Objects;
import java.util.Random;

public class ItemPhysics extends Feature {//fancier version that's not just flat items on the ground like the fabric mod

    public final Property<Integer> rollSpeedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Roll Spin Speed")
            .value(0)
            .max(50)
            .build();
    public final Property<Integer> pitchSpeedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Pitch Spin Speed")
            .value(25)
            .max(50)
            .build();
    public final Property<Integer> yawSpeedProperty = new Property.PropertyBuilder<Integer>(this.getClass())
            .name("Yaw Spin Speed")
            .value(25)
            .max(50)
            .build();

    private final HashMap<ItemEntity, Vec3d> itemRotations = new HashMap<>();
    private final HashMap<ItemEntity, Vec3d> prevItemRotations = new HashMap<>();
    private final HashMap<ItemEntity, Vec3d> negValues = new HashMap<>();

    public ItemPhysics() {
        super(Category.VISUAL, "Items will rotate around in the air and flop down");
    }

    @EventPointer
    private final EventListener<EventRotateItemEntity> eventRotateItemEntityEventListener = new EventListener<>(event -> {
        ItemEntity itemEntity = event.getItemEntity();
        MatrixStack matrixStack = event.getPoseStack();
        float g = event.getG();
        float n = itemEntity.getRotation(g);

        if (!prevItemRotations.containsKey(itemEntity))
            return;
        Vec3d prev = prevItemRotations.get(itemEntity);
        Vec3d current = itemRotations.get(itemEntity);
        matrixStack.translate(0, itemEntity.getHeight() / 1.5f, 0);
        BakedModel bakedModel = Wrapper.INSTANCE.getMinecraft().getItemRenderer().getModel(itemEntity.getStack(), itemEntity.world, null, itemEntity.getId());
        float roll = (float)MathHelper.lerp(Wrapper.INSTANCE.getMinecraft().getTickDelta(), prev.z, current.z);
        float pitch = (float)MathHelper.lerp(Wrapper.INSTANCE.getMinecraft().getTickDelta(), prev.x, current.x);
        float yaw = (float)MathHelper.lerp(Wrapper.INSTANCE.getMinecraft().getTickDelta(), prev.y, current.y);

        if (itemEntity.isOnGround())
            matrixStack.translate(0, bakedModel.hasDepth() ? -0.04 : -0.151f, 0);

        matrixStack.multiply(new Quaternion(new Vec3f(negValues.get(itemEntity).x == 1 ? -1 : 1, 0, 0), pitch, true));
        matrixStack.multiply(new Quaternion(new Vec3f(0, 0, negValues.get(itemEntity).z == 1 ? -1 : 1), roll, true));
        matrixStack.multiply(new Quaternion(new Vec3f(0, negValues.get(itemEntity).y == 1 ? -1 : 1, 0), yaw, true));

        matrixStack.translate(0, -(itemEntity.getHeight() / 1.5f), 0);

        matrixStack.multiply(Vec3f.NEGATIVE_Y.getRadialQuaternion(n));
        float l = MathHelper.sin(((float)itemEntity.getItemAge() + g) / 10.0F + itemEntity.uniqueOffset) * 0.1F + 0.1F;
        float m = bakedModel.getTransformation().getTransformation(ModelTransformation.Mode.GROUND).scale.getY();
        matrixStack.translate(0.0D, -(l + 0.25F * m), 0.0D);
    });

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Wrapper.INSTANCE.getWorld() != null)
            Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                if (entity instanceof ItemEntity itemEntity) {
                    if (!itemRotations.containsKey(itemEntity)) {
                        Random r = new Random();
                        float roll = r.nextFloat() * 360;
                        float yaw = r.nextFloat() * 360;
                        float pitch = r.nextFloat() * 360;
                        itemRotations.put(itemEntity, new Vec3d(pitch, yaw, roll));
                        prevItemRotations.put(itemEntity, new Vec3d(pitch, yaw, roll));
                        negValues.put(itemEntity, new Vec3d(booleanToBinary(r.nextBoolean()), booleanToBinary(r.nextBoolean()), booleanToBinary(r.nextBoolean())));
                    }
                    prevItemRotations.replace(itemEntity, itemRotations.get(itemEntity));
                    Vec3d vec = itemRotations.get(itemEntity);
                    if (!itemEntity.isOnGround()) {
                        vec = new Vec3d(vec.x + pitchSpeedProperty.value(), vec.y + yawSpeedProperty.value(), vec.z + rollSpeedProperty.value());
                    } else
                        vec = new Vec3d(90, 0, vec.z);
                    itemRotations.replace(itemEntity, vec);
                }
            });
        itemRotations.keySet().removeIf(Objects::isNull);
        prevItemRotations.keySet().removeIf(Objects::isNull);
        negValues.keySet().removeIf(Objects::isNull);
    }, new TickFilter(EventTick.Mode.PRE));

    private int booleanToBinary(boolean bl) {
        return bl ? 1 : 0;
    }
}
