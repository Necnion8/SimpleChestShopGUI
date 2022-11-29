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
    private ItemStack itemStack;

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

        if (!isAllowAdminShop() && setting.isAdminShop()) {
            setting.setAdminShop(false);
            saveToSign();
        }

        slots[9+1] = createItem();
        slots[9+3] = createAdminShopButton();
        slots[9+4] = createOverrideOwnerButton();
        slots[9+5] = createImportPreviousButton();
        slots[9+7] = createCreateButton();

        List<String> lines = createSettingPreview(false, false, false);
        slots[9*3+1] = PanelItem.createItem(Material.WHITE_WOOL, lines.remove(0), lines);
        slots[9*3+2] = createChangeAmountButton(1);
        slots[9*3+3] = createChangeAmountButton(5);
        slots[9*3+4] = createChangeAmountButton(10);
        slots[9*3+5] = createChangeAmountButton(50);
        slots[9*3+6] = createChangeAmountButton(100);
        slots[9*3+7] = createChangeAmountButton(1000);

        slots[9*4+1] = PanelItem.createItem(Material.AIR, "", Lists.newArrayList(ChatColor.GRAY + "クリックで切り替え")).setItemBuilder((p) -> {
            String name = ChatColor.GOLD + "アイテム販売: ";
            if (setting.getPriceBuy() == null || setting.getPriceBuy() < 0) {
                name += ChatColor.RED + "オフ";
            } else {
                name += ChatColor.GREEN + "オン";
            }
            return PanelItem.createItem(Material.ORANGE_WOOL, name, createSettingPreview(false, true, false)).getItemStack();

        }).setClickListener((e, p) -> {
            if (ClickType.LEFT.equals(e.getClick())) {
                if (setting.getPriceBuy() == null || setting.getPriceBuy() < 0) {
                    setting.setPriceBuy(-Optional.ofNullable(setting.getPriceBuy()).orElse(-1));
                } else {
                    setting.setPriceBuy(-Math.max(1, setting.getPriceBuy()));  // freeだったら1にする
                }
                saveToSign();
                this.update();
            }
        });
        slots[9*4+2] = createChangeBuyPriceButton(1);
        slots[9*4+3] = createChangeBuyPriceButton(5);
        slots[9*4+4] = createChangeBuyPriceButton(10);
        slots[9*4+5] = createChangeBuyPriceButton(50);
        slots[9*4+6] = createChangeBuyPriceButton(100);
        slots[9*4+7] = createChangeBuyPriceButton(1000);

        slots[9*5+1] = PanelItem.createItem(Material.AIR, "", Lists.newArrayList(ChatColor.GRAY + "クリックで切り替え")).setItemBuilder((p) -> {
            String name = ChatColor.GOLD + "アイテム買取: ";
            if (setting.getPriceSell() == null || setting.getPriceSell() < 0) {
                name += ChatColor.RED + "オフ";
            } else {
                name += ChatColor.GREEN + "オン";
            }
            return PanelItem.createItem(Material.GREEN_WOOL, name, createSettingPreview(false, false, true)).getItemStack();

        }).setClickListener((e, p) -> {
            if (ClickType.LEFT.equals(e.getClick())) {
                if (setting.getPriceSell() == null || setting.getPriceSell() < 0) {
                    setting.setPriceSell(-Optional.ofNullable(setting.getPriceSell()).orElse(-1));
                } else {
                    setting.setPriceSell(-Math.max(1, setting.getPriceSell()));  // freeだったら1にする
                }
                saveToSign();
                this.update();
            }
        });;
        slots[9*5+2] = createChangeSellPriceButton(1);
        slots[9*5+3] = createChangeSellPriceButton(5);
        slots[9*5+4] = createChangeSellPriceButton(10);
        slots[9*5+5] = createChangeSellPriceButton(50);
        slots[9*5+6] = createChangeSellPriceButton(100);
        slots[9*5+7] = createChangeSellPriceButton(1000);

        return slots;
    }

    private PanelItem createAdminShopButton() {
        if (isAllowAdminShop()) {
            return PanelItem.createItem(Material.AIR, "").setItemBuilder((p) -> {
                String name;
                if (setting.isAdminShop()) {
                    name = ChatColor.DARK_PURPLE + "アドミンショップ: " + ChatColor.GREEN + "オン";
                } else {
                    name = ChatColor.DARK_PURPLE + "アドミンショップ: " + ChatColor.RED + "オフ";
                }
                return PanelItem.createItem(Material.COMMAND_BLOCK, name).getItemStack();

            }).setClickListener((e, p) -> {
                if (ClickType.LEFT.equals(e.getClick())) {
                    setting.setAdminShop(!setting.isAdminShop());
                    saveToSign();
                    this.update();
                }
            });
        }
        return null;
    }

    private PanelItem createOverrideOwnerButton() {
        // switch: override owner
        if (isAllowOverrideOwner() && !player.getUniqueId().equals(setting.getOwnerId())) {
            List<String> lines = Lists.newArrayList(ChatColor.GRAY + "現在の作成者: " + ChatColor.WHITE + getOwnerName().orElse(setting.getOwnerId().toString()));
            return PanelItem.createItem(Material.ANVIL, ChatColor.DARK_AQUA + "作成者を上書き", lines).setClickListener((e, p) -> {
                if (ClickType.LEFT.equals(e.getClick())) {
                    setting.setOwnerId(player.getUniqueId());
                    saveToSign();
                    this.update();
                }
            });
        }
        return null;
    }

    private PanelItem createImportPreviousButton() {
        return getPreviousSetting().map(prev -> {
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

            return PanelItem.createItem(Material.OAK_SIGN, ChatColor.GOLD + "前回の設定を読み込む", lines).setClickListener((e, p) -> {
                if (ClickType.LEFT.equals(e.getClick())) {
                    setting.copyFrom(prev);
                    saveToSign();
                    this.update();
                }
            });
        }).orElse(null);
    }

    private PanelItem createItem() {
        return PanelItem.createItem(Material.AIR, "").setItemBuilder((p) -> {
            if (itemStack != null) {
                return itemStack;
            } else {
                ItemStack itemStack = Optional.ofNullable(setting.getItemId())
                                .map(MaterialUtil::getItem).orElse(null);
                if (itemStack != null)
                    return itemStack;
                return PanelItem.createItem(Material.PAPER, ChatColor.YELLOW + "商品にするアイテムをクリックしてください").getItemStack();
            }
        });
    }

    private PanelItem createCreateButton() {
        return  PanelItem.createItem(Material.AIR, "").setItemBuilder((p) -> {
            String name = ChatColor.DARK_RED + "アイテムが設定されていません";
            if (setting.getItemId() != null && MaterialUtil.getItem(setting.getItemId()) != null) {
                if (setting.getPriceBuy() == null || setting.getPriceBuy() < 0 || setting.getPriceSell() == null || setting.getPriceSell() < 0) {
                    name = ChatColor.DARK_RED + "値段が設定されていません";
                } else {
                    name = ChatColor.GOLD + "ショップを作成する！";
                }
            }
            return PanelItem.createItem(Material.VILLAGER_SPAWN_EGG, name).getItemStack();

        }).setClickListener((e, p) -> {
            if (ClickType.LEFT.equals(e.getClick())) {
                if (setting.getItemId() != null && MaterialUtil.getItem(setting.getItemId()) != null) {
                    if ((setting.getPriceBuy() != null && setting.getPriceBuy() >= 0) || (setting.getPriceSell() != null && setting.getPriceSell() >= 0)) {
                        setting.setPrevious();
                        saveToSign();
                    }
                }
            }
        });
    }

    private PanelItem createChangeAmountButton(int value) {
        return PanelItem.createItem(
                Material.WHITE_DYE, ChatColor.YELLOW + "個数を " + value + " 増やす",
                createSettingPreview(true, false, false)
        ).setClickListener((e, p) -> {
            int mod;
            if (ClickType.LEFT.equals(e.getClick())) {
                mod = 1;
            } else if (ClickType.RIGHT.equals(e.getClick())) {
                mod = -1;
            } else {
                return;
            }
            setting.setAmount(Math.max(0, setting.getAmount() + (value * mod)));
            saveToSign();
            this.update();
        });
    }

    private PanelItem createChangeBuyPriceButton(int value) {
        if (setting.getPriceBuy() == null || setting.getPriceBuy() < 0)
            return null;

        return PanelItem.createItem(
                Material.ORANGE_DYE, ChatColor.YELLOW + "売値を " + value + " 増やす",
                createSettingPreview(false, true, false)
        ).setClickListener((e, p) -> {
            int mod;
            if (ClickType.LEFT.equals(e.getClick())) {
                mod = 1;
            } else if (ClickType.RIGHT.equals(e.getClick())) {
                mod = -1;
            } else {
                return;
            }
            setting.setPriceBuy(Math.max(0, setting.getPriceBuy() + (value * mod)));
            saveToSign();
            this.update();
        });
    }

    private PanelItem createChangeSellPriceButton(int value) {
        if (setting.getPriceSell() == null || setting.getPriceSell() < 0)
            return null;

        return PanelItem.createItem(
                Material.GREEN_DYE, ChatColor.YELLOW + "買値を " + value + " 増やす",
                createSettingPreview(false, false, true)
        ).setClickListener((e, p) -> {
            int mod;
            if (ClickType.LEFT.equals(e.getClick())) {
                mod = 1;
            } else if (ClickType.RIGHT.equals(e.getClick())) {
                mod = -1;
            } else {
                return;
            }
            setting.setPriceSell(Math.max(0, setting.getPriceSell() + (value * mod)));
            saveToSign();
            this.update();
        });
    }

    private List<String> createSettingPreview(boolean markAmount, boolean markBuy, boolean markSell) {
        int amount = setting.getAmount();
        List<String> lines = Lists.newArrayList(
                ChatColor.GRAY + "個数: " + (markAmount ? ChatColor.WHITE.toString() + ChatColor.UNDERLINE : ChatColor.WHITE) + amount
        );
        if (setting.getPriceBuy() != null && setting.getPriceBuy() >= 0) {
            StringBuilder sb = new StringBuilder(ChatColor.GRAY + "売値: " + ChatColor.WHITE + (markBuy ? ChatColor.UNDERLINE : ""));
            if (setting.getPriceBuy() == 0) {
                sb.append("Free");
            } else {
                sb.append(setting.getPriceBuy());
                sb.append(ChatColor.GRAY);
                sb.append("  (1個あたりの値段: 約");
                sb.append(((Math.round((double) setting.getPriceBuy() / amount * 10) / 10d)));
                sb.append(")");
            }
            lines.add(sb.toString());
        }
        if (setting.getPriceSell() != null && setting.getPriceSell() >= 0) {
            StringBuilder sb = new StringBuilder(ChatColor.GRAY + "買値: " + ChatColor.WHITE + (markSell ? ChatColor.UNDERLINE : ""));
            if (setting.getPriceSell() == 0) {
                sb.append("Free");
            } else {
                sb.append(setting.getPriceSell());
                sb.append(ChatColor.GRAY);
                sb.append("  (1個あたりの値段: 約");
                sb.append(((Math.round((double) setting.getPriceSell() / amount * 10) / 10d)));
                sb.append(")");
            }
            lines.add(sb.toString());
        }
        return lines;
    }


}
