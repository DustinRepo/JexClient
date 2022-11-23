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
import net.minecraft.client.network.Address;
import net.minecraft.client.network.AllowedAddressResolver;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.util.Session;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.encryption.NetworkEncryptionException;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerKeyPair;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Lazy;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.RaycastContext;
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
    private final Session session;
    public static ClientPlayNetworkHandler savedNetworkHandler;
    public static BotClientConnection currentConnection;
    private BotClientConnection clientConnection;
    private PlayerEntity player;
    private PlayerInventory playerInventory;
    private boolean connected;

    private boolean using, attacking;
    private int useDelay, attackDelay;
    private int countedTicks;

    private PlayerKeyPair keyPair;
    private ClientWorld world;

    public PlayerBot(GameProfile gameProfile, Session session) {
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
                Optional<InetSocketAddress> optional = AllowedAddressResolver.DEFAULT.resolve(serverAddress).map(Address::getInetSocketAddress);
                if (optional.isEmpty()) {
                    ChatHelper.INSTANCE.addClientMessage("Error logging in player while grabbing IP");
                    disconnect();
                    return;
                }
                savedNetworkHandler = Wrapper.INSTANCE.getMinecraft().getNetworkHandler();
                this.clientConnection = currentConnection = connect(optional.get());
                clientConnection.setPacketListener(new BotLoginNetworkHandler(clientConnection, Wrapper.INSTANCE.getMinecraft(), null, this::log, gameProfile, this));
                clientConnection.send(new HandshakeC2SPacket(serverAddress.getAddress(), serverAddress.getPort(), NetworkState.LOGIN));
                clientConnection.send(new LoginHelloC2SPacket(gameProfile.getName(), keyPair == null ? Optional.empty() : Optional.ofNullable(keyPair.publicKey().data()), Optional.ofNullable(this.session.getUuidOrNull())));
            } catch (Exception e) {
                ChatHelper.INSTANCE.addClientMessage("Error logging in player");
                e.printStackTrace();
                disconnect();
            }
        });
        loginThread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(JexClient.INSTANCE.getLogger()));
        loginThread.start();
    }

    private PlayerKeyPair generateKeyPair() throws NetworkEncryptionException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Content-Length", "0");
        headers.put("Authorization", "Bearer " + getSession().getAccessToken());
        WebHelper.HttpResponse httpResponse = WebHelper.INSTANCE.httpRequest("https://api.minecraftservices.com/player/certificates", null, headers, "POST");

        KeyPairResponse keyPairResponse = JsonHelper.INSTANCE.gson.fromJson(httpResponse.data(), KeyPairResponse.class);
        PublicKey publicKey = NetworkEncryptionUtils.decodeRsaPublicKeyPem(keyPairResponse.getPublicKey());
        ByteBuffer keySig = Base64.getDecoder().decode(keyPairResponse.getPublicKeySignature());
        return new PlayerKeyPair(NetworkEncryptionUtils.decodeRsaPrivateKeyPem(keyPairResponse.getPrivateKey()), new PlayerPublicKey(new PlayerPublicKey.PublicKeyData(Instant.parse(keyPairResponse.getExpiresAt()), publicKey, keySig.array())), Instant.parse(keyPairResponse.getRefreshedAfter()));
    }

    public void disconnect() {
        if (clientConnection != null && clientConnection.isOpen()) {
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
            if (this.clientConnection.isOpen()) {
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
            if ((attackDelay == -1 && player.getAttackCooldownProgress(0) == 1) || attackDelay == 0 || countedTicks % attackDelay == 0) {
                attack();
            }
        }
        if (player != null && !(player instanceof ClientPlayerEntity)) {
            if (player.getX() != player.prevX || player.getY() != player.prevY || player.getZ() != player.prevZ)
                this.clientConnection.send(new PlayerMoveC2SPacket.PositionAndOnGround(player.getX(), player.getY(), player.getZ(), player.isOnGround()));
            if (player.getYaw() != player.prevYaw || player.getPitch() != player.prevPitch)
                this.clientConnection.send(new PlayerMoveC2SPacket.LookAndOnGround(player.getYaw(), player.getPitch(), player.isOnGround()));
            player.tick();
            player.setVelocity(0, player.getVelocity().getY(), 0);
            if (!player.isOnGround() && !player.isTouchingWater() && !player.isInLava()) {
                player.setVelocity(0, player.getVelocity().getY() - 0.06499, 0);
            }
            player.move(MovementType.PLAYER, player.getVelocity());
        }
        countedTicks++;
    }, new TickFilter(EventTick.Mode.PRE));

    public void log(Text text) {
        JexClient.INSTANCE.getLogger().info(text.getString());
        ChatHelper.INSTANCE.addClientMessage(text.getString());
    }

    public GameProfile getGameProfile() {
        return gameProfile;
    }

    public void sendMessage(String chat) {
        //TODO: fix this
        /*Instant instant = Instant.now();
        MessageSignatureData chatSigData = new MessageSignatureData(UUID.fromString(getSession().getUuid()), instant, sigForMessage(instant, chat));
        this.clientConnection.send(new ChatMessageC2SPacket(chat, chatSigData, false));*/
    }

    private NetworkEncryptionUtils.SignatureData sigForMessage(Instant instant, String string) {
        try {
            Signature signature = getSignature();
            if (signature != null) {
                long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
                updateSig(signature, l, UUID.fromString(session.getUuid()), instant, string);
                return new NetworkEncryptionUtils.SignatureData(l, signature.sign());
            }
        } catch (GeneralSecurityException var6) {
            JexClient.INSTANCE.getLogger().error("Failed to sign chat message {}", instant, var6);
        }

        return NetworkEncryptionUtils.SignatureData.NONE;
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
        PlayerActionC2SPacket.Action action = all ? PlayerActionC2SPacket.Action.DROP_ALL_ITEMS : PlayerActionC2SPacket.Action.DROP_ITEM;
        if (getPlayerInventory() != null)
            this.getPlayerInventory().dropSelectedItem(all);
        this.clientConnection.send(new PlayerActionC2SPacket(action, BlockPos.ORIGIN, Direction.DOWN));
    }

    public void dropInventory() {
        for (int i = 0; i < playerInventory.size(); i++) {
            ItemStack stack = playerInventory.getStack(i);
            playerInventory.removeStack(i);
            Int2ObjectOpenHashMap<ItemStack> int2ObjectMap = new Int2ObjectOpenHashMap<ItemStack>();
            getClientConnection().send(new ClickSlotC2SPacket(Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.syncId, Wrapper.INSTANCE.getLocalPlayer().currentScreenHandler.getRevision(), i, 1, SlotActionType.THROW, stack, int2ObjectMap));
        }
    }

    public void use() {
        Entity crosshair = getCrosshairEntity(Wrapper.INSTANCE.getClientPlayerInteractionManager().getReachDistance());
        if (crosshair != null) {
            clientConnection.send(PlayerInteractEntityC2SPacket.interact(crosshair, player.isSneaking(), Hand.MAIN_HAND));
            clientConnection.send(new HandSwingC2SPacket(Hand.MAIN_HAND));
        } else if (raycast(Wrapper.INSTANCE.getClientPlayerInteractionManager().getReachDistance(), 1, false) instanceof BlockHitResult blockHitResult && WorldHelper.INSTANCE.getBlockState(blockHitResult.getBlockPos()).getOutlineShape(player.getWorld(), blockHitResult.getBlockPos()) != VoxelShapes.empty()) {
            Wrapper.INSTANCE.getClientPlayerInteractionManager().interactBlock(Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, blockHitResult);
            if (WorldHelper.INSTANCE.canUseOnPos(blockHitResult.getBlockPos()))
                clientConnection.send(new HandSwingC2SPacket(Hand.MAIN_HAND));
        } else
            Wrapper.INSTANCE.getClientPlayerInteractionManager().interactItem(Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND);
    }

    public void attack() {
        Entity crosshair = getCrosshairEntity(Wrapper.INSTANCE.getClientPlayerInteractionManager().getReachDistance());
        if (crosshair != null) {
            player.resetLastAttackedTicks();
            clientConnection.send(PlayerInteractEntityC2SPacket.attack(crosshair, player.isSneaking()));
            clientConnection.send(new HandSwingC2SPacket(Hand.MAIN_HAND));
        } else if (raycast(Wrapper.INSTANCE.getClientPlayerInteractionManager().getReachDistance(), 1, false) instanceof BlockHitResult blockHitResult && world.getBlockState(blockHitResult.getBlockPos()).getOutlineShape(player.getWorld(), blockHitResult.getBlockPos()) != VoxelShapes.empty()) {
            clientConnection.send(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, blockHitResult.getBlockPos(), blockHitResult.getSide()));
            clientConnection.send(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockHitResult.getBlockPos(), blockHitResult.getSide()));
            clientConnection.send(new HandSwingC2SPacket(Hand.MAIN_HAND));
        } else {
            clientConnection.send(new HandSwingC2SPacket(Hand.MAIN_HAND));
            player.resetLastAttackedTicks();
        }
    }

    public boolean canUseOnPos(BlockPos pos) {
        return world.getBlockState(pos).onUse(Wrapper.INSTANCE.getWorld(), Wrapper.INSTANCE.getLocalPlayer(), Hand.MAIN_HAND, new BlockHitResult(Vec3d.ZERO, Direction.UP, BlockPos.ORIGIN, false)) != ActionResult.PASS;
    }

    public HitResult raycast(double maxDistance, float tickDelta, boolean includeFluids) {
        Vec3d vec3d = getCameraPosVec();
        Vec3d vec3d2 = getRotationVector(player.getPitch(), player.getYaw());
        Vec3d vec3d3 = vec3d.add(vec3d2.x * maxDistance, vec3d2.y * maxDistance, vec3d2.z * maxDistance);
        return world.raycast(new RaycastContext(vec3d, vec3d3, RaycastContext.ShapeType.OUTLINE, includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE, player));
    }

    protected final Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * ((float)Math.PI / 180);
        float g = -yaw * ((float)Math.PI / 180);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    public final Vec3d getCameraPosVec() {
        if (player == null)
            return Vec3d.ZERO;
        return new Vec3d(player.getX(), player.getY() + player.getStandingEyeHeight(), player.getZ());
    }

    public Entity getCrosshairEntity(float reach) {
        if (player != null) {
            if (world != null) {
                Vec3d vec3d = getCameraPosVec();
                Vec3d vec3d2 = getRotationVector(player.getPitch(), player.getYaw());
                Vec3d vec3d3 = vec3d.add(vec3d2.x * reach, vec3d2.y * reach, vec3d2.z * reach);

                Box box = player.getBoundingBox().stretch(vec3d2.multiply(reach)).expand(1.0D, 1.0D, 1.0D);
                EntityHitResult entityHitResult = raycast(player, vec3d, vec3d3, box, (entityx) -> !entityx.isSpectator() && entityx.canHit(), reach);
                if (entityHitResult != null) {
                    Entity entity2 = entityHitResult.getEntity();
                    if (entity2 instanceof LivingEntity || entity2 instanceof ItemFrameEntity) {
                        return entity2;
                    }
                }
            }
        }
        return null;
    }

    public EntityHitResult raycast(Entity entity, Vec3d min, Vec3d max, Box box, Predicate<Entity> predicate, double d) {
        double e = d;
        Entity entity2 = null;
        Vec3d vec3d = null;
        for (Entity entity3 : world.getOtherEntities(entity, box, predicate)) {
            Vec3d vec3d2;
            double f;
            Box box2 = entity3.getBoundingBox().expand(entity3.getTargetingMargin());
            Optional<Vec3d> optional = box2.raycast(min, max);
            if (box2.contains(min)) {
                if (!(e >= 0.0)) continue;
                entity2 = entity3;
                vec3d = optional.orElse(min);
                e = 0.0;
                continue;
            }
            if (optional.isEmpty() || !((f = min.squaredDistanceTo(vec3d2 = optional.get())) < e) && e != 0.0) continue;
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

    public void setRotation(Vec3d vec) {
        if (player != null) {
            this.player.prevYaw = this.player.getYaw();
            this.player.prevPitch = this.player.getPitch();
            this.player.setYaw((float) vec.x);
            this.player.setPitch((float) vec.y);
        }
        this.clientConnection.send(new PlayerMoveC2SPacket.LookAndOnGround((float)vec.x, (float)vec.y, player == null || player.isOnGround()));
    }

    private BotClientConnection connect(InetSocketAddress address) {
        final BotClientConnection clientConnection = new BotClientConnection(NetworkSide.CLIENTBOUND);
        Class<? extends Channel> class2;
        Lazy<?> lazy2;
        if (Epoll.isAvailable() && Wrapper.INSTANCE.getOptions().shouldUseNativeTransport()) {
            class2 = EpollSocketChannel.class;
            lazy2 = ClientConnection.EPOLL_CLIENT_IO_GROUP;
        } else {
            class2 = NioSocketChannel.class;
            lazy2 = ClientConnection.CLIENT_IO_GROUP;
        }
        ProxyHelper.INSTANCE.clientConnection = clientConnection;
        Bootstrap bootstrap = new Bootstrap();
        bootstrap = bootstrap.group((EventLoopGroup)lazy2.get());
        bootstrap = bootstrap.handler(ProxyHelper.INSTANCE.initChannel);
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

    public PlayerInventory getPlayerInventory() {
        return playerInventory;
    }

    public PlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(OtherClientPlayerEntity player) {
        this.player = player;
    }

    public void setPlayerInventory(PlayerInventory playerInventory) {
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

    public Session getSession() {
        return session;
    }

    public ClientWorld getWorld() {
        return world;
    }

    public void setWorld(ClientWorld world) {
        this.world = world;
    }

    public PlayerKeyPair getKeyPair() {
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
