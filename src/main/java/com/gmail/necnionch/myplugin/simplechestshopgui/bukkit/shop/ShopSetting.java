package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.shop;

import com.Acrobot.Breeze.Utils.PriceUtil;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.SChestShopGUIPlugin;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ShopSetting {

    private UUID ownerId;
    private String itemId;
    private int amount;
    private Integer priceBuy;
    private Integer priceSell;
    private boolean adminShop;

    private final static Map<UUID, ShopSetting> PLAYER_CACHES = Maps.newHashMap();

    public ShopSetting(UUID ownerId, @Nullable String itemId, int amount, Integer priceBuy, Integer priceSell, boolean adminShop) {
        this.ownerId = ownerId;
        this.itemId = itemId;
        this.amount = amount;
        this.priceBuy = priceBuy;
        this.priceSell = priceSell;
        this.adminShop = adminShop;
    }

    public static ShopSetting createEmpty(UUID ownerId) {
        return new ShopSetting(ownerId, null, 1, 100, null, false);
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

    public int getAmount() {
        return amount;
    }

    public boolean isAdminShop() {
        return adminShop;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public void setPriceBuy(Integer priceBuy) {
        this.priceBuy = priceBuy;
    }

    public void setPriceSell(Integer priceSell) {
        this.priceSell = priceSell;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setAdminShop(boolean adminShop) {
        this.adminShop = adminShop;
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

    public void copyFrom(ShopSetting setting) {
//        setOwnerId(setting.getOwnerId());
        setItemId(setting.getItemId());
        setAmount(setting.getAmount());
        setPriceBuy(setting.getPriceBuy());
        setPriceSell(setting.getPriceSell());
        setAdminShop(setting.isAdminShop());
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
        data.set(new NamespacedKey(plugin, "amount"), PersistentDataType.INTEGER, amount);
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
        if (adminShop) {
            data.set(new NamespacedKey(plugin, "admin"), PersistentDataType.INTEGER, 1);
        } else {
            data.remove(new NamespacedKey(plugin, "admin"));
        }
        sign.update(true);
    }

    public static ShopSetting createFromSign(Sign sign) throws IllegalArgumentException {
        PersistentDataContainer data = sign.getPersistentDataContainer();
        SChestShopGUIPlugin plugin = JavaPlugin.getPlugin(SChestShopGUIPlugin.class);

        UUID owner = UUID.fromString(data.getOrDefault(new NamespacedKey(plugin, "owner"), PersistentDataType.STRING, ""));
        String itemId = data.get(new NamespacedKey(plugin, "item"), PersistentDataType.STRING);
        int amount = data.getOrDefault(new NamespacedKey(plugin, "amount"), PersistentDataType.INTEGER, 1);
        Integer priceBuy = data.get(new NamespacedKey(plugin, "pricebuy"), PersistentDataType.INTEGER);
        Integer priceSell = data.get(new NamespacedKey(plugin, "pricesell"), PersistentDataType.INTEGER);
        boolean adminShop = data.getOrDefault(new NamespacedKey(plugin, "admin"), PersistentDataType.INTEGER, 0) >= 1;

        return new ShopSetting(owner, itemId, amount, priceBuy, priceSell, adminShop);
    }

    public boolean createChestShop(Player shopOwner, Sign sign) {
        if (itemId == null || !sign.isPlaced())
            return false;

        // format lines
        String[] lines = new String[4];
        lines[0] = shopOwner.getName();
        lines[1] = "" + Math.min(1, amount);

        // price
        StringBuilder price = new StringBuilder();
        Integer sellValue = priceSell == null || priceSell < 0 ? null : priceSell;
        Integer buyValue = priceBuy == null || priceBuy < 0 ? null : priceBuy;

        if (buyValue != null) {
            price.append("B ").append(formatPriceText(buyValue));
            if (sellValue != null) {
                price.append(" : ").append(formatPriceText(sellValue)).append(" S");
            }
        } else if (sellValue != null) {
            price.append("S ").append(formatPriceText(sellValue));
        }

        lines[2] = price.toString();
        lines[3] = itemId;

        // clear setting
        PersistentDataContainer data = sign.getPersistentDataContainer();
        SChestShopGUIPlugin plugin = JavaPlugin.getPlugin(SChestShopGUIPlugin.class);
        data.remove(new NamespacedKey(plugin, "owner"));
        data.remove(new NamespacedKey(plugin, "item"));
        data.remove(new NamespacedKey(plugin, "amount"));
        data.remove(new NamespacedKey(plugin, "pricebuy"));
        data.remove(new NamespacedKey(plugin, "pricesell"));
        data.remove(new NamespacedKey(plugin, "admin"));
        for (int i = 0; i < 4; i++) {
            sign.setLine(i, "");
        }
        sign.update(true);

        // event
        SignChangeEvent event = new SignChangeEvent(sign.getBlock(), shopOwner, lines);
        Bukkit.getServer().getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private static String formatPriceText(Integer price) {
        if (price == 0)
            return PriceUtil.FREE_TEXT;

        String tmp = String.valueOf(Math.round(price * 100) / 100d);
        if (tmp.endsWith(".0"))
            tmp = tmp.substring(0, tmp.length() - 2);
        return tmp;
    }


    public static Map<UUID, ShopSetting> getPlayerPrevious() {
        return Collections.unmodifiableMap(PLAYER_CACHES);
    }

    public static Optional<ShopSetting> getPlayerPrevious(UUID ownerId) {
        return Optional.ofNullable(PLAYER_CACHES.get(ownerId));
    }

}
