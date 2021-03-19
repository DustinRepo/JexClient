package me.dustin.jex.module.impl.render.storageesp.impl;

import me.dustin.events.core.Event;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.extension.ModuleExtension;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.render.Stencil;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.module.core.Module;
import me.dustin.jex.module.impl.render.storageesp.StorageESP;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_LINE_SMOOTH;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public class OutlineStorageESP extends ModuleExtension {
    private StorageESP storageESP;
    public OutlineStorageESP() {
        super("Outline", StorageESP.class);
    }

    @Override
    public void pass(Event event) {
        if (storageESP == null) {
            storageESP = (StorageESP) Module.get(StorageESP.class);
        }
        if (event instanceof EventRender3D) {
            EventRender3D eventRender3D = (EventRender3D)event;
            GL11.glPushMatrix();
            GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
            GL11.glDisable(GL11.GL_LIGHTING);
            Stencil.INSTANCE.write();
            GL11.glLineWidth(3f);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glEnable(GL_LINE_SMOOTH);
            GL11.glDisable(GL_TEXTURE_2D);
            WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                if (storageESP.isValid(blockEntity)) {
                    renderTileEntity(blockEntity, eventRender3D, storageESP);
                }
            });
            Stencil.INSTANCE.erase();
            GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            WorldHelper.INSTANCE.getBlockEntities().forEach(blockEntity -> {
                if (storageESP.isValid(blockEntity)) {
                    renderTileEntity(blockEntity, eventRender3D, storageESP);
                }
            });
            Stencil.INSTANCE.dispose();
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL_LINE_SMOOTH);
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

    private void renderTileEntity(BlockEntity blockEntity, EventRender3D eventRender3D, StorageESP esp) {
        double x = blockEntity.getPos().getX() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().getX();
        double y = blockEntity.getPos().getY() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().getY();
        double z = blockEntity.getPos().getZ() - Wrapper.INSTANCE.getMinecraft().getEntityRenderDispatcher().camera.getPos().getZ();
        BlockState blockState = blockEntity.getCachedState();

        blockState.getOutlineShape(Wrapper.INSTANCE.getWorld(), blockEntity.getPos()).getBoundingBoxes().forEach(bb -> {
            Render3DHelper.INSTANCE.drawFilledBox(bb.offset(Render3DHelper.INSTANCE.getRenderPosition(blockEntity.getPos().getX(), blockEntity.getPos().getY(), blockEntity.getPos().getZ())), esp.getColor(blockEntity));
        });
    }

}
