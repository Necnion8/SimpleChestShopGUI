package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.commands;

import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.SChestShopGUIPlugin;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.gui.ShopSettingPanel;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.shop.ShopSetting;
import com.google.common.collect.Maps;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EditCommand implements TabExecutor {

    private final SChestShopGUIPlugin plugin;

    public EditCommand(SChestShopGUIPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;
        Block block = player.getTargetBlock(null, 3);

        if (block.getState() instanceof Sign) {
            Sign sign = (Sign) block.getState();
            if (tryEditingSign(player, sign)) {
                lookups.remove(player);
                return true;
            } else if (ChestShopSign.isValid(sign) && ChestShopSign.canAccess(player, sign)) {
                lookups.remove(player);
                tryEditSign(player, sign);
                return true;
            }
        }

        if (!lookups.containsKey(player)) {
            new LookupListener(player).listen();
            player.sendMessage(ChatColor.YELLOW + "編集したいショップ看板に触れてください");
        }
        return true;
    }

    private void tryEditSign(Player player, Sign sign) {
        if (!ChestShopSign.isValid(sign)) {
            player.sendMessage(ChatColor.RED + "ショップ看板ではありません");
        } else if (!ChestShopSign.canAccess(player, sign)) {
            player.sendMessage(ChatColor.RED + "この看板にはアクセスできません");
        } else {
            ShopSettingPanel.newSign(player, ShopSetting.createFromChestShop(sign, player.getUniqueId()), sign).open();
        }
    }

    private boolean tryEditingSign(Player player, Sign sign) {
        if (!player.hasPermission(SChestShopGUIPlugin.USE_PERM))
            return false;

        ShopSetting setting;
        try {
            setting = ShopSetting.createFromSign(sign);
        } catch (IllegalArgumentException e) {
            return false;
        }

        if (player.getUniqueId().equals(setting.getOwnerId()) || player.hasPermission(SChestShopGUIPlugin.OTHER_ACCESS_PERM)) {
            ShopSettingPanel.newSign(player, setting, sign).open();
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return Collections.emptyList();
    }


    private final Map<Player, LookupListener> lookups = Maps.newHashMap();

    private final class LookupListener implements Listener {
        private final Player player;

        public LookupListener(Player player) {
            this.player = player;
        }

        @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
        public void onInteract(PlayerInteractEvent event) {
            if (!event.getPlayer().equals(player) || !EquipmentSlot.HAND.equals(event.getHand()) || event.getClickedBlock() == null)
                return;
            if (!(event.getClickedBlock().getState() instanceof Sign))
                return;

            event.setCancelled(true);
            cancel();
            Sign sign = (Sign) event.getClickedBlock().getState();

            if (!tryEditingSign(player, sign))
                tryEditSign(player, sign);
        }

        @EventHandler
        public void onDead(PlayerDeathEvent event) {
            if (event.getEntity().equals(player))
                cancel();
        }

        @EventHandler
        public void onQuit(PlayerQuitEvent event) {
            if (event.getPlayer().equals(player))
                cancel();
        }


        public void cancel() {
            HandlerList.unregisterAll(this);
            lookups.remove(player);
        }

        public void listen() {
            lookups.put(player, this);
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }

    }

}
