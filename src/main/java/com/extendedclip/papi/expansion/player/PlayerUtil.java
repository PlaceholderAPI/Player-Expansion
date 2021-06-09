package com.extendedclip.papi.expansion.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.function.Function;

/*
 *
 * Player-Expansion
 * Copyright (C) 2018 Ryan McCarthy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */
public final class PlayerUtil {

    public static final int ticksAtMidnight = 18000;
    public static final int ticksPerDay = 24000;
    public static final int ticksPerHour = 1000;
    public static final double ticksPerMinute = 1000d / 60d;
    private static final SimpleDateFormat twentyFour = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private static final SimpleDateFormat twelve = new SimpleDateFormat("h:mm aa", Locale.ENGLISH);

    private PlayerUtil() {
    }

    private static final Function<Player, Integer> PLAYER_GET_PING = new Function<Player, Integer>() {

        private Field ping;
        private Method getHandle;

        @Override
        public Integer apply(final Player player) {
            if (ping == null) {
                try {
                    cacheReflection(player);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException ex) {
                    ex.printStackTrace();
                }
            }

            try {
                return ping.getInt(getHandle.invoke(player));
            } catch (final IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
            return -1;
        }


        private void cacheReflection(final Player player) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            getHandle = player.getClass().getDeclaredMethod("getHandle");
            getHandle.setAccessible(true);

            final Object entityPlayer = getHandle.invoke(player);

            ping = entityPlayer.getClass().getDeclaredField("ping");
            ping.setAccessible(true);
        }
    };

    private static final Function<Player, String> PLAYER_GET_LOCALE = new Function<Player, String>() {

        private Field locale;
        private Method getHandle;

        @Override
        public String apply(final Player player) {
            if (locale == null) {
                try {
                    cacheReflection(player);
                } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException | NoSuchFieldException ex) {
                    ex.printStackTrace();
                }
            }

            try {
                final Object entityPlayer = getHandle.invoke(player);
                return (String) locale.get(entityPlayer);
            } catch (final IllegalAccessException | InvocationTargetException ex) {
                ex.printStackTrace();
            }
            return "en_US";
        }


        private void cacheReflection(final Player player) throws NoSuchFieldException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
            getHandle = player.getClass().getDeclaredMethod("getHandle");
            getHandle.setAccessible(true);

            final Object entityPlayer = getHandle.invoke(player);

            locale = entityPlayer.getClass().getField("locale");
        }
    };


    public static int getPing(final Player player) {
        return PLAYER_GET_PING.apply(player);
    }

    public static String getLocale(final Player player) {
        return PLAYER_GET_LOCALE.apply(player);
    }

    public static String format12(long ticks) {
        try {
            return twelve.format(twentyFour.parse(ticksToTime(ticks)));
        } catch (ParseException e) {
            return ticksToTime(ticks);
        }
    }

    public static String format24(long ticks) {
        return ticksToTime(ticks);
    }

    private static String ticksToTime(long ticks) {
        ticks = ticks - ticksAtMidnight + ticksPerDay;
        long hours = ticks / ticksPerHour;
        ticks -= hours * ticksPerHour;
        long mins = (long) Math.floor(ticks / ticksPerMinute);
        if (hours >= 24) {
            hours = hours - 24;
        }
        return (hours < 10 ? "0" + hours : hours) + ":" + (mins < 10 ? "0" + mins : mins);
    }

    public static String getCardinalDirection(Player player) {
        double rotation = player.getLocation().getYaw() - 180.0F;
        if (rotation < 0.0D) {
            rotation += 360.0D;
        }
        if ((0.0D <= rotation) && (rotation < 22.5D)) {
            return "N";
        }
        if ((22.5D <= rotation) && (rotation < 67.5D)) {
            return "NE";
        }
        if ((67.5D <= rotation) && (rotation < 112.5D)) {
            return "E";
        }
        if ((112.5D <= rotation) && (rotation < 157.5D)) {
            return "SE";
        }
        if ((157.5D <= rotation) && (rotation < 202.5D)) {
            return "S";
        }
        if ((202.5D <= rotation) && (rotation < 247.5D) || (rotation <= -119.33) && (rotation > -179)) {
            return "SW";
        }
        if ((247.5D <= rotation) && (rotation < 292.5D) || (rotation <= -59.66) && (rotation > -119.33)) {
            return "W";
        }
        if ((292.5D <= rotation) && (rotation < 337.5D) || (rotation <= -0.0) && (rotation > -59.66)) {
            return "NW";
        }
        if ((337.5D <= rotation) && (rotation < 360.0D)) {
            return "N";
        }
        return "";
    }

    public static String getXZDirection(Player player) {
        double rotation = player.getLocation().getYaw();
        if (rotation < 0.0D) {
            rotation += 360.0D;
        }

        if (Math.abs(rotation) <= 45 || Math.abs(rotation - 360) <= 45) {
            return "+Z";
        } else if (Math.abs(rotation - 90) <= 45) {
            return "-X";
        } else if (Math.abs(rotation - 180) <= 45) {
            return "-Z";
        } else if (Math.abs(rotation - 270) <= 45) {
            return "+X";
        }

        return "";
    }

    public static ItemStack itemInHand(Player p) {
        try {
            return p.getInventory().getItemInMainHand();
        } catch (NoSuchMethodError e) {
            return p.getInventory().getItemInHand();
        }
    }

    public static int getEmptySlots(Player p) {
        int slots = 0;
        PlayerInventory inv = p.getInventory();
        for (ItemStack is : inv.getContents()) {
            if (is == null) slots++;
        }

        if (!Bukkit.getBukkitVersion().contains("1.7") && !Bukkit.getBukkitVersion().contains("1.8")) {
            if (inv.getItemInOffHand() == null || inv.getItemInOffHand().getType() == Material.AIR) slots--;
            if (inv.getHelmet() == null) slots--;
            if (inv.getChestplate() == null) slots--;
            if (inv.getLeggings() == null) slots--;
            if (inv.getBoots() == null) slots--;
        }
        return slots;
    }

    private static int getExperienceAtLevel(int level) {
        if (level <= 15) {
            return (level << 1) + 7;
        }
        if (level <= 30) {
            return (level * 5) - 38;
        }
        return (level * 9) - 158;
    }

    public static int getTotalExperience(Player player) {
        int experience = Math.round(getExperienceAtLevel(player.getLevel()) * player.getExp());
        int currentLevel = player.getLevel();
        while (currentLevel > 0) {
            currentLevel--;
            experience += getExperienceAtLevel(currentLevel);
        }
        if (experience < 0) {
            experience = 0;
        }
        return experience;
    }

    public static String getBiome(Player p) {
        return String.valueOf(p.getLocation().getBlock().getBiome());
    }

    public static String getCapitalizedBiome(Player p) {
        String[] biomeWords = getBiome(p).split("_");
        for (int i = 0; i < biomeWords.length; i++) {
            biomeWords[i] = biomeWords[i].substring(0, 1).toUpperCase() + biomeWords[i].substring(1).toLowerCase();
        }
        return String.join(" ", biomeWords);
    }
}
