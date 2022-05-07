package me.dustin.jex.feature.mod.impl.combat;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.player.EventAttackEntity;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.feature.option.annotate.OpChild;
import me.dustin.jex.helper.network.NetworkHelper;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.LivingEntity;

@Feature.Manifest(category = Feature.Category.COMBAT, description = "Automatically deal critical strikes when attacking.")
public class Criticals extends Feature {

    @Op(name = "Living Only")
    public boolean livingOnly = true;
    @Op(name = "Extra Particles")
    public boolean extraParticles;
    @OpChild(name = "Amount", min = 1, max = 20, inc = 1, parent = "Extra Particles")
    public int amount = 5;

    @EventPointer
    private final EventListener<EventAttackEntity> eventAttackEntityEventListener = new EventListener<>(event -> {
        if (livingOnly && !(event.getEntity() instanceof LivingEntity))
            return;
        if (extraParticles) {
            for (int i = 0; i < amount; i++) {
                Wrapper.INSTANCE.getLocalPlayer().crit(event.getEntity());
            }
        }
        if (Wrapper.INSTANCE.getLocalPlayer().isSprinting()) //mc recently (1.15?) made it so you can't crit while sprinting
            NetworkHelper.INSTANCE.sendPacket(new ServerboundPlayerCommandPacket(Wrapper.INSTANCE.getLocalPlayer(), ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
        crit();
    });

    public void crit() {
        if (Wrapper.INSTANCE.getLocalPlayer().isOnGround() && !(Wrapper.INSTANCE.getLocalPlayer().isInLava() || Wrapper.INSTANCE.getLocalPlayer().isInWater())) {
            Wrapper.INSTANCE.getLocalPlayer().connection.send(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 0.05F, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().connection.send(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().connection.send(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY() + 0.012511F, Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
            Wrapper.INSTANCE.getLocalPlayer().connection.send(new ServerboundMovePlayerPacket.Pos(Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), false));
        }
    }
}
