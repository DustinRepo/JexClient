package me.dustin.jex.feature.mod.impl.render;

import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.event.filters.PlayerPacketsFilter;
import me.dustin.jex.event.player.EventPlayerPackets;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.jex.feature.property.Property;
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

public class Trail extends Feature {

    public final Property<Boolean> showOnFriendsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
            .name("Show On Friends")
            .value(true)
            .build();
    public Property<Boolean> particlesProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Particles")
			.description("Select particle to view.")
			.value(true)
			.build()
        //----------------------------------------------------------------------------------------------
    public Property<Boolean> liquidsProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Liquids")
			.description("Liquids particles.")
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> bubble = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Bubble")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> bubbleup = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("BubbleColumnUp")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> bubblepop = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("BubblePop")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> dolphin = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Dolphin")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> lavadrip = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("LavaDrip")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> waterdrip = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("WaterDrip")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> lavafalling = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("LavaFalling")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> waterfalling = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("WaterFalling")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> fishing = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Fishing")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> lava = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Lava")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> splash  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Splash")
			.description("Select particle to view.")
            .parent(liquidsProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    //----------------------------------------------------------------------------------------------
    public Property<Boolean> damageProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Damage")
			.description("Damage particles")
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> crit  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Crit")
			.description("Select particle to view.")
            .parent(damageProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> angryvillager  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("AngryVillager")
			.description("Select particle to view.")
            .parent(damageProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> enchanthit  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("EnchantHit")
			.description("Select particle to view.")
            .parent(damageProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> damageindicator  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("DamageIndicator")
			.description("Select particle to view.")
            .parent(damageProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
  //-----------------------------------------------------------------------------------------------
    public Property<Boolean> fireProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Fire")
			.description("Fire particles")
            .parent(particlesProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> flame  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Flame")
			.description("Select particle to view.")
            .parent(fireProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> smoke  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Smoke")
			.description("Select particle to view.")
            .parent(fireProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> campfirecosy  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("CampfireCosy")
			.description("Select particle to view.")
            .parent(fireProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> campfiresignal  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("CampfireSignal")
			.description("Select particle to view.")
            .parent(fireProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> soulfire  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("SoulFire")
			.description("Select particle to view.")
            .parent(fireProperty)
            .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
    public Property<Boolean> largesmoke  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("LargeSmoke")
			.description("Select particle to view.")
			.value(true)
			.build();
    //--------------------------------------------------------------------------------------------
    
  
    private static final ArrayList<ParticleType<?>> particles = new ArrayList<>();
    private final Random r = new Random();

    public Trail() {
        super(Category.VISUAL, "Render a configurable list of particles as a trail behind you. Use command .trail to configure");
    }

    @EventPointer
    private final EventListener<EventPlayerPackets> eventPlayerPacketsEventListener = new EventListener<>(event -> {
        particles.sort((c1, c2) -> r.nextInt(2) - 1);
        particles.forEach(particleType -> {
            if (PlayerHelper.INSTANCE.isMoving())
                Wrapper.INSTANCE.getMinecraft().particleManager.addParticle((ParticleEffect) particleType, Wrapper.INSTANCE.getLocalPlayer().getX(), Wrapper.INSTANCE.getLocalPlayer().getY(), Wrapper.INSTANCE.getLocalPlayer().getZ(), 0, 0, 0);
            if (showOnFriendsProperty.value())
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
            if(.value()) {particles.add(ParticleTypes.DRAGON_BREATH);
           if(.value()) {particles.add(ParticleTypes.CLOUD);
            if(.value()) {particles.add(ParticleTypes.ASH);
            if(campfirecosy.value()) {particles.add(ParticleTypes.CAMPFIRE_COSY_SMOKE);
            if(flame.value()) {particles.add(ParticleTypes.FLAME);
           if(campfiresignal.value()) { particles.add(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE);
            if(.value()) {particles.add(ParticleTypes.CRIMSON_SPORE);
           if(.value()) { particles.add(ParticleTypes.DUST);
          if(soulfire.value()) {  particles.add(ParticleTypes.SOUL_FIRE_FLAME);
           if(smoke.value()) { particles.add(ParticleTypes.SMOKE);
           if(.value()) { particles.add(ParticleTypes.SNEEZE);
           if(largesmoke.value()) { particles.add(ParticleTypes.LARGE_SMOKE);
           if(.value()) { particles.add(ParticleTypes.SPIT);
           if(.value()) { particles.add(ParticleTypes.SPLASH);
          if(.value()) {  particles.add(ParticleTypes.POOF);
          if(.value()) {  particles.add(ParticleTypes.WHITE_ASH);
          if(.value()) {  particles.add(ParticleTypes.SQUID_INK);
          if(.value()) {  particles.add(ParticleTypes.PORTAL);
          if(.value()) {  particles.add(ParticleTypes.REVERSE_PORTAL);
          if(.value()) {  particles.add(ParticleTypes.WITCH);
            
          if(.value()) {  particles.add(ParticleTypes.BARRIER);
          if(.value()) {  particles.add(ParticleTypes.BLOCK);
          if(.value()) {  particles.add(ParticleTypes.ITEM);
          if(.value()) {  particles.add(ParticleTypes.ITEM_SLIME);
          if(.value()) {  particles.add(ParticleTypes.ITEM_SNOWBALL);
          if(.value()) {  particles.add(ParticleTypes.LANDING_HONEY);
          if(.value()) {  particles.add(ParticleTypes.LANDING_OBSIDIAN_TEAR);
          if(.value()) {  particles.add(ParticleTypes.MYCELIUM);
            
            if(bubble.value()) {particles.add(ParticleTypes.BUBBLE);          
            if(bubbleup.value()) {particles.add(ParticleTypes.BUBBLE_COLUMN_UP);
            if(bubblepop.value()) {particles.add(ParticleTypes.BUBBLE_POP);
            if(.value()) {particles.add(ParticleTypes.CURRENT_DOWN);
            if(dolphin.value()) {particles.add(ParticleTypes.DOLPHIN);
            if(.value()) {particles.add(ParticleTypes.DRIPPING_HONEY);           
            if(lavadrip.value()) {particles.add(ParticleTypes.DRIPPING_LAVA);            
            if(.value()) {particles.add(ParticleTypes.DRIPPING_OBSIDIAN_TEAR);     
            if(waterdrip.value()) {particles.add(ParticleTypes.DRIPPING_WATER);
            if(.value()) {particles.add(ParticleTypes.FALLING_DUST);            
            if(.value()) {particles.add(ParticleTypes.FALLING_HONEY);            
            if(.value()) {particles.add(ParticleTypes.FALLING_LAVA);
            if(.value()) {particles.add(ParticleTypes.FALLING_NECTAR):
            if(.value()) {particles.add(ParticleTypes.FALLING_OBSIDIAN_TEAR);
            if(.value()) {particles.add(ParticleTypes.FALLING_WATER);
            if(lava.value()) {particles.add(ParticleTypes.LAVA);
            if(.value()) {particles.add(ParticleTypes.LANDING_LAVA);
            if(fishing.value()) {particles.add(ParticleTypes.FISHING);
            if(.value()) {particles.add(ParticleTypes.UNDERWATER);
            if(.value()) {particles.add(ParticleTypes.RAIN);
            
           if(.value()){particles.add(ParticleTypes.COMPOSTER);
          if(.value()) {particles.add(ParticleTypes.TOTEM_OF_UNDYING);
          if(flash.value()) {  particles.add(ParticleTypes.FLASH);
           if(.value()) { particles.add(ParticleTypes.FIREWORK);
            if(.value()) {particles.add(ParticleTypes.HAPPY_VILLAGER);
           if(.value()) { particles.add(ParticleTypes.END_ROD);
           if(.value()) { particles.add(ParticleTypes.WARPED_SPORE);
           
           if(damageindicator.value()) { particles.add(ParticleTypes.DAMAGE_INDICATOR);
           if(crit.value()) { particles.add(ParticleTypes.CRIT);
          if(enchanthit.value()) {  particles.add(ParticleTypes.ENCHANTED_HIT);
           if(.value()) { particles.add(ParticleTypes.SWEEP_ATTACK);
          if(.value()) {  particles.add(ParticleTypes.HEART);
         
          if(.value()) {  particles.add(ParticleTypes.INSTANT_EFFECT);
           if(.value()) { particles.add(ParticleTypes.ENTITY_EFFECT);
           if(.value()) { particles.add(ParticleTypes.EFFECT);
          if(.value()) {  particles.add(ParticleTypes.SOUL);
           
          if(.value()) {  particles.add(ParticleTypes.SWEEP_ATTACK);
          if(.value()) {  particles.add(ParticleTypes.EXPLOSION);
          if(.value()) {  particles.add(ParticleTypes.EXPLOSION_EMITTER);
          if(.value()) {  particles.add(ParticleTypes.NOTE);
          if(.value()) {  particles.add(ParticleTypes.ENCHANT);
          if(.value()) {  particles.add(ParticleTypes.NAUTILUS);     
        }
        super.onEnable();
    }

    public static ArrayList<ParticleType<?>> getParticles() {
        return particles;
    }
}
