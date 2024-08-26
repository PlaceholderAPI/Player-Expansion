package at.helpch.placeholderapi.expansion.player;

import at.helpch.placeholderapi.expansion.player.handler.PlayerLocaleHandler;
import at.helpch.placeholderapi.expansion.player.ping.PingFormatter;
import at.helpch.placeholderapi.expansion.player.util.ItemUtil;
import at.helpch.placeholderapi.expansion.player.util.PlayerUtil;
import at.helpch.placeholderapi.expansion.player.util.TimeUtil;
import at.helpch.placeholderapi.expansion.player.util.VersionHelper;
import com.google.common.collect.ImmutableMap;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public final class PlayerExpansion extends PlaceholderExpansion implements Configurable {

    private PingFormatter pingFormatter;
    private Map<String, Object> directionNames;

    @Override
    public @NotNull String getIdentifier() {
        return "player";
    }

    @Override
    public @NotNull String getAuthor() {
        return "HelpChat";
    }

    @Override
    public @NotNull String getVersion() {
        return "2.1.0";
    }

    @Override
    public Map<String, Object> getDefaults() {
        return ImmutableMap.<String, Object>builder()
            .put("direction.north", "N")
            .put("direction.north_east", "NE")
            .put("direction.east", "E")
            .put("direction.south_east", "SE")
            .put("direction.south", "S")
            .put("direction.south_west", "SW")
            .put("direction.west", "W")
            .put("direction.north_west", "NW")
            .put("ping.formatting.>200", "&4")
            .put("ping.formatting.>150", "&c")
            .put("ping.formatting.>100", "&e")
            .put("ping.formatting.>75", "&2")
            .put("ping.formatting.>50", "&a")
            .put("ping.formatting.>25", "&b")
            .put("ping.formatting.=0", "&9")
            .build();
    }

    @Override
    public boolean canRegister() {
        pingFormatter = new PingFormatter(getConfigSection("ping.formatting"));
        directionNames = Optional.ofNullable(getConfigSection("direction"))
            .map(section -> section.getValues(false))
            .orElseGet(HashMap::new);
        return true;
    }

    /**
     * Format a {@code boolean} as {@link PlaceholderAPIPlugin#booleanTrue()} or {@link PlaceholderAPIPlugin#booleanFalse()}
     *
     * @param bool boolean
     * @return {@link PlaceholderAPIPlugin#booleanTrue()} if the boolean is true, otherwise {@link PlaceholderAPIPlugin#booleanFalse()}
     */
    private @NotNull String bool(final boolean bool) {
        return bool ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
    }

    private @NotNull String getItemInfo(@Nullable final ItemStack item, @NotNull final String args) {
        switch (args) {
            case "_name":
                return ItemUtil.getName(item);
            case "_data":
                return String.valueOf(ItemUtil.getDamage(item));
            case "_durability":
                return String.valueOf(ItemUtil.getDurability(item));
            default:
                return ItemUtil.getMaterialName(item);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        final boolean targetedPing = params.startsWith("ping_");
        final boolean targetedColoredPing = params.startsWith("colored_ping_");

        if (targetedPing || targetedColoredPing) {
            final String targetName = params.substring((targetedPing ? "ping_" : "colored_ping_").length());
            return Optional.ofNullable(Bukkit.getPlayerExact(targetName))
                .map(target -> pingFormatter.getPing(target, targetedColoredPing))
                .orElse("0");
        }

        if (offlinePlayer == null) {
            return "";
        }

        switch (params) {
            case "name":
                return offlinePlayer.getName();
            case "uuid":
                return offlinePlayer.getUniqueId().toString();
            case "first_played":
            case "first_join":
                return String.valueOf(offlinePlayer.getFirstPlayed());
            case "first_played_formatted":
            case "first_join_date":
                return PlaceholderAPIPlugin.getDateFormat().format(new Date(offlinePlayer.getFirstPlayed()));
            case "last_played":
            case "last_join":
                //noinspection deprecation
                return String.valueOf(offlinePlayer.getLastPlayed());
            case "last_played_formatted":
            case "last_join_date":
                //noinspection deprecation
                return PlaceholderAPIPlugin.getDateFormat().format(new Date(offlinePlayer.getLastPlayed()));

            //<editor-fold desc="Bed location placeholders">
            case "bed_is_set":
                return bool(offlinePlayer.getBedSpawnLocation() != null);
            case "bed_x":
                return PlayerUtil.getBedLocationInfo(offlinePlayer, "0", Location::getBlockX);
            case "bed_y":
                return PlayerUtil.getBedLocationInfo(offlinePlayer, "0", Location::getBlockY);
            case "bed_z":
                return PlayerUtil.getBedLocationInfo(offlinePlayer, "0", Location::getBlockZ);
            case "bed_world":
                return PlayerUtil.getBedLocationInfo(offlinePlayer, "N/A", location -> location.getWorld().getName());
            //</editor-fold>

            //<editor-fold desc="Various boolean placeholders">
            case "has_played_before":
                return bool(offlinePlayer.hasPlayedBefore());
            case "is_banned":
                return bool(offlinePlayer.isBanned());
            case "is_op":
                return bool(offlinePlayer.isOp());
            case "is_whitelisted":
                return bool(offlinePlayer.isWhitelisted());
            case "online":
                return bool(offlinePlayer.isOnline());
            //</editor-fold>
        }

        if (!offlinePlayer.isOnline()) {
            return "";
        }

        final Player player = offlinePlayer.getPlayer();

        // To get rid of IDE warnings
        if (player == null) {
            return "";
        }

        switch (params) {
            //<editor-fold desc="General placeholders">
            case "displayname":
                //noinspection deprecation
                return player.getDisplayName();
            case "custom_name":
                return Optional.ofNullable(player.getCustomName()).orElseGet(player::getName);
            case "list_name":
                //noinspection deprecation
                return player.getPlayerListName();

            case "absorption": {
                if (VersionHelper.HAS_ABSORPTION_METHODS) {
                    return String.valueOf((int) player.getAbsorptionAmount());
                } else {
                    return "-1";
                }
            }
            case "fly_speed":
                return String.valueOf(player.getFlySpeed());
            case "food_level":
                return String.valueOf(player.getFoodLevel());

            case "gamemode":
                return player.getGameMode().name();

            case "health":
                return String.valueOf(player.getHealth());
            case "health_rounded":
                return String.valueOf(Math.round(player.getHealth()));
            case "health_scale":
                return String.valueOf(player.getHealthScale());
            case "health_boost": {
                if (player.getHealthScale() > 20) {
                    return String.valueOf(player.getHealthScale() - 20);
                } else {
                    return "0";
                }
            }

            case "last_damage":
                return String.valueOf(player.getLastDamage());
            case "max_air":
                return String.valueOf(player.getMaximumAir());
            case "max_health":
                return String.valueOf(PlayerUtil.getMaxHealth(player));
            case "max_health_rounded":
                return String.valueOf(Math.round(PlayerUtil.getMaxHealth(player)));
            case "max_no_damage_ticks":
                return String.valueOf(player.getMaximumNoDamageTicks());
            case "no_damage_ticks":
                return String.valueOf(player.getNoDamageTicks());
            case "remaining_air":
                return String.valueOf(player.getRemainingAir());
            case "saturation":
                return String.valueOf(player.getSaturation());
            case "sleep_ticks":
                return String.valueOf(player.getSleepTicks());
            case "ticks_lived":
                return String.valueOf(player.getTicksLived());
            case "seconds_lived":
                return String.valueOf(player.getTicksLived() / 20L);
            case "minutes_lived":
                return String.valueOf(TimeUnit.SECONDS.toMinutes(player.getTicksLived() / 20L));
            case "walk_speed":
                return String.valueOf(player.getWalkSpeed());

            case "ip":
                return Optional.ofNullable(player.getAddress())
                    .map(InetSocketAddress::getAddress)
                    .map(InetAddress::getHostAddress)
                    .orElse("");
            case "ping":
            case "colored_ping":
                return pingFormatter.getPing(player, params.startsWith("colored"));

            case "time":
                return String.valueOf(player.getPlayerTime());
            case "time_offset":
                return String.valueOf(player.getPlayerTimeOffset());
            //</editor-fold>

            //<editor-fold desc="Location / position placeholders">
            case "world":
                return player.getWorld().getName();
            case "world_type": {
                final World.Environment environment = player.getWorld().getEnvironment();
                if (environment == World.Environment.NETHER) {
                    return "Nether";
                } else if (environment == World.Environment.THE_END) {
                    return "The End";
                } else if (environment == World.Environment.NORMAL) {
                    return "Overworld";
                } else if (environment.name().equals("CUSTOM")) {
                    return "Custom";
                } else {
                    return environment.name();
                }
            }
            case "world_time":
                return String.valueOf(player.getWorld().getTime());
            case "world_time_12":
            case "world_time_24":
                return TimeUtil.formatWorldTime(player.getWorld().getTime(), params.equals("world_time_24"));
            case "weather_duration":
                return String.valueOf(player.getWorld().getWeatherDuration());
            case "thunder_duration":
                return String.valueOf(player.getWorld().getThunderDuration());

            case "direction":
                // NORTH_EAST -> north_east -> value from config (e.g. NE)
                return Optional.of(PlayerUtil.getDirection(player))
                    .map(BlockFace::name)
                    .map(String::toLowerCase)
                    .map(directionNames::get)
                    .map(String::valueOf)
                    .orElse("");
            case "direction_xz":
                return PlayerUtil.getXZDirection(player);
            case "x":
                return String.valueOf(player.getLocation().getBlockX());
            case "x_long":
                return String.valueOf(player.getLocation().getX());
            case "y":
                return String.valueOf(player.getLocation().getBlockY());
            case "y_long":
                return String.valueOf(player.getLocation().getY());
            case "z":
                return String.valueOf(player.getLocation().getBlockZ());
            case "z_long":
                return String.valueOf(player.getLocation().getZ());
            case "yaw":
                return String.valueOf(player.getLocation().getYaw());
            case "pitch":
                return String.valueOf(player.getLocation().getPitch());
            case "block_underneath":
                return String.valueOf(
                    player.getLocation()
                        .clone()
                        .subtract(0, 1, 0)
                        .getBlock()
                        .getType()
                );
            case "light_level":
                return String.valueOf(player.getLocation().getBlock().getLightLevel());
            case "biome":
            case "biome_capitalized": {
                final String biome = player.getLocation().getBlock().getBiome().toString();

                if (!params.endsWith("_capitalized")) {
                    return biome;
                }

                final StringJoiner joiner = new StringJoiner(" ");

                for (String part : biome.split("_")) {
                    joiner.add(part.substring(0, 1).toUpperCase() + part.substring(1).toLowerCase());
                }

                return joiner.toString();
            }

            case "compass_x":
                return PlayerUtil.getCompassLocationInfo(player, "0", Location::getBlockX);
            case "compass_y":
                return PlayerUtil.getCompassLocationInfo(player, "0", Location::getBlockY);
            case "compass_z":
                return PlayerUtil.getCompassLocationInfo(player, "0", Location::getBlockZ);
            case "compass_world":
                return PlayerUtil.getCompassLocationInfo(player, Bukkit.getWorlds().get(0).getName(), location -> location.getWorld().getName());
            //</editor-fold>

            //<editor-fold desc="Experience placeholders">
            case "exp":
                return String.valueOf(player.getExp());
            case "current_exp":
                return String.valueOf(PlayerUtil.getTotalExperience(player));
            case "total_exp":
                return String.valueOf(player.getTotalExperience());
            case "exp_to_level":
                return String.valueOf(player.getExpToLevel());
            case "level":
                return String.valueOf(player.getLevel());
            //</editor-fold>

            //<editor-fold desc="Inventory placeholders">
            case "has_empty_slot":
                return bool(player.getInventory().firstEmpty() > -1);
            case "empty_slots":
                return String.valueOf(PlayerUtil.getEmptySlotsCount(player));

            case "item_in_hand":
            case "item_in_hand_name":
            case "item_in_hand_data":
            case "item_in_hand_durability":
                return getItemInfo(PlayerUtil.getItemInMainHand(player), params.substring("item_in_hand".length()));

            case "item_in_offhand":
            case "item_in_offhand_name":
            case "item_in_offhand_data":
            case "item_in_offhand_durability":
                return getItemInfo(player.getInventory().getItemInOffHand(), params.substring("item_in_offhand".length()));

            case "armor_helmet":
            case "armor_helmet_name":
            case "armor_helmet_data":
            case "armor_helmet_durability":
                return getItemInfo(player.getInventory().getHelmet(), params.substring("armor_helmet".length()));

            case "armor_chestplate":
            case "armor_chestplate_name":
            case "armor_chestplate_data":
            case "armor_chestplate_durability":
                return getItemInfo(player.getInventory().getChestplate(), params.substring("armor_chestplate".length()));

            case "armor_leggings":
            case "armor_leggings_name":
            case "armor_leggings_data":
            case "armor_leggings_durability":
                return getItemInfo(player.getInventory().getLeggings(), params.substring("armor_leggings".length()));

            case "armor_boots":
            case "armor_boots_name":
            case "armor_boots_data":
            case "armor_boots_durability":
                return getItemInfo(player.getInventory().getBoots(), params.substring("armor_boots".length()));
            //</editor-fold>

            //<editor-fold desc="Various boolean placeholders">
            case "allow_flight":
                return bool(player.getAllowFlight());
            case "can_pickup_items":
                return bool(player.getCanPickupItems());
            case "has_health_boost":
                return bool(player.hasPotionEffect(PotionEffectType.HEALTH_BOOST));
            case "is_conversing":
                return bool(player.isConversing());
            case "is_dead":
                return bool(player.isDead());
            case "is_flying":
                return bool(player.isFlying());
            case "is_gliding":
                return bool(player.isGliding());
            case "is_inside_vehicle":
                return bool(player.isInsideVehicle());
            case "is_leashed":
                return bool(player.isLeashed());
            case "is_sleeping":
                return bool(player.isSleeping());
            case "is_sneaking":
                return bool(player.isSneaking());
            case "is_sprinting":
                return bool(player.isSprinting());
            //</editor-fold>

            //<editor-fold desc="⚠ Deprecated placeholders">
            case "server":
            case "servername":
                return "now available in the server expansion";
            //</editor-fold>
        }

        // has_permission_<permission.node>
        if (params.startsWith("has_permission_")) {
            final String permission = params.substring("has_permission_".length());
            return bool(player.hasPermission(permission));
        }

        // has_potioneffect_<effect>
        if (params.startsWith("has_potioneffect_")) {
            final String effectName = params.substring("has_potioneffect_".length());
            final Optional<PotionEffectType> effectType = Optional.ofNullable(PotionEffectType.getByName(effectName));

            if (!effectType.isPresent()) {
                return "Unknown potion effect " + effectName;
            }

            return effectType.map(player::hasPotionEffect)
                .map(this::bool)
                .get();
        }

        // potion_effect_level_<effect>
        if (params.startsWith("potion_effect_level_")) {
            final String effectName = params.substring("potion_effect_level_".length());
            final Optional<PotionEffectType> effectType = Optional.ofNullable(PotionEffectType.getByName(effectName));

            if (!effectType.isPresent()) {
                return "Unknown potion effect " + effectName;
            }

            return effectType.filter(player::hasPotionEffect)
                .map(player::getPotionEffect)
                .map(PotionEffect::getAmplifier)
                .map(String::valueOf)
                .orElse("0");
        }

        // item_in_hand_level_<enchantment name or key>
        if (params.startsWith("item_in_hand_level_")) {
            return ItemUtil.getEnchantmentLevel(PlayerUtil.getItemInMainHand(player), params.substring("item_in_hand_level_".length()));
        }

        // item_in_offhand_level_<enchantment name or key>
        if (params.startsWith("item_in_offhand_level_")) {
            return ItemUtil.getEnchantmentLevel(player.getInventory().getItemInOffHand(), params.substring("item_in_offhand_level_".length()));
        }

        // locale or locale_<option>
        if (params.startsWith("locale")) {
            final String localeString = PlayerLocaleHandler.INSTANCE.apply(player);
            final Locale locale = Locale.forLanguageTag(localeString.replace('_', '-'));

            switch (params) {
                case "locale":
                    return localeString;
                case "locale_country":
                    return Optional.ofNullable(locale).map(Locale::getCountry).orElse("");
                case "locale_display_country":
                    return Optional.ofNullable(locale).map(Locale::getDisplayCountry).orElse("");
                case "locale_display_name":
                    return Optional.ofNullable(locale).map(Locale::getDisplayName).orElse("");
                case "locale_short":
                    return localeString.substring(0, localeString.indexOf("_"));
            }
        }

        // has_unlocked_recipe_<recipe-key>
        if (params.startsWith("has_unlocked_recipe_") && VersionHelper.HAS_KEYED_API) {
            final String recipeName = params.substring("has_unlocked_recipe_".length());
            final NamespacedKey recipeKey = (recipeName.contains(":")) ? NamespacedKey.fromString(recipeName) : NamespacedKey.minecraft(recipeName);

            if (recipeKey == null) {
                return "Unknown recipe " + recipeName;
            }

            return bool(player.hasDiscoveredRecipe(recipeKey));
        }

        return null;
    }

}
