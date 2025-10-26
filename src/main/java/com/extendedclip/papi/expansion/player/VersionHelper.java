package com.extendedclip.papi.expansion.player;

import com.google.common.primitives.Ints;
import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matt (<a href="https://github.com/ipsk">@ipsk</a>)
 */
public final class VersionHelper {

    private static final int VERSION = getCurrentVersion();

    /**
     * @see Damageable#getAbsorptionAmount()
     */
    public static final boolean HAS_ABSORPTION_METHODS = VERSION >= 1_15_0;

    /**
     * @see Player#getPing()
     */
    public static final boolean IS_1_17_OR_NEWER = VERSION >= 1_17_0;
    
    /**
     * @see Player#getLocale()
     */
    public static final boolean IS_1_20_2_OR_NEWER = VERSION >= 1_20_2;
    
    /**
     * @see Player#getLocale()
     */
    public static final boolean IS_1_20_4_OR_NEWER = VERSION >= 1_20_4;

    public static final boolean IS_1_21_3_OR_NEWER = VERSION >= 1_21_3;

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
