package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.util;

import com.Acrobot.Breeze.Utils.PriceUtil;

public class ShopUtil {

    public static String formatOwner(String name) {
        return name;
    }

    public static String formatAmount(int amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("amount > 0");
        return String.valueOf(amount);
    }

    public static String formatPrice(Integer buy, Integer sell) {
        StringBuilder price = new StringBuilder();
        sell = sell == null || sell < 0 ? null : sell;
        buy = buy == null || buy < 0 ? null : buy;

        if (buy != null) {
            price.append("B ").append(formatPriceText(buy));
            if (sell != null) {
                price.append(" : ").append(formatPriceText(sell)).append(" S");
            }
        } else if (sell != null) {
            price.append("S ").append(formatPriceText(sell));
        }

        return price.toString();
    }

    public static String formatPriceText(Integer price) {
        if (price == 0)
            return PriceUtil.FREE_TEXT;

        String tmp = String.valueOf(Math.round(price * 100) / 100d);
        if (tmp.endsWith(".0"))
            tmp = tmp.substring(0, tmp.length() - 2);
        return tmp;
    }


}
