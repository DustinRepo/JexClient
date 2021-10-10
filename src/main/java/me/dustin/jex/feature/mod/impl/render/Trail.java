package me.dustin.jex.feature.mod.impl.render;

import com.google.gson.JsonArray;
import me.dustin.events.core.annotate.EventListener;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.file.ModFileHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.player.PlayerHelper;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@Feature.Manifest(name = "Trail", category = Feature.Category.VISUAL, description = "Render a configurable list of particles as a trail behind you. Use command .trail to configure")
public class Trail extends Feature {

    public ArrayList<ParticleType<?>> particles = new ArrayList<>();
    private Random r = new Random();
    @EventListener(events = {EventPlayerPackets.class})
    private void runMethod(EventPlayerPackets eventPlayerPackets) {
        if (eventPlayerPackets.getMode() == EventPlayerPackets.Mode.PRE) {
            particles.sort((c1, c2) -> r.nextInt(2) - 1);
            particles.forEach(particleType -> {
                if (PlayerHelper.INSTANCE.isMoving())
                    Wrapper.INSTANCE.getMinecraft().particleManager.addParticle((ParticleEffect) particleType, Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), 0, 0, 0);
            });
        }
    }

    @Override
    public void onEnable() {
        read();
        if (particles.isEmpty()) {
            particles.add(ParticleTypes.DRAGON_BREATH);
            particles.add(ParticleTypes.SOUL_FIRE_FLAME);
            particles.add(ParticleTypes.ASH);
        }
        super.onEnable();
    }

    public void write() {
        JsonArray jsonArray = new JsonArray();
        particles.forEach(particleType -> {
            jsonArray.add(Registry.PARTICLE_TYPE.getId(particleType).toString());
        });
        ArrayList<String> stringList = new ArrayList<>(Arrays.asList(JsonHelper.INSTANCE.prettyGson.toJson(jsonArray).split("\n")));
        try {
            File file = getTrailsFile();
            PrintWriter printWriter = new PrintWriter(file);
            StringBuilder stringBuilder = new StringBuilder();
            stringList.forEach(string -> {
                stringBuilder.append(string + "\r\n");
            });
            printWriter.print(stringBuilder);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void read() {
        if (!getTrailsFile().exists())
            return;
        particles.clear();
        try {
            StringBuilder stringBuffer = new StringBuilder("");
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(getTrailsFile().getPath()), "UTF8"));
            String line;
            while ((line = in.readLine()) != null) {
                stringBuffer.append(line);
            }
            JsonArray array = JsonHelper.INSTANCE.prettyGson.fromJson(stringBuffer.toString(), JsonArray.class);
            for (int i = 0; i < array.size(); i++) {
                String particle = array.get(i).getAsString();
                particles.add(Registry.PARTICLE_TYPE.get(new Identifier(particle)));
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getTrailsFile() {
        return new File(ModFileHelper.INSTANCE.getJexDirectory(), "Trails.json");
    }
}
