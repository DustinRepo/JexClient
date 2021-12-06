package me.dustin.jex.feature.mod.impl.render.storageesp.impl;

import java.util.ArrayList;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.extension.FeatureExtension;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.storageesp.StorageESP;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.Render3DHelper.BoxStorage;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class BoxStorageESP extends FeatureExtension {
    private StorageESP storageESP;
    public BoxStorageESP() {
        super("Box", StorageESP.class);
    }

    @Override
    public void pass(Event event) {
        if (storageESP == null) {
            storageESP = (StorageESP) Feature.get(StorageESP.class);
        }
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D)event;
            ArrayList<BoxStorage> list = new ArrayList<>();
            WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                if (storageESP.isValid(blockEntity)) {
                	Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(blockEntity.getPos());
                    Box box = new Box(renderPos.x, renderPos.y, renderPos.z, renderPos.x + 1, renderPos.y + 1, renderPos.z + 1);
                    list.add(new BoxStorage(box, storageESP.getColor(blockEntity)));
                }
            });
            Render3DHelper.INSTANCE.drawList(eventRender3D.getMatrixStack(), list, true);
        }
    }
}
