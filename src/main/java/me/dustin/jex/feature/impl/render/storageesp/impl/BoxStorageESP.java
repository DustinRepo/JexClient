package me.dustin.jex.feature.impl.render.storageesp.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.FeatureExtension;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.core.Feature;
import me.dustin.jex.feature.impl.render.storageesp.StorageESP;
import net.minecraft.util.math.Box;

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
            WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                if (storageESP.isValid(blockEntity)) {
                    double x = blockEntity.getPos().getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().getX();
                    double y = blockEntity.getPos().getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().getY();
                    double z = blockEntity.getPos().getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().getZ();
                    Box box = new Box(x, y, z, x + 1, y + 1, z + 1);
                    /*if (blockEntity instanceof ChestBlockEntity) {
                        ChestType chestType = (ChestType)Wrapper.INSTANCE.getWorld().getBlockState(blockEntity.getPos()).get(ChestBlock.CHEST_TYPE);
                        if (chestType == ChestType.LEFT) {
                            switch (ChestBlock.getFacing(Wrapper.INSTANCE.getWorld().getBlockState(blockEntity.getPos()))) {
                                case SOUTH:
                                    removePos.add(blockEntity.getPos().east());
                                    box = new Box(x, y, z, x + 2, y + 1, z + 1);
                                    break;
                                case WEST:
                                    removePos.add(blockEntity.getPos().west());
                                    box = new Box(x - 1, y, z, x + 1, y + 1, z + 1);
                                    break;
                                case NORTH:
                                    removePos.add(blockEntity.getPos().north());
                                    box = new Box(x, y, z - 1, x + 1, y + 1, z + 1);
                                    break;
                                case EAST:
                                    removePos.add(blockEntity.getPos().south());
                                    box = new Box(x, y, z, x + 1, y + 1, z + 2);
                                    break;
                            }
                        } else if (chestType == ChestType.RIGHT) {
                            switch (ChestBlock.getFacing(Wrapper.INSTANCE.getWorld().getBlockState(blockEntity.getPos()))) {
                                case EAST://good
                                    removePos.add(blockEntity.getPos().south());
                                    box = new Box(x, y, z, x + 1, y + 1, z + 2);
                                    break;
                                case SOUTH:
                                    removePos.add(blockEntity.getPos().east());
                                    box = new Box(x, y, z, x + 2, y + 1, z + 1);
                                    break;
                                case WEST://good
                                    removePos.add(blockEntity.getPos().west());
                                    box = new Box(x - 1, y, z, x + 1, y + 1, z + 1);
                                    break;
                                case NORTH://good
                                    removePos.add(blockEntity.getPos().north());
                                    box = new Box(x, y, z - 1, x + 1, y + 1, z + 1);
                                    break;
                            }
                        }
                    }*/
                    Render3DHelper.INSTANCE.drawBox(eventRender3D.getMatrixStack(), box, storageESP.getColor(blockEntity));
                }
            });
        }
    }
}
