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
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.text.SimpleDateFormat;
import java.util.*;

public final class PlayerExpansion extends PlaceholderExpansion implements Taskable, Configurable {

    @Getter private final String identifier = "player";
    @Getter private final String author = "clip";
    @Getter private final String version = "2.1.0";
    @Getter private final Map<String, Object> defaults;

    private final SimpleDateFormat dateFormat = PlaceholderAPIPlugin.getDateFormat();
    private final VersionHelper versionHelper;
    private final PlayerListener listener;

    final Map<Player, Long> joinTimes = new HashMap<>();
    private final Map<String,String> pingColors = new HashMap<>();
    private final Map<BlockFace,String> directions = new HashMap<>();

    public PlayerExpansion() {
        defaults = new HashMap<>() {{
            put("ping",new HashMap<>() {{
                put("-1", "&c");
                put("0-100", "&a");
                put("100-150", "&e");
                put("150-", "&c");
            }});

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
        listener = new PlayerListener(this);
    }

    @Override
    public void start() {
        ConfigurationSection ping = getConfigSection("ping");
        if (ping != null) ping.getValues(false).forEach((range,color)->pingColors.put(range,String.valueOf(color)));


        ConfigurationSection directionsCfg = getConfigSection("direction");
        if (directionsCfg != null) directionsCfg.getValues(false).forEach((direction,output)->{
            try {
                BlockFace face = BlockFace.valueOf(direction.toUpperCase());
                directions.put(face,String.valueOf(output));
            }
            catch (Exception ignored) {}
        });

        Bukkit.getServer().getPluginManager().registerEvents(listener,getPlaceholderAPI());
    }

    @Override
    public void stop() {
        HandlerList.unregisterAll(listener);
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {

        final boolean targetedColoredPing = identifier.startsWith("colored_ping_");
        if (identifier.startsWith("ping_") || targetedColoredPing) {
            final Player target = Bukkit.getServer().getPlayer(identifier.substring(targetedColoredPing ? 13 : 5));
            return target == null ? "0" : retrievePing(target, targetedColoredPing);
        }

        if (player == null) return "";

        Object output = request(player,identifier);
        return output == null ? null
                : output instanceof Boolean bool ?
                    bool ? PlaceholderAPIPlugin.booleanTrue()
                            : PlaceholderAPIPlugin.booleanFalse()
                : String.valueOf(output);
    }

    public Object request(OfflinePlayer player, String identifier) {
        return switch (identifier) {
            // offline placeholders
            case "name" -> player.getName();
            case "uuid" -> player.getUniqueId();
            case "has_played_before" -> player.hasPlayedBefore();
            case "online" -> player.isOnline();
            case "is_whitelisted" -> player.isWhitelisted();
            case "is_banned" -> player.isBanned();
            case "is_op" -> player.isOp();
            case "first_played", "first_join" -> player.getFirstPlayed();
            case "first_played_formatted", "first_join_date" -> dateFormat.format(new Date(player.getFirstPlayed()));
            case "last_played", "last_join" -> player.getLastPlayed();
            case "last_played_formatted", "last_join_date" -> dateFormat.format(new Date(player.getLastPlayed()));
            case "time_since_last_played", "time_since_last_join" -> PlayerUtil.msToSToStr(player.getLastPlayed());
            case "bed_x", "bed_y", "bed_z", "bed_world" -> PlayerUtil.getLocation(player.getBedSpawnLocation(),identifier.substring(4));
            default -> {
                // online placeholders
                if (!player.isOnline()) yield "";

                Player p = player.getPlayer();

                // to get rid of IDE warnings
                if (p == null) yield "";

                if (identifier.startsWith("has_permission_"))
                    yield p.hasPermission(identifier.substring(15));

                if (identifier.startsWith("has_potion_effect_")) {
                    String effect = identifier.substring(18);
                    PotionEffectType potion = PotionEffectType.getByName(effect);
                    yield potion != null && p.hasPotionEffect(potion);
                }

                if (identifier.startsWith("potion_effect_level_")) {
                    String effect = identifier.substring(20);
                    PotionEffectType potion = PotionEffectType.getByName(effect);
                    if (potion == null || !p.hasPotionEffect(potion)) yield "0";
                    PotionEffect potionEffect = p.getPotionEffect(potion);
                    yield potionEffect == null ? "0" : potionEffect.getAmplifier();
                }

                if (identifier.startsWith("has_unlocked_recipe_")) {
                    String recipe = identifier.substring(20);
                    NamespacedKey key = NamespacedKey.fromString(recipe);
                    yield key != null && p.hasDiscoveredRecipe(key);
                }

                if (identifier.startsWith("item_in_hand_level_"))
                    yield PlayerUtil.getItemEnchantment(identifier.substring(19),versionHelper.getItemInHand(p));
                if (identifier.startsWith("item_in_offhand_level_"))
                    yield PlayerUtil.getItemEnchantment(identifier.substring(22),p.getInventory().getItemInOffHand());

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
                    case "displayname" -> p.getDisplayName();
                    case "list_name" -> p.getPlayerListName();
                    case "custom_name" -> p.getCustomName() != null ? p.getCustomName() : p.getName();
                    case "gamemode" -> p.getGameMode().name();
                    case "ip" -> p.getAddress() == null ? "" : p.getAddress().getAddress().getHostAddress();
                    case "ping" -> retrievePing(p, false);
                    case "colored_ping" -> retrievePing(p, true);

                    case "is_sleeping" -> p.isSleeping();
                    case "is_conversing" -> p.isConversing();
                    case "is_dead" -> p.isDead();
                    case "is_sneaking" -> p.isSneaking();
                    case "is_sprinting" -> p.isSprinting();
                    case "is_leashed" -> p.isLeashed();
                    case "is_inside_vehicle" -> p.isInsideVehicle();

                    case "world" -> p.getWorld().getName();
                    case "world_type" -> switch (p.getWorld().getEnvironment()) {
                        case NETHER -> "Nether";
                        case THE_END -> "The End";
                        case NORMAL -> "Overworld";
                        case CUSTOM -> "Custom";
                    };
                    case "biome","biome_capitalized" -> {
                        String biome = String.valueOf(p.getLocation().getBlock().getBiome());
                        if (identifier.equals("biome")) yield biome;

                        String[] biomeWords = biome.split("_");
                        for (int i = 0; i < biomeWords.length; i++) {
                            biomeWords[i] = biomeWords[i].substring(0, 1).toUpperCase() + biomeWords[i].substring(1).toLowerCase();
                        }
                        yield String.join(" ", biomeWords);
                    }

                    case "x_long", "y_long", "z_long", "yaw", "pitch" -> PlayerUtil.getLocation(p.getLocation(),identifier.contains("_")
                            ? identifier.substring(0,identifier.indexOf("_"))
                            : identifier);
                    case "x","y","z" -> PlayerUtil.getLocation(p.getLocation(),"block_"+identifier);
                    case "block_underneath" -> p.getLocation().clone().subtract(0, 1, 0).getBlock().getType();
                    case "compass_x", "compass_y", "compass_z", "compass_world" -> PlayerUtil.getLocation(p.getCompassTarget(),identifier.substring(8));

                    case "light_level" -> p.getLocation().getBlock().getLightLevel();

                    case "direction" -> {
                        BlockFace direction = PlayerUtil.getDirection(p);
                        yield directions.getOrDefault(direction,direction.toString());
                    }
                    case "direction_xz" -> PlayerUtil.getXZDirection(p);

                    case "allow_flight" -> p.getAllowFlight();
                    case "is_flying" -> p.isFlying();
                    case "fly_speed" -> p.getFlySpeed();
                    case "walk_speed" -> p.getWalkSpeed();

                    case "exp" -> p.getExp();
                    case "current_exp" -> PlayerUtil.getTotalExperience(p);
                    case "total_exp" -> p.getTotalExperience();
                    case "exp_to_level" -> p.getExpToLevel();
                    case "level" -> p.getLevel();

                    case "absorption" -> versionHelper.getAbsorption(p);
                    case "health" -> p.getHealth();
                    case "health_rounded" -> Math.round(p.getHealth());
                    case "health_scale" -> p.getHealthScale();
                    case "has_health_boost" -> p.hasPotionEffect(PotionEffectType.HEALTH_BOOST);
                    case "health_boost" -> PlayerUtil.getHealthBoost(p);
                    case "health_full" -> p.getHealth()+versionHelper.getAbsorption(p)+PlayerUtil.getHealthBoost(p);
                    case "health_full_rounded" -> Math.round(p.getHealth()+versionHelper.getAbsorption(p)+PlayerUtil.getHealthBoost(p));
                    case "max_health" -> versionHelper.getMaxHealth(p);
                    case "max_health_rounded" -> Math.round(versionHelper.getMaxHealth(p));
                    case "food_level" -> p.getFoodLevel();
                    case "saturation" -> p.getSaturation();
                    case "max_air" -> p.getMaximumAir();
                    case "remaining_air" -> p.getRemainingAir();
                    case "max_no_damage_ticks" -> p.getMaximumNoDamageTicks();
                    case "no_damage_ticks" -> p.getNoDamageTicks();
                    case "last_damage" -> p.getLastDamage();

                    case "has_empty_slot" -> p.getInventory().firstEmpty() > -1;
                    case "empty_slots" -> PlayerUtil.getEmptySlots(p);
                    case "can_pickup_items" -> p.getCanPickupItems();

                    case "item_in_hand","item_in_hand_name","item_in_hand_data","item_in_hand_durability",
                            "item_in_offhand","item_in_offhand_name","item_in_offhand_data","item_in_offhand_durability",
                            "armor_helmet_name","armor_helmet_data","armor_helmet_durability",
                            "armor_chestplate_name","armor_chestplate_data","armor_chestplate_durability",
                            "armor_leggings_name","armor_leggings_data","armor_leggings_durability",
                            "armor_boots_name","armor_boots_data","armor_boots_durability" -> {
                        if (identifier.startsWith("item_in_hand")) yield requestItem(identifier,12,versionHelper.getItemInHand(p));
                        if (identifier.startsWith("item_in_offhand")) yield requestItem(identifier,15,p.getInventory().getItemInOffHand());
                        if (identifier.startsWith("armor_helmet")) yield requestItem(identifier,12,p.getInventory().getHelmet());
                        if (identifier.startsWith("armor_chestplate")) yield requestItem(identifier,16,p.getInventory().getChestplate());
                        if (identifier.startsWith("armor_leggings")) yield requestItem(identifier,14,p.getInventory().getLeggings());
                        if (identifier.startsWith("armor_boots")) yield requestItem(identifier,11,p.getInventory().getBoots());
                        yield null;
                    }

                    case "time_since_join" -> PlayerUtil.msToSToStr(joinTimes.getOrDefault(p,0L));
                    case "sleep_ticks" -> p.getSleepTicks();
                    case "thunder_duration" -> p.getWorld().getThunderDuration();
                    case "ticks_lived" -> p.getTicksLived();
                    case "seconds_lived" -> p.getTicksLived() / 20;
                    case "minutes_lived" -> p.getTicksLived() / 1200;

                    case "world_time" -> p.getWorld().getTime();
                    case "world_time_12" -> PlayerUtil.format12(p.getWorld().getTime());
                    case "world_time_24" -> PlayerUtil.format24(p.getWorld().getTime());
                    case "time" -> p.getPlayerTime();
                    case "time_offset" -> p.getPlayerTimeOffset();
                    case "weather_duration" -> p.getWorld().getWeatherDuration();

                    default -> null;
                };
            }
        };
    }

    private Object requestItem(String identifier, int substring, ItemStack item) {
        identifier = identifier.substring(substring);
        if (item == null) return identifier.isEmpty() || identifier.equals("_name") ? ""
                : identifier.equals("_data") || identifier.equals("_durability") ? "0" : null;
        return switch (identifier) {
            case "" -> item.getType();
            case "_name" -> {
                ItemMeta meta = item.getItemMeta();
                yield item.getType() != Material.AIR && meta != null && meta.hasDisplayName() ? meta.getDisplayName() : "";
            }
            case "_lore" -> {
                ItemMeta meta = item.getItemMeta();
                yield meta != null && meta.hasLore() && meta.getLore() != null ? String.join("\n",meta.getLore()) : "";
            }
            case "_data", "_durability" -> {
                int damage = versionHelper.getItemDamage(item);
                yield identifier.equals("_data") ? damage : item.getType().getMaxDurability() - damage;
            }
            default -> null;
        };
    }

    private String retrievePing(final Player player, final boolean colored) {
        int ping = versionHelper.getPing(player);
        String pingStr = String.valueOf(ping);
        if (!colored) return pingStr;

        for (String range : pingColors.keySet()) {
            if (range.contains("-") && !range.startsWith("-")) {
                String[] bounds = range.split("-");
                int bound1 = Integer.parseInt(bounds[0]);
                if (bounds.length == 1 && ping < bound1) continue;
                int bound2 = Integer.parseInt(bounds[1]);
                if (ping < bound1 || ping > bound2) continue;
            } else if (!pingStr.equals(range)) continue;

            return ChatColor.translateAlternateColorCodes('&', pingColors.get(range)+pingStr);
        }
        return pingStr;
    }

}
