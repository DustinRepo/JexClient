package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.annotate.EventPointer;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.network.Packet;
import me.dustin.events.core.EventListener;
import me.dustin.jex.feature.property.Property;
import me.dustin.jex.feature.mod.core.FeatureExtension;
import me.dustin.jex.helper.misc.Wrapper;

public class PacketCanceller extends Feature {

 public PacketCanceller() {
        super(Category.MISC, "Cancel the client and server packets");
    }

public final Property<Boolean> c2spackets =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("C2S")
.description("C2S player packets")
.value(true)
.build();

public final Property<Boolean> advancementtab =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("AdvancementTabC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> boatpaddlestate =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("BoatPaddleStateC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> bookupdate =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("BookUpdateC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> buttonclick =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ButtonClickC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> chatmessage =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChatMessageC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clickslot =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClickSlotC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientcommand =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClientCommandC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientsettings =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClientSettingsC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientstatus =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClientStatusC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> closehandledscreen =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CloseHandledScreenC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> craftrequest =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CraftRequestC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> creativeinventoryaction =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CreativeInventoryActionC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> custompayload =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CustomPayloadC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> handswing =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("HandSwingC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> jigsawgenerating =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("JigsawGeneratingC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> keepalive =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("KeepAliveC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> pickfrominventory =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PickFromInventoryC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playeraction =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerActionC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinput =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerInputC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractblock =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerInteractBlockC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractentity =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerInteractEntityC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractitem =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerInteractItemC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playermove =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerMoveC2SPacket")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playpong =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayPongC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> queryblocknbt =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("QueryBlockNbtC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> queryentitynbt =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("QueryEntityNbtC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> recipebookdata =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("RecipeBookDataC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> recipecategoryoptions =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("RecipeCategoryOptionsC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> renameitem =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("RenameItemC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> requestcommandcompletions =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("RequestCommandCompletionsC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> resourcepackstatus =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ResourcePackStatusC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> selectmerchanttrade =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SelectMerchantTradeC2SPackett")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> spectatorteleport =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SpectatorTeleportC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> teleportconfirm =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("TeleportConfirmC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatebeacon =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateBeaconC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatecommandblock =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateCommandBlockC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatecommandblockminecart =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateCommandBlockMinecartC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatedifficulty =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateDifficultyC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatedifficultylock =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateDifficultyLockC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatejigsaw =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateJigsawC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updateplayerabilities =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdatePlayerAbilitiesC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updateselectedslot =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateSelectedSlotC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatesign =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateSignC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatestructureblock =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateStructureBlockC2SPacket")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> vehiclemove =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("VehicleMoveC2SPacket")
.value(true)
.parent(c2spackets)
.build();

//-------------------

public final Property<Boolean> s2cpackets =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("S2C")
.description("s2c player packets")
.value(true)
.build();

public final Property<Boolean> advancementupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("AdvancementUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockbreakingprogresss2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("BlockBreakingProgressS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockentityupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("BlockEntityUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockevents2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("BlockEventS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("BlockUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> bossbars2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("BossBarS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkdatas2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChunkDataS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkdeltaupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChunkDeltaUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkloaddistances2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChunkLoadDistanceS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkrenderdistancecenters2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ChunkRenderDistanceCenterS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> cleartitles2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ClearTitleS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> closescreens2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CloseScreenS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> commandsuggestionss2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CommandSuggestionsS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> commandtrees2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CommandTreeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> cooldownupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CooldownUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> craftfailedresponses2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CraftFailedResponseS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> custompayloads2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("CustomPayloadS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> deathmessages2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("DeathMessageS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> difficultys2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("DifficultyS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> disconnects2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("DisconnectS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> endcombats2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EndCombatS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entercombats2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EnterCombatS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityanimations2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityAnimationS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityattachs2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityAttachS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityattributess2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityAttributesS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitydestroys2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityDestroyS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityequipmentupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityEquipmentUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitypassengersSets2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityPassengersSetS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitypositions2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityPositionS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitys2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitySetheadyaws2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntitySetHeadYawS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityspawns2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntitySpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitystatuseffects2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityStatusEffectS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitystatuss2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityStatusS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitytrackerupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityTrackerUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityvelocityupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("EntityVelocityUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> experiencebarupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ExperienceBarUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> experienceorbspawns2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ExperienceOrbSpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> explosions2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ExplosionS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gamejoins2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("GameJoinS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gamemessages2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("GameMessageS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gamestatechanges2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("GameStateChangeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> healthupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("HealthUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> inventorys2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("InventoryS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> itempickupanimations2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ItemPickupAnimationS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> keepalives2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("KeepAliveS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> lightupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("LightUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> lookats2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("LookAtS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> mapupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("MapUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> mobspawns2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("MobSpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> nbtqueryresponses2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("NbtQueryResponseS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openhorsescreens2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("OpenHorseScreenS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openscreens2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("OpenScreenS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openwrittenbooks2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("OpenWrittenBookS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> overlaymessages2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("OverlayMessageS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> paintingspawns2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PaintingSpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> particles2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ParticleS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerabilitiess2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerAbilitiesS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playeractionresponses2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerActionResponseS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerlistheaders2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerListHeaderS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerlists2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerListS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerpositionlooks2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerPositionLookS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerrespawns2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerRespawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerspawnpositions2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerSpawnPositionS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerspawns2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayerSpawnS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playpings2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlayPingS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsoundfromentitys2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlaySoundFromEntityS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsoundids2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlaySoundIdS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsounds2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("PlaySoundS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> removeentitystatuseffects2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("RemoveEntityStatusEffectS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> resourcepacksends2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ResourcePackSendS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboarddisplays2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScoreboardDisplayS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboardobjectiveupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScoreboardObjectiveUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboardplayerupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScoreboardPlayerUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> screenhandlerpropertyupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScreenHandlerPropertyUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> screenhandlerslotupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("ScreenHandlerSlotUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> selectadvancementtabs2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SelectAdvancementTabS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> setcameraentitys2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SetCameraEntityS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> settradeofferss2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SetTradeOffersS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> signeditoropens2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SignEditorOpenS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> statisticss2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("StatisticsS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> stopsounds2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("StopSoundS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> subtitles2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SubtitleS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> synchronizerecipess2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SynchronizeRecipesS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> synchronizetagss2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("SynchronizeTagsS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> teams2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("TeamS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> titlefades2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("TitleFadeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> titles2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("TitleS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> unloadchunks2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UnloadChunkS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> unlockrecipess2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UnlockRecipesS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> updateselectedslots2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("UpdateSelectedSlotS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> vehiclemoves2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("VehicleMoveS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> vibrations2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("VibrationS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldbordercenterchangeds2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderCenterChangedS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderinitializes2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderInitializeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderinterpolatesizes2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderInterpolateSizeS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldbordersizechangeds2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderSizeChangedS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderwarningblockschangeds2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderWarningBlocksChangedS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderwarningtimechangeds2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldBorderWarningTimeChangedS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldevents2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldEventS2CPacket")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldtimeupdates2c =  Property.PropertyBuilder<Boolean>(this.getClass())
.name("WorldTimeUpdateS2CPacket")
.value(true)
.parent(s2cpackets)
.build();

@eventPointer
private final EventListener<eventPacketSent> eventPacketSenteventListener =  EventListener<>(event -> {

if(advancementtab.value()) {
 AdvancementUpdates2cPacket packet1 = (AdvancementUpdates2cPacket)  event.getPacket();
 return null;
}

if(boatpaddlestate.value()){
BoatPaddleStateC2SPacket packet2 = (BoatPaddleStateC2SPacket) event.getPacket();
return null;
}

if(bookupdate.value()){
BookUpdateC2SPacket packet3 = (BookUpdateC2SPacket) event.getPacket();
return null;
}

if(buttonclick.value()){
ButtonClickC2SPacket packet4 = (ButtonClickC2SPacket) event.getPacket();
return null;
}

if(chatmessage.value()){
ChatMessageC2SPacket packet5 = (ChatMessageC2SPacket) event.getPacket();
return null;
}

if(clickslot.value()){
ClickSlotC2SPacket packet6 = (ClickSlotC2SPacket) event.getPacket();
return null;
}

if(clientcommand.value()){
ClientCommandC2SPacket packet7 = (ClientCommandC2SPacket) event.getPacket();
return null;
}

if(clientcettings.value()){
ClientSettingsC2SPacket packet8 = (ClientSettingsC2SPacket) event.getPacket();
return null;
}

if(clientstatus.value()){
ClientStatusC2SPacket packet9 = (ClientStatusC2SPacket) event.getPacket();
return null;
}

if(closehandledscreen.value()){
CloseHandledScreenC2SPacket packet10 = (CloseHandledScreenC2SPacket) event.getPacket();
return null;
}

if(craftrequest.value()){
CraftRequestC2SPacket packet11 = (CraftRequestC2SPacket) event.getPacket();
return null;
}

if(creativeinventoryaction.value()){
CreativeInventoryActionC2SPacket packet12 = (CreativeInventoryActionC2SPacket) event.getPacket();
return null;
}

if(custompayload.value()){
CustomPayloadC2SPacket packet13 = (CustomPayloadC2SPacket) event.getPacket();
return null;
}

if(handswing.value()){
HandSwingC2SPacket packet14 = (HandSwingC2SPacket) event.getPacket();
return null;
}

if(jigsawgenerating.value()){
Jigsaws2ceneratingC2SPacket packet15 = (Jigsaws2ceneratingC2SPacket) event.getPacket();
return null;
}

if(keepalive.value()){
KeepAliveC2SPacket packet16 = (KeepAliveC2SPacket) event.getPacket();
return null;
}

if(pickfrominventory.value()){
PickFromInventoryC2SPacket packet17 = (PickFromInventoryC2SPacket) event.getPacket();
return null;
}
if(playeraction.value()){
PlayerActionC2SPacket packet18 = (PlayerActionC2SPacket) event.getPacket();
return null;
}
if(playerinput.value()){
PlayerInputC2SPacket packet19 = (PlayerInputC2SPacket) event.getPacket();
return null;
}
if(playerinteractblock.value()){
PlayerInteractBlockC2SPacket packet20 = (PlayerInteractBlockC2SPacket) event.getPacket();
return null;
}
if(playerinteractentity.value()){
PlayerInteractentityC2SPacket packet21 = (PlayerInteractentityC2SPacket) event.getPacket();
return null;
}
if(playerinteractitem.value()){
PlayerInteractItemC2SPacket packet22 = (PlayerInteractItemC2SPacket) event.getPacket();
return null;
}
if(playermove.value()){
PlayerMoveC2SPacket packet23 = (PlayerMoveC2SPacket) event.getPacket();
return null;
}
if(playpong.value()){
PlayPongC2SPacket packet24 = (PlayPongC2SPacket) event.getPacket();
return null;
}
if(queryblocknbt.value()){
QueryBlockNbtC2SPacket packet25 = (QueryBlockNbtC2SPacket) event.getPacket();
return null;
}
if(queryentitynbt.value()){
QueryentityNbtC2SPacket packet26 = (QueryentityNbtC2SPacket) event.getPacket();
return null;
}
if(recipebookdata.value()){
RecipeBookDataC2SPacket packet27 = (RecipeBookDataC2SPacket) event.getPacket();
return null;
}
if(recipecategoryoptions.value()){
RecipeCategoryOptionsC2SPacket packet28 = (RecipeCategoryOptionsC2SPacket) event.getPacket();
return null;
}
if(renameitem.value()){
RenameItemC2SPacket packet29 = (RenameItemC2SPacket) event.getPacket();
return null;
}
if(requestcommandcompletions.value()){
RequestCommandCompletionsC2SPacket packet30 = (RequestCommandCompletionsC2SPacket) event.getPacket();
return null;
}
if(resourcepackstatus.value()){
ResourcePackStatusC2SPacket packet31 = (ResourcePackStatusC2SPacket) event.getPacket();
return null;
}
if(selectmerchanttrade.value()){
SelectMerchantTradeC2SPacket packet32 = (SelectMerchantTradeC2SPacket) event.getPacket();
return null;
}
if(spectatorteleport.value()){
SpectatorTeleportC2SPacket packet33 = (SpectatorTeleportC2SPacket) event.getPacket();
return null;
}
if(teleportconfirm.value()){
TeleportConfirmC2SPacket packet34 = (TeleportConfirmC2SPacket) event.getPacket();
return null;
}
if(updatebeacon.value()){
UpdateBeaconC2SPacket packet35 = (UpdateBeaconC2SPacket) event.getPacket();
return null;
}
if(updatecommandblockminecart.value()){
UpdateCommandBlockMinecartC2SPacket packet36 = (UpdateCommandBlockMinecartC2SPacket) event.getPacket();
return null;
}
if(updatedifficulty.value()){
UpdateDifficultyC2SPacket packet37 = (UpdateDifficultyC2SPacket) event.getPacket();
return null;
}
if(updatedifficultylock.value()){
UpdateDifficultyLockC2SPacket packet38 = (UpdateDifficultyLockC2SPacket) event.getPacket();
return null;
}
if(updatejigsaw.value()){
UpdateJigsawC2SPacket packet39 = (UpdateJigsawC2SPacket) event.getPacket();

}return null;
if(updateplayerabilities.value()){
UpdatePlayerAbilitiesC2SPacket packet40 = (UpdatePlayerAbilitiesC2SPacket) event.getPacket();
return null;
}
if(updateselectedslot.value()){
UpdateSelectedSlotC2SPacket packet41 = (UpdateSelectedSlotC2SPacket) event.getPacket();
return null;
}
if(updatesign.value()){
UpdateSignC2SPacket packet42 = (UpdateSignC2SPacket)  event.getPacket();
return null;
}
if(updatestructureblock.value()){
UpdateStructureBlockC2SPacket packet43 = (UpdateStructureBlockC2SPacket) event.getPacket();
return null;
}
if(vehiclemove.value()){
VehicleMoveC2SPacket packet44 = (VehicleMoveC2SPacket)  event.getPacket();
return null;
}

//--- s2c

if(advancementupdates2c.value()){
AdvancementUpdateS2CPacket packet45 = (AdvancementUpdateS2CPacket)  event.getPacket();
return null;
}
if(blockbreakingprogresss2c.value()){
BlockBreakingProgressS2CPacket packet46 = (BlockBreakingProgressS2CPacket) event.getPacket();
return null;
}
if(blockentityupdates2c.value()){
BlockEntityUpdateS2CPacket packet47 = (BlockEntityUpdateS2CPacket) event.getPacket();
return null;
}
if(blockevents2c.value()){
BlockEventS2CPacket packet48 = (BlockEventS2CPacket) event.getPacket();
return null;
}
if(blockupdates2c.value()){
BlockUpdateS2CPacket packet49 = (BlockUpdateS2CPacket) event.getPacket();
return null;
}
if(bossbars2c.value()){
BossBarS2CPacket packet50 = (BossBarS2CPacket) event.getPacket();
return null;
}
if(chunkdatas2c.value()){
ChunkDataS2CPacket packet51 = (ChunkDataS2CPacket) event.getPacket();
return null;
}
if(chunkdeltaupdates2c.value()){
ChunkDeltaUpdateS2CPacket packet52 = (ChunkDeltaUpdateS2CPacket) event.getPacket();
return null;
}
if(chunkloaddistances2c.value()){
ChunkLoadDistanceS2CPacket packet53 = (ChunkLoadDistanceS2CPacket) event.getPacket();
return null;
}
if(chunkrenderdistancecenters2c.value()){
ChunkRenderDistanceCenterS2CPacket packet54 = (ChunkRenderDistanceCenterS2CPacket) event.getPacket();
return null;
}
if(cleartitles2c.value()){
ClearTitleS2CPacket packet55 = (ClearTitleS2CPacket) event.getPacket();
return null;
}
if(closescreens2c.value()){
CloseScreenS2CPacket packet56 = (CloseScreenS2CPacket) event.getPacket();
return null;
}
if(commandsuggestionss2c.value()){
CommandSuggestionsS2CPacket packet57 = (CommandSuggestionsS2CPacket) event.getPacket();
return null;
}
if(commandtrees2c.value()){
CommandTreeS2CPacket packet58 = (CommandTreeS2CPacket) event.getPacket();
return null;
}
if(cooldownupdates2c.value()){
CooldownUpdateS2CPacket packet59 = (CooldownUpdateS2CPacket) event.getPacket();
return null;
}
if(craftfailedresponses2c.value()){
CraftFailedResponseS2CPacket packet60 = (CraftFailedResponseS2CPacket) event.getPacket();
return null;
}
if(custompayloads2c.value()){
CustomPayloadS2CPacket packet61 = (CustomPayloadS2CPacket) event.getPacket();
return null;
}
if(deathmessages2c.value()){
DeathMessageS2CPacket packet62 = (DeathMessageS2CPacket) event.getPacket();
return null;
}
if(difficultys2c.value()){
DifficultyS2CPacket packet63 = (DifficultyS2CPacket) event.getPacket();
return null;
}
if(disconnects2c.value()){
DisconnectS2CPacket packet64 = (DisconnectS2CPacket) event.getPacket();
return null;
}
if(endcombats2c.value()){
EndCombatS2CPacket packet65 = (EndCombatS2CPacket) event.getPacket();
return null;
}
if(entercombats2c.value()){
EnterCombatS2CPacket packet66 = (EnterCombatS2CPacket) event.getPacket();
return null;
}
if(entityanimations2c.value()){
EntityAnimationS2CPacket packet67 = (EntityAnimationS2CPacket) event.getPacket();
return null;
}
if(entityattachs2c.value()){
EntityAttachS2CPacket packet68 = (EntityAttachS2CPacket) event.getPacket();
return null;
}
if(entityattributess2c.value()){
EntityAttributesS2CPacket packet69 = (EntityAttributesS2CPacket) event.getPacket();
return null;
}
if(entitydestroys2c.value()){
EntityDestroyS2CPacket packet70 = (EntityDestroyS2CPacket) event.getPacket();
return null;
}
if(entityequipmentupdates2c.value()){
EntityEquipmentUpdateS2CPacket packet71 = (EntityEquipmentUpdateS2CPacket) event.getPacket();
return null;
}
if(entitypassengerssets2c.value()){
EntityPassengersSetS2CPacket packet72 = (EntityPassengersSetS2CPacket) event.getPacket();
return null;
}
if(entitypositions2c.value()){
EntityPositionS2CPacket packet73 = (EntityPositionS2CPacket) event.getPacket();
return null;
}
if(entitys2c.value()){
EntityS2CPacket packet74 = (EntityS2CPacket) event.getPacket();
return null;
}
if(entitysetheadyaws2c.value()){
EntitySetHeadYawS2CPacket packet75 = (EntitySetHeadYawS2CPacket) event.getPacket();
return null;
}
if(entityspawns2c.value()){
EntitySpawnS2CPacket packet76 = (EntitySpawnS2CPacket) event.getPacket();
return null;
}
if(entitystatuseffects2c.value()){
EntityStatusEffectS2CPacket packet77 = (EntityStatusEffectS2CPacket) event.getPacket();
return null;
}
if(entitystatuss2c.value()){
EntityStatusS2CPacket packet78 = (EntityStatusS2CPacket) event.getPacket();
return null;
}
if(entitytrackerupdates2c.value()){
EntityTrackerUpdateS2CPacket packet79 = (EntityTrackerUpdateS2CPacket) event.getPacket();
return null;
}
if(entityvelocityupdates2c.value()){
EntityVelocityUpdateS2CPacket packet80 = (EntityVelocityUpdateS2CPacket) event.getPacket();
return null;
}
if(experiencebarupdates2c.value()){
ExperienceBarUpdateS2CPacket packet81 = (ExperienceBarUpdateS2CPacket) event.getPacket();
return null;
}
if(experienceorbspawns2c.value()){
ExperienceOrbSpawnS2CPacket packet82 = (ExperienceOrbSpawnS2CPacket) event.getPacket();
return null;
}
if(explosions2c.value()){
ExplosionS2CPacket packet83 = (ExplosionS2CPacket) event.getPacket();
return null;
}
if(gamejoins2c.value()){
GameJoinS2CPacket packet84 = (GameJoinS2CPacket) event.getPacket();
return null;
}
if(gamemessages2c.value()){
GameMessageS2CPacket packet85 = (GameMessageS2CPacket) event.getPacket();
return null;
}
if(gamestatechanges2c.value()){
GameStateChangeS2CPacket packet86 = (GameStateChangeS2CPacket) event.getPacket();
return null;
}
if(healthupdates2c.value()){
HealthUpdateS2CPacket packet87 = (HealthUpdateS2CPacket) event.getPacket();
return null;
}
if(inventorys2c.value()){
InventoryS2CPacket packet88 = (InventoryS2CPacket) event.getPacket();
return null;
}
if(itempickupanimations2c.value()){
ItemPickupAnimationS2CPacket packet89 = (ItemPickupAnimationS2CPacket) event.getPacket();
return null;
}
if(keepalives2c.value()){
KeepAliveS2CPacket packet90 = (KeepAliveS2CPacket) event.getPacket();
return null;
}
if(lightupdates2c.value()){
LightUpdateS2CPacket packet91 = (LightUpdateS2CPacket) event.getPacket();
return null;
}
if(lookats2c.value()){
LookAtS2CPacket packet92 = (LookAtS2CPacket) event.getPacket();

}
if(mapupdates2c.value()){
MapUpdateS2CPacket packet93 = (MapUpdateS2CPacket) event.getPacket();
return null;
}
if(mobspawns2c.value()){
MobSpawnS2CPacket packet94 = (MobSpawnS2CPacket) event.getPacket();
return null;
}
if(nbtqueryresponses2c.value()){
NbtQueryResponseS2CPacket packet95 = (NbtQueryResponseS2CPacket) event.getPacket();
return null;
}
if(openhorsescreens2c.value()){
OpenHorseScreenS2CPacket packet96 = (OpenHorseScreenS2CPacket) event.getPacket();
return null;
}
if(openscreens2c.value()){
OpenScreenS2CPacket packet97 = (OpenScreenS2CPacket) event.getPacket();
return null;
}
if(openwrittenbooks2c.value()){
OpenWrittenBookS2CPacket packet98 = (OpenWrittenBookS2CPacket) event.getPacket();
return null;
}
if(overlaymessages2c.value()){
OverlayMessageS2CPacket packet99 = (OverlayMessageS2CPacket) event.getPacket();
return null;
}
if(paintingspawns2c.value()){
PaintingSpawnS2CPacket packet100 = (PaintingSpawnS2CPacket) event.getPacket();
return null;
}
if(particles2c.value()){
ParticleS2CPacket packet101 = (ParticleS2CPacket) event.getPacket();
return null;
}
if(playerabilitiess2c.value()){
PlayerAbilitiesS2CPacket packet102 = (PlayerAbilitiesS2CPacket) event.getPacket();
return null;
}
if(playeractionresponses2c.value()){
PlayerActionResponseS2CPacket packet103 =(PlayerActionResponseS2CPacket) event.getPacket();
return null;
}
if(playerlistheaders2c.value()){
PlayerListHeaderS2CPacket packet104 = (PlayerListHeaderS2CPacket) event.getPacket();
return null;
}
if(playerlists2c.value()){
PlayerListS2CPacket packet105 = (PlayerListS2CPacket) event.getPacket();
return null;
}
if(playerpositionlooks2c.value()){
PlayerPositionLookS2CPacket packet106 = (PlayerPositionLookS2CPacket) event.getPacket();
return null;
}
if(playerrespawns2c.value()){
PlayerRespawnS2CPacket packet107 = (PlayerRespawnS2CPacket) event.getPacket();
return null;
}
if(playerspawnpositions2c.value()){
PlayerSpawnPositionS2CPacket packet108 = (PlayerSpawnPositionS2CPacket) event.getPacket();
return null;
}
if(playerspawns2c.value()){
PlayerSpawnS2CPacket packet109 = (PlayerSpawnS2CPacket) event.getPacket();
return null;
}
if(playpings2c.value()){
PlayPingS2CPacket packet110 = (PlayPingS2CPacket) event.getPacket();
return null;
}
if(playsoundfromentitys2c.value()) {
PlaySoundFromEntityS2CPacket packet111 = (PlaySoundFromEntityS2CPacket) event.getPacket();
return null;
}
if(playsoundids2c.value()){
PlaySoundIdS2CPacket packet112 = (PlaySoundIdS2CPacket) event.getPacket();
return null;
}
if(playsounds2c.value()){
PlaySoundS2CPacket packet113 = (PlaySoundS2CPacket) event.getPacket();
return null;
}
if(removeentitystatuseffects2c.value()){
RemoveEntityStatusEffectS2CPacket packet114 = (RemoveEntityStatusEffectS2CPacket) event.getPacket();
return null;
}
if(resourcepacksends2c.value()){
ResourcePackSendS2CPacket packet115 = (ResourcePackSendS2CPacket) event.getPacket();
return null;
}
if(scoreboarddisplays2c.value()){
ScoreboardDisplayS2CPacket packet116 = (ScoreboardDisplayS2CPacket) event.getPacket();
return null;
}
if(scoreboardobjectiveupdates2c.value()){
ScoreboardObjectiveUpdateS2CPacket packet117 = (ScoreboardObjectiveUpdateS2CPacket) event.getPacket();
return null;
}
if(scoreboardplayerupdates2c.value()){
ScoreboardPlayerUpdateS2CPacket packet118 = (ScoreboardPlayerUpdateS2CPacket) event.getPacket();
return null;
}
if(screenhandlerpropertyupdates2c.value()){
ScreenHandlerPropertyUpdateS2CPacket packet119 = (ScreenHandlerPropertyUpdateS2CPacket) event.getPacket();
return null;
}
if(screenhandlerslotupdates2c.value()){
ScreenHandlerSlotUpdateS2CPacket packet120 = (ScreenHandlerSlotUpdateS2CPacket) event.getPacket();
return null;
}
if(selectadvancementtabs2c.value()){
SelectAdvancementTabS2CPacket packet121 = (SelectAdvancementTabS2CPacket) event.getPacket();
return null;
}
if(setcameraentitys2c.value()){
SetCameraEntityS2CPacket packet122 = (SetCameraEntityS2CPacket) event.getPacket();
return null;
}
if(settradeofferss2c.value()){
SetTradeOffersS2CPacket packet123 = (SetTradeOffersS2CPacket) event.getPacket();
return null;
}
if(signeditoropens2c.value()){
SignEditorOpenS2CPacket packet124 = (SignEditorOpenS2CPacket) event.getPacket();
return null;
}
if(statisticss2c.value()){
StatisticsS2CPacket packet125 = (StatisticsS2CPacket) event.getPacket();
return null;
}
if(stopsounds2c.value()){
StopSoundS2CPacket packet126 = (StopSoundS2CPacket) event.getPacket();
return null;
}
if(subtitles2c.value()){
SubtitleS2CPacket packet127 = (SubtitleS2CPacket) event.getPacket();
return null;
}
if(synchronizerecipess2c.value()){
SynchronizeRecipesS2CPacket packet128 =  (SynchronizeRecipesS2CPacket) event.getPacket();
return null;
}
if(synchronizetagss2c.value()){
SynchronizeTagsS2CPacket packet129 = (SynchronizeTagsS2CPacket) event.getPacket();
return null;
}
if(teams2c.value()){
TeamS2CPacket packet130 = (TeamS2CPacket) event.getPacket();
return null;
}
if(titlefades2c.value()){
TitleFadeS2CPacket packet131 = (TitleFadeS2CPacket) event.getPacket();
return null;
}
if(titles2c.value()){
TitleS2CPacket packet132 = (TitleS2CPacket) event.getPacket();
return null;
}
if(unloadchunks2c.value()){
UnloadChunkS2CPacket packet133 = (UnloadChunkS2CPacket) event.getPacket();
return null;
}
if(unlockrecipess2c.value()){
UnlockRecipesS2CPacket packet134 = (UnlockRecipesS2CPacket) event.getPacket();
return null;
}
if(updateselectedslots2c.value()){
UpdateSelectedSlotS2CPacket packet135 = (UpdateSelectedSlotS2CPacket) event.getPacket();
return null;
}
if(vehiclemoves2c.value()){
VehicleMoveS2CPacket packet136 = (VehicleMoveS2CPacket) event.getPacket();
return null;
}
if(wibrations2c.value()){
VibrationS2CPacket packet137 = (VibrationS2CPacket) event.getPacket();
return null;
}
if(worldbordercenterchangeds2c.value()){
WorldBorderCenterChangedS2CPacket packet138 = (WorldBorderCenterChangedS2CPacket) event.getPacket();
return null;
}
if(worldborderinitializes2c.value()){
WorldBorderInitializeS2CPacket packet139 = (WorldBorderInitializeS2CPacket) event.getPacket();
return null;
}
if(worldborderinterpolatesizes2c.value()){
WorldBorderInterpolateSizeS2CPacket packet140 = (WorldBorderInterpolateSizeS2CPacket) event.getPacket();
return null;
}
if(worldbordersizechangeds2c.value()){
WorldBorderSizeChangedS2CPacket packet141 = (WorldBorderSizeChangedS2CPacket) event.getPacket();
return null;
}
if(worldborderwarningblockschangeds2c.value()){
WorldBorderWarningBlocksChangedS2CPacket packet142 = (WorldBorderWarningBlocksChangedS2CPacket) event.getPacket();
return null;
}
if(worldborderwarningtimechangeds2c.value()){
WorldBorderWarningTimeChangedS2CPacket packet143 = (WorldBorderWarningTimeChangedS2CPacket) event.getPacket();
return null;
}
if(worldevents2c.value()){
WorldEventS2CPacket packet144 = (WorldEventS2CPacket) event.getPacket();
return null;
}
if(worldtimeupdates2c.value()){
WorldTimeUpdateS2CPacket packet145 = (WorldTimeUpdateS2CPacket) event.getPacket();
return null;
}
});
}
