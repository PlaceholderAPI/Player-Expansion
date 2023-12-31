package at.helpch.placeholderapi.expansion.player.util;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class PlayerUtil {

    private static final BlockFace[] radial = {
        BlockFace.NORTH, BlockFace.NORTH_EAST,
        BlockFace.EAST, BlockFace.SOUTH_EAST,
        BlockFace.SOUTH, BlockFace.SOUTH_WEST,
        BlockFace.WEST, BlockFace.NORTH_WEST
    };

    private static final PlayerPingHandler pingHandler = new PlayerPingHandler();
    private static final PlayerLocaleHandler localeHandler = new PlayerLocaleHandler();

    /**
     * Get player's item in hand using the right method depending on the server version
     *
     * @param player player
     * @return item in hand
     * @see Player#getItemInHand() method for < 1.9
     * @see PlayerInventory#getItemInMainHand() method for >= 1.9
     */
    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    public static @Nullable ItemStack getItemInMainHand(@NotNull final Player player) {
        return VersionHelper.HAS_OFF_HAND ? player.getInventory().getItemInMainHand() : player.getItemInHand();
    }

    public static @NotNull String getBedCoordinate(
        @NotNull final OfflinePlayer player, @NotNull final Function<@NotNull Location, @NotNull Number> coordinateMapper
    ) {
        return Optional.ofNullable(player.getBedSpawnLocation())
            .map(coordinateMapper)
            .map(String::valueOf)
            .orElse("0");
    }

    @SuppressWarnings("OptionalOfNullableMisuse")
    public static @NotNull String getCompassCoordinate(
        @NotNull final Player player, @NotNull final Function<@NotNull Location, @NotNull Number> coordinateMapper
    ) {
        return Optional.ofNullable(player.getCompassTarget())
            .map(coordinateMapper)
            .map(String::valueOf)
            .orElse("0");
    }

    public static int getEmptySlots(@NotNull final Player player) {
        final PlayerInventory inventory = player.getInventory();
        final List<ItemStack> items = new ArrayList<>(Arrays.asList(inventory.getContents()));
        items.add(inventory.getHelmet());
        items.add(inventory.getChestplate());
        items.add(inventory.getLeggings());
        items.add(inventory.getBoots());

        if (VersionHelper.HAS_OFF_HAND) {
            items.add(inventory.getItemInOffHand());
        }

        return (int) items.stream().filter(ItemUtil::isNullOrAir).count();
    }

    public static @NotNull BlockFace getDirection(@NotNull final Player player) {
        return radial[Math.round(player.getLocation().getYaw() / 45f) & 0x7].getOppositeFace();
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
        } else {
            return "";
        }
    }

    public static @NotNull String getLocale(@NotNull final Player player) {
        return localeHandler.apply(player);
    }

    public static @NotNull Integer getPing(@NotNull final Player player) {
        return pingHandler.apply(player);
    }

    public static double getMaxHealth(@NotNull final Player player) {
        if (VersionHelper.HAS_ATTRIBUTE_API) {
            return Optional.ofNullable(player.getAttribute(Attribute.GENERIC_MAX_HEALTH))
                .map(AttributeInstance::getValue)
                .orElse(20.0);
        } else {
            //noinspection deprecation
            return player.getMaxHealth();
        }
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

    private static class PlayerPingHandler implements Function<@NotNull Player, @NotNull Integer> {

        private Field ping;
        private Method getHandle;

        private void cacheReflection(@NotNull final Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
            getHandle = player.getClass().getDeclaredMethod("getHandle");
            getHandle.setAccessible(true);

            ping = getHandle.invoke(player).getClass().getDeclaredField("ping");
            ping.setAccessible(true);
        }

        @Override
        public Integer apply(@NotNull final Player player) {
            if (VersionHelper.HAS_PLAYER_PING_METHOD) {
                return player.getPing();
            }

            if (ping == null) {
                try {
                    cacheReflection(player);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                         NoSuchFieldException e) {
                    Logging.error(e, "Could not cache reflection for {0} (player: {1})", getClass().getSimpleName(), player.getName());
                }
            }

            if (ping != null) {
                try {
                    return ping.getInt(getHandle.invoke(player));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logging.error(e, "Could not get the ping of {0}, fallback to -1", player.getName());
                }
            }

            return -1;
        }

    }

    private static class PlayerLocaleHandler implements Function<@NotNull Player, @NotNull String> {

        private Field locale;
        private Method getHandle;

        private void cacheReflection(@NotNull final Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
            getHandle = player.getClass().getDeclaredMethod("getHandle");
            getHandle.setAccessible(true);

            locale = getHandle.invoke(player).getClass().getDeclaredField("locale");
        }

        @Override
        public @NotNull String apply(@NotNull final Player player) {
            if (VersionHelper.HAS_PLAYER_LOCALE_METHOD) {
                //noinspection deprecation
                return player.getLocale();
            }

            if (locale == null) {
                try {
                    cacheReflection(player);
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                         NoSuchFieldException e) {
                    Logging.error(e, "Could not cache reflection for {0} (player: {1})", getClass().getSimpleName(), player.getName());
                }
            }

            if (locale != null) {
                try {
                    return (String) locale.get(getHandle.invoke(player));
                } catch (IllegalAccessException | InvocationTargetException e) {
                    Logging.error(e, "Could not get the locale of {0}, fallback to en_US", player.getName());
                }
            }

            return "en_US";
        }

    }

}
