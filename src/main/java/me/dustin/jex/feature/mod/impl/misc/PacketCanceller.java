package me.dustin.jex.feature.mod.impl.misc;

import io.netty.buffer.Unpooled;
import me.dustin.jex.event.filters.ClientPacketFilter;
import me.dustin.jex.event.filters.DirectClientPacketFilter;
import net.minecraft.network.PacketByteBuf;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.events.core.EventListener;
import me.dustin.jex.event.packet.EventPacketSent;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import java.nio.charset.StandardCharsets;

public class PacketCanceller extends Feature {

 public PacketCanceller() {
        super(Category.MISC, "Cancel the client and server packets");
    }

public final Property<Boolean> c2spackets = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("C2S")
.description("C2S player packets")
.value(true)
.build();

public final Property<Boolean> advancementtab = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("AdvancementTabC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> boatpaddlestate = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("BoatPaddleStateC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> bookupdate = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("BookUpdateC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> buttonclick = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ButtonClickC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> chatmessage = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChatMessageC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clickslot = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClickSlotC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientcommand = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClientCommandC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientsettings = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClientSettingsC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientstatus = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClientStatusC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> closehandledscreen = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CloseHandledScreenC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> craftrequest = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CraftRequestC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> creativeinventoryaction = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CreativeInventoryActionC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> custompayload = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CustomPayloadC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> handswing = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("HandSwingC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> jigsawgenerating = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("JigsawGeneratingC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> keepalive = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("KeepAliveC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> pickfrominventory = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PickFromInventoryC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playeraction = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerActionC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinput = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerInputC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractblock = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerInteractBlockC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractentity = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerInteractEntityC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractitem = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerInteractItemC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playermove = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerMoveC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playpong = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayPongC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> queryblocknbt = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("QueryBlockNbtC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> queryentitynbt = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("QueryEntityNbtC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> recipebookdata = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("RecipeBookDataC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> recipecategoryoptions = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("RecipeCategoryOptionsC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> renameitem = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("RenameItemC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> requestcommandcompletions = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("RequestCommandCompletionsC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> resourcepackstatus = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ResourcePackStatusC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> selectmerchanttrade = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SelectMerchantTradeC2SPackett")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> spectatorteleport = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SpectatorTeleportC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> teleportconfirm = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("TeleportConfirmC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatebeacon = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateBeaconC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatecommandblock = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateCommandBlockC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatecommandblockminecart = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateCommandBlockMinecartC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatedifficulty = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateDifficultyC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatedifficultylock = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateDifficultyLockC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatejigsaw = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateJigsawC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updateplayerabilities = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdatePlayerAbilitiesC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updateselectedslot = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateSelectedSlotC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatesign = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateSignC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatestructureblock = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateStructureBlockC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> vehiclemove = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("VehicleMoveC2SPacket")
.value(true)
.parent(c2spackets)
.build();

//-------------------

public final Property<Boolean> s2cpackets = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("S2C")
.description("s2c player packets")
.value(true)
.build();

public final Property<Boolean> advancementupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("AdvancementUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockbreakingprogresss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("BlockBreakingProgressS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockentityupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("BlockEntityUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockevents2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("BlockEventS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("BlockUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> bossbars2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("BossBarS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkdatas2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChunkDataS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkdeltaupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChunkDeltaUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkloaddistances2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChunkLoadDistanceS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkrenderdistancecenters2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChunkRenderDistanceCenterS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> cleartitles2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClearTitleS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> closescreens2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CloseScreenS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> commandsuggestionss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CommandSuggestionsS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> commandtrees2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CommandTreeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> cooldownupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CooldownUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> craftfailedresponses2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CraftFailedResponseS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> custompayloads2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("CustomPayloadS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> deathmessages2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("DeathMessageS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> difficultys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("DifficultyS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> disconnects2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("DisconnectS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> endcombats2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EndCombatS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entercombats2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EnterCombatS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityanimations2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityAnimationS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityattachs2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityAttachS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityattributess2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityAttributesS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitydestroys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityDestroyS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityequipmentupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityEquipmentUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitypassengersSets2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityPassengersSetS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitypositions2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityPositionS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitySetheadyaws2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntitySetHeadYawS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntitySpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitystatuseffects2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityStatusEffectS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitystatuss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityStatusS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitytrackerupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityTrackerUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityvelocityupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityVelocityUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> experiencebarupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ExperienceBarUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> experienceorbspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ExperienceOrbSpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> explosions2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ExplosionS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gamejoins2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("GameJoinS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gamemessages2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("GameMessageS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gamestatechanges2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("GameStateChangeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> healthupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("HealthUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> inventorys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("InventoryS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> itempickupanimations2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ItemPickupAnimationS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> keepalives2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("KeepAliveS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> lightupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("LightUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> lookats2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("LookAtS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> mapupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("MapUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> mobspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("MobSpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> nbtqueryresponses2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("NbtQueryResponseS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openhorsescreens2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("OpenHorseScreenS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openscreens2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("OpenScreenS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openwrittenbooks2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("OpenWrittenBookS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> overlaymessages2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("OverlayMessageS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> paintingspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PaintingSpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> particles2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ParticleS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerabilitiess2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerAbilitiesS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playeractionresponses2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerActionResponseS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerlistheaders2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerListHeaderS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerlists2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerListS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerpositionlooks2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerPositionLookS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerrespawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerRespawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerspawnpositions2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerSpawnPositionS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerSpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playpings2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayPingS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsoundfromentitys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlaySoundFromEntityS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsoundids2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlaySoundIdS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsounds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlaySoundS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> removeentitystatuseffects2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("RemoveEntityStatusEffectS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> resourcepacksends2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ResourcePackSendS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboarddisplays2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScoreboardDisplayS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboardobjectiveupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScoreboardObjectiveUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboardplayerupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScoreboardPlayerUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> screenhandlerpropertyupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScreenHandlerPropertyUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> screenhandlerslotupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScreenHandlerSlotUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> selectadvancementtabs2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SelectAdvancementTabS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> setcameraentitys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SetCameraEntityS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> settradeofferss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SetTradeOffersS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> signeditoropens2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SignEditorOpenS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> statisticss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("StatisticsS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> stopsounds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("StopSoundS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> subtitles2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SubtitleS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> synchronizerecipess2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SynchronizeRecipesS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> synchronizetagss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("SynchronizeTagsS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> teams2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("TeamS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> titlefades2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("TitleFadeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> titles2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("TitleS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> unloadchunks2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UnloadChunkS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> unlockrecipess2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UnlockRecipesS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> updateselectedslots2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateSelectedSlotS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> vehiclemoves2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("VehicleMoveS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> vibrations2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("VibrationS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldbordercenterchangeds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderCenterChangedS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderinitializes2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderInitializeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderinterpolatesizes2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderInterpolateSizeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldbordersizechangeds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderSizeChangedS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderwarningblockschangeds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderWarningBlocksChangedS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderwarningtimechangeds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderWarningTimeChangedS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldevents2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldEventS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldtimeupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldTimeUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();

@EventPointer
private final EventListener<EventPacketSent> eventPacketSenteventListener = new eventListener<>(event -> {

if(advancementtab.value()) {
 AdvancementUpdateC2SPacket packet1;
 event.cancel(packet1);
}

if(boatpaddlestate.value()){
BoatPaddleStateC2SPacket packet2
event.cancel(packet2);
}

if(bookupdate.value()){
BookUpdateC2SPacket packet3;
event.cancel(packet3);
}

if(buttonclick.value()){
ButtonClickC2SPacket packet4;
event.cancel(packet4);
}

if(chatmessage.value()){
ChatMessageC2SPacket packet5;
event.cancel(packet5);
}

if(clickslot.value()){
ClickSlotC2SPacket packet6;
event.cancel(packet6);
}

if(clientcommand.value()){
ClientCommandC2SPacket packet7;
event.cancel(packet7);
}

if(clientcettings.value()){
ClientSettingsC2SPacket packet8;
event.cancel(packet8);
}

if(clientstatus.value()){
ClientStatusC2SPacket packet9;
event.cancel(packet9);
}

if(closehandledscreen.value()){
CloseHandledScreenC2SPacket packet10;
event.cancel(packet10);
}

if(craftrequest.value()){
CraftRequestC2SPacket packet11;
event.cancel(packet11);
}

if(creativeinventoryaction.value()){
CreativeInventoryActionC2SPacket packet12;
event.cancel(packet12);
}

if(custompayload.value()){
CustomPayloadC2SPacket packet13;
event.cancel(packet13);
}

if(handswing.value()){
HandSwingC2SPacket packet14;
event.cancel(packet14);
}

if(jigsawgenerating.value()){
Jigsaws2ceneratingC2SPacket packet15;
event.cancel(packet15);
}

if(keepalive.value()){
KeepAliveC2SPacket packet16;
event.cancel(packet16);
}

if(pickfrominventory.value()){
PickFromInventoryC2SPacket packet17;
event.cancel(packet17);
}
if(playeraction.value()){
PlayerActionC2SPacket packet18;
event.cancel(packet18);
}
if(playerinput.value()){
PlayerInputC2SPacket packet19;
event.cancel(packet19);
}
if(playerinteractblock.value()){
PlayerInteractBlockC2SPacket packet20;
event.cancel(packet20);
}
if(playerinteractentity.value()){
PlayerInteractentityC2SPacket packet21;
event.cancel(packet21);
}
if(playerinteractitem.value()){
PlayerInteractItemC2SPacket packet22;
event.cancel(packet22);
}
if(playermove.value()){
PlayerMoveC2SPacket packet23;
event.cancel(packet23);
}
if(playpong.value()){
PlayPongC2SPacket packet24;
event.cancel(packet24);
}
if(queryblocknbt.value()){
QueryBlockNbtC2SPacket packet25;
event.cancel(packet25);
}
if(queryentitynbt.value()){
QueryentityNbtC2SPacket packet26;
event.cancel(packet26);
}
if(recipebookdata.value()){
RecipeBookDataC2SPacket packet27;
event.cancel(packet27);
}
if(recipecategoryoptions.value()){
RecipeCategoryOptionsC2SPacket packet28;
event.cancel(packet28);
}
if(renameitem.value()){
RenameItemC2SPacket packet29;
event.cancel(packet29);
}
if(requestcommandcompletions.value()){
RequestCommandCompletionsC2SPacket packet30;
event.cancel(packet30);
}
if(resourcepackstatus.value()){
ResourcePackStatusC2SPacket packet31;
event.cancel(packet31);
}
if(selectmerchanttrade.value()){
SelectMerchantTradeC2SPacket packet32;
event.cancel(packet32);
}
if(spectatorteleport.value()){
SpectatorTeleportC2SPacket packet33;
event.cancel(packet33);
}
if(teleportconfirm.value()){
TeleportConfirmC2SPacket packet34;
event.cancel(packet34);
}
if(updatebeacon.value()){
UpdateBeaconC2SPacket packet35;
event.cancel(packet35);
}
if(updatecommandblockminecart.value()){
UpdateCommandBlockMinecartC2SPacket packet36;
event.cancel(packet36);
}
if(updatedifficulty.value()){
UpdateDifficultyC2SPacket packet37;
event.cancel(packet37);
}
if(updatedifficultylock.value()){
UpdateDifficultyLockC2SPacket packet38;
event.cancel(packet38);
}
if(updatejigsaw.value()){
UpdateJigsawC2SPacket packet39;
event.cancel(packet39);
}
if(updateplayerabilities.value()){
UpdatePlayerAbilitiesC2SPacket packet40;
event.cancel(packet40);
}
if(updateselectedslot.value()){
UpdateSelectedSlotC2SPacket packet41;
event.cancel(packet41);
}
if(updatesign.value()){
UpdateSignC2SPacket packet42;
event.cancel(packet42);
}
if(updatestructureblock.value()){
UpdateStructureBlockC2SPacket packet43;
event.cancel(packet43);
}
if(vehiclemove.value()){
VehicleMoveC2SPacket packet44;
event.cancel(packet44);
}

//--- s2c

if(advancementupdates2c.value()){
AdvancementUpdateS2CPacket packet45;
event.cancel(packet45);
}
if(blockbreakingprogresss2c.value()){
BlockBreakingProgressS2CPacket packet46;
event.cancel(packet46);
}
if(blockentityupdates2c.value()){
BlockEntityUpdateS2CPacket packet47;
event.cancel(packet47);
}
if(blockevents2c.value()){
BlockEventS2CPacket packet48;
event.cancel(packet48);
}
if(blockupdates2c.value()){
BlockUpdateS2CPacket packet49;
event.cancel(packet49);
}
if(bossbars2c.value()){
BossBarS2CPacket packet50;
event.cancel(packet50);
}
if(chunkdatas2c.value()){
ChunkDataS2CPacket packet51;
event.cancel(packet51);
}
if(chunkdeltaupdates2c.value()){
ChunkDeltaUpdateS2CPacket packet52;
event.cancel(packet52);
}
if(chunkloaddistances2c.value()){
ChunkLoadDistanceS2CPacket packet53;
event.cancel(packet53);
}
if(chunkrenderdistancecenters2c.value()){
ChunkRenderDistanceCenterS2CPacket packet54;
event.cancel(packet54);
}
if(cleartitles2c.value()){
ClearTitleS2CPacket packet55;
event.cancel(packet55);
}
if(closescreens2c.value()){
CloseScreenS2CPacket packet56;
event.cancel(packet56);
}
if(commandsuggestionss2c.value()){
CommandSuggestionsS2CPacket packet57;
event.cancel(packet57);
}
if(commandtrees2c.value()){
CommandTreeS2CPacket packet58;
event.cancel(packet58);
}
if(cooldownupdates2c.value()){
CooldownUpdateS2CPacket packet59;
event.cancel(packet59);
}
if(craftfailedresponses2c.value()){
CraftFailedResponseS2CPacket packet60;
event.cancel(packet60);
}
if(custompayloads2c.value()){
CustomPayloadS2CPacket packet61;
event.cancel(packet61);
}
if(deathmessages2c.value()){
DeathMessageS2CPacket packet62;
event.cancel(packet62);
}
if(difficultys2c.value()){
DifficultyS2CPacket packet63;
event.cancel(packet63);
}
if(disconnects2c.value()){
DisconnectS2CPacket packet64;
event.cancel(packet64);
}
if(endcombats2c.value()){
EndCombatS2CPacket packet65;
event.cancel(packet65);
}
if(entercombats2c.value()){
EnterCombatS2CPacket packet66;
event.cancel(packet66);
}
if(entityanimations2c.value()){
EntityAnimationS2CPacket packet67;
event.cancel(packet67);
}
if(entityattachs2c.value()){
EntityAttachS2CPacket packet68;
event.cancel(packet68);
}
if(entityattributess2c.value()){
EntityAttributesS2CPacket packet69;
event.cancel(packet69);
}
if(entitydestroys2c.value()){
EntityDestroyS2CPacket packet70;
event.cancel(packet70);
}
if(entityequipmentupdates2c.value()){
EntityEquipmentUpdateS2CPacket packet71;
event.cancel(packet71);
}
if(entitypassengerssets2c.value()){
EntityPassengersSetS2CPacket packet72;
event.cancel(packet72);
}
if(entitypositions2c.value()){
EntityPositionS2CPacket packet73;
event.cancel(packet73);
}
if(entitys2c.value()){
EntityS2CPacket packet74;
event.cancel(packet74);
}
if(entitysetheadyaws2c.value()){
EntitySetHeadYawS2CPacket packet75);
event.cancel(packet75);
}
if(entityspawns2c.value()){
EntitySpawnS2CPacket packet76;
event.cancel(packet76);
}
if(entitystatuseffects2c.value()){
EntityStatusEffectS2CPacket packet77;
event.cancel(packet77);
}
if(entitystatuss2c.value()){
EntityStatusS2CPacket packet78;
event.cancel(packet78);
}
if(entitytrackerupdates2c.value()){
EntityTrackerUpdateS2CPacket packet79;
event.cancel(packet79);
}
if(entityvelocityupdates2c.value()){
EntityVelocityUpdateS2CPacket packet80;
event.cancel(packet80);
}
if(experiencebarupdates2c.value()){
ExperienceBarUpdateS2CPacket packet81;
event.cancel(packet81);
}
if(experienceorbspawns2c.value()){
ExperienceOrbSpawnS2CPacket packet82;
event.cancel(packet82);
}
if(explosions2c.value()){
ExplosionS2CPacket packet83;
event.cancel(packet83);
}
if(gamejoins2c.value()){
GameJoinS2CPacket packet84 ;
event.cancel(packet84);
}
if(gamemessages2c.value()){
GameMessageS2CPacket packet85;
event.cancel(packet85);
}
if(gamestatechanges2c.value()){
GameStateChangeS2CPacket packet86;
event.cancel(packet86);
}
if(healthupdates2c.value()){
HealthUpdateS2CPacket packet87;
event.cancel(packet87);
}
if(inventorys2c.value()){
InventoryS2CPacket packet88;
event.cancel(packet88);
}
if(itempickupanimations2c.value()){
ItemPickupAnimationS2CPacket packet89;
event.cancel(packet89);
}
if(keepalives2c.value()){
KeepAliveS2CPacket packet90;
event.cancel(packet90);
}
if(lightupdates2c.value()){
LightUpdateS2CPacket packet91;
event.cancel(packet91);
}
if(lookats2c.value()){
LookAtS2CPacket packet92;
event.cancel(packet92);
}
if(mapupdates2c.value()){
MapUpdateS2CPacket packet93;
event.cancel(packet93);
}
if(mobspawns2c.value()){
MobSpawnS2CPacket packet94;
event.cancel(packet94);
}
if(nbtqueryresponses2c.value()){
NbtQueryResponseS2CPacket packet95;
event.cancel(packet95);
}
if(openhorsescreens2c.value()){
OpenHorseScreenS2CPacket packet96;
event.cancel(packet96);
}
if(openscreens2c.value()){
OpenScreenS2CPacket packet97;
event.cancel(packet97);
}
if(openwrittenbooks2c.value()){
OpenWrittenBookS2CPacket packet98;
event.cancel(packet98);
}
if(overlaymessages2c.value()){
OverlayMessageS2CPacket packet99;
event.cancel(packet99);
}
if(paintingspawns2c.value()){
PaintingSpawnS2CPacket packet100;
event.cancel(packet100);
}
if(particles2c.value()){
ParticleS2CPacket packet101;
event.cancel(packet101);
}
if(playerabilitiess2c.value()){
PlayerAbilitiesS2CPacket packet102;
event.cancel(packet102);
}
if(playeractionresponses2c.value()){
PlayerActionResponseS2CPacket packet103;
event.cancel(packet103);
}
if(playerlistheaders2c.value()){
PlayerListHeaderS2CPacket packet104;
event.cancel(packet104);
}
if(playerlists2c.value()){
PlayerListS2CPacket packet105;
event.cancel(packet105);
}
if(playerpositionlooks2c.value()){
PlayerPositionLookS2CPacket packet106;
event.cancel(packet106);
}
if(playerrespawns2c.value()){
PlayerRespawnS2CPacket packet107;
event.cancel(packet107);
}
if(playerspawnpositions2c.value()){
PlayerSpawnPositionS2CPacket packet108;
event.cancel(packet108);
}
if(playerspawns2c.value()){
PlayerSpawnS2CPacket packet109;
event.cancel(packet109);
}
if(playpings2c.value()){
PlayPingS2CPacket packet110;
event.cancel(packet110);
}
if(playsoundfromentitys2c.value()) {
PlaySoundFromEntityS2CPacket packet111;
event.cancel(packet111);
}
if(playsoundids2c.value()){
PlaySoundIdS2CPacket packet112;
event.cancel(packet112);
}
if(playsounds2c.value()){
PlaySoundS2CPacket packet113;
event.cancel(packet113);
}
if(removeentitystatuseffects2c.value()){
RemoveEntityStatusEffectS2CPacket packet114;
event.cancel(packet114);
}
if(resourcepacksends2c.value()){
ResourcePackSendS2CPacket packet115;
event.cancel(packet115);
}
if(scoreboarddisplays2c.value()){
ScoreboardDisplayS2CPacket packet116;
event.cancel(packet116);
}
if(scoreboardobjectiveupdates2c.value()){
ScoreboardObjectiveUpdateS2CPacket packet117;
event.cancel(packet117);
}
if(scoreboardplayerupdates2c.value()){
ScoreboardPlayerUpdateS2CPacket packet118;
event.cancel(packet118);
}
if(screenhandlerpropertyupdates2c.value()){
ScreenHandlerPropertyUpdateS2CPacket packet119;
event.cancel(packet119);
}
if(screenhandlerslotupdates2c.value()){
ScreenHandlerSlotUpdateS2CPacket packet120;
event.cancel(packet120);
}
if(selectadvancementtabs2c.value()){
SelectAdvancementTabS2CPacket packet121;
event.cancel(packet121);
}
if(setcameraentitys2c.value()){
SetCameraEntityS2CPacket packet122;
event.cancel(packet122);
}
if(settradeofferss2c.value()){
SetTradeOffersS2CPacket packet123;
event.cancel(packet123);
}
if(signeditoropens2c.value()){
SignEditorOpenS2CPacket packet124;
event.cancel(packet124);
}
if(statisticss2c.value()){
StatisticsS2CPacket packet125;
event.cancel(packet125);
}
if(stopsounds2c.value()){
StopSoundS2CPacket packet126;
event.cancel(packet126);
}
if(subtitles2c.value()){
SubtitleS2CPacket packet127;
event.cancel(packet127);
}
if(synchronizerecipess2c.value()){
SynchronizeRecipesS2CPacket packet128;
event.cancel(packet128);
}
if(synchronizetagss2c.value()){
SynchronizeTagsS2CPacket packet129;
event.cancel(packet129);
}
if(teams2c.value()){
TeamS2CPacket packet130;
event.cancel(packet130);
}
if(titlefades2c.value()){
TitleFadeS2CPacket packet131;
event.cancel(packet131);
}
if(titles2c.value()){
TitleS2CPacket packet132;
event.cancel(packet132);
}
if(unloadchunks2c.value()){
UnloadChunkS2CPacket packet133;
event.cancel(packet133);
}
if(unlockrecipess2c.value()){
UnlockRecipesS2CPacket packet134);
event.cancel(packet134);
}
if(updateselectedslots2c.value()){
UpdateSelectedSlotS2CPacket packet135;
event.cancel(packet135);
}
if(vehiclemoves2c.value()){
VehicleMoveS2CPacket packet136;
event.cancel(packet136);
}
if(wibrations2c.value()){
VibrationS2CPacket packet137;
event.cancel(packet137);
}
if(worldbordercenterchangeds2c.value()){
WorldBorderCenterChangedS2CPacket;
event.cancel(packet138);
}
if(worldborderinitializes2c.value()){
WorldBorderInitializeS2CPacket;
event.cancel(packet139);
}
if(worldborderinterpolatesizes2c.value()){
WorldBorderInterpolateSizeS2CPacket;
event.cancel(packet140);
}
if(worldbordersizechangeds2c.value()){
WorldBorderSizeChangedS2CPacket packet141;
event.cancel(packet141);
}
if(worldborderwarningblockschangeds2c.value()){
WorldBorderWarningBlocksChangedS2CPacket packet142;
event.cancel(packet142);
}
if(worldborderwarningtimechangeds2c.value()){
WorldBorderWarningTimeChangedS2CPacket packet143;
event.cancel(packet143);
}
if(worldevents2c.value()){
WorldEventS2CPacket packet144;
event.cancel(packet144);
}
if(worldtimeupdates2c.value()){
WorldTimeUpdateS2CPacket packet145;
event.cancel(packet145);
}
});
}
