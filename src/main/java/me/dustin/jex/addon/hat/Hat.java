package me.dustin.jex.addon.hat;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;

public class Hat {

    public static ArrayList<HatInfo> hatPlayers = new ArrayList<>();

    public static Block topHat;
    public static Block halo;
    public static Block propeller;
    public static Block crown;
    public static Block cowboyHat;

    public static void setHat(String uuid, String hat) {
        if (!Hat.hasHat(uuid)) {
            Hat.hatPlayers.add(new HatInfo(uuid, getHatType(hat)));
        } else {
            Hat.hatPlayers.remove(getInfo(uuid));
            Hat.hatPlayers.add(new HatInfo(uuid, getHatType(hat)));
        }
    }

    public static HatType getHatType(String type) {
        for (HatType hat : HatType.values()) {
            if (hat.name().equalsIgnoreCase(type))
                return hat;
        }
        return HatType.TOP_HAT;
    }

    public static ItemStack getHat(PlayerEntity playerEntity) {
        HatInfo info = getInfo(playerEntity);
        if (info == null)
            return new ItemStack(Items.AIR);
        switch (info.type) {
            case TOP_HAT:
                return new ItemStack(topHat);
            case HALO:
                return new ItemStack(halo);
            case PROPELLER:
                return new ItemStack(propeller);
            case CROWN:
                return new ItemStack(crown);
            case COWBOY_HAT:
                return new ItemStack(cowboyHat);
        }
        return new ItemStack(topHat);
    }

    public static HatType getType(PlayerEntity playerEntity) {
        HatInfo info = getInfo(playerEntity);
        return info.type;
    }

    public static boolean hasHat(PlayerEntity playerEntity) {
        for (HatInfo hatInfo : hatPlayers) {
            if (hatInfo.uuid.equalsIgnoreCase(playerEntity.getUuid().toString().replace("-", "")))
                return true;
        }
        return false;
    }

    public static boolean hasHat(String uuid) {
        for (HatInfo hatInfo : hatPlayers) {
            if (hatInfo.uuid.equalsIgnoreCase(uuid))
                return true;
        }
        return false;
    }

    public static HatInfo getInfo(PlayerEntity playerEntity) {
        for (HatInfo info : hatPlayers) {
            if (info.uuid.equalsIgnoreCase(playerEntity.getUuid().toString().replace("-", "")))
                return info;
        }
        return null;
    }

    public static HatInfo getInfo(String uuid) {
        for (HatInfo info : hatPlayers) {
            if (info.uuid.equalsIgnoreCase(uuid))
                return info;
        }
        return null;
    }

    private static Block register(String string_1, Block block_1) {
        return Registry.register(Registry.BLOCK, new Identifier(string_1), block_1);
    }

    protected static Item register(Block block_1, Item item_1) {
        return register(Registry.BLOCK.getId(block_1), item_1);
    }

    private static Item register(Identifier identifier_1, Item item_1) {
        if (item_1 instanceof BlockItem) {
            ((BlockItem) item_1).appendBlocks(Item.BLOCK_ITEMS, item_1);
        }

        return (Item) Registry.register(Registry.ITEM, identifier_1, item_1);
    }

    private static Item register(BlockItem blockItem_1) {
        return register((Block) blockItem_1.getBlock(), (Item) blockItem_1);
    }

    public void load() {
        topHat = register("jex:top_hat", new Block(Block.Settings.of(Material.STONE)));
        register(new BlockItem(topHat, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));

        halo = register("jex:halo", new Block(Block.Settings.of(Material.STONE)));
        register(new BlockItem(halo, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));

        propeller = register("jex:propeller_hat", new Block(Block.Settings.of(Material.STONE)));
        register(new BlockItem(propeller, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));

        crown = register("jex:crown", new Block(Block.Settings.of(Material.STONE)));
        register(new BlockItem(crown, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));

        cowboyHat = register("jex:cowboy_hat", new Block(Block.Settings.of(Material.STONE)));
        register(new BlockItem(cowboyHat, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));
    }

    public enum HatType {
        TOP_HAT, HALO, PROPELLER, CROWN, COWBOY_HAT
    }

    public static class HatInfo {
        public String uuid;
        public HatType type;

        public HatInfo(String uuid, HatType type) {
            this.uuid = uuid;
            this.type = type;
        }
    }
}
