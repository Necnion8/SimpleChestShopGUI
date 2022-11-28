package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit;

import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.gui.ShopSettingPanel;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.shop.ShopSetting;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class SChestShopGUIPlugin extends JavaPlugin implements Listener {
    public static final String USE_PERM = "simplechestshopgui.use";
    public static final String OTHER_ACCESS_PERM = "simplechestshopgui.access-other";

    @Override
    public void onEnable() {
        Panel.OWNER = this;
        getServer().getPluginManager().registerEvents(this, this);

    }

    @Override
    public void onDisable() {
        Panel.destroyAll();
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
            if (line.equalsIgnoreCase("new shop") || line.equalsIgnoreCase("newshop") || line.equalsIgnoreCase("shopnew")) {
                event.setCancelled(true);

                ShopSetting draft = ShopSetting.createEmpty(event.getPlayer().getUniqueId());
                Sign sign = (Sign) event.getBlock().getState();
//                sign.setLine(0, "ショップ作成中");
//                sign.setLine(1, "by");
//                sign.setLine(2, event.getPlayer().getName());
//                draft.saveToSign(sign);

                new ShopSettingPanel(event.getPlayer(), draft).withSign(sign).open();

                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onClick(PlayerInteractEvent event) {
        if (!event.getPlayer().hasPermission(USE_PERM))
            return;

        if (event.getClickedBlock() == null || !(event.getClickedBlock().getState() instanceof Sign))
            return;

        if (!EquipmentSlot.HAND.equals(event.getHand()) || !Action.RIGHT_CLICK_BLOCK.equals(event.getAction()))
            return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        ShopSetting setting;
        try {
            setting = ShopSetting.createFromSign(sign);
        } catch (IllegalArgumentException ignored) {
            return;
        }

        event.setCancelled(true);

        if (!event.getPlayer().getUniqueId().equals(setting.getOwnerId())) {
            if (!event.getPlayer().hasPermission(OTHER_ACCESS_PERM)) {
                String ownerName = Optional.ofNullable(Bukkit.getOfflinePlayer(setting.getOwnerId()).getPlayer())
                        .map(HumanEntity::getName)
                        .orElse("他のプレイヤー");
                event.getPlayer().sendMessage(ChatColor.DARK_RED + "この看板は" + ownerName + "によって編集されています");
                return;
            }
        }
        new ShopSettingPanel(event.getPlayer(), setting).withSign(sign).open();

    }


}
