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
			.build();
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
	public Property<Boolean> fallingwaterProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("FallingWater")
                        .parent(liquidsProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> fallinglavaProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("FallingLava")
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
    public Property<Boolean> otherProperty = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Other")
			.description("Fire particles")
                        .parent(particlesProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> dragonbreathProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("DragonBreath")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> cloudProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Cloud")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> ashProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Ash")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> crimsonsporeProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("CrimsonSpore")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> dustProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Dust")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> sneezeProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Sneeze")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> spitProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Spit")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> splashProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Splash")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> poofProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Poof")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> whiteashProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("WhiteAsh")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> portalProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Portal")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> reverseportalProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("ReversePortal")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> witchProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Witch")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> heartProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Heart")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> instanteffectProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> effectProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Effect")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> soulProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Soul")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> explosionProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Explosion")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> explosionemitterProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("ExplosionEmitter")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> noteProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Note")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> enchantProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Enchant")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> nautilusProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Nautilus")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> barrierProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Barrier")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> blockProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Block")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> itemProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Item")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> itemslimeProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("ItemSlime")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> itemsnowballProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("ItemSnowball")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> landinghoneyProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("LandingHoney")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> landingobstearProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("LandingObsidianTear")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> myceliumProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Mycelium")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> drippinghoneyProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("DrippingHoney")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> drippingobstearProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("DrippingObsidianTear")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> fallingdustProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("FallingDust")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> fallinghoneyProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("FallingHoney")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> fallinglavaProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("FallingLava")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> fallingnectarProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("FallingNectar")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> fallingobstearProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("FallingObsidianTear")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> rainProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Rain")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> composterProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Composter")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> totemProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("TotemOfUndying")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> FireworkProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("Firework")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> endrodProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("End_Rod")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> warpedsporeProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("WarpedSpore")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
			.value(true)
			.build();
	public Property<Boolean> sweepattackProperty  = new Property.PropertyBuilder<Boolean>(this.getClass())
			.name("SweepAttack")
                        .parent(otherProperty)
                        .depends(parent -> (boolean) parent.value())
		        .value(true)
		        .build();
		
  
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
           if(dragonbreathProperty.value()) {
		   particles.add(ParticleTypes.DRAGON_BREATH);
	   }
           if(cloudProperty.value()) 
	   {
		   particles.add(ParticleTypes.CLOUD);
	   }
           if(ashProperty.value()) 
	   {
		   particles.add(ParticleTypes.ASH);
           }
           if(campfirecosyProperty.value()) 
	   {
		   particles.add(ParticleTypes.CAMPFIRE_COSY_SMOKE);
	   }
           if(flameProperty.value()) 
	   {
		   particles.add(ParticleTypes.FLAME);
	   }
           if(campfiresignalProperty.value()) 
	   {
		   particles.add(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE);
	   }
           if(crimsonsporeProperty.value()) 
	   {
		   particles.add(ParticleTypes.CRIMSON_SPORE);
	   }
           if(dustProperty.value()) 
	   { 
		   particles.add(ParticleTypes.DUST);
	   }
           if(soulfireProperty.value()) 
	   {
		   particles.add(ParticleTypes.SOUL_FIRE_FLAME);
	   }
           if(smokeProperty.value()) 
	   {
		   particles.add(ParticleTypes.SMOKE);
	   }
           if(sneezeProperty.value()) 
	   {
		   particles.add(ParticleTypes.SNEEZE);
	   }
           if(largesmokeProperty.value()) 
	   {
		   particles.add(ParticleTypes.LARGE_SMOKE);
	   }
           if(spitProperty.value()) 
	   {
		   particles.add(ParticleTypes.SPIT);
	   }
           if(splashProperty.value()) 
	   {
		   particles.add(ParticleTypes.SPLASH);
				      }
           if(poofProperty.value()) 
	   {
		   particles.add(ParticleTypes.POOF);
	   }
           if(whiteashProperty.value()) 
	   {
		   particles.add(ParticleTypes.WHITE_ASH);
	   }
           if(squidinkProperty.value()) 
	   {
		   particles.add(ParticleTypes.SQUID_INK);
	   }
           if(Property.value()) 
	   {
		   particles.add(ParticleTypes.PORTAL);
           }
           if(Property.value()) 
	   {
		   particles.add(ParticleTypes.REVERSE_PORTAL);
	   }
           if(Property.value()) 
	   {
		   particles.add(ParticleTypes.WITCH);
	   } 
           if(barrierProperty.value()) 
	   {
		   particles.add(ParticleTypes.BARRIER);
	   }
           if(blockProperty.value()) 
	   {
		   particles.add(ParticleTypes.BLOCK);
				     }
           if(itemProperty.value()) 
	   {
		   particles.add(ParticleTypes.ITEM);
	   }
           if(itemslimeProperty.value()) 
	   {
		   particles.add(ParticleTypes.ITEM_SLIME);
	   }
           if(itemsnowballProperty.value()) 
	   {
		   particles.add(ParticleTypes.ITEM_SNOWBALL);
	   }
           if(landinghoneyProperty.value()) 
	   {
		   particles.add(ParticleTypes.LANDING_HONEY);
	   }
           if(landingobstearProperty.value()) 
	   {
		   particles.add(ParticleTypes.LANDING_OBSIDIAN_TEAR);
	   }
           if(myceliumProperty.value()) 
	   {
		   particles.add(ParticleTypes.MYCELIUM);
	   }
            
           if(bubbleProperty.value()) 
	   {
		   particles.add(ParticleTypes.BUBBLE);
	   }          
           if(bubbleupProperty.value()) 
	   {
		   particles.add(ParticleTypes.BUBBLE_COLUMN_UP);
	   }
           if(bubblepopProperty.value()) 
	   {
		   particles.add(ParticleTypes.BUBBLE_POP);
	   }
           if(Property.value()) 
	   {
		   particles.add(ParticleTypes.CURRENT_DOWN);
	   }
           if(dolphinProperty.value()) 
	   {
		   particles.add(ParticleTypes.DOLPHIN);
	   }
           if(drippinghoneyProperty.value()) 
	   {
		   particles.add(ParticleTypes.DRIPPING_HONEY);
	   }         
           if(lavadripProperty.value()) 
	   {
		   particles.add(ParticleTypes.DRIPPING_LAVA);
	   }        
           if(Property.value()) 
	   {
		   particles.add(ParticleTypes.DRIPPING_OBSIDIAN_TEAR);
	   }   
           if(waterdripProperty.value()) 
	   {
		   particles.add(ParticleTypes.DRIPPING_WATER);
	   }
           if(fallingdustProperty.value()) 
	   {
		   particles.add(ParticleTypes.FALLING_DUST);
	   }            
           if(fallinghoneyProperty.value()) 
	   {
		   particles.add(ParticleTypes.FALLING_HONEY);
	   }           
           if(fallinglavaProperty.value()) 
	   {
		   particles.add(ParticleTypes.FALLING_LAVA);
	   }
           if(fallingnectarProperty.value()) 
	   {
		   particles.add(ParticleTypes.FALLING_NECTAR);
	   }
           if(fallingobstearProperty.value()) 
	   {
		   particles.add(ParticleTypes.FALLING_OBSIDIAN_TEAR);
	   }
           if(fallingwaterProperty.value()) 
	   {
		   particles.add(ParticleTypes.FALLING_WATER);
           }
           if(lavaProperty.value()) 
	   {
		   particles.add(ParticleTypes.LAVA);
	   }
           if(landinglavaProperty.value()) 
	   {
		   particles.add(ParticleTypes.LANDING_LAVA);
	   }
           if(fishingProperty.value()) 
	   {
		   particles.add(ParticleTypes.FISHING);
	   }
           if(rainProperty.value()) 
	   {
		   particles.add(ParticleTypes.RAIN);
	   }
           if(composterProperty.value())
	   {
		   particles.add(ParticleTypes.COMPOSTER);
	   }
           if(totemProperty.value()) 
	   {
		   particles.add(ParticleTypes.TOTEM_OF_UNDYING);
	   }
           if(flashProperty.value()) 
	   {  
		   particles.add(ParticleTypes.FLASH);
           }
           if(fireworkProperty.value()) 
	   {
		   particles.add(ParticleTypes.FIREWORK);
	   }
           if(happyvillagerProperty.value()) 
	   {
		   particles.add(ParticleTypes.HAPPY_VILLAGER);
	   }
           if(endrodProperty.value()) 
	   {
		   particles.add(ParticleTypes.END_ROD);
           }
           if(warpedsporeProperty.value()) 
	   {
		   particles.add(ParticleTypes.WARPED_SPORE);
	   }
           if(damageindicatorPropertyProperty.value()) 
	   { 
		   particles.add(ParticleTypes.DAMAGE_INDICATOR);
           }
           if(critProperty.value()) 
	   { 
		   particles.add(ParticleTypes.CRIT);
	   }
           if(enchanthitProperty.value()) 
	   {  
		   particles.add(ParticleTypes.ENCHANTED_HIT);
	   }
           if(sweepattackProperty.value()) 
	   { 
		   particles.add(ParticleTypes.SWEEP_ATTACK);
	   }
           if(heartProperty.value()) 
	   {  
		   particles.add(ParticleTypes.HEART);
	   }
         
           if(instanteffectProperty.value()) 
	   {  
		   particles.add(ParticleTypes.INSTANT_EFFECT);
	   }
           if(entityeffectProperty.value()) 
	   { 
		   particles.add(ParticleTypes.ENTITY_EFFECT);
	   }
           if(effectProperty.value()) 
	   { 
		   particles.add(ParticleTypes.EFFECT);
	   }
           if(soulProperty.value()) 
	   {  
		   particles.add(ParticleTypes.SOUL);
	   }
           
           if(explosionProperty.value()) 
	   {  
		   particles.add(ParticleTypes.EXPLOSION);
           }
           if(explosionemitterProperty.value()) 
	   {  
		   particles.add(ParticleTypes.EXPLOSION_EMITTER);
	   }
           if(noteProperty.value()) 
	   {  
		   particles.add(ParticleTypes.NOTE);
				    }
           if(enchantProperty.value()) 
	   {  
		   particles.add(ParticleTypes.ENCHANT);
           }
           if(nautilusProperty.value()) 
	   {  
		   particles.add(ParticleTypes.NAUTILUS);
	   }   
        }
        super.onEnable();
    }

    public static ArrayList<ParticleType<?>> getParticles() {
        return particles;
    }
}
