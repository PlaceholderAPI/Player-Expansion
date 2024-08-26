package at.helpch.placeholderapi.expansion.player.ping;

import at.helpch.placeholderapi.expansion.player.handler.PlayerPingHandler;
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

    private final List<PingFormattingRule> pingFormattingRules = new ArrayList<>();

    public PingFormatter(@Nullable final ConfigurationSection config) {
        if (config == null) {
            Logging.warn("Could not find the 'ping.formatting' section in config");
            return;
        }

        load(config);
    }

    private void load(@NotNull ConfigurationSection config) {
        for (final String key : config.getKeys(false)) {
            final String formatting = config.getString(key);

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

    public @NotNull String getPing(@NotNull final Player player, final boolean colored) {
        final int ping = PlayerPingHandler.INSTANCE.apply(player);

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
