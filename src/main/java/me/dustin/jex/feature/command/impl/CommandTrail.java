package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.ParticleTypeArgumentType;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.mod.impl.render.Trail;
import me.dustin.jex.helper.misc.ChatHelper;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.particle.ParticleType;
import net.minecraft.util.registry.Registry;

import java.util.StringJoiner;

@Cmd(name = "trail", description = "Add or remove particles to Trail mod.", syntax = ".trail <add/del> <particle>/.trail list")
public class CommandTrail extends Command {

    @Override
    public void registerCommand() {
        Trail trail = (Trail) Feature.get(Trail.class);
        dispatcher.register(literal(this.name).then(literal("add").then(argument("particle", ParticleTypeArgumentType.particleType()).executes(context -> {

            ParticleType<?> particleType = ParticleTypeArgumentType.getParticleType(context, "particle");
            String particleName = Registry.PARTICLE_TYPE.getId(particleType).toString();
            if (trail.particles.contains(particleType)) {
                ChatHelper.INSTANCE.addClientMessage("This trail is already enabled");
                return 0;
            }
            trail.particles.add(particleType);
            ChatHelper.INSTANCE.addClientMessage("\247b" + particleName + "\2477 has been added to Trail");
            trail.write();
            return 1;
        }))).then(literal("del").then(argument("particle", ParticleTypeArgumentType.particleType()).executes(context -> {
            ParticleType<?> particleType = ParticleTypeArgumentType.getParticleType(context, "particle");
            String particleName = Registry.PARTICLE_TYPE.getId(particleType).toString();
            if (!trail.particles.contains(particleType)) {
                ChatHelper.INSTANCE.addClientMessage("This trail is already disabled");
                return 0;
            }
            trail.particles.remove(particleType);
            ChatHelper.INSTANCE.addClientMessage("\247b" + particleName + "\2477 has been removed from Trail");
            trail.write();
            return 1;
        }))).then(literal("list").executes(context -> {
            StringJoiner stringJoiner = new StringJoiner("\n");
            trail.particles.forEach(particleType -> {
                String particleName = Registry.PARTICLE_TYPE.getId(particleType).toString();
                stringJoiner.add(particleName);
            });
            ChatHelper.INSTANCE.addClientMessage("Trails: \n" + stringJoiner);
            return 1;
        })));
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
