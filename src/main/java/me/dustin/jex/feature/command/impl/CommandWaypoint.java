package me.dustin.jex.feature.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.dustin.jex.feature.command.core.Command;
import me.dustin.jex.feature.command.core.annotate.Cmd;
import me.dustin.jex.feature.command.core.arguments.ColorArgumentType;
import me.dustin.jex.feature.command.core.arguments.Vec3ArgumentType;
import me.dustin.jex.file.core.ConfigManager;
import me.dustin.jex.file.impl.WaypointFile;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.render.Render2DHelper;
import me.dustin.jex.helper.world.WorldHelper;
import me.dustin.jex.feature.mod.impl.world.Waypoints;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.world.phys.Vec3;

@Cmd(name = "waypoint", description = "Add or remove waypoints", syntax = {".waypoint add <name> <x/y/z/here> <color>", ".waypoint del <name>"})
public class CommandWaypoint extends Command {

    @Override
    public void registerCommand() {
        LiteralArgumentBuilder<FabricClientCommandSource> builder = literal(this.name).then(literal("add").then(argument("name", StringArgumentType.string()).then(literal("here").then(argument("color", ColorArgumentType.color()).executes(context -> {
            //adding "here"
            String server = WorldHelper.INSTANCE.getCurrentServerName();
            String name = StringArgumentType.getString(context,"name").replace("_", " ");
            int color = Render2DHelper.INSTANCE.hex2Rgb("0x" + Integer.toHexString(ColorArgumentType.getColor(context, "color").getColor())).getRGB();
            float x = (float) Wrapper.INSTANCE.getLocalPlayer().getX();
            float y = (float) Wrapper.INSTANCE.getLocalPlayer().getY();
            float z = (float) Wrapper.INSTANCE.getLocalPlayer().getZ();
            Waypoints.waypoints.add(new Waypoints.Waypoint(name, server, x, y, z, WorldHelper.INSTANCE.getDimensionID().toString(), color));
            ConfigManager.INSTANCE.get(WaypointFile.class).write();
            ChatHelper.INSTANCE.addClientMessage("Added waypoint " + name + ".");
            return 1;
        }))).then(argument("pos", Vec3ArgumentType.vec3()).then(argument("color", ColorArgumentType.color()).executes(context -> {
            //adding with coords
            String server = WorldHelper.INSTANCE.getCurrentServerName();
            Vec3 pos = Vec3ArgumentType.getVec3(context, "pos");
            String name = StringArgumentType.getString(context,"name").replace("_", " ");
            int color = Render2DHelper.INSTANCE.hex2Rgb("0x" + Integer.toHexString(ColorArgumentType.getColor(context, "color").getColor())).getRGB();
            float x = (float) pos.x();
            float y = (float) pos.y();
            float z = (float) pos.z();
            Waypoints.waypoints.add(new Waypoints.Waypoint(name, server, x, y, z, WorldHelper.INSTANCE.getDimensionID().toString(), color));
            ConfigManager.INSTANCE.get(WaypointFile.class).write();
            ChatHelper.INSTANCE.addClientMessage("Added waypoint " + name + ".");
            return 1;
        }))))).then(literal("del").then(argument("name", StringArgumentType.string()).executes(context -> {
            //deleting
            String server = WorldHelper.INSTANCE.getCurrentServerName();
            String name = StringArgumentType.getString(context,"name").replace("_", " ");
            Waypoints.Waypoint waypoint = Waypoints.get(name, server);
            if (waypoint != null) {
                Waypoints.waypoints.remove(waypoint);
                ConfigManager.INSTANCE.get(WaypointFile.class).write();
                ChatHelper.INSTANCE.addClientMessage("Removed waypoint " + name + ".");
            } else {
                ChatHelper.INSTANCE.addClientMessage("That waypoint does not exist on this server!");
            }
            return 1;
        })));
        dispatcher.register(builder);
    }

    @Override
    public int run(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        return 0;
    }
}
