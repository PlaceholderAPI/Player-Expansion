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

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import static com.extendedclip.papi.expansion.player.PlayerUtil.durability;
import static com.extendedclip.papi.expansion.player.PlayerUtil.format12;
import static com.extendedclip.papi.expansion.player.PlayerUtil.format24;
import static com.extendedclip.papi.expansion.player.PlayerUtil.getBiome;
import static com.extendedclip.papi.expansion.player.PlayerUtil.getCapitalizedBiome;
import static com.extendedclip.papi.expansion.player.PlayerUtil.getEmptySlots;
import static com.extendedclip.papi.expansion.player.PlayerUtil.getDirection;
import static com.extendedclip.papi.expansion.player.PlayerUtil.getTotalExperience;
import static com.extendedclip.papi.expansion.player.PlayerUtil.getXZDirection;
import static com.extendedclip.papi.expansion.player.PlayerUtil.itemInHand;

public final class PlayerExpansion extends PlaceholderExpansion implements Configurable {
    private String low;
    private String medium;
    private String high;

    private int mediumValue;
    private int highValue;

    private String north;
    private String northEast;
    private String east;
    private String southEast;
    private String south;
    private String southWest;
    private String west;
    private String northWest;

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
        return "2.0.9";
    }

    @Override
    public Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put("ping_color.high", "&c");
        defaults.put("ping_color.medium", "&e");
        defaults.put("ping_color.low", "&a");
        defaults.put("ping_value.medium", 50);
        defaults.put("ping_value.high", 100);
        defaults.put("direction.north", "N");
        defaults.put("direction.north_east", "NE");
        defaults.put("direction.east", "E");
        defaults.put("direction.south_east", "SE");
        defaults.put("direction.south", "S");
        defaults.put("direction.south_west", "SW");
        defaults.put("direction.west", "W");
        defaults.put("direction.north_west", "NW");
        return defaults;
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {

        final boolean targetedPing = identifier.startsWith("ping_");
        final boolean targetedColoredPing = identifier.startsWith("colored_ping_");
        if (targetedPing || targetedColoredPing) {
            final Player target = Bukkit.getPlayer(identifier.substring(targetedPing ? 5 : 13)); // yes, I know, magic value

            return target == null ? "0" : retrievePing(target, targetedColoredPing);
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

        // to get rid of IDE warnings
        if (p == null) {
            return "";
        }

        if (identifier.startsWith("has_permission_")) {
            if (identifier.split("has_permission_").length > 1) {
                String perm = identifier.split("has_permission_")[1];
                return bool(p.hasPermission(perm));
            }
            return bool(false);
        }

        if (identifier.startsWith("has_potioneffect_")) {
            if (identifier.split("has_potioneffect_").length > 1) {
                String effect = identifier.split("has_potioneffect_")[1];
                PotionEffectType potion = PotionEffectType.getByName(effect);
                return bool(p.hasPotionEffect(potion));
            }
        }

        if (identifier.startsWith("item_in_hand_level_")) {
            if (identifier.split("item_in_hand_level_").length > 1) {
                String enchantment = identifier.split("item_in_hand_level_")[1];
                return String.valueOf(itemInHand(p).getEnchantmentLevel(Enchantment.getByName(enchantment)));
            }
            return "0";
        }
        if (identifier.startsWith("item_in_offhand_level_")) {
            if (identifier.split("item_in_offhand_level_").length > 1) {
                String enchantment = identifier.split("item_in_offhand_level_")[1];
                return String.valueOf(p.getInventory().getItemInOffHand().getEnchantmentLevel(Enchantment.getByName(enchantment)));
            }
            return "0";
        }

        if (identifier.startsWith("locale")) {
            String localeStr = PlayerUtil.getLocale(p);
            String localeStrISO = localeStr.replace("_", "-");

            switch (identifier) {
                case "locale":
                    return localeStr;
                case "locale_country":
                    Locale locale = Locale.forLanguageTag(localeStrISO);
                    if (locale == null)
                        return "";
                    return locale.getCountry();
                case "locale_display_country":
                    locale = Locale.forLanguageTag(localeStrISO);
                    if (locale == null)
                        return "";
                    return locale.getDisplayCountry();
                case "locale_display_name":
                    locale = Locale.forLanguageTag(localeStrISO);
                    if (locale == null)
                        return "";
                    return locale.getDisplayName();
                case "locale_short":
                    return localeStr.substring(0, localeStr.indexOf("_"));
            }
        }

        switch (identifier) {
            case "absorption": {
                if (VersionHelper.HAS_ABSORPTION_METHODS) {
                    return Integer.toString((int) p.getAbsorptionAmount());
                } else {
                    return "-1";
                }
            }
            case "has_empty_slot":
                return bool(p.getInventory().firstEmpty() > -1);
            case "empty_slots":
                return String.valueOf(getEmptySlots(p));
            case "server":
            case "servername":
                return "now available in the server expansion";
            case "displayname":
                return p.getDisplayName();
            case "list_name":
                return p.getPlayerListName();
            case "gamemode":
                return p.getGameMode().name();
            case "direction":
                switch (getDirection(p)) {
                    case NORTH:
                        return north;
                    case NORTH_EAST:
                        return northEast;
                    case EAST:
                        return east;
                    case SOUTH_EAST:
                        return southEast;
                    case SOUTH:
                        return south;
                    case SOUTH_WEST:
                        return southWest;
                    case WEST:
                        return west;
                    case NORTH_WEST:
                        return northWest;
                }
                return "";
            case "direction_xz":
                return getXZDirection(p);
            case "world":
                return p.getWorld().getName();
            case "world_type":
                World.Environment environment = p.getWorld().getEnvironment();
                if (environment == World.Environment.NETHER) {
                    return "Nether";
                } else if (environment == World.Environment.THE_END) {
                    return "The End";
                } else if (environment == World.Environment.NORMAL) {
                    return "Overworld";
                }
                return "";
            case "x":
                return String.valueOf(p.getLocation().getBlockX());
            case "x_long":
                return String.valueOf(p.getLocation().getX());
            case "y":
                return String.valueOf(p.getLocation().getBlockY());
            case "y_long":
                return String.valueOf(p.getLocation().getY());
            case "z":
                return String.valueOf(p.getLocation().getBlockZ());
            case "z_long":
                return String.valueOf(p.getLocation().getZ());
            case "yaw":
                return String.valueOf(p.getLocation().getYaw());
            case "pitch":
                return String.valueOf(p.getLocation().getPitch());
            case "biome":
                return getBiome(p);
            case "biome_capitalized":
                return getCapitalizedBiome(p);
            case "light_level":
                return String.valueOf(p.getLocation().getBlock().getLightLevel());
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
            case "block_underneath":
                return String.valueOf(p.getLocation().clone().subtract(0, 1, 0).getBlock().getType());
            case "custom_name":
                return p.getCustomName() != null ? p.getCustomName() : p.getName();
            case "exp":
                return String.valueOf(p.getExp());
            case "current_exp":
                return String.valueOf(getTotalExperience(p));
            case "total_exp":
                return String.valueOf(p.getTotalExperience());
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
            case "has_health_boost":
                return bool(p.hasPotionEffect(PotionEffectType.HEALTH_BOOST));
            case "health_boost": {
                if (p.getHealthScale() > 20) {
                    return Double.toString(p.getHealthScale() - 20);
                } else {
                    return "0";
                }
            }
            case "item_in_hand":
                return String.valueOf(itemInHand(p).getType());
            case "item_in_hand_name":
                return itemInHand(p).getType() != Material.AIR && itemInHand(p).getItemMeta().hasDisplayName() ? itemInHand(p).getItemMeta().getDisplayName() : "";
            case "item_in_hand_data":
                return itemInHand(p).getType() != Material.AIR ? String.valueOf(itemInHand(p).getDurability()) : "0";
            case "item_in_hand_durability":
                return String.valueOf(durability(itemInHand(p)));
            case "item_in_offhand":
                return String.valueOf(p.getInventory().getItemInOffHand().getType());
            case "item_in_offhand_name":
                return p.getInventory().getItemInOffHand().getType() != Material.AIR && p.getInventory().getItemInOffHand().getItemMeta().hasDisplayName() ? p.getInventory().getItemInOffHand().getItemMeta().getDisplayName() : "";
            case "item_in_offhand_data":
                return p.getInventory().getItemInOffHand().getType() != Material.AIR ? String.valueOf(p.getInventory().getItemInOffHand().getDurability()) : "0";
            case "item_in_offhand_durability":
                return String.valueOf(durability(p.getInventory().getItemInOffHand()));
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
            case "armor_helmet_durability":
                return String.valueOf(durability(p.getInventory().getHelmet()));
            case "armor_chestplate_name":
                return Optional.ofNullable(p.getInventory().getChestplate()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
            case "armor_chestplate_data":
                return p.getInventory().getChestplate() != null ? String.valueOf(p.getInventory().getChestplate().getDurability()) : "0";
            case "armor_chestplate_durability":
                return String.valueOf(durability(p.getInventory().getChestplate()));
            case "armor_leggings_name":
                return Optional.ofNullable(p.getInventory().getLeggings()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
            case "armor_leggings_data":
                return p.getInventory().getLeggings() != null ? String.valueOf(p.getInventory().getLeggings().getDurability()) : "0";
            case "armor_leggings_durability":
                return String.valueOf(durability(p.getInventory().getLeggings()));
            case "armor_boots_name":
                return Optional.ofNullable(p.getInventory().getBoots()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
            case "armor_boots_data":
                return p.getInventory().getBoots() != null ? String.valueOf(p.getInventory().getBoots().getDurability()) : "0";
            case "armor_boots_durability":
                return String.valueOf(durability(p.getInventory().getBoots()));
            case "ping":
                return retrievePing(p, false);
            case "colored_ping":
                return retrievePing(p, true);
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
            case "thunder_duration":
                return String.valueOf(p.getWorld().getThunderDuration());
            case "ticks_lived":
                return String.valueOf(p.getTicksLived());
            case "seconds_lived":
                return String.valueOf(p.getTicksLived() / 20);
            case "minutes_lived":
                return String.valueOf((p.getTicksLived() / 20) / 60);
            case "walk_speed":
                return String.valueOf(p.getWalkSpeed());
            case "weather_duration":
                return String.valueOf(p.getWorld().getWeatherDuration());
            case "world_time":
                return String.valueOf(p.getWorld().getTime());
            case "world_time_12":
                return format12(p.getWorld().getTime());
            case "world_time_24":
                return format24(p.getWorld().getTime());
            case "is_flying":
                return bool(p.isFlying());
            case "is_sleeping":
                return bool(p.isSleeping());
            case "is_conversing":
                return bool(p.isConversing());
            case "is_dead":
                return bool(p.isDead());
            case "is_sneaking":
                return bool(p.isSneaking());
            case "is_sprinting":
                return bool(p.isSprinting());
            case "is_leashed":
                return bool(p.isLeashed());
            case "is_inside_vehicle":
                return bool(p.isInsideVehicle());
        }
        // return null for unknown placeholders
        return null;
    }

    @Override
    public boolean register() {
        low = this.getString("ping_color.low", "&a");
        medium = this.getString("ping_color.medium", "&e");
        high = this.getString("ping_color.high", "&c");
        mediumValue = this.getInt("ping_value.medium", 50);
        highValue = this.getInt("ping_value.high", 100);
        north = this.getString("direction.north", "N");
        northEast = this.getString("direction.north_east", "NE");
        east = this.getString("direction.east", "E");
        southEast = this.getString("direction.south_east", "SE");
        south = this.getString("direction.south", "S");
        southWest = this.getString("direction.south_west", "SW");
        west = this.getString("direction.west", "W");
        northWest = this.getString("direction.north_west", "NW");


        return super.register();
    }

    public String bool(boolean b) {
        return b ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }


    private String retrievePing(final Player player, final boolean colored) {
        final int ping = PlayerUtil.getPing(player);
        if (!colored) {
            return String.valueOf(ping);
        }

        return ChatColor.translateAlternateColorCodes('&', ping > highValue ? high : ping > mediumValue ? medium : low) + ping;
    }

}
