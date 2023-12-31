package at.helpch.placeholderapi.expansion.player;

import at.helpch.placeholderapi.expansion.player.util.ItemUtil;
import at.helpch.placeholderapi.expansion.player.util.PlayerUtil;
import at.helpch.placeholderapi.expansion.player.util.VersionHelper;
import com.google.common.collect.ImmutableMap;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.Configurable;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

public final class PlayerExpansion extends PlaceholderExpansion implements Configurable {

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
        return "3.0.0";
    }

    @Override
    public Map<String, Object> getDefaults() {
        return ImmutableMap.<String, Object>builder()
            .put("ping_color.high", "&c")
            .put("ping_color.medium", "&e")
            .put("ping_color.low", "&a")
            .put("ping_value.medium", 50)
            .put("ping_value.high", 100)
            .put("direction.north", "N")
            .put("direction.north_east", "NE")
            .put("direction.east", "E")
            .put("direction.south_east", "SE")
            .put("direction.south", "S")
            .put("direction.south_west", "SW")
            .put("direction.west", "W")
            .put("direction.north_west", "NW")
            .build();
    }

    @Override
    public boolean canRegister() {
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
                return String.valueOf(ItemUtil.getData(item));
            case "_durability":
                return String.valueOf(ItemUtil.getDurability(item));
            default:
                return ItemUtil.getMaterialName(item);
        }
    }

    @SuppressWarnings("SpellCheckingInspection")
    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        //TODO: add target ping placeholders

        if (offlinePlayer == null) {
            return "";
        }

        switch (params) {
            case "name":
                return offlinePlayer.getName();
            case "uuid":
                return offlinePlayer.getUniqueId().toString();
            case "has_played_before":
                return bool(offlinePlayer.hasPlayedBefore());
            case "is_whitelisted":
                return bool(offlinePlayer.isWhitelisted());
            case "is_banned":
                return bool(offlinePlayer.isBanned());
            case "is_op":
                return bool(offlinePlayer.isOnline());
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
            case "bed_x":
                return PlayerUtil.getBedCoordinate(offlinePlayer, Location::getX);
            case "bed_y":
                return PlayerUtil.getBedCoordinate(offlinePlayer, Location::getY);
            case "bed_z":
                return PlayerUtil.getBedCoordinate(offlinePlayer, Location::getZ);
            case "bed_world":
                return Optional.ofNullable(offlinePlayer.getBedSpawnLocation())
                    .map(Location::getWorld)
                    .map(World::getName)
                    .orElse("");
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
            case "displayname":
                //noinspection deprecation
                return player.getDisplayName();
            case "custom_name":
                return Optional.ofNullable(player.getCustomName()).orElseGet(player::getName);
            case "list_name":
                //noinspection deprecation
                return player.getPlayerListName();
            case "gamemode":
                return player.getGameMode().name();

            //<editor-fold desc="Location / position placeholders">
            //TODO: add direction and direction_xz
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
                } else {
                    return "";
                }
            }
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
            case "compass_x":
                return PlayerUtil.getCompassCoordinate(player, Location::getBlockX);
            case "compass_y":
                return PlayerUtil.getCompassCoordinate(player, Location::getBlockY);
            case "compass_z":
                return PlayerUtil.getCompassCoordinate(player, Location::getBlockZ);
            case "compass_world":
                //noinspection OptionalOfNullableMisuse
                return Optional.ofNullable(player.getCompassTarget())
                    .map(Location::getWorld)
                    .map(World::getName)
                    .orElse("");
            //</editor-fold>

            //TODO: exp placeholders
            case "fly_speed":
                return String.valueOf(player.getFlySpeed());
            case "food_level":
                return String.valueOf(player.getFoodLevel());
            case "health":
                return String.valueOf(player.getHealth());
            case "health_rounded":
                return String.valueOf(Math.round(player.getHealth()));
            case "health_scale":
                return String.valueOf(player.getHealthScale());
            case "has_health_boost":
                return bool(player.hasPotionEffect(PotionEffectType.HEALTH_BOOST));
            case "health_boost": {
                if (player.getHealthScale() > 20) {
                    return Double.toString(player.getHealthScale() - 20);
                } else {
                    return "0";
                }
            }
            case "absorption": {
                if (VersionHelper.HAS_ABSORPTION_METHODS) {
                    return String.valueOf((int) player.getAbsorptionAmount());
                } else {
                    return "-1";
                }
            }

            //<editor-fold desc="Inventory placeholders">
            case "has_empty_slot":
                return bool(player.getInventory().firstEmpty() > -1);
            case "empty_slots":
                return String.valueOf(PlayerUtil.getEmptySlots(player));

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
        }

        return super.onRequest(offlinePlayer, params);
    }

}
