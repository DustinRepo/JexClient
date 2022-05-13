package me.dustin.jex.helper.player.bot;

import com.google.common.primitives.Longs;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.response.KeyPairResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.dustin.events.EventManager;
import me.dustin.events.core.EventListener;
import me.dustin.events.core.annotate.EventPointer;
import me.dustin.jex.JexClient;
import me.dustin.jex.event.filters.TickFilter;
import me.dustin.jex.event.misc.EventTick;
import me.dustin.jex.helper.file.JsonHelper;
import me.dustin.jex.helper.misc.ChatHelper;
import me.dustin.jex.helper.misc.Wrapper;
import me.dustin.jex.helper.network.ProxyHelper;
import me.dustin.jex.helper.network.WebHelper;
import me.dustin.jex.helper.world.WorldHelper;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.resolver.ResolvedServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.resolver.ServerNameResolver;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.handshake.ClientIntentionPacket;
import net.minecraft.network.protocol.login.ServerboundHelloPacket;
import net.minecraft.util.Crypt;
import net.minecraft.util.CryptException;
import net.minecraft.util.LazyLoadedValue;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfileKeyPair;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

public class PlayerBot {
    private static final ArrayList<PlayerBot> playerBots = new ArrayList<>();
    private final GameProfile gameProfile;
    private final User session;
    public static ClientPacketListener savedNetworkHandler;
    public static BotClientConnection currentConnection;
    private BotClientConnection clientConnection;
    private Player player;
    private Inventory playerInventory;
    private boolean connected;

    private boolean using, attacking;
    private int useDelay, attackDelay;
    private int countedTicks;

    private ProfileKeyPair keyPair;
    private ClientLevel world;

    public PlayerBot(GameProfile gameProfile, User session) {
        this.gameProfile = gameProfile;
        this.session = session;
        try {
            keyPair = generateKeyPair();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventManager.register(this);
    }

    public void connect(ServerAddress serverAddress) {
        Thread loginThread = new Thread(() -> {
            try {
                Optional<InetSocketAddress> optional = ServerNameResolver.DEFAULT.resolveAddress(serverAddress).map(ResolvedServerAddress::asInetSocketAddress);
                if (optional.isEmpty()) {
                    ChatHelper.INSTANCE.addClientMessage("Error logging in player while grabbing IP");
                    disconnect();
                    return;
                }
                savedNetworkHandler = Wrapper.INSTANCE.getMinecraft().getConnection();
                this.clientConnection = currentConnection = connect(optional.get());
                clientConnection.setListener(new BotLoginNetworkHandler(clientConnection, Wrapper.INSTANCE.getMinecraft(), null, this::log, gameProfile, this));
                clientConnection.send(new ClientIntentionPacket(serverAddress.getHost(), serverAddress.getPort(), ConnectionProtocol.LOGIN));
                clientConnection.send(new ServerboundHelloPacket(gameProfile.getName(), keyPair == null ? Optional.empty() : Optional.ofNullable(keyPair.publicKey().data())));
            } catch (Exception e) {
                ChatHelper.INSTANCE.addClientMessage("Error logging in player");
                e.printStackTrace();
                disconnect();
            }
        });
        loginThread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(JexClient.INSTANCE.getLogger()));
        loginThread.start();
    }

    private ProfileKeyPair generateKeyPair() throws CryptException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Content-Length", "0");
        headers.put("Authorization", "Bearer " + getSession().getAccessToken());
        WebHelper.HttpResponse httpResponse = WebHelper.INSTANCE.httpRequest("https://api.minecraftservices.com/player/certificates", null, headers, "POST");

        KeyPairResponse keyPairResponse = JsonHelper.INSTANCE.gson.fromJson(httpResponse.data(), KeyPairResponse.class);
        PublicKey publicKey = Crypt.stringToRsaPublicKey(keyPairResponse.getPublicKey());
        byte[] keySig = Base64.getDecoder().decode(keyPairResponse.getPublicKeySignature());
        return new ProfileKeyPair(Crypt.stringToPemRsaPrivateKey(keyPairResponse.getPrivateKey()), new ProfilePublicKey(new ProfilePublicKey.Data(Instant.parse(keyPairResponse.getExpiresAt()), publicKey, keySig)), Instant.parse(keyPairResponse.getRefreshedAfter()));
    }

    public void disconnect() {
        if (clientConnection != null && clientConnection.isConnected()) {
            this.clientConnection.handleDisconnection();
            this.clientConnection.close();
            this.clientConnection = null;
        }
        setConnected(false);
        EventManager.unregister(this);
    }

    @EventPointer
    private final EventListener<EventTick> eventTickEventListener = new EventListener<>(event -> {
        if (this.clientConnection != null) {
            if (this.clientConnection.isConnected()) {
                this.clientConnection.tick();
            } else {
                player = null;
                playerInventory = null;
                disconnect();
            }
        }
        if (Wrapper.INSTANCE.getWorld() == null) {
            player = null;
            playerInventory = null;
            disconnect();
            return;
        }
        if (isUsing()) {
            if (useDelay == 0 || countedTicks % useDelay == 0) {
                use();
            }
        }
        if (isAttacking()) {
            if ((attackDelay == -1 && player.getAttackStrengthScale(0) == 1) || attackDelay == 0 || countedTicks % attackDelay == 0) {
                attack();
            }
        }
        if (player != null && !(player instanceof LocalPlayer)) {
            if (player.getX() != player.xo || player.getY() != player.yo || player.getZ() != player.zo)
                this.clientConnection.send(new ServerboundMovePlayerPacket.Pos(player.getX(), player.getY(), player.getZ(), player.isOnGround()));
            if (player.getYRot() != player.yRotO || player.getXRot() != player.xRotO)
                this.clientConnection.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.isOnGround()));
            player.tick();
            player.setDeltaMovement(0, player.getDeltaMovement().y(), 0);
            if (!player.isOnGround() && !player.isInWater() && !player.isInLava()) {
                player.setDeltaMovement(0, player.getDeltaMovement().y() - 0.06499, 0);
            }
            player.move(MoverType.PLAYER, player.getDeltaMovement());
        }
        countedTicks++;
    }, new TickFilter(EventTick.Mode.PRE));

    public void log(Component text) {
        JexClient.INSTANCE.getLogger().info(text.getString());
        ChatHelper.INSTANCE.addClientMessage(text.getString());
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void sendMessage(String chat) {
        Instant instant = Instant.now();
        MessageSignature chatSigData = new MessageSignature(UUID.fromString(getSession().getUuid()), instant, sigForMessage(instant, chat));
        this.clientConnection.send(new ServerboundChatPacket(chat, chatSigData, false));
    }

    private Crypt.SaltSignaturePair sigForMessage(Instant instant, String string) {
        try {
            Signature signature = getSignature();
            if (signature != null) {
                long l = Crypt.SaltSupplier.getLong();
                updateSig(signature, l, UUID.fromString(session.getUuid()), instant, string);
                return new Crypt.SaltSignaturePair(l, signature.sign());
            }
        } catch (GeneralSecurityException var6) {
            JexClient.INSTANCE.getLogger().error("Failed to sign chat message {}", instant, var6);
        }

        return Crypt.SaltSignaturePair.EMPTY;
    }

    private static void updateSig(Signature signature, long l, UUID uUID, Instant instant, String string) throws SignatureException {
        signature.update(Longs.toByteArray(l));
        signature.update(uuidToBytes(uUID.getMostSignificantBits(), uUID.getLeastSignificantBits()));
        signature.update(Longs.toByteArray(instant.getEpochSecond()));
        signature.update(string.getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] uuidToBytes(long l, long m) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        byteBuffer.putLong(l).putLong(m);
        return byteBuffer.array();
    }

    public Signature getSignature() throws GeneralSecurityException {
        if (keyPair == null)
            return null;
        PrivateKey privateKey = keyPair.privateKey();
        if (privateKey == null) {
            return null;
        } else {
            Signature signature = Signature.getInstance("SHA1withRSA");
            signature.initSign(privateKey);
            return signature;
        }
    }

    public void drop(boolean all) {
        ServerboundPlayerActionPacket.Action action = all ? ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS : ServerboundPlayerActionPacket.Action.DROP_ITEM;
        if (getPlayerInventory() != null)
            this.getPlayerInventory().removeFromSelected(all);
        this.clientConnection.send(new ServerboundPlayerActionPacket(action, BlockPos.ZERO, Direction.DOWN));
    }

    public void dropInventory() {
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            ItemStack stack = playerInventory.getItem(i);
            playerInventory.removeItemNoUpdate(i);
            Int2ObjectOpenHashMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<ItemStack>();
            getClientConnection().send(new ServerboundContainerClickPacket(Wrapper.INSTANCE.getLocalPlayer().containerMenu.containerId, Wrapper.INSTANCE.getLocalPlayer().containerMenu.getStateId(), i, 1, ClickType.THROW, stack, int2ObjectMap));
        }
    }

    public void use() {
        Entity crosshair = getCrosshairEntity(Wrapper.INSTANCE.getMultiPlayerGameMode().getPickRange());
        if (crosshair != null) {
            clientConnection.send(ServerboundInteractPacket.createInteractionPacket(crosshair, player.isShiftKeyDown(), InteractionHand.MAIN_HAND));
            clientConnection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        } else if (raycast(Wrapper.INSTANCE.getMultiPlayerGameMode().getPickRange(), 1, false) instanceof BlockHitResult blockHitResult && WorldHelper.INSTANCE.getBlockState(blockHitResult.getBlockPos()).getShape(player.getLevel(), blockHitResult.getBlockPos()) != Shapes.empty()) {
            Wrapper.INSTANCE.getMultiPlayerGameMode().useItemOn(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, blockHitResult);
            if (WorldHelper.INSTANCE.canUseOnPos(blockHitResult.getBlockPos()))
                clientConnection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        } else
            Wrapper.INSTANCE.getMultiPlayerGameMode().useItem(Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND);
    }

    public void attack() {
        Entity crosshair = getCrosshairEntity(Wrapper.INSTANCE.getMultiPlayerGameMode().getPickRange());
        if (crosshair != null) {
            player.resetAttackStrengthTicker();
            clientConnection.send(ServerboundInteractPacket.createAttackPacket(crosshair, player.isShiftKeyDown()));
            clientConnection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        } else if (raycast(Wrapper.INSTANCE.getMultiPlayerGameMode().getPickRange(), 1, false) instanceof BlockHitResult blockHitResult && world.getBlockState(blockHitResult.getBlockPos()).getShape(player.getLevel(), blockHitResult.getBlockPos()) != Shapes.empty()) {
            clientConnection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK, blockHitResult.getBlockPos(), blockHitResult.getDirection()));
            clientConnection.send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockHitResult.getBlockPos(), blockHitResult.getDirection()));
            clientConnection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
        } else {
            clientConnection.send(new ServerboundSwingPacket(InteractionHand.MAIN_HAND));
            player.resetAttackStrengthTicker();
        }
    }

    public boolean canUseOnPos(BlockPos pos) {
        return world.getBlockState(pos).use(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false)) != InteractionResult.PASS;
    }

    public HitResult raycast(double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3 vec3d = getCameraPosVec();
        Vec3 vec3d2 = getRotationVector(player.getXRot(), player.getYRot());
        Vec3 vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
        return world.clip(new ClipContext(vec3d, vec3d3, ClipContext.Block.OUTLINE, includeFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, player));
    }

    protected final Vec3 getRotationVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);
        return new Vec3(i * j, -k, h * j);
    }

    public final Vec3 getCameraPosVec() {
        if (player == null)
            return Vec3.ZERO;
        return new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
    }

    public Entity getCrosshairEntity(float reach) {
        if (player != null) {
            if (world != null) {
                Vec3 vec3d = getCameraPosVec();
                Vec3 vec3d2 = getRotationVector(player.getXRot(), player.getYRot());
                Vec3 vec3d3 = vec3d.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);

                AABB box = player.getBoundingBox().expandTowards(vec3d2.scale(reach)).inflate(1.0D, 1.0D, 1.0D);
                EntityHitResult entityHitResult = raycast(player, vec3d, vec3d3, box, (entityx) -> !entityx.isSpectator() && entityx.isPickable(), reach);
                if (entityHitResult != null) {
                    Entity entity2 = entityHitResult.getEntity();
                    if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrame) {
                        return entity2;
                    }
                }
            }
        }
        return null;
    }

    public EntityHitResult raycast(Entity entity, Vec3 min, Vec3 max, AABB box, Predicate<Entity> predicate, double d) {
        double e = d;
        Entity entity2 = null;
        Vec3 vec3d = null;
        for (Entity entity3 : world.getEntities(entity, box, predicate)) {
            Vec3 vec3d2;
            double f;
            AABB box2 = entity3.getBoundingBox().inflate(entity3.getPickRadius());
            Optional<Vec3> optional = box2.clip(min, max);
            if (box2.contains(min)) {
                if (!(e >= 0.0)) continue;
                entity2 = entity3;
                vec3d = optional.orElse(min);
                e = 0.0;
                continue;
            }
            if (optional.isEmpty() || !((f = min.distanceToSqr(vec3d2 = optional.get())) < e) && e != 0.0) continue;
            if (entity3.getRootVehicle() == entity.getRootVehicle()) {
                if (e != 0.0) continue;
                entity2 = entity3;
                vec3d = vec3d2;
                continue;
            }
            entity2 = entity3;
            vec3d = vec3d2;
            e = f;
        }
        if (entity2 == null) {
            return null;
        }
        return new EntityHitResult(entity2, vec3d);
    }

    public void setRotation(Vec3 vec) {
        if (player != null) {
            this.player.yRotO = this.player.getYRot();
            this.player.xRotO = this.player.getXRot();
            this.player.setYRot((float) vec.x);
            this.player.setXRot((float) vec.y);
        }
        this.clientConnection.send(new ServerboundMovePlayerPacket.Rot((float)vec.x, (float)vec.y, player == null || player.isOnGround()));
    }

    private BotClientConnection connect(InetSocketAddress address) {
        final BotClientConnection clientConnection = new BotClientConnection(PacketFlow.CLIENTBOUND);
        Class<? extends Channel> class2;
        LazyLoadedValue<?> lazy2;
        if (Epoll.isAvailable() && Wrapper.INSTANCE.getOptions().useNativeTransport()) {
            class2 = EpollSocketChannel.class;
            lazy2 = Connection.NETWORK_EPOLL_WORKER_GROUP;
        } else {
            class2 = NioSocketChannel.class;
            lazy2 = Connection.NETWORK_WORKER_GROUP;
        }
        ProxyHelper.INSTANCE.clientConnection = clientConnection;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap = bootstrap.group((EventLoopGroup)lazy2.get());
        bootstrap = bootstrap.handler(ProxyHelper.INSTANCE.channelInitializer);
        bootstrap = bootstrap.channel(class2);
        bootstrap.connect(address.getAddress(), address.getPort());
        return clientConnection;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public static ArrayList<PlayerBot> getPlayerBots() {
        return playerBots;
    }

    public Inventory getPlayerInventory() {
        return playerInventory;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(RemotePlayer player) {
        this.player = player;
    }

    public void setPlayerInventory(Inventory playerInventory) {
        this.playerInventory = playerInventory;
    }

    public boolean isUsing() {
        return using;
    }

    public void setUsing(boolean using) {
        this.using = using;
    }

    public boolean isAttacking() {
        return attacking;
    }

    public void setAttacking(boolean attacking) {
        this.attacking = attacking;
    }

    public int getUseDelay() {
        return useDelay;
    }

    public void setUseDelay(int useDelay) {
        this.useDelay = useDelay;
    }

    public int getAttackDelay() {
        return attackDelay;
    }

    public void setAttackDelay(int attackDelay) {
        this.attackDelay = attackDelay;
    }

    public BotClientConnection getClientConnection() {
        return clientConnection;
    }

    public User getSession() {
        return session;
    }

    public ClientLevel getWorld() {
        return world;
    }

    public void setWorld(ClientLevel world) {
        this.world = world;
    }

    public ProfileKeyPair getKeyPair() {
        return keyPair;
    }

    public static PlayerBot getBot(String name) {
        for (int i = 0; i < PlayerBot.getPlayerBots().size(); i++) {
            PlayerBot playerBot = PlayerBot.getPlayerBots().get(i);
            if (playerBot.clientConnection == null || !playerBot.isConnected())
                PlayerBot.getPlayerBots().remove(i);
        }
        for (PlayerBot playerBot : PlayerBot.getPlayerBots()) {
            if (playerBot.getGameProfile().getName().equalsIgnoreCase(name)) {
                return playerBot;
            }
        }
        return null;
    }
}