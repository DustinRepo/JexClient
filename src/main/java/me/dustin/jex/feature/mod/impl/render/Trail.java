package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.option.annotate.Op;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.TrailsFile;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.FriendHelper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import java.util.ArrayList;
import java.util.Random;

@Feature.Manifest(category = Feature.Category.VISUAL, description = "Render a configurable list of particles as a trail behind you. Use command .trail to configure")
public class Trail extends Feature {

    @Op(name = "Show on Friends")
    public boolean showOnFriends = true;

    private static final ArrayList<ParticleType<?>> particles = new ArrayList<>();
    private final Random r = new Random();

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        particles.sort((c1, c2) -> r.nextInt(2) - 1);
        particles.forEach(particleType -> {
            if (PlayerHelper.INSTANCE.isMoving())
                Wrapper.INSTANCE.getMinecraft().particleManager.addParticle((ParticleEffect) particleType, Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), 0, 0, 0);
            if (showOnFriends)
                Wrapper.INSTANCE.getWorld().getEntities().forEach(entity -> {
                    if (entity != Wrapper.INSTANCE.getLocalPlayer() && entity instanceof PlayerEntity playerEntity) {
                        if (FriendHelper.INSTANCE.isFriend(playerEntity.getGameProfile().getName()) && (playerEntity.getVelocity().getX() != 0 || playerEntity.getVelocity().getZ() != 0))
                            Wrapper.INSTANCE.getMinecraft().particleManager.addParticle((ParticleEffect) particleType, Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), 0, 0, 0);
                    }
                });
        });
    }, new PlayerPacketsFilter(EventPlayerPackets.Mode.PRE));

    @Override
    public void onEnable() {
        ConfigManager.INSTANCE.get(TrailsFile.class).read();
        if (particles.isEmpty()) {
            particles.add(ParticleTypes.DRAGON_BREATH);
            particles.add(ParticleTypes.SOUL_FIRE_FLAME);
            particles.add(ParticleTypes.ASH);
        }
        super.onEnable();
    }

    public static ArrayList<ParticleType<?>> getParticles() {
        return particles;
    }
}
