package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.shop;

import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.SChestShopGUIPlugin;
import com.google.common.collect.Maps;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShopSetting {

    private final UUID ownerId;
    private String itemId;
    private Integer priceBuy;
    private Integer priceSell;

    private final static Map<UUID, ShopSetting> PLAYER_CACHES = Maps.newHashMap();

    public ShopSetting(UUID ownerId, @Nullable String itemId, Integer priceBuy, Integer priceSell) {
        this.ownerId = ownerId;
        this.itemId = itemId;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
    }

    public static ShopSetting createEmpty(UUID ownerId) {
        return new ShopSetting(ownerId, null, null, null);
    }


    public UUID getOwnerId() {
        return ownerId;
    }

    public String getItemId() {
        return itemId;
    }

    public Integer getPriceBuy() {
        return priceBuy;
    }

    public Integer getPriceSell() {
        return priceSell;
    }

    public Optional<ShopSetting> getPrevious() {
        return Optional.ofNullable(PLAYER_CACHES.get(ownerId));
    }

    public void setPrevious() {
        PLAYER_CACHES.put(ownerId, this);
    }

    public void removePrevious() {
        PLAYER_CACHES.remove(ownerId, this);
    }

    public void saveToSign(Sign sign) {
        PersistentDataContainer data = sign.getPersistentDataContainer();
        SChestShopGUIPlugin plugin = JavaPlugin.getPlugin(SChestShopGUIPlugin.class);
        data.set(new NamespacedKey(plugin, "owner"), PersistentDataType.STRING, ownerId.toString());
        if (itemId != null) {
            data.set(new NamespacedKey(plugin, "item"), PersistentDataType.STRING, itemId);
        } else {
            data.remove(new NamespacedKey(plugin, "item"));
        }
        if (priceBuy != null) {
            data.set(new NamespacedKey(plugin, "pricebuy"), PersistentDataType.INTEGER, priceBuy);
        } else {
            data.remove(new NamespacedKey(plugin, "pricebuy"));
        }
        if (priceSell != null) {
            data.set(new NamespacedKey(plugin, "pricesell"), PersistentDataType.INTEGER, priceSell);
        } else {
            data.remove(new NamespacedKey(plugin, "pricesell"));
        }
        sign.update(true);
    }

    public static ShopSetting createFromSign(Sign sign) throws IllegalArgumentException {
        PersistentDataContainer data = sign.getPersistentDataContainer();
        SChestShopGUIPlugin plugin = JavaPlugin.getPlugin(SChestShopGUIPlugin.class);

        UUID owner = UUID.fromString(data.getOrDefault(new NamespacedKey(plugin, "owner"), PersistentDataType.STRING, ""));
        String itemId = data.get(new NamespacedKey(plugin, "item"), PersistentDataType.STRING);
        Integer priceBuy = data.get(new NamespacedKey(plugin, "pricebuy"), PersistentDataType.INTEGER);
        Integer priceSell = data.get(new NamespacedKey(plugin, "pricesell"), PersistentDataType.INTEGER);

        return new ShopSetting(owner, itemId, priceBuy, priceSell);
    }


    public static Map<UUID, ShopSetting> getPlayerPrevious() {
        return Collections.unmodifiableMap(PLAYER_CACHES);
    }

    public static Optional<ShopSetting> getPlayerPrevious(UUID ownerId) {
        return Optional.ofNullable(PLAYER_CACHES.get(ownerId));
    }

}
