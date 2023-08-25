package com.extendedclip.papi.expansion.player;

import me.clip.placeholderapi.PlaceholderAPIPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Matt (<a href="https://github.com/ipsk">@ipsk</a>)
 */
public final class VersionHelper {

    private final int VERSION = getCurrentVersion();

    private final boolean HAS_LOCALE_METHOD = VERSION >= 1_12_0;
    private final boolean HAS_ABSORPTION_METHODS = VERSION >= 1_15_0;
    private final boolean IS_1_17_OR_NEWER = VERSION >= 1_17_0;

    private Method getHandle;
    private Field ping;
    private Field locale;

    public VersionHelper() {
        if (IS_1_17_OR_NEWER) return;
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            getHandle = Class.forName("org.bukkit.craftbukkit."+ version +".entity.CraftPlayer").getDeclaredMethod("getHandle");
            getHandle.setAccessible(true);

            Class<?> entityPlayerClass = Class.forName("net.minecraft.server."+version+".EntityPlayer");
            try {
                ping = entityPlayerClass.getDeclaredField("ping");
                ping.setAccessible(true);
            } catch (NoSuchFieldException e) {
                PlaceholderAPIPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not get access ping field! Player ping placeholders won't work.");
            }

            if (HAS_LOCALE_METHOD) return;
            try {
                locale = entityPlayerClass.getField("locale");
                locale.setAccessible(true);
            } catch (NoSuchFieldException e) {
                PlaceholderAPIPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not get access locale field! Player locale placeholders won't work.");
            }
        } catch (Exception e) {
            PlaceholderAPIPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not load NMS classes correctly! Ping and Locale placeholders may not work.");
        }
    }

    /**
     * Gets the current server version
     *
     * @return A protocol like number representing the version, for example 1.16.5 - 1165
     */
    private int getCurrentVersion() {
        // No need to cache since will only run once
        final Matcher matcher = Pattern.compile("(?<version>\\d+\\.\\d+)(?<patch>\\.\\d+)?").matcher(Bukkit.getBukkitVersion());
        final StringBuilder stringBuilder = new StringBuilder();

        if (matcher.find()) {
            final String patch = matcher.group("patch");

            stringBuilder
                    .append(matcher.group("version").replace(".", ""))
                    .append((patch == null) ? "0" : patch.replace(".", ""));
        }

        try {
            return Integer.parseInt(stringBuilder.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not retrieve server version!");
        }
    }

    public int getPing(Player player) {
        if (IS_1_17_OR_NEWER) return player.getPing();
        try {return (int) ping.get(getHandle.invoke(player));}
        catch (IllegalAccessException | InvocationTargetException e) {return -1;}
    }

    public String getLocale(Player player) {
        if (HAS_LOCALE_METHOD) return player.getLocale();
        try {return (String) locale.get(getHandle.invoke(player));}
        catch (IllegalAccessException | InvocationTargetException e) {return "en_US";}
    }

    public double getAbsorption(Player player) {
        return HAS_ABSORPTION_METHODS ? player.getAbsorptionAmount() : -1;
    }

}
