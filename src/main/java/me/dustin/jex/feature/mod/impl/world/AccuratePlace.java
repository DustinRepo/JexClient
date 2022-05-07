package me.dustin.jex.feature.mod.impl.world;

import com.mojang.blaze3d.vertex.PoseStack;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.KeyPressFilter;
import me.dustin.jex.event.misc.EventKeyPressed;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.render.EventRender3D;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.helper.math.vector.RotationVector;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.render.Render3DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

@Feature.Manifest(category = Feature.Category.WORLD, description = "")
public class AccuratePlace extends Feature {

    @Op(name = "Next Key", isKeybind = true)
    public int nextKey = GLFW.GLFW_KEY_UP;
    @Op(name = "Last Key", isKeybind = true)
    public int lastKey = GLFW.GLFW_KEY_DOWN;

    private RotationVector saved;
    private Direction facing = Direction.DOWN;
    private int index = 0;

    @EventPointer
    private final EventListener<EventPacketSent> eventPacketSentEventListener = new EventListener<>(event -> {
        ServerboundUseItemOnPacket playerInteractBlockC2SPacket = (ServerboundUseItemOnPacket) event.getPacket();
        if (Wrapper.INSTANCE.getLocalPlayer().getItemInHand(playerInteractBlockC2SPacket.getHand()).getItem() instanceof BlockItem) {
            RotationVector rotationVector = null;
            switch (facing) {
                case NORTH -> rotationVector = new RotationVector(-180, PlayerHelper.INSTANCE.getPitch());
                case SOUTH -> rotationVector = new RotationVector(0, PlayerHelper.INSTANCE.getPitch());
                case EAST -> rotationVector = new RotationVector(-90, PlayerHelper.INSTANCE.getPitch());
                case WEST -> rotationVector = new RotationVector(90, PlayerHelper.INSTANCE.getPitch());
                case UP -> rotationVector = new RotationVector(PlayerHelper.INSTANCE.getYaw(), -90);
                case DOWN -> rotationVector = new RotationVector(PlayerHelper.INSTANCE.getYaw(), 90);
            }
            RotationVector saved = RotationVector.fromPlayer();
            PlayerHelper.INSTANCE.setRotation(rotationVector);
            NetworkHelper.INSTANCE.sendPacketDirect(new ServerboundMovePlayerPacket.Rot(rotationVector.getYaw(), rotationVector.getPitch(), Wrapper.INSTANCE.getLocalPlayer().isOnGround()));
            PlayerHelper.INSTANCE.setRotation(saved);

            BlockHitResult blockHitResult = playerInteractBlockC2SPacket.getHitResult();
            Vec3 newVec = blockHitResult.getLocation().add(blockHitResult.getDirection().getStepX(), blockHitResult.getDirection().getStepY(), blockHitResult.getDirection().getStepZ()).add(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            BlockPos newPos = blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
            BlockHitResult newHitResult = new BlockHitResult(newVec, facing, newPos, blockHitResult.isInside());
            ServerboundUseItemOnPacket newPacket = new ServerboundUseItemOnPacket(playerInteractBlockC2SPacket.getHand(), newHitResult, playerInteractBlockC2SPacket.getSequence());
            event.setPacket(newPacket);
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, ServerboundUseItemOnPacket.class));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
       //do rendering
        PoseStack matrixStack = event.getPoseStack();
        HitResult hitResult = Wrapper.INSTANCE.getMinecraft().hitResult;
        if (hitResult instanceof BlockHitResult blockHitResult && WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()) != Blocks.AIR) {
            matrixStack.pushPose();
            Render3DHelper.INSTANCE.setup3DRender(true);
            Vec3 centerOf = Vec3.atCenterOf(blockHitResult.getBlockPos().relative(blockHitResult.getDirection()));
            Vec3 renderPos = Render3DHelper.INSTANCE.getRenderPosition(centerOf);
            matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);
            Render3DHelper.INSTANCE.directionTranslate(matrixStack, facing);
            AABB box = new AABB(-0.5f, -0.5f, -0.5f, 0.5f, -0.45f, 0.5f);
            Render3DHelper.INSTANCE.drawBox(matrixStack, box, 0xffff0000);
            matrixStack.translate(-centerOf.x, -centerOf.y, -centerOf.z);
            Render3DHelper.INSTANCE.end3DRender();
            matrixStack.popPose();
        }
    });

    @EventPointer
    private final EventListener<EventKeyPressed> eventKeyPressedEventListener = new EventListener<>(event -> {
        if (event.getKey() == nextKey) {
            index++;
            if (index > Direction.values().length - 1)
                index = 0;
            facing = Direction.values()[index];
        } else if (event.getKey() == lastKey) {
            index--;
            if (index < 0)
                index = Direction.values().length - 1;
            facing = Direction.values()[index];
        }
        setSuffix(StringUtils.capitalize(facing.getName()));
    }, new KeyPressFilter(EventKeyPressed.PressType.IN_GAME));
}
