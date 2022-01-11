package me.dustin.jex.feature.mod.impl.world;

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
import me.dustin.jex.helper.render.font.FontHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
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
        PlayerInteractBlockC2SPacket playerInteractBlockC2SPacket = (PlayerInteractBlockC2SPacket) event.getPacket();
        if (Wrapper.INSTANCE.getLocalPlayer().getStackInHand(playerInteractBlockC2SPacket.getHand()).getItem() instanceof BlockItem) {
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
            NetworkHelper.INSTANCE.sendPacketDirect(new PlayerMoveC2SPacket.LookAndOnGround(rotationVector.getYaw(), rotationVector.getPitch(), Wrapper.INSTANCE.getLocalPlayer().isOnGround()));
            PlayerHelper.INSTANCE.setRotation(saved);

            BlockHitResult blockHitResult = playerInteractBlockC2SPacket.getBlockHitResult();
            Vec3d newVec = blockHitResult.getPos().add(blockHitResult.getSide().getOffsetX(), blockHitResult.getSide().getOffsetY(), blockHitResult.getSide().getOffsetZ()).add(facing.getOffsetX(), facing.getOffsetY(), facing.getOffsetZ());
            BlockPos newPos = blockHitResult.getBlockPos().offset(blockHitResult.getSide());
            BlockHitResult newHitResult = new BlockHitResult(newVec, facing, newPos, blockHitResult.isInsideBlock());
            PlayerInteractBlockC2SPacket newPacket = new PlayerInteractBlockC2SPacket(playerInteractBlockC2SPacket.getHand(), newHitResult);
            event.setPacket(newPacket);
        }
    }, new ClientPacketFilter(EventPacketSent.Mode.PRE, PlayerInteractBlockC2SPacket.class));

    @EventPointer
    private final EventListener<EventRender3D> eventRender3DEventListener = new EventListener<>(event -> {
       //do rendering
        MatrixStack matrixStack = event.getMatrixStack();
        HitResult hitResult = Wrapper.INSTANCE.getMinecraft().crosshairTarget;
        if (hitResult instanceof BlockHitResult blockHitResult && WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()) != Blocks.AIR) {
            matrixStack.push();
            Render3DHelper.INSTANCE.setup3DRender(true);
            Vec3d centerOf = Vec3d.ofCenter(blockHitResult.getBlockPos().offset(blockHitResult.getSide()));
            Vec3d renderPos = Render3DHelper.INSTANCE.getRenderPosition(centerOf);
            matrixStack.translate(renderPos.x, renderPos.y, renderPos.z);
            Render3DHelper.INSTANCE.directionTranslate(matrixStack, facing);
            Box box = new Box(-0.5f, -0.5f, -0.5f, 0.5f, -0.45f, 0.5f);
            Render3DHelper.INSTANCE.drawBox(matrixStack, box, 0xffff0000);
            matrixStack.translate(-centerOf.x, -centerOf.y, -centerOf.z);
            Render3DHelper.INSTANCE.end3DRender();
            matrixStack.pop();
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
