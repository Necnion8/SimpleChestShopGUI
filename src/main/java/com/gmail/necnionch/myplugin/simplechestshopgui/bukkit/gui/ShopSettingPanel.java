package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.gui;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Permission;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.SChestShopGUIPlugin;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.shop.ShopSetting;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Optional;

public class ShopSettingPanel extends Panel {

    private final ShopSetting setting;
    private final Player player;
    private Sign sign;

    public ShopSettingPanel(Player player, ShopSetting setting) {
        super(player, 54, ChatColor.DARK_AQUA + "ショップ編集", new ItemStack(Material.AIR));
        this.setting = setting;
        this.player = player;
    }

    public ShopSettingPanel withSign(Sign sign) {
        this.sign = sign;
        return this;
    }

    private boolean isAllowAdminShop() {
        return player.hasPermission(Permission.ADMIN_SHOP.getPermission());
    }

    private boolean isAllowOverrideOwner() {
        return player.hasPermission(SChestShopGUIPlugin.OTHER_ACCESS_PERM);
    }

    private Optional<ShopSetting> getPreviousSetting() {
        return ShopSetting.getPlayerPrevious(player.getUniqueId());
    }

    private Optional<String> getOwnerName() {
        return Optional.ofNullable(Bukkit.getOfflinePlayer(setting.getOwnerId()).getPlayer())
                .map(HumanEntity::getName);
    }

    private void saveToSign() {
        if (sign != null) {
            sign.setLine(0, ChatColor.DARK_RED + "[ChestShop GUI]");
            sign.setLine(1, getOwnerName().orElse("?"));
            sign.setLine(2, "");
            sign.setLine(3, "");
            setting.saveToSign(sign);
            sign.update(true);
        }
    }


    @Override
    public PanelItem[] build() {
        PanelItem[] slots = new PanelItem[getSize()];

        // switch: admin shop
        if (isAllowAdminShop()) {
            slots[0] = PanelItem.createItem(Material.AIR, "").setItemBuilder((p) -> {
                String name;
                if (setting.isAdminShop()) {
                    name = ChatColor.DARK_PURPLE + "アドミンショップ: " + ChatColor.GREEN + "有効";
                } else {
                    name = ChatColor.DARK_PURPLE + "アドミンショップ: " + ChatColor.RED + "無効";
                }
                return PanelItem.createItem(Material.COMMAND_BLOCK, name).getItemStack();

            }).setClickListener((e, p) -> {
                if (ClickType.LEFT.equals(e.getClick())) {
                    setting.setAdminShop(!setting.isAdminShop());
                    saveToSign();
                    this.update();
                }
            });

        } else if (setting.isAdminShop()) {
            setting.setAdminShop(false);
            saveToSign();
        }

        // switch: override owner
        if (isAllowOverrideOwner() && !player.getUniqueId().equals(setting.getOwnerId())) {
            List<String> lines = Lists.newArrayList(ChatColor.GRAY + "現在の作成者: " + ChatColor.WHITE + getOwnerName().orElse(setting.getOwnerId().toString()));
            slots[1] = PanelItem.createItem(Material.ANVIL, ChatColor.DARK_AQUA + "作成者を上書き", lines).setClickListener((e, p) -> {
                if (ClickType.LEFT.equals(e.getClick())) {
                    setting.setOwnerId(player.getUniqueId());
                    saveToSign();
                    this.update();
                }
            });
        }

        // import: previous
        getPreviousSetting().ifPresent(prev -> {
            String itemLabel = Optional.ofNullable(prev.getItemId())
                    .map(itemId -> ChatColor.GOLD + itemId + ChatColor.WHITE + "[x" + prev.getAmount() + "]")
                    .orElse("");

            List<String> lines = Lists.newArrayList(ChatColor.YELLOW + "アイテム: " + itemLabel);

            if (prev.getPriceBuy() != null) {
                String line = ChatColor.GRAY + "売値: " + ChatColor.WHITE + prev.getPriceBuy();
                lines.add(line);
            }
            if (prev.getPriceSell() != null) {
                String line = ChatColor.GRAY + "買値: " + ChatColor.WHITE + prev.getPriceSell();
                lines.add(line);
            }

            slots[8] = PanelItem.createItem(Material.OAK_SIGN, ChatColor.GOLD + "前回の設定を読み込む", lines).setClickListener((e, p) -> {
                if (ClickType.LEFT.equals(e.getClick())) {
                    setting.copyFrom(prev);
                    saveToSign();
                    this.update();
                }
            });
        });

        slots[9+4] = null;  // item

        slots[9*2+2] = null;  // sell price
        slots[9*2+6] = null;  // buy price

        slots[9*4+4] = PanelItem.createItem(Material.AIR, "").setItemBuilder((p) -> {
            String name = ChatColor.DARK_RED + "アイテムが設定されていません";
            if (setting.getItemId() != null && MaterialUtil.getItem(setting.getItemId()) != null) {
                name = ChatColor.GOLD + "ショップを作成する！";
            }
            return PanelItem.createItem(Material.VILLAGER_SPAWN_EGG, name).getItemStack();

        }).setClickListener((e, p) -> {
            if (ClickType.LEFT.equals(e.getClick()) && setting.getItemId() != null && MaterialUtil.getItem(setting.getItemId()) != null) {
                setting.setPrevious();
                saveToSign();
            }
        });

        return slots;
    }



}
