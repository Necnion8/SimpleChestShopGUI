package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit;

import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.shop.ShopSetting;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class SChestShopGUIPlugin extends JavaPlugin implements Listener {
    public static final String USE_PERM = "simplechestshopgui.use";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // remove previous cache
        ShopSetting.getPlayerPrevious(event.getPlayer().getUniqueId()).ifPresent(ShopSetting::removePrevious);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSign(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission(USE_PERM))
            return;

        for (String line : event.getLines()) {
            if (line.equalsIgnoreCase("new shop")) {
                event.setCancelled(true);

                ShopSetting draft = ShopSetting.createEmpty(event.getPlayer().getUniqueId());
                Sign sign = (Sign) event.getBlock().getState();
                sign.setLine(0, "ショップ作成中");
                sign.setLine(1, "by");
                sign.setLine(2, event.getPlayer().getName());
                draft.saveToSign(sign);
                return;
            }
        }
    }


}
