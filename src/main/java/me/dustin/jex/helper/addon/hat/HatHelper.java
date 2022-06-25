package me.dustin.jex.helper.addon.hat;

import java.util.ArrayList;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public enum HatHelper {
    INSTANCE;
    public static final ArrayList<HatInfo> hatPlayers = new ArrayList<>();

    public Block topHat;
    public Block halo;
    public Block propeller;
    public Block crown;
    public Block cowboyHat;

    public void setHat(String uuid, String hat) {
        if (!hasHat(uuid)) {
            hatPlayers.add(new HatInfo(uuid, getHatType(hat)));
        } else {
            hatPlayers.remove(getInfo(uuid));
            hatPlayers.add(new HatInfo(uuid, getHatType(hat)));
        }
    }

    public void clearHat(String uuid) {
        hatPlayers.remove(getInfo(uuid));
    }

    public HatType getHatType(String type) {
        for (HatType hat : HatType.values()) {
            if (hat.name().equalsIgnoreCase(type))
                return hat;
        }
        return HatType.TOP_HAT;
    }

    public ItemStack getHat(PlayerEntity playerEntity) {
        HatInfo info = getInfo(playerEntity);
        if (info == null)
            return new ItemStack(Items.AIR);
        return switch (info.type) {
            case TOP_HAT -> new ItemStack(topHat);
            case HALO -> new ItemStack(halo);
            case PROPELLER -> new ItemStack(propeller);
            case CROWN -> new ItemStack(crown);
            case COWBOY_HAT -> new ItemStack(cowboyHat);
            default -> new ItemStack(Items.AIR);
        };
    }


    public HatType getType(PlayerEntity playerEntity) {
        HatInfo info = getInfo(playerEntity);
        return info.type;
    }

    public boolean hasHat(PlayerEntity playerEntity) {
        for (HatInfo hatInfo : hatPlayers) {
            if (hatInfo.uuid.equalsIgnoreCase(playerEntity.getUuid().toString().replace("-", "")))
                return true;
        }
        return false;
    }

    public boolean hasHat(String uuid) {
        for (HatInfo hatInfo : hatPlayers) {
            if (hatInfo.uuid.equalsIgnoreCase(uuid))
                return true;
        }
        return false;
    }

    public HatInfo getInfo(PlayerEntity playerEntity) {
        for (HatInfo info : hatPlayers) {
            if (info.uuid.equalsIgnoreCase(playerEntity.getUuid().toString().replace("-", "")))
                return info;
        }
        return null;
    }

    public HatInfo getInfo(String uuid) {
        for (HatInfo info : hatPlayers) {
            if (info.uuid.equalsIgnoreCase(uuid))
                return info;
        }
        return null;
    }

    private Block register(String string_1, Block block_1) {
        return Registry.register(Registry.BLOCK, new Identifier(string_1), block_1);
    }

    private Item register(Block block_1, Item item_1) {
        return register(Registry.BLOCK.getId(block_1), item_1);
    }

    private Item register(Identifier identifier_1, Item item_1) {
        if (item_1 instanceof BlockItem) {
            ((BlockItem) item_1).appendBlocks(Item.BLOCK_ITEMS, item_1);
        }

        return (Item) Registry.register(Registry.ITEM, identifier_1, item_1);
    }

    private Item register(BlockItem blockItem_1) {
        return register(blockItem_1.getBlock(), blockItem_1);
    }

    public void load() {
        topHat = register("jex:top_hat", new Block(AbstractBlock.Settings.of(Material.STONE)));
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