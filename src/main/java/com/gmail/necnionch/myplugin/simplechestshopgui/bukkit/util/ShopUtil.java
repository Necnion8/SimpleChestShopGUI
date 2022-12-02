package com.gmail.necnionch.myplugin.simplechestshopgui.bukkit.util;

import com.Acrobot.Breeze.Utils.PriceUtil;
import com.Acrobot.Breeze.Utils.QuantityUtil;
import com.Acrobot.Breeze.Utils.StringUtil;
import org.bukkit.block.Sign;

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


    public static String getOwner(Sign sign) {
        return getOwner(sign.getLines());
    }

    public static String getOwner(String[] lines) {
        return StringUtil.stripColourCodes(strip(StringUtil.stripColourCodes(lines[0])));
    }

    public static String getQuantityLine(String[] lines) throws IllegalArgumentException {
        return lines.length > 1 ? strip(StringUtil.stripColourCodes(lines[1])) : "";
    }

    public static int getQuantity(Sign sign) throws IllegalArgumentException {
        return getQuantity(sign.getLines());
    }

    public static int getQuantity(String[] lines) throws IllegalArgumentException {
        return QuantityUtil.parseQuantity(getQuantityLine(lines));
    }

    public static String getPrice(Sign sign) {
        return strip(StringUtil.stripColourCodes(sign.getLine(2)));
    }

    public static String getItem(Sign sign) {
        return getItem(sign.getLines());
    }

    public static String getItem(String[] lines) {
        return lines.length > 3 ? strip(StringUtil.stripColourCodes(lines[3])) : "";
    }

    private static String strip(String string) {
        if (string == null)
            return null;
        StringBuilder stripped = new StringBuilder();
        StringBuilder cachedWhitespace = new StringBuilder();
        for (int codePoint : string.codePoints().toArray()) {
            if (!Character.isWhitespace(codePoint)) {
                if (cachedWhitespace.length() > 0) {
                    stripped.append(cachedWhitespace);
                    cachedWhitespace = new StringBuilder();
                }
                stripped.appendCodePoint(codePoint);
            } else if (stripped.length() > 0) {
                cachedWhitespace.appendCodePoint(codePoint);
            }
        }
        return stripped.toString();
    }

}
