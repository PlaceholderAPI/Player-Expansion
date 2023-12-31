package at.helpch.placeholderapi.expansion.player.util;

import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matt (<a href="https://github.com/lichthund">@LichtHund</a>)
 */
public final class VersionHelper {

    private static final int VERSION = getCurrentVersion();

    /**
     * @see PlayerInventory#getItemInOffHand()
     * @since MC 1.9
     */
    public static final boolean HAS_OFF_HAND = VERSION >= 1_9_0;

    /**
     * @see org.bukkit.inventory.meta.Damageable
     * @since MC 1.13
     */
    public static final boolean HAS_DAMAGEABLE_ITEM_META = VERSION >= 1_13_0;

    /**
     * @see Damageable#getAbsorptionAmount()
     * @since MC 1.15
     */
    public static final boolean HAS_ABSORPTION_METHODS = VERSION >= 1_15_0;

    /**
     * @see Player#getPing()
     * @since MC 1.17
     */
    public static final boolean HAS_PLAYER_PING_METHOD = VERSION >= 1_17_0;

    private VersionHelper() { }

    /**
     * Gets the current server version
     *
     * @return A protocol like number representing the version, for example 1.16.5 - 1165
     */
    private static int getCurrentVersion() {
        // No need to cache since will only run once
        final Matcher matcher = Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(Bukkit.getBukkitVersion());
        final StringBuilder stringBuilder = new StringBuilder();

        if (matcher.find()) {
            final String patch = matcher.group("patch");

            stringBuilder
                .append(matcher.group("version").replace(".", ""))
                .append((patch == null) ? "0" : patch.replace(".", ""));
        }

        final Integer version = Ints.tryParse(stringBuilder.toString());

        // Should never fail
        if (version == null) {
            throw new IllegalArgumentException("Could not retrieve server version!");
        }

        return version;
    }

}
