package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.events.EventManager;
import me.dustin.events.core.Event;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.DirectClientPacketFilter;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.AutoTool;
import me.dustin.jex.feature.mod.impl.player.SpeedMine;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

@Cmd(name = "dupe", alias = {"d"}, description = "Relog. Put items you want to dupe in a shulker. Stare at shulker and type .dupe. Wait for items to dupe.")
public class CommandDupe extends Command {

    private BlockHitResult blockHitResult;
    private boolean speedmine;
    private boolean all;
    private boolean throwItems = false;
    @Override
    public void registerCommand() {
        dispatcher.register(literal("d").redirect(dispatcher.register(literal(this.name).executes(this).then(literal("all").executes(context -> {
            this.all = true;
            run(context);
            return 1;
        }).then(literal("throw").executes(context -> {
            this.all = true;
            this.throwItems = true;
            run(context);
            return 1;
        }))).then(literal("throw").executes(context -> {
            this.throwItems = true;
            run(context);
            return 1;
        })))));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        assert Wrapper.INSTANCE.getMinecraft().crosshairTarget != null;
        if (Wrapper.INSTANCE.getMinecraft().crosshairTarget.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) Wrapper.INSTANCE.getMinecraft().crosshairTarget;
            if (WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()) instanceof ShulkerBoxBlock) {
                Wrapper.INSTANCE.getInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Wrapper.INSTANCE.getWorld(), Hand.MAIN_HAND, blockHitResult);
                ChatHelper.INSTANCE.addClientMessage("Running dupe");
                this.blockHitResult = blockHitResult;
                this.speedmine = Feature.getState(SpeedMine.class) && Feature.get(SpeedMine.class).mode.equalsIgnoreCase("Instant");
                if (speedmine) {
                    Feature.get(SpeedMine.class).setState(false);
                }
                EventManager.register(this);
            } else {
                ChatHelper.INSTANCE.addClientMessage("You must be staring at a shulker");
            }
        }
        return 1;
    }

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (Feature.getState(AutoTool.class))
            new EventClickBlock(blockHitResult.getBlockPos(), blockHitResult.getSide(), EventClickBlock.Mode.PRE).run();
        Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(blockHitResult.getBlockPos(), blockHitResult.getSide());
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketSent.EventPacketSentDirect> eventPacketSentEventListener = new EventListener<>(event -> {
        PlayerActionC2SPacket playerActionC2SPacket = (PlayerActionC2SPacket) event.getPacket();
        if (playerActionC2SPacket.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
            if (Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler instanceof ShulkerBoxScreenHandler shulkerBoxScreenHandler) {
                ChatHelper.INSTANCE.addClientMessage("Sending window click and turning off");
                if (speedmine) {
                    Feature.get(SpeedMine.class).setState(true);
                }
                if (all) {
                    int most = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size() - 36;
                    for (int i = 0; i < most; i++) {
                        ItemStack stack = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.getSlot(i).getStack();
                        if (stack != null && stack.getItem() != Items.AIR) {
                            InventoryHelper.INSTANCE.windowClick(shulkerBoxScreenHandler, i, throwItems ? SlotActionType.THROW : SlotActionType.QUICK_MOVE, throwItems ? 1 : 0);
                        }
                    }
                } else {
                    InventoryHelper.INSTANCE.windowClick(shulkerBoxScreenHandler, 0, throwItems ? SlotActionType.THROW : SlotActionType.QUICK_MOVE, throwItems ? 1 : 0);
                }
            }
            EventManager.unregister(this);
            this.throwItems = false;
            this.all = false;
        }
    }, new DirectClientPacketFilter(EventPacketSent.Mode.POST, PlayerActionC2SPacket.class));
}
