package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
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
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ShulkerBoxMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

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
        assert Wrapper.INSTANCE.getMinecraft().hitResult != null;
        if (Wrapper.INSTANCE.getMinecraft().hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) Wrapper.INSTANCE.getMinecraft().hitResult;
            if (WorldHelper.INSTANCE.getBlock(blockHitResult.getBlockPos()) instanceof ShulkerBoxBlock) {
                Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, blockHitResult);
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
            new EventClickBlock(blockHitResult.getBlockPos(), blockHitResult.getDirection(), EventClickBlock.Mode.PRE).run();
        Wrapper.INSTANCE.getMultiPlayerGameMode().continueDestroyBlock(blockHitResult.getBlockPos(), blockHitResult.getDirection());
    }, new TickFilter(EventTick.Mode.PRE));

    @EventPointer
    private final EventListener<EventPacketSent.EventPacketSentDirect> eventPacketSentEventListener = new EventListener<>(event -> {
        ServerboundPlayerActionPacket playerActionC2SPacket = (ServerboundPlayerActionPacket) event.getPacket();
        if (playerActionC2SPacket.getAction() == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            if (Wrapper.INSTANCE.getLocalPlayer().containerMenu instanceof ShulkerBoxMenu shulkerBoxScreenHandler) {
                ChatHelper.INSTANCE.addClientMessage("Sending window click and turning off");
                if (speedmine) {
                    Feature.get(SpeedMine.class).setState(true);
                }
                if (all) {
                    int most = Wrapper.INSTANCE.getLocalPlayer().containerMenu.slots.size() - 36;
                    for (int i = 0; i < most; i++) {
                        ItemStack stack = Wrapper.INSTANCE.getLocalPlayer().containerMenu.getSlot(i).getItem();
                        if (stack != null && stack.getItem() != Items.AIR) {
                            InventoryHelper.INSTANCE.windowClick(shulkerBoxScreenHandler, i, throwItems ? ClickType.THROW : ClickType.QUICK_MOVE, throwItems ? 1 : 0);
                        }
                    }
                } else {
                    InventoryHelper.INSTANCE.windowClick(shulkerBoxScreenHandler, 0, throwItems ? ClickType.THROW : ClickType.QUICK_MOVE, throwItems ? 1 : 0);
                }
            }
            EventManager.unregister(this);
            this.throwItems = false;
            this.all = false;
        }
    }, new DirectClientPacketFilter(EventPacketSent.Mode.POST, ServerboundPlayerActionPacket.class));
}
