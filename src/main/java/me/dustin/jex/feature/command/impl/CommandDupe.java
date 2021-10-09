package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.events.api.EventAPI;
import me.dustin.events.core.Event;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.misc.EventDisplayScreen;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.event.packet.EventPacketReceive;
import me.dustin.jex.event.packet.EventPacketSent;
import me.dustin.jex.event.world.EventClickBlock;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.player.AutoTool;
import me.dustin.jex.feature.mod.impl.player.SpeedMine;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.NetworkHelper;
import me.dustin.jex.helper.player.InventoryHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.CloseScreenS2CPacket;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.ArrayList;
import java.util.Optional;

@Cmd(name = "dupe", alias = {"d"}, description = "Relog. Put items you want to dupe in a shulker. Stare at shulker and type .dupe. Wait for items to dupe.")
public class CommandDupe extends Command {

    private BlockHitResult blockHitResult;
    private boolean speedmine;

    @Override
    public void registerCommand() {
        dispatcher.register(literal("d").redirect(dispatcher.register(literal(this.name).executes(this))));
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
                this.speedmine = Feature.get(SpeedMine.class).getState() && ((SpeedMine)Feature.get(SpeedMine.class)).mode.equalsIgnoreCase("Instant");
                if (speedmine) {
                    Feature.get(SpeedMine.class).setState(false);
                }
                EventAPI.getInstance().register(this);
            } else {
                ChatHelper.INSTANCE.addClientMessage("You must be staring at a shulker");
            }
        }
        return 1;
    }

    @EventListener(events = {EventTick.class, EventPacketSent.class})
    private void runMethod(Event event) {
        if (event instanceof EventTick) {
            if (Feature.get(AutoTool.class).getState())
                new EventClickBlock(blockHitResult.getBlockPos(), blockHitResult.getSide()).run();
            Wrapper.INSTANCE.getInteractionManager().updateBlockBreakingProgress(blockHitResult.getBlockPos(), blockHitResult.getSide());
        } else if (event instanceof EventPacketSent eventPacketSent && eventPacketSent.getPacket() instanceof PlayerActionC2SPacket playerActionC2SPacket && playerActionC2SPacket.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
            if (eventPacketSent.getMode() == EventPacketSent.Mode.POST) {
                if (Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler instanceof ShulkerBoxScreenHandler shulkerBoxScreenHandler) {
                    ChatHelper.INSTANCE.addClientMessage("Sending window click and turning off");
                    if (speedmine) {
                        Feature.get(SpeedMine.class).setState(true);
                    }
                    int most = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.slots.size() - 36;
                    for (int i = 0; i < most; i++) {
                        ItemStack stack = Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.getSlot(i).getStack();
                        if (stack != null && stack.getItem() != Items.AIR) {
                            InventoryHelper.INSTANCE.windowClick(shulkerBoxScreenHandler, i, SlotActionType.QUICK_MOVE);
                        }
                    }
                }
                while (EventAPI.getInstance().alreadyRegistered(this))
                    EventAPI.getInstance().unregister(this);
            }
        }
    }
}
