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

import java.util.Optional;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

import java.util.Date;

public class PlayerExpansion extends PlaceholderExpansion {

    private final String VERSION = getClass().getPackage().getImplementationVersion();

    @Override
    public String getIdentifier() {
        return "player";
    }

    @Override
    public String getAuthor() {
        return "clip";
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    public String onRequest(OfflinePlayer player, String identifier) {

        if (identifier.startsWith("ping_")) {
            identifier = identifier.split("ping_")[1];
            Player t = Bukkit.getPlayer(identifier);
            if (t != null) {
                return PlayerUtil.getPing(t);
            }
            return "0";
        }

        if (player == null) {
            return "";
        }

        // offline placeholders
        switch (identifier) {
            case "name":
                return player.getName();
            case "uuid":
                return player.getUniqueId().toString();
            case "has_played_before":
                return bool(player.hasPlayedBefore());
            case "online":
                return bool(player.isOnline());
            case "is_whitelisted":
                return bool(player.isWhitelisted());
            case "is_banned":
                return bool(player.isBanned());
            case "is_op":
                return bool(player.isOp());
            case "first_played":
            case "first_join":
                return String.valueOf(player.getFirstPlayed());
            case "first_played_formatted":
            case "first_join_date":
                return PlaceholderAPIPlugin.getDateFormat().format(new Date(player.getFirstPlayed()));
            case "last_played":
            case "last_join":
                return String.valueOf(player.getLastPlayed());
            case "last_played_formatted":
            case "last_join_date":
                return PlaceholderAPIPlugin.getDateFormat().format(new Date(player.getLastPlayed()));
            case "bed_x":
                return player.getBedSpawnLocation() != null ? String.valueOf(player.getBedSpawnLocation().getX()) : "";
            case "bed_y":
                return player.getBedSpawnLocation() != null ? String.valueOf(player.getBedSpawnLocation().getY()) : "";
            case "bed_z":
                return player.getBedSpawnLocation() != null ? String.valueOf(player.getBedSpawnLocation().getZ()) : "";
            case "bed_world":
                return player.getBedSpawnLocation() != null ? player.getBedSpawnLocation().getWorld()
                        .getName() : "";
        }

        // online placeholders
        if (!player.isOnline()) {
            return "";
        }

        Player p = player.getPlayer();

        if (identifier.startsWith("has_permission_")) {
            String perm = identifier.split("has_permission_")[1];
            return bool(p.hasPermission(perm));
        }

        if (identifier.startsWith("item_in_hand_level_")){
            String enchantment = identifier.split("item_in_hand_level_")[1];
            return String.valueOf(PlayerUtil.itemInHand(p).getEnchantmentLevel(Enchantment.getByName(enchantment)));
        }

        switch (identifier) {
            case "has_empty_slot":
                return bool(p.getInventory().firstEmpty() > - 1);
            case "server":
            case "servername":
                return Bukkit.getServerName();
            case "displayname":
                return p.getDisplayName();
            case "locale":
                return p.getLocale();
            case "gamemode":
                return p.getGameMode().name();
            case "direction":
                return PlayerUtil.getCardinalDirection(p);
            case "world":
                return p.getWorld().getName();
            case "x":
                return String.valueOf(p.getLocation().getBlockX());
            case "y":
                return String.valueOf(p.getLocation().getBlockY());
            case "z":
                return String.valueOf(p.getLocation().getBlockZ());
            case "ip":
                return p.getAddress().getAddress().getHostAddress();
            case "allow_flight":
                return bool(p.getAllowFlight());
            case "can_pickup_items":
                return bool(p.getCanPickupItems());
            case "compass_x":
                return p.getCompassTarget() != null ? String.valueOf(p.getCompassTarget().getBlockX()) : "";
            case "compass_y":
                return p.getCompassTarget() != null ? String.valueOf(p.getCompassTarget().getBlockY()) : "";
            case "compass_z":
                return p.getCompassTarget() != null ? String.valueOf(p.getCompassTarget().getBlockZ()) : "";
            case "compass_world":
                return p.getCompassTarget() != null ? p.getCompassTarget().getWorld().getName() : "";
            case "custom_name":
                return p.getCustomName() != null ? p.getCustomName() : p.getName();
            case "exp":
                return String.valueOf(p.getExp());
            case "exp_to_level":
                return String.valueOf(p.getExpToLevel());
            case "level":
                return String.valueOf(p.getLevel());
            case "fly_speed":
                return String.valueOf(p.getFlySpeed());
            case "food_level":
                return String.valueOf(p.getFoodLevel());
            case "health":
                return String.valueOf(p.getHealth());
            case "health_rounded":
                return String.valueOf(Math.round(p.getHealth()));
            case "health_scale":
                return String.valueOf(p.getHealthScale());
            case "item_in_hand":
                return String.valueOf(PlayerUtil.itemInHand(p).getType());
            case "item_in_hand_name":
                return PlayerUtil.itemInHand(p).getType() != Material.AIR && PlayerUtil.itemInHand(p).getItemMeta().hasDisplayName() ? PlayerUtil.itemInHand(p).getItemMeta().getDisplayName() : "";
            case "item_in_hand_data":
                return PlayerUtil.itemInHand(p).getType() != Material.AIR ? String.valueOf(PlayerUtil.itemInHand(p).getDurability()) : "0";
            case "item_in_offhand":
                return String.valueOf(p.getInventory().getItemInOffHand().getType());
            case "item_in_offhand_name":
                return p.getInventory().getItemInOffHand().getType() != Material.AIR && p.getInventory().getItemInOffHand().getItemMeta().hasDisplayName() ? p.getInventory().getItemInOffHand().getItemMeta().getDisplayName() : "";
            case "item_in_offhand_data":
                return p.getInventory().getItemInOffHand().getType() != Material.AIR ? String.valueOf(p.getInventory().getItemInOffHand().getDurability()) : "0";
            case "last_damage":
                return String.valueOf(p.getLastDamage());
            case "max_health":
                return String.valueOf(p.getMaxHealth());
            case "max_health_rounded":
                return String.valueOf(Math.round(p.getMaxHealth()));
            case "max_air":
                return String.valueOf(p.getMaximumAir());
            case "max_no_damage_ticks":
                return String.valueOf(p.getMaximumNoDamageTicks());
            case "no_damage_ticks":
                return String.valueOf(p.getNoDamageTicks());
            case "armor_helmet_name":
                return Optional.ofNullable(p.getInventory().getHelmet()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
            case "armor_helmet_data":
                return p.getInventory().getHelmet() != null ? String.valueOf(p.getInventory().getHelmet().getDurability()) : "0";
            case "armor_chestplate_name":
                return Optional.ofNullable(p.getInventory().getChestplate()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
            case "armor_chestplate_data":
                return p.getInventory().getChestplate() != null ? String.valueOf(p.getInventory().getChestplate().getDurability()) : "0";
            case "armor_leggings_name":
                return Optional.ofNullable(p.getInventory().getLeggings()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
            case "armor_leggings_data":
                return p.getInventory().getLeggings() != null ? String.valueOf(p.getInventory().getLeggings().getDurability()) : "0";
            case "armor_boots_name":
                return Optional.ofNullable(p.getInventory().getBoots()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
            case "armor_boots_data":
                return p.getInventory().getBoots() != null ? String.valueOf(p.getInventory().getBoots().getDurability()) : "0";
            case "ping":
                return PlayerUtil.getPing(p);
            case "time":
                return String.valueOf(p.getPlayerTime());
            case "time_offset":
                return String.valueOf(p.getPlayerTimeOffset());
            case "remaining_air":
                return String.valueOf(p.getRemainingAir());
            case "saturation":
                return String.valueOf(p.getSaturation());
            case "sleep_ticks":
                return String.valueOf(p.getSleepTicks());
            case "ticks_lived":
                return String.valueOf(p.getTicksLived());
            case "seconds_lived":
                return String.valueOf(p.getTicksLived() * 20);
            case "minutes_lived":
                return String.valueOf((p.getTicksLived() * 20) / 60);
            case "total_exp":
                return String.valueOf(p.getTotalExperience());
            case "walk_speed":
                return String.valueOf(p.getWalkSpeed());
            case "world_time":
                return String.valueOf(p.getWorld().getTime());
            case "world_time_12":
                return PlayerUtil.format12(p.getWorld().getTime());
            case "world_time_24":
                return PlayerUtil.format24(p.getWorld().getTime());
        }
        // return null for unknown placeholders
        return null;
    }

    public String bool(boolean b) {
        return b ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

}