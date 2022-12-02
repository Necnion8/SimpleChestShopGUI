package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit;

import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.commands.EditCommand;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.gui.Panel;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.gui.ShopSettingPanel;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.shop.ShopSetting;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.util.PriceFormatter;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class SChestShopGUIPlugin extends JavaPlugin implements Listener {
    public static final String USE_PERM = "simplechestshopgui.use";
    public static final String OTHER_ACCESS_PERM = "simplechestshopgui.access-other";
    public static final Function<String, Boolean> SIGN_NEW_SHOP = (line) -> line.equalsIgnoreCase("new shop") || line.equalsIgnoreCase("newshop") || line.equalsIgnoreCase("shopnew");
    private static final List<PriceFormatter> priceFormatters = Lists.newArrayList(
            value -> String.format("%,3.1f", value).replaceAll("\\.0$", "")
    );
    private PriceFormatter economyFormatter;

    @Override
    public void onEnable() {
        Panel.OWNER = this;
        getServer().getPluginManager().registerEvents(this, this);
        registerCommands();
        registerEconomy();
    }

    @Override
    public void onDisable() {
        if (economyFormatter != null)
            priceFormatters.remove(economyFormatter);
        Panel.destroyAll();
    }

    public void registerCommands() {
        Optional.ofNullable(getCommand("editshop"))
                .ifPresent(cmd -> cmd.setExecutor(new EditCommand(this)));
    }

    public void registerEconomy() {
        if (economyFormatter != null)
            priceFormatters.remove(economyFormatter);

        try {
            Class.forName("net.milkbowl.vault.economy.Economy");
        } catch (ClassNotFoundException e) {
            return;
        }

        RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
        Optional.ofNullable(provider)
                .map(RegisteredServiceProvider::getProvider)
                .ifPresent(eco -> {
                    economyFormatter = eco::format;
                    priceFormatters.add(0, economyFormatter);
                });
    }

    public static List<PriceFormatter> priceFormatters() {
        return priceFormatters;
    }

    public static String formatPrice(double value) {
        if (priceFormatters.isEmpty())
            return String.valueOf(value);
        return priceFormatters.get(0).format(value);
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // remove previous cache
        ShopSetting.getPlayerPrevious(event.getPlayer().getUniqueId())
                .ifPresent(ShopSetting::removePrevious);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSign(SignChangeEvent event) {
        if (!event.getPlayer().hasPermission(USE_PERM))
            return;

        for (String line : event.getLines()) {
            if (SIGN_NEW_SHOP.apply(line)) {
                event.setCancelled(true);

                ShopSetting draft = ShopSetting.createEmpty(event.getPlayer().getUniqueId());
                Sign sign = (Sign) event.getBlock().getState();

                ShopSettingPanel.newSign(event.getPlayer(), draft, sign).open();
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
        ShopSettingPanel.newSign(event.getPlayer(), setting, sign).open();
    }


}
