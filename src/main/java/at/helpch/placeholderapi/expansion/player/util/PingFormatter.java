package at.helpch.placeholderapi.expansion.player.util;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PingFormatter {

    private final int mediumValue;
    private final int highValue;

    private final String lowColor;
    private final String mediumColor;
    private final String highColor;

    public PingFormatter(
        final int mediumValue, final int highValue, @NotNull final String lowColor,
        @NotNull final String mediumColor, @NotNull final String highColor
    ) {
        this.mediumValue = mediumValue;
        this.highValue = highValue;
        this.lowColor = lowColor;
        this.mediumColor = mediumColor;
        this.highColor = highColor;
    }

    private @NotNull String getColor(final int ping) {
        if (ping < mediumValue) {
            return lowColor;
        }

        if (ping < highValue) {
            return mediumColor;
        }

        return highColor;
    }

    public @NotNull String getPing(@NotNull final Player player, final boolean colored) {
        final int ping = PlayerUtil.getPing(player);

        if (ping < 0 || !colored) {
            return String.valueOf(ping);
        }

        return ChatColor.translateAlternateColorCodes('&', getColor(ping) + ping);
    }

}
