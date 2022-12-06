package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.gui;

import com.Acrobot.Breeze.Utils.MaterialUtil;
import com.Acrobot.ChestShop.Permission;
import com.Acrobot.ChestShop.Signs.ChestShopSign;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.SChestShopGUIPlugin;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.shop.ShopSetting;
import com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.util.ShopUtil;
import com.google.common.collect.Lists;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ShopSettingPanel extends Panel {

    private final ShopSetting setting;
    private final Player player;
    private final Sign sign;
    private ItemStack itemStack;

    public ShopSettingPanel(Player player, ShopSetting setting, Sign sign) {
        super(player, 54, ChatColor.DARK_AQUA + "ショップ編集", new ItemStack(Material.AIR));
        this.setting = setting;
        this.player = player;
        this.sign = sign;
    }

    public static ShopSettingPanel newSign(Player player, ShopSetting setting, Sign sign) {
        return new ShopSettingPanel(player, setting, sign);
    }

    private boolean isCreatedShop() {
        return ChestShopSign.isValid(sign);
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
        if (sign != null && !isCreatedShop()) {
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
        slots[9+4] = createOverrideEditorButton();
        slots[9+5] = createImportPreviousButton();
        slots[9+7] = createCreateButton();

        List<String> lines = createSettingPreview(false, false, false);
        slots[9*3+1] = PanelItem.createItem(Material.WHITE_WOOL, lines.remove(0), lines);
        slots[9*3+2] = createChangeAmountButton(1);
        slots[9*3+3] = createChangeAmountButton(10);
        slots[9*3+4] = createChangeAmountButton(50);
        slots[9*3+5] = createChangeAmountButton(100);
        slots[9*3+6] = createChangeAmountButton(1000);
        slots[9*3+7] = createChangeAmountButton(10000);

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
                if (setting.getPriceBuy() == null) {
                    setting.setPriceBuy(Math.abs(Optional.ofNullable(setting.getPriceSell()).orElse(1)));
                } else if (setting.getPriceBuy() < 0) {
                    setting.setPriceBuy(-Optional.ofNullable(setting.getPriceBuy()).orElse(-1));
                } else {
                    setting.setPriceBuy(-Math.max(1, setting.getPriceBuy()));  // freeだったら1にする
                }
                saveToSign();
                this.update();
            }
        });
        slots[9*4+2] = createChangeBuyPriceButton(1);
        slots[9*4+3] = createChangeBuyPriceButton(10);
        slots[9*4+4] = createChangeBuyPriceButton(50);
        slots[9*4+5] = createChangeBuyPriceButton(100);
        slots[9*4+6] = createChangeBuyPriceButton(1000);
        slots[9*4+7] = createChangeBuyPriceButton(10000);

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
                if (setting.getPriceSell() == null) {
                    setting.setPriceSell(Math.abs(Optional.ofNullable(setting.getPriceBuy()).orElse(1)));
                } else if (setting.getPriceSell() < 0) {
                    setting.setPriceSell(-Optional.ofNullable(setting.getPriceSell()).orElse(-1));
                } else {
                    setting.setPriceSell(-Math.max(1, setting.getPriceSell()));  // freeだったら1にする
                }
                saveToSign();
                this.update();
            }
        });
        slots[9*5+2] = createChangeSellPriceButton(1);
        slots[9*5+3] = createChangeSellPriceButton(10);
        slots[9*5+4] = createChangeSellPriceButton(50);
        slots[9*5+5] = createChangeSellPriceButton(100);
        slots[9*5+6] = createChangeSellPriceButton(1000);
        slots[9*5+7] = createChangeSellPriceButton(10000);

        return slots;
    }

    @Override
    public void onEvent(InventoryClickEvent event) {
        if (!player.getInventory().equals(event.getClickedInventory()))
            return;
        if (event.getCurrentItem() == null)
            return;

        event.setCancelled(true);
        event.setResult(Event.Result.DENY);
        itemStack = event.getCurrentItem().clone();
        setting.setAmount(itemStack.getAmount());
        itemStack.setAmount(1);
        setting.setItemId(null);
        saveToSign();
        this.update();
    }

    private PanelItem createAdminShopButton() {
        if (isAllowAdminShop() && !isCreatedShop()) {
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

    private PanelItem createOverrideEditorButton() {
        // switch: override owner
        if (isAllowOverrideOwner() && !player.getUniqueId().equals(setting.getOwnerId())) {
            List<String> lines = Lists.newArrayList(ChatColor.GRAY + "現在の編集者: " + ChatColor.WHITE + getOwnerName().orElse(setting.getOwnerId().toString()));
            return PanelItem.createItem(Material.ANVIL, ChatColor.DARK_AQUA + "編集者を上書き", lines).setClickListener((e, p) -> {
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
                    .map(itemId -> ChatColor.GOLD + itemId)
                    .orElse("");

            List<String> lines = createSettingPreview(prev, false, false, false);
            lines.add(0, ChatColor.YELLOW + "アイテム: " + itemLabel);

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
            List<String> lines = Collections.emptyList();

            if ((setting.getItemId() != null && MaterialUtil.getItem(setting.getItemId()) != null) || itemStack != null) {
                if ((setting.getPriceBuy() == null || setting.getPriceBuy() < 0) && (setting.getPriceSell() == null || setting.getPriceSell() < 0)) {
                    name = ChatColor.DARK_RED + "値段が設定されていません";
                } else {
                    name = ChatColor.GOLD + (isCreatedShop() ? "ショップを更新する！" : "ショップを作成する！");
                    lines = createSettingPreview(false, false, false);
                }
            }
            return PanelItem.createItem(Material.VILLAGER_SPAWN_EGG, name, lines).getItemStack();

        }).setClickListener((e, p) -> {
            if (ClickType.LEFT.equals(e.getClick())) {
                if ((setting.getItemId() != null && MaterialUtil.getItem(setting.getItemId()) != null) || itemStack != null) {
                    if ((setting.getPriceBuy() != null && setting.getPriceBuy() >= 0) || (setting.getPriceSell() != null && setting.getPriceSell() >= 0)) {
                        createChestShop();
                        destroy(true);
                    }
                }
            }
        });
    }

    private PanelItem createChangeAmountButton(int value) {
        return PanelItem.createItem(
                Material.WHITE_DYE, ChatColor.YELLOW + "個数を " + formatValue(value) + "個 増やす",
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
            setting.setAmount(Math.max(1, setting.getAmount() + (value * mod)));
            saveToSign();
            this.update();
        });
    }

    private PanelItem createChangeBuyPriceButton(int value) {
        if (setting.getPriceBuy() == null || setting.getPriceBuy() < 0)
            return null;

        return PanelItem.createItem(
                Material.ORANGE_DYE, ChatColor.YELLOW + "売値を " + formatPrice(value) + " 増やす",
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
                Material.GREEN_DYE, ChatColor.YELLOW + "買値を " + formatPrice(value) + " 増やす",
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
        return createSettingPreview(setting, markAmount, markBuy, markSell);
    }

    private List<String> createSettingPreview(ShopSetting setting, boolean markAmount, boolean markBuy, boolean markSell) {
        int amount = setting.getAmount();
        List<String> lines = Lists.newArrayList(
                ChatColor.GRAY + "個数: " + (markAmount ? ChatColor.WHITE.toString() + ChatColor.UNDERLINE : ChatColor.WHITE) + formatValue(amount) + "個"
        );
        if (setting.getPriceBuy() != null && setting.getPriceBuy() >= 0) {
            StringBuilder sb = new StringBuilder(ChatColor.GRAY + "売値: " + ChatColor.WHITE + (markBuy ? ChatColor.UNDERLINE : ""));
            if (setting.getPriceBuy() == 0) {
                sb.append("Free");
            } else {
                sb.append(formatPrice(setting.getPriceBuy()));
                sb.append(ChatColor.GRAY);
                sb.append("  (1個あたりの値段: 約");
                sb.append(formatPrice((double) setting.getPriceBuy() / amount));
                sb.append(")");
            }
            lines.add(sb.toString());
        }
        if (setting.getPriceSell() != null && setting.getPriceSell() >= 0) {
            StringBuilder sb = new StringBuilder(ChatColor.GRAY + "買値: " + ChatColor.WHITE + (markSell ? ChatColor.UNDERLINE : ""));
            if (setting.getPriceSell() == 0) {
                sb.append("Free");
            } else {
                sb.append(formatPrice(setting.getPriceSell()));
                sb.append(ChatColor.GRAY);
                sb.append("  (1個あたりの値段: 約");
                sb.append(formatPrice((double) setting.getPriceSell() / amount));
                sb.append(")");
            }
            lines.add(sb.toString());
        }
        return lines;
    }


    private void createChestShop() {
        if (sign == null || !sign.isPlaced()) {
            JavaPlugin.getPlugin(SChestShopGUIPlugin.class).getLogger().warning("No set placed sign");
            return;
        }

        String itemId = setting.getItemId();
        if (itemStack != null) {
            itemId = MaterialUtil.getSignName(itemStack);
        }
        if (itemId == null) {
            JavaPlugin.getPlugin(SChestShopGUIPlugin.class).getLogger().warning("No itemId");
            return;
        }

        if (isCreatedShop()) {
            sign.setLine(2, ShopUtil.formatPrice(setting.getPriceBuy(), setting.getPriceSell()));
            sign.setLine(3, itemId);
            sign.update(false);
        } else {
            setting.setItemId(itemId);
            setting.setPrevious();
            setting.createChestShop(player, sign);
        }
    }


    private String formatValue(long num) {
        return String.format("%,d", num);
    }

    private String formatPrice(double num) {
        return SChestShopGUIPlugin.formatPrice(num);
    }

}
