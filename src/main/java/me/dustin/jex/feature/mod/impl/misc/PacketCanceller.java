package me.dustin.jex.feature.mod.impl.misc;

import me.dustin.jex.feature.mod.core.Category;
import me.dustin.jex.feature.mod.core.Feature;
import me.dustin.events.core.annotate.EventPointer;
import net.minecraft.network.packet.c2s.*;
import net.minecraft.network.packet.s2c.*;

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
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> boatpaddlestate = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> bookupdate = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> buttonclick = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> chatmessage = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clickslot = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientcommand = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientsettings = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> clientstatus = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> closehandledscreen = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> craftrequest = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> creativeinventoryaction = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> custompayload = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> handswing = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> jigsawgenerating = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> keepalive = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> pickfrominventory = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playeraction = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinput = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractblock = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractentity = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playerinteractitem = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playermove = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

public final Property<Boolean> playpong = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> queryblocknbt = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> queryentitynbt = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> recipebookdata = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> recipecategoryoptions = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> renameitem = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> requestcommandcompletions = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> resourcepackstatus = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> selectmerchanttrade = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> spectatorteleport = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> teleportconfirm = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatebeacon = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatecommandblock = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatecommandblockminecart = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatedifficulty = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatedifficultylock = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatejigsaw = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updateplayerabilities = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updateselectedslot = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatesign = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> updatestructureblock = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();
public final Property<Boolean> vehiclemove = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(c2spackets)
.build();

//-------------------

public final Property<Boolean> s2cpackets = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("s2c")
.description("s2c player packets")
.value(true)
.build();

public final Property<Boolean> advancementupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockbreakingprogresss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockentityupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockevents2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> blockupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> bossbars2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkdatas2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkdeltaupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkloaddistances2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> chunkrenderdistancecenters2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> cleartitles2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> closescreens2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> commandsuggestionss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> commandtrees2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> cooldownupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> craftfailedresponses2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> custompayloads2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> deathmessages2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> difficultys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> disconnects2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> endcombats2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entercombats2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityanimations2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityattachs2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityattributess2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitydestroys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityequipmentupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitypassengersSets2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitypositions2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitySetheadyaws2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitystatuseffects2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityStatuss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entitytrackerupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> entityvelocityupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> experiencebarupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> experienceorbSpawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> explosions2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gameJoins2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gamemessages2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> gamestatechanges2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> healthupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> inventorys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> itempickupanimations2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> keepalives2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> lightupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> lookats2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> mapupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> mobspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> nbtqueryresponses2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openhorsescreens2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openscreens2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> openwrittenbooks2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> overlaymessages2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> paintingspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> particles2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerabilitiess2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playeractionresponses2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerlistheaders2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerlists2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerpositionlooks2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerrespawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerspawnpositions2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playerspawns2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playpings2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsoundfromentitys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsoundids2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> playsounds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> removeentitystatuseffects2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> resourcepacksends2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboarddisplays2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboardobjectiveupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> scoreboardplayerupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> screenhandlerpropertyupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> screenhandlerslotupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> selectadvancementtabs2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> setcameraentitys2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> settradeofferss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> signeditoropens2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> statisticss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> stopsounds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> subtitles2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> synchronizerecipess2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> synchronizetagss2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> teams2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> titlefades2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> titles2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> unloadchunks2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> unlockrecipess2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> updateselectedslots2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> vehiclemoves2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> vibrations2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldbordercenterchangeds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderinitializes2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderinterpolatesizes2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldbordersizechangeds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderwarningblockschangeds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldborderwarningtimechangeds2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldevents2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();
public final Property<Boolean> worldtimeupdates2c = new Property.PropertyBuilder<Boolean>(this.getClass())
.name("")
.value(true)
.parent(s2cpackets)
.build();

@eventPointer
private final eventListener<eventPacketSent> eventPacketSenteventListener = new eventListener<>(event -> {

if(advancementtab.value()) {
 AdvancementUpdates2cPacket packet1 = new AdvancementUpdates2cPacket();
 event.cancel(packet1);
}

if(boatpaddlestate.value()){
BoatPaddleStateC2SPacket packet2 = new BoatPaddleStateC2SPacket()
event.cancel(packet2);
}

if(bookupdate.value()){
BookUpdateC2SPacket packet3 = new BookUpdateC2SPacket();
event.cancel(packe3);
}

if(buttonclick.value()){
ButtonClickC2SPacket packet4 = new ButtonClickC2SPacket()
event.cancel(packet4);
}

if(chatmessage.value()){
ChatMessageC2SPacket packet5 = new ChatMessageC2SPacket()
event.cancel(packet5);
}

if(clickslot.value()){
ClickSlotC2SPacket packet6 = new ClickSlotC2SPacket()
event.cancel(packet6);
}

if(clientcommand.value()){
ClientCommandC2SPacket packet7 = new ClientCommandC2SPacket()
event.cancel(packet7);
}

if(clientcettings.value()){
ClientSettingsC2SPacket packet8 = new ClientSettingsC2SPacket()
event.cancel(packet8);
}

if(clientstatus.value()){
ClientStatusC2SPacket packet9 = new ClientStatusC2SPacket()
event.cancel(packet9);
}

if(closehandledscreen.value()){
CloseHandledScreenC2SPacket packet10 = new CloseHandledScreenC2SPacket()
event.cancel(packet10);
}

if(craftrequest.value()){
CraftRequestC2SPacket packet11 = new CraftRequestC2SPacket()
event.cancel(packet11);
}

if(creativeinventoryaction.value()){
CreativeInventoryActionC2SPacket packet12 = new CreativeInventoryActionC2SPacket()
event.cancel(packet12);
}

if(custompayload.value()){
CustomPayloadC2SPacket packet13 = new CustomPayloadC2SPacket()
event.cancel(packet13);
}

if(handswing.value()){
HandSwingC2SPacket packet14 = new HandSwingC2SPacket()
event.cancel(packet14);
}

if(jigsawgenerating.value()){
Jigsaws2ceneratingC2SPacket packet15 = new Jigsaws2ceneratingC2SPacket()
event.cancel(packet15);
}

if(keepalive.value()){
KeepAliveC2SPacket packet16 = new KeepAliveC2SPacket()
event.cancel(packet16);
}

if(pickfrominventory.value()){
PickFromInventoryC2SPacket packet17 = new PickFromInventoryC2SPacket()
event.cancel(packet17);
}
if(playeraction.value()){
PlayerActionC2SPacket packet18 = new PlayerActionC2SPacket()
event.cancel(packet18);
}
if(playerinput.value()){
PlayerInputC2SPacket packet19 = new PlayerInputC2SPacket()
event.cancel(packet19);
}
if(playerinteractblock.value()){
PlayerInteractBlockC2SPacket packet20 = new PlayerInteractBlockC2SPacket()
event.cancel(packet120);
}
if(playerinteractentity.value()){
PlayerInteractentityC2SPacket packet21 = new PlayerInteractentityC2SPacket()
event.cancel(packet21);
}
if(playerinteractitem.value()){
PlayerInteractItemC2SPacket packet22 = new PlayerInteractItemC2SPacket()
event.cancel(packet22);
}
if(playermove.value()){
PlayerMoveC2SPacket packet23 = new PlayerMoveC2SPacket()
event.cancel(packet23);
}
if(playpong.value()){
PlayPongC2SPacket packet24 = new PlayPongC2SPacket()
event.cancel(packet24);
}
if(queryblocknbt.value()){
QueryBlockNbtC2SPacket packet25 = new QueryBlockNbtC2SPacket()
event.cancel(packet25);
}
if(queryentitynbt.value()){
QueryentityNbtC2SPacket packet26 = new QueryentityNbtC2SPacket()
event.cancel(packet26);
}
if(recipebookdata.value()){
RecipeBookDataC2SPacket packet27 = new RecipeBookDataC2SPacket()
event.cancel(packet27);
}
if(recipecategoryoptions.value()){
RecipeCategoryOptionsC2SPacket packet28 = new RecipeCategoryOptionsC2SPacket()
event.cancel(packet28);
}
if(renameitem.value()){
RenameItemC2SPacket packet29 = new RenameItemC2SPacket()
event.cancel(packet29);
}
if(requestcommandcompletions.value()){
RequestCommandCompletionsC2SPacket packet30 = new RequestCommandCompletionsC2SPacket()
event.cancel(packet30);
}
if(resourcepackstatus.value()){
ResourcePackStatusC2SPacket packet31 = new ResourcePackStatusC2SPacket()
event.cancel(packet31);
}
if(selectmerchanttrade.value()){
SelectMerchantTradeC2SPacket packet32 = new SelectMerchantTradeC2SPacket()
event.cancel(packet32);
}
if(spectatorteleport.value()){
SpectatorTeleportC2SPacket packet33 = new SpectatorTeleportC2SPacket()
event.cancel(packet33);
}
if(teleportconfirm.value()){
TeleportConfirmC2SPacket packet34 = new TeleportConfirmC2SPacket()
event.cancel(packet34);
}
if(updatebeacon.value()){
UpdateBeaconC2SPacket packet35 = new UpdateBeaconC2SPacket()
event.cancel(packet35);
}
if(updatecommandblockminecart.value()){
UpdateCommandBlockMinecartC2SPacket packet36 = new UpdateCommandBlockMinecartC2SPacket()
event.cancel(packet36);
}
if(updatedifficulty.value()){
UpdateDifficultyC2SPacket packet37 = new UpdateDifficultyC2SPacket()
event.cancel(packet37);
}
if(updatedifficultylock.value()){
UpdateDifficultyLockC2SPacket packet38 = new UpdateDifficultyLockC2SPacket()
event.cancel(packet38);
}
if(updatejigsaw.value()){
UpdateJigsawC2SPacket packet39 = new UpdateJigsawC2SPacket()
event.cancel(packet39);
}
if(updateplayerabilities.value()){
UpdatePlayerAbilitiesC2SPacket packet40 = new UpdatePlayerAbilitiesC2SPacket()
event.cancel(packet40);
}
if(updateselectedslot.value()){
UpdateSelectedSlotC2SPacket packet41 = new UpdateSelectedSlotC2SPacket()
event.cancel(packet41);
}
if(updatesign.value()){
UpdateSignC2SPacket packet42 = new UpdateSignC2SPacket()
event.cancel(packet42);
}
if(updatestructureblock.value()){
UpdateStructureBlockC2SPacket packet43 = newUpdateStructureBlockC2SPacket()
event.cancel(packet43);
}
if(vehiclemove.value()){
VehicleMoveC2SPacket packet44 = new VehicleMoveC2SPacket()
event.cancel(packet44);
}

//--- s2c

if(advancementupdates2c.value()){
AdvancementUpdateS2CPacket packet45 = new AdvancementUpdateS2CPacket();
event.cancel(packet45);
}
if(blockbreakingprogresss2c.value()){
BlockBreakingProgressS2CPacket packet46 = new BlockBreakingProgressS2CPacket();
event.cancel(packet46);
}
if(blockentityupdates2c.value()){
BlockEntityUpdateS2CPacket packet47 = new BlockEntityUpdateS2CPacket();
event.cancel(packet47);
}
if(blockevents2c.value()){
BlockEventS2CPacket packet48 = new BlockEventS2CPacket();
event.cancel(packet48);
}
if(blockupdates2c.value()){
BlockUpdateS2CPacket packet49 = new BlockUpdateS2CPacket();
event.cancel(packet49);
}
if(bossbars2c.value()){
BossBarS2CPacket packet50 = new BossBarS2CPacket();
event.cancel(packet50);
}
if(chunkdatas2c.value()){
ChunkDataS2CPacket packet51 = new ChunkDataS2CPacket();
event.cancel(packet51);
}
if(chunkdeltaupdates2c.value()){
ChunkDeltaUpdateS2CPacket packet52 = new ChunkDeltaUpdateS2CPacket();
event.cancel(packet52);
}
if(chunkloaddistances2c.value()){
ChunkLoadDistanceS2CPacket packet53 = new ChunkLoadDistanceS2CPacket();
event.cancel(packet53);
}
if(chunkrenderdistancecenters2c.value()){
ChunkRenderDistanceCenterS2CPacket packet54 = new ChunkRenderDistanceCenterS2CPacket();
event.cancel(packet54);
}
if(cleartitles2c.value()){
ClearTitleS2CPacket packet55 = new ClearTitleS2CPacket();
event.cancel(packet55);
}
if(closescreens2c.value()){
CloseScreenS2CPacket packet56 = new CloseScreenS2CPacket();
event.cancel(packet56);
}
if(commandsuggestionss2c.value()){
CommandSuggestionsS2CPacket packet57 = new CommandSuggestionsS2CPacket();
event.cancel(packet57);
}
if(commandtrees2c.value()){
CommandTreeS2CPacket packet58 = new CommandTreeS2CPacket();
event.cancel(packet58);
}
if(cooldownupdates2c.value()){
CooldownUpdateS2CPacket packet59 = new CooldownUpdateS2CPacket();
event.cancel(packet59);
}
if(craftfailedresponses2c.value()){
CraftFailedResponseS2CPacket packet60 = new CraftFailedResponseS2CPacket();
event.cancel(packet60);
}
if(custompayloads2c.value()){
CustomPayloadS2CPacket packet61 = new CustomPayloadS2CPacket();
event.cancel(packet61);
}
if(deathmessages2c.value()){
DeathMessageS2CPacket packet62 = new DeathMessageS2CPacket();
event.cancel(packet62);
}
if(difficultys2c.value()){
DifficultyS2CPacket packet63 = new DifficultyS2CPacket();
event.cancel(packet63);
}
if(disconnects2c.value()){
DisconnectS2CPacket packet64 = new DisconnectS2CPacket();
event.cancel(packet64);
}
if(endcombats2c.value()){
EndCombatS2CPacket packet65 = new EndCombatS2CPacket();
event.cancel(packet65);
}
if(entercombats2c.value()){
EnterCombatS2CPacket packet66 = new EnterCombatS2CPacket();
event.cancel(packet66);
}
if(entityanimations2c.value()){
EntityAnimationS2CPacket packet67 = new EntityAnimationS2CPacket();
event.cancel(packet67);
}
if(entityattachs2c.value()){
EntityAttachS2CPacket packet68 = new EntityAttachS2CPacket();
event.cancel(packet68);
}
if(entityattributess2c.value()){
EntityAttributesS2CPacket packet69 = new EntityAttributesS2CPacket();
event.cancel(packet69);
}
if(entitydestroys2c.value()){
EntityDestroyS2CPacket packet70 = new EntityDestroyS2CPacket();
event.cancel(packet70);
}
if(entityequipmentupdates2c.value()){
EntityEquipmentUpdateS2CPacket packet71 = new EntityEquipmentUpdateS2CPacket();
event.cancel(packet71);
}
if(entitypassengerssets2c.value()){
EntityPassengersSetS2CPacket packet72 = new EntityPassengersSetS2CPacket();
event.cancel(packet72);
}
if(entitypositions2c.value()){
EntityPositionS2CPacket packet73 = new EntityPositionS2CPacket();
event.cancel(packet73);
}
if(entitys2c.value()){
EntityS2CPacket packet74 = new EntityS2CPacket();
event.cancel(packet74);
}
if(entitysetheadyaws2c.value()){
EntitySetHeadYawS2CPacket packet75 = new EntitySetHeadYawS2CPacket();
event.cancel(packet75);
}
if(entityspawns2c.value()){
EntitySpawnS2CPacket packet76 = new EntitySpawnS2CPacket();
event.cancel(packet76);
}
if(entitystatuseffects2c.value()){
EntityStatusEffectS2CPacket packet77 = new EntityStatusEffectS2CPacket();
event.cancel(packet77);
}
if(entitystatuss2c.value()){
EntityStatusS2CPacket packet78 = new EntityStatusS2CPacket();
event.cancel(packet78);
}
if(entitytrackerupdates2c.value()){
EntityTrackerUpdateS2CPacket packet79 = new EntityTrackerUpdateS2CPacket();
event.cancel(packet79);
}
if(entityvelocityupdates2c.value()){
EntityVelocityUpdateS2CPacket packet80 = new EntityVelocityUpdateS2CPacket();
event.cancel(packet80);
}
if(experiencebarupdates2c.value()){
ExperienceBarUpdateS2CPacket packet81 = new ExperienceBarUpdateS2CPacket();
event.cancel(packet81);
}
if(experienceorbspawns2c.value()){
ExperienceOrbSpawnS2CPacket packet82 = new ExperienceOrbSpawnS2CPacket();
event.cancel(packet82);
}
if(explosions2c.value()){
ExplosionS2CPacket packet83 = new ExplosionS2CPacket();
event.cancel(packet83);
}
if(gamejoins2c.value()){
GameJoinS2CPacket packet84 = new GameJoinS2CPacket();
event.cancel(packet84);
}
if(gamemessages2c.value()){
GameMessageS2CPacket packet85 = new GameMessageS2CPacket();
event.cancel(packet85);
}
if(gamestatechanges2c.value()){
GameStateChangeS2CPacket packet86 = new GameStateChangeS2CPacket();
event.cancel(packet86);
}
if(healthupdates2c.value()){
HealthUpdateS2CPacket packet87 = new HealthUpdateS2CPacket();
event.cancel(packet87);
}
if(inventorys2c.value()){
InventoryS2CPacket packet88 = new InventoryS2CPacket();
event.cancel(packet88);
}
if(itempickupanimations2c.value()){
ItemPickupAnimationS2CPacket packet89 = new ItemPickupAnimationS2CPacket();
event.cancel(packet89);
}
if(keepalives2c.value()){
KeepAliveS2CPacket packet90 = new KeepAliveS2CPacket();
event.cancel(packet90);
}
if(lightupdates2c.value()){
LightUpdateS2CPacket packet91 = new LightUpdateS2CPacket();
event.cancel(packet91);
}
if(lookats2c.value()){
LookAtS2CPacket packet92 = new LookAtS2CPacket();
event.cancel(packet92);
}
if(mapupdates2c.value()){
MapUpdateS2CPacket packet93 = new MapUpdateS2CPacket();
event.cancel(packet93);
}
if(mobspawns2c.value()){
MobSpawnS2CPacket packet94 = new MobSpawnS2CPacket();
event.cancel(packet94);
}
if(nbtqueryresponses2c.value()){
NbtQueryResponseS2CPacket packet95 = new NbtQueryResponseS2CPacket();
event.cancel(packet95);
}
if(openhorsescreens2c.value()){
OpenHorseScreenS2CPacket packet96 = new OpenHorseScreenS2CPacket();
event.cancel(packet96);
}
if(openscreens2c.value()){
OpenScreenS2CPacket packet97 = new OpenScreenS2CPacket();
event.cancel(packet97);
}
if(openwrittenbooks2c.value()){
OpenWrittenBookS2CPacket packet98 = new OpenWrittenBookS2CPacket();
event.cancel(packet98);
}
if(overlaymessages2c.value()){
OverlayMessageS2CPacket packet99 = new OverlayMessageS2CPacket();
event.cancel(packet99);
}
if(paintingspawns2c.value()){
PaintingSpawnS2CPacket packet100 = new PaintingSpawnS2CPacket();
event.cancel(packet100);
}
if(particles2c.value()){
ParticleS2CPacket packet101 = new ParticleS2CPacket();
event.cancel(packet101);
}
if(playerabilitiess2c.value()){
PlayerAbilitiesS2CPacket packet102 = new PlayerAbilitiesS2CPacket();
event.cancel(packet102);
}
if(playeractionresponses2c.value()){
PlayerActionResponseS2CPacket packet103 = PlayerActionResponseS2CPacket();
event.cancel(packet103);
}
if(playerlistheaders2c.value()){
PlayerListHeaderS2CPacket packet104 = new PlayerListHeaderS2CPacket();
event.cancel(packet104);
}
if(playerlists2c.value()){
PlayerListS2CPacket packet105 = new PlayerListS2CPacket();
event.cancel(packet105);
}
if(playerpositionlooks2c.value()){
PlayerPositionLookS2CPacket packet106 = new PlayerPositionLookS2CPacket();
event.cancel(packet106);
}
if(playerrespawns2c.value()){
PlayerRespawnS2CPacket packet107 = new PlayerRespawnS2CPacket();
event.cancel(packet107);
}
if(playerspawnpositions2c.value()){
PlayerSpawnPositionS2CPacket packet108 = new PlayerSpawnPositionS2CPacket();
event.cancel(packet108);
}
if(playerspawns2c.value()){
PlayerSpawnS2CPacket packet109 = new PlayerSpawnS2CPacket();
event.cancel(packet109);
}
if(playpings2c.value()){
PlayPingS2CPacket packet110 = new PlayPingS2CPacket();
event.cancel(packet110);
}
if(playsoundfromentitys2c.value()) {
PlaySoundFromEntityS2CPacket packet111 = new PlaySoundFromEntityS2CPacket();
event.cancel(packet111);
}
if(playsoundids2c.value()){
PlaySoundIdS2CPacket packet112 = new PlaySoundIdS2CPacket();
event.cancel(packet112);
}
if(playsounds2c.value()){
PlaySoundS2CPacket packet113 = new PlaySoundS2CPacket();
event.cancel(packet113);
}
if(removeentitystatuseffects2c.value()){
RemoveEntityStatusEffectS2CPacket packet114 = new RemoveEntityStatusEffectS2CPacket();
event.cancel(packet114);
}
if(resourcepacksends2c.value()){
ResourcePackSendS2CPacket packet115 = new ResourcePackSendS2CPacket();
event.cancel(packet115);
}
if(scoreboarddisplays2c.value()){
ScoreboardDisplayS2CPacket packet116 = new ScoreboardDisplayS2CPacket();
event.cancel(packet116);
}
if(scoreboardobjectiveupdates2c.value()){
ScoreboardObjectiveUpdateS2CPacket packet117 = new ScoreboardObjectiveUpdateS2CPacket();
event.cancel(packet117);
}
if(scoreboardplayerupdates2c.value()){
ScoreboardPlayerUpdateS2CPacket packet118 = new ScoreboardPlayerUpdateS2CPacket();
event.cancel(packet118);
}
if(screenhandlerpropertyupdates2c.value()){
ScreenHandlerPropertyUpdateS2CPacket packet119 = new ScreenHandlerPropertyUpdateS2CPacket();
event.cancel(packet119);
}
if(screenhandlerslotupdates2c.value()){
ScreenHandlerSlotUpdateS2CPacket packet120 = new ScreenHandlerSlotUpdateS2CPacket();
event.cancel(packet120);
}
if(selectadvancementtabs2c.value()){
SelectAdvancementTabS2CPacket packet121 = new SelectAdvancementTabS2CPacket();
event.cancel(packet121);
}
if(setcameraentitys2c.value()){
SetCameraEntityS2CPacket packet122 = new SetCameraEntityS2CPacket();
event.cancel(packet122);
}
if(settradeofferss2c.value()){
SetTradeOffersS2CPacket packet123 = new SetTradeOffersS2CPacket();
event.cancel(packet123);
}
if(signeditoropens2c.value()){
SignEditorOpenS2CPacket packet124 = new SignEditorOpenS2CPacket();
event.cancel(packet124);
}
if(statisticss2c.value()){
StatisticsS2CPacket packet125 = new StatisticsS2CPacket();
event.cancel(packet125);
}
if(stopsounds2c.value()){
StopSoundS2CPacket packet126 = new StopSoundS2CPacket();
event.cancel(packet126);
}
if(subtitles2c.value()){
SubtitleS2CPacket packet127 = new SubtitleS2CPacket();
event.cancel(packet127);
}
if(synchronizerecipess2c.value()){
SynchronizeRecipesS2CPacket packet128 = new SynchronizeRecipesS2CPacket();
event.cancel(packet128);
}
if(synchronizetagss2c.value()){
SynchronizeTagsS2CPacket packet129 = new SynchronizeTagsS2CPacket();
event.cancel(packet129);
}
if(teams2c.value()){
TeamS2CPacket packet130 = new TeamS2CPacket();
event.cancel(packet130);
}
if(titlefades2c.value()){
TitleFadeS2CPacket packet131 = new TitleFadeS2CPacket();
event.cancel(packet131);
}
if(titles2c.value()){
TitleS2CPacket packet132 = new TitleS2CPacket();
event.cancel(packet132);
}
if(unloadchunks2c.value()){
UnloadChunkS2CPacket packet133 = new UnloadChunkS2CPacket();
event.cancel(packet133);
}
if(unlockrecipess2c.value()){
UnlockRecipesS2CPacket packet134 = new UnlockRecipesS2CPacket();
event.cancel(packet134);
}
if(updateselectedslots2c.value()){
UpdateSelectedSlotS2CPacket packet135 = new UpdateSelectedSlotS2CPacket();
event.cancel(packet135);
}
if(vehiclemoves2c.value()){
VehicleMoveS2CPacket packet136 = new VehicleMoveS2CPacket();
event.cancel(packet136);
}
if(wibrations2c.value()){
VibrationS2CPacket packet137 = new VibrationS2CPacket();
event.cancel(packet137);
}
if(worldbordercenterchangeds2c.value()){
WorldBorderCenterChangedS2CPacket packet138 = new WorldBorderCenterChangedS2CPacket();
event.cancel(packet138);
}
if(worldborderinitializes2c.value()){
WorldBorderInitializeS2CPacket packet139 = new WorldBorderInitializeS2CPacket();
event.cancel(packet139);
}
if(worldborderinterpolatesizes2c.value()){
WorldBorderInterpolateSizeS2CPacket packet140 = new WorldBorderInterpolateSizeS2CPacket();
event.cancel(packet140);
}
if(worldbordersizechangeds2c.value()){
WorldBorderSizeChangedS2CPacket packet141 = new WorldBorderSizeChangedS2CPacket();
event.cancel(packet141);
}
if(worldborderwarningblockschangeds2c.value()){
WorldBorderWarningBlocksChangedS2CPacket packet142 = new WorldBorderWarningBlocksChangedS2CPacket();
event.cancel(packet142);
}
if(worldborderwarningtimechangeds2c.value()){
WorldBorderWarningTimeChangedS2CPacket packet143 = new WorldBorderWarningTimeChangedS2CPacket();
event.cancel(packet143);
}
if(worldevents2c.value()){
WorldEventS2CPacket packet144 = new WorldEventS2CPacket();
event.cancel(packet144);
}
if(worldtimeupdates2c.value()){
WorldTimeUpdateS2CPacket packet145 = new WorldTimeUpdateS2CPacket();
event.cancel(packet145);
}
});
}