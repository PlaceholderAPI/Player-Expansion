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

package com.extendedclip.papi.expansion.player;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class PlayerUtil {

    public static final int ticksAtMidnight = 18000;
    public static final int ticksPerDay = 24000;
    public static final int ticksPerHour = 1000;
    public static final double ticksPerMinute = 1000d / 60d;
    private static final SimpleDateFormat twentyFour = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
    private static final SimpleDateFormat twelve = new SimpleDateFormat("h:mm aa", Locale.ENGLISH);
    private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

    private PlayerUtil() {}

    public static String msToSToStr(double ms) {
        return String.valueOf(Math.round((System.currentTimeMillis()-ms)/1000));
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

    public static BlockFace getDirection(Player player) {
        return radial[Math.round(player.getLocation().getYaw() / 45f) & 0x7].getOppositeFace();
    }

    public static String getXZDirection(Player player) {
        double rotation = player.getLocation().getYaw();
        if (rotation < 0.0D) rotation += 360.0D;

        if (Math.abs(rotation) <= 45 || Math.abs(rotation - 360) <= 45)  return "+Z";
        if (Math.abs(rotation - 90) <= 45) return "-X";
        if (Math.abs(rotation - 180) <= 45) return "-Z";
        if (Math.abs(rotation - 270) <= 45) return "+X";
        return "";
    }

    public static int getEmptySlots(Player p) {
        int slots = 0;
        PlayerInventory inv = p.getInventory();
        for (ItemStack is : inv.getStorageContents())
            if (is == null) slots++;

        if (Bukkit.getServer().getBukkitVersion().contains("1.7") || Bukkit.getServer().getBukkitVersion().contains("1.8"))
            return slots;

        if (inv.getItemInOffHand() == null || inv.getItemInOffHand().getType() == Material.AIR) slots--;

        return slots;
    }

    private static int getExperienceAtLevel(int level) {
        return level <= 15
                ? (level << 1) + 7
                : level <= 30
                    ? (level * 5) - 38
                    : (level * 9) - 158;
    }

    public static int getTotalExperience(Player player) {
        int experience = Math.round(getExperienceAtLevel(player.getLevel()) * player.getExp());
        int currentLevel = player.getLevel();
        while (currentLevel > 0) {
            currentLevel--;
            experience += getExperienceAtLevel(currentLevel);
        }
        return Math.max(experience, 0);
    }

    public static Object getLocation(Location location, String pos) {
        if (location == null) return "";
        return switch (pos) {
            case "x" -> location.getX();
            case "y" -> location.getY();
            case "z" -> location.getZ();
            case "world" -> location.getWorld() == null ? "" : location.getWorld().getName();
            case "block_x" -> location.getBlockX();
            case "block_y" -> location.getBlockY();
            case "block_z" -> location.getBlockZ();
            default -> "";
        };
    }

    @SuppressWarnings("deprecation")
    public static String getItemEnchantment(String enchant, ItemStack item) {
        Enchantment enchantment = Enchantment.getByName(enchant);
        if (enchantment == null) return "0";
        return String.valueOf(item.getEnchantmentLevel(enchantment));
    }

}
