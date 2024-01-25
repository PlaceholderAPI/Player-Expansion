package at.helpch.placeholderapi.expansion.player.ping;

import at.helpch.placeholderapi.expansion.player.util.Logging;
import at.helpch.placeholderapi.expansion.player.util.PlayerUtil;
import com.google.common.primitives.Ints;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PingFormatter {

    /*private final int mediumValue;
    private final int highValue;

    private final String lowColor;
    private final String mediumColor;
    private final String highColor;*/

    private final List<PingFormattingRule> pingFormattingRules = new ArrayList<>();

    /*public PingFormatter(
        final int mediumValue, final int highValue, @NotNull final String lowColor,
        @NotNull final String mediumColor, @NotNull final String highColor
    ) {
        this.mediumValue = mediumValue;
        this.highValue = highValue;
        this.lowColor = lowColor;
        this.mediumColor = mediumColor;
        this.highColor = highColor;
    }*/

    public PingFormatter(@Nullable final ConfigurationSection configurationSection) {
        if (configurationSection == null) {
            Logging.warn("Could not find the 'ping.formatting' section in config");
            return;
        }

        for (final String key : configurationSection.getKeys(false)) {
            final String formatting = configurationSection.getString(key);

            if (formatting == null) {
                continue;
            }

            Integer ping = Ints.tryParse(key.replaceAll("\\D", ""));

            if (ping == null) {
                Logging.warn("(ping formatting) Invalid ping condition \"{0}\"", key);
                continue;
            }

            switch (key.charAt(0)) {
                case '>':
                    this.pingFormattingRules.add(PingFormattingRule.greaterThan(ping, formatting));
                    break;

                case '<':
                    this.pingFormattingRules.add(PingFormattingRule.lessThan(ping, formatting));
                    break;

                case '=':
                default:
                    this.pingFormattingRules.add(PingFormattingRule.equalTo(ping, formatting));
                    break;
            }
        }
    }

    /*private @NotNull String getColor(final int ping) {
        if (ping < mediumValue) {
            return lowColor;
        }

        if (ping < highValue) {
            return mediumColor;
        }

        return highColor;
    }*/

    public @NotNull String getPing(@NotNull final Player player, final boolean colored) {
        final int ping = PlayerUtil.getPing(player);

        if (ping < 0 || !colored) {
            return String.valueOf(ping);
        }

        return pingFormattingRules.stream()
            .filter(rule -> rule.test(ping))
            .findFirst()
            .map(rule ->  ChatColor.translateAlternateColorCodes('&', rule.getFormatting() + ping))
            .orElseGet(() -> String.valueOf(ping));
    }

}
