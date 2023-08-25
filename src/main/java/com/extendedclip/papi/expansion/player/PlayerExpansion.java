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

import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Taskable;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
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

public final class PlayerExpansion extends PlaceholderExpansion implements Taskable, Configurable {

    @Getter private final String identifier = "player";
    @Getter private final String author = "clip";
    @Getter private final String version = "2.1.0";
    @Getter private final Map<String, Object> defaults;

    private final SimpleDateFormat dateFormat = PlaceholderAPIPlugin.getDateFormat();
    private final VersionHelper versionHelper;

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

    public PlayerExpansion() {
        defaults = new HashMap<>() {{
            put("ping_color.high", "&c");
            put("ping_color.medium", "&e");
            put("ping_color.low", "&a");
            put("ping_value.medium", 50);
            put("ping_value.high", 100);
            put("direction.north", "N");
            put("direction.north_east", "NE");
            put("direction.east", "E");
            put("direction.south_east", "SE");
            put("direction.south", "S");
            put("direction.south_west", "SW");
            put("direction.west", "W");
            put("direction.north_west", "NW");
        }};
        versionHelper = new VersionHelper();
    }

    @Override
    public void start() {
        low = getString("ping_color.low", "&a");
        medium = getString("ping_color.medium", "&e");
        high = getString("ping_color.high", "&c");
        mediumValue = getInt("ping_value.medium", 50);
        highValue = getInt("ping_value.high", 100);

        north = getString("direction.north", "N");
        northEast = getString("direction.north_east", "NE");
        east = getString("direction.east", "E");
        southEast = getString("direction.south_east", "SE");
        south = getString("direction.south", "S");
        southWest = getString("direction.south_west", "SW");
        west = getString("direction.west", "W");
        northWest = getString("direction.north_west", "NW");


    }

    @Override
    public void stop() {}

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {

        final boolean targetedPing = identifier.startsWith("ping_");
        final boolean targetedColoredPing = identifier.startsWith("colored_ping_");
        if (targetedPing || targetedColoredPing) {
            final Player target = Bukkit.getPlayer(identifier.substring(targetedPing ? 5 : 13)); // yes, I know, magic value

            return target == null ? "0" : retrievePing(target, targetedColoredPing);
        }

        if (player == null) return "";

        // offline placeholders
        return switch (identifier) {
            case "name" -> player.getName();
            case "uuid" -> player.getUniqueId().toString();
            case "has_played_before" -> bool(player.hasPlayedBefore());
            case "online" -> bool(player.isOnline());
            case "is_whitelisted" -> bool(player.isWhitelisted());
            case "is_banned" -> bool(player.isBanned());
            case "is_op" -> bool(player.isOp());
            case "first_played", "first_join" -> String.valueOf(player.getFirstPlayed());
            case "first_played_formatted", "first_join_date" -> dateFormat.format(new Date(player.getFirstPlayed()));
            case "last_played", "last_join" -> String.valueOf(player.getLastPlayed());
            case "last_played_formatted", "last_join_date" -> dateFormat.format(new Date(player.getLastPlayed()));
            case "bed_x", "bed_y", "bed_z", "bed_world" -> PlayerUtil.getBedLocation(player,identifier.substring(4));
            default -> {
                // online placeholders
                if (!player.isOnline()) yield "";

                Player p = player.getPlayer();

                // to get rid of IDE warnings
                if (p == null) yield "";

                if (identifier.startsWith("has_permission_"))
                    yield bool(p.hasPermission(identifier.substring(15)));

                if (identifier.startsWith("has_potioneffect_")) {
                    String effect = identifier.substring(17);
                    PotionEffectType potion = PotionEffectType.getByName(effect);
                    yield bool(potion != null && p.hasPotionEffect(potion));
                }

                if (identifier.startsWith("item_in_hand_level_") || identifier.startsWith("item_in_offhand_level_"))
                    yield PlayerUtil.getItemEnchantment(p,identifier,identifier.startsWith("item_in_hand_level_"));

                if (identifier.startsWith("locale")) {
                    String localeStr = versionHelper.getLocale(p);

                    yield switch (identifier) {
                        case "locale" -> localeStr;
                        case "locale_short" -> localeStr.substring(0, localeStr.indexOf("_"));
                        case "locale_country", "locale_display_country", "locale_display_name" -> {
                            String localeStrISO = localeStr.replace("_", "-");
                            Locale locale = Locale.forLanguageTag(localeStrISO);
                            if (locale == null) yield "";
                            yield switch (identifier) {
                                case "locale_country" -> locale.getCountry();
                                case "locale_display_country" -> locale.getDisplayCountry();
                                case "locale_display_name" -> locale.getDisplayName();
                                default -> null;
                            };
                        }
                        default -> null;
                    };
                }

                yield switch (identifier) {
                    case "absorption" -> String.valueOf(versionHelper.getAbsorption(p));
                    case "has_empty_slot" -> bool(p.getInventory().firstEmpty() > -1);
                    case "empty_slots" -> String.valueOf(getEmptySlots(p));
                    case "displayname" -> p.getDisplayName();
                    case "list_name" -> p.getPlayerListName();
                    case "gamemode" -> p.getGameMode().name();
                    case "direction" -> switch (getDirection(p)) {
                            case NORTH -> north;
                            case NORTH_EAST -> northEast;
                            case EAST -> east;
                            case SOUTH_EAST -> southEast;
                            case SOUTH -> south;
                            case SOUTH_WEST -> southWest;
                            case WEST -> west;
                            case NORTH_WEST -> northWest;
                            default -> "";
                    };
                    case "direction_xz" -> getXZDirection(p);
                    case "world" -> p.getWorld().getName();
                    case "world_type" -> switch (p.getWorld().getEnvironment()) {
                        case NETHER -> "Nether";
                        case THE_END -> "The End";
                        case NORMAL -> "Overworld";
                        case CUSTOM -> "Custom";
                    };
                    case "x" -> String.valueOf(p.getLocation().getBlockX());
                    case "x_long" -> String.valueOf(p.getLocation().getX());
                    case "y" -> String.valueOf(p.getLocation().getBlockY());
                    case "y_long" -> String.valueOf(p.getLocation().getY());
                    case "z" -> String.valueOf(p.getLocation().getBlockZ());
                    case "z_long" -> String.valueOf(p.getLocation().getZ());
                    case "yaw" -> String.valueOf(p.getLocation().getYaw());
                    case "pitch" -> String.valueOf(p.getLocation().getPitch());
                    case "biome" -> getBiome(p);
                    case "biome_capitalized" -> getCapitalizedBiome(p);
                    case "light_level" -> String.valueOf(p.getLocation().getBlock().getLightLevel());
                    case "ip" -> p.getAddress().getAddress().getHostAddress();
                    case "allow_flight" -> bool(p.getAllowFlight());
                    case "can_pickup_items" -> bool(p.getCanPickupItems());
                    case "compass_x" -> p.getCompassTarget() != null ? String.valueOf(p.getCompassTarget().getBlockX()) : "";
                    case "compass_y" -> p.getCompassTarget() != null ? String.valueOf(p.getCompassTarget().getBlockY()) : "";
                    case "compass_z" -> p.getCompassTarget() != null ? String.valueOf(p.getCompassTarget().getBlockZ()) : "";
                    case "compass_world" -> p.getCompassTarget() != null ? p.getCompassTarget().getWorld().getName() : "";
                    case "block_underneath" -> String.valueOf(p.getLocation().clone().subtract(0, 1, 0).getBlock().getType());
                    case "custom_name" -> p.getCustomName() != null ? p.getCustomName() : p.getName();
                    case "exp" -> String.valueOf(p.getExp());
                    case "current_exp" -> String.valueOf(getTotalExperience(p));
                    case "total_exp" -> String.valueOf(p.getTotalExperience());
                    case "exp_to_level" -> String.valueOf(p.getExpToLevel());
                    case "level" -> String.valueOf(p.getLevel());
                    case "fly_speed" -> String.valueOf(p.getFlySpeed());
                    case "food_level" -> String.valueOf(p.getFoodLevel());
                    case "health" -> String.valueOf(p.getHealth());
                    case "health_rounded" -> String.valueOf(Math.round(p.getHealth()));
                    case "health_scale" -> String.valueOf(p.getHealthScale());
                    case "has_health_boost" -> bool(p.hasPotionEffect(PotionEffectType.HEALTH_BOOST));
                    case "health_boost" -> p.getHealthScale() > 20 ? Double.toString(p.getHealthScale() - 20) : "0";
                    case "item_in_hand" -> String.valueOf(itemInHand(p).getType());
                    case "item_in_hand_name" ->
                            itemInHand(p).getType() != Material.AIR && itemInHand(p).getItemMeta().hasDisplayName() ? itemInHand(p).getItemMeta().getDisplayName() : "";
                    case "item_in_hand_data" ->
                            itemInHand(p).getType() != Material.AIR ? String.valueOf(itemInHand(p).getDurability()) : "0";
                    case "item_in_hand_durability" -> String.valueOf(durability(itemInHand(p)));
                    case "item_in_offhand" -> String.valueOf(p.getInventory().getItemInOffHand().getType());
                    case "item_in_offhand_name" ->
                            p.getInventory().getItemInOffHand().getType() != Material.AIR && p.getInventory().getItemInOffHand().getItemMeta().hasDisplayName() ? p.getInventory().getItemInOffHand().getItemMeta().getDisplayName() : "";
                    case "item_in_offhand_data" ->
                            p.getInventory().getItemInOffHand().getType() != Material.AIR ? String.valueOf(p.getInventory().getItemInOffHand().getDurability()) : "0";
                    case "item_in_offhand_durability" ->
                            String.valueOf(durability(p.getInventory().getItemInOffHand()));
                    case "last_damage" -> String.valueOf(p.getLastDamage());
                    case "max_health" -> String.valueOf(p.getMaxHealth());
                    case "max_health_rounded" -> String.valueOf(Math.round(p.getMaxHealth()));
                    case "max_air" -> String.valueOf(p.getMaximumAir());
                    case "max_no_damage_ticks" -> String.valueOf(p.getMaximumNoDamageTicks());
                    case "no_damage_ticks" -> String.valueOf(p.getNoDamageTicks());
                    case "armor_helmet_name" ->
                            Optional.ofNullable(p.getInventory().getHelmet()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
                    case "armor_helmet_data" ->
                            p.getInventory().getHelmet() != null ? String.valueOf(p.getInventory().getHelmet().getDurability()) : "0";
                    case "armor_helmet_durability" -> String.valueOf(durability(p.getInventory().getHelmet()));
                    case "armor_chestplate_name" ->
                            Optional.ofNullable(p.getInventory().getChestplate()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
                    case "armor_chestplate_data" ->
                            p.getInventory().getChestplate() != null ? String.valueOf(p.getInventory().getChestplate().getDurability()) : "0";
                    case "armor_chestplate_durability" -> String.valueOf(durability(p.getInventory().getChestplate()));
                    case "armor_leggings_name" ->
                            Optional.ofNullable(p.getInventory().getLeggings()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
                    case "armor_leggings_data" ->
                            p.getInventory().getLeggings() != null ? String.valueOf(p.getInventory().getLeggings().getDurability()) : "0";
                    case "armor_leggings_durability" -> String.valueOf(durability(p.getInventory().getLeggings()));
                    case "armor_boots_name" ->
                            Optional.ofNullable(p.getInventory().getBoots()).map(a -> a.getItemMeta().getDisplayName()).orElse("");
                    case "armor_boots_data" ->
                            p.getInventory().getBoots() != null ? String.valueOf(p.getInventory().getBoots().getDurability()) : "0";
                    case "armor_boots_durability" -> String.valueOf(durability(p.getInventory().getBoots()));
                    case "ping" -> retrievePing(p, false);
                    case "colored_ping" -> retrievePing(p, true);
                    case "time" -> String.valueOf(p.getPlayerTime());
                    case "time_offset" -> String.valueOf(p.getPlayerTimeOffset());
                    case "remaining_air" -> String.valueOf(p.getRemainingAir());
                    case "saturation" -> String.valueOf(p.getSaturation());
                    case "sleep_ticks" -> String.valueOf(p.getSleepTicks());
                    case "thunder_duration" -> String.valueOf(p.getWorld().getThunderDuration());
                    case "ticks_lived" -> String.valueOf(p.getTicksLived());
                    case "seconds_lived" -> String.valueOf(p.getTicksLived() / 20);
                    case "minutes_lived" -> String.valueOf((p.getTicksLived() / 20) / 60);
                    case "walk_speed" -> String.valueOf(p.getWalkSpeed());
                    case "weather_duration" -> String.valueOf(p.getWorld().getWeatherDuration());
                    case "world_time" -> String.valueOf(p.getWorld().getTime());
                    case "world_time_12" -> format12(p.getWorld().getTime());
                    case "world_time_24" -> format24(p.getWorld().getTime());
                    case "is_flying" -> bool(p.isFlying());
                    case "is_sleeping" -> bool(p.isSleeping());
                    case "is_conversing" -> bool(p.isConversing());
                    case "is_dead" -> bool(p.isDead());
                    case "is_sneaking" -> bool(p.isSneaking());
                    case "is_sprinting" -> bool(p.isSprinting());
                    case "is_leashed" -> bool(p.isLeashed());
                    case "is_inside_vehicle" -> bool(p.isInsideVehicle());
                    default -> null;
                };
            }
        };
    }

    public String bool(boolean b) {
        return b ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }


    private String retrievePing(final Player player, final boolean colored) {
        final int ping = versionHelper.getPing(player);
        if (!colored) return String.valueOf(ping);

        return ChatColor.translateAlternateColorCodes('&', ping > highValue ? high : ping > mediumValue ? medium : low) + ping;
    }

}
