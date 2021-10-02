package at.helpch.placeholderapi.expansion.player;

import at.helpch.placeholderapi.api.expansion.Expansion;
import at.helpch.placeholderapi.api.expansion.ExpansionDescription;
import at.helpch.placeholderapi.api.expansion.Platform;
import at.helpch.placeholderapi.api.expansion.placeholder.Placeholder;
import at.helpch.placeholderapi.api.expansion.placeholder.PlaceholderContext;
import at.helpch.placeholderapi.api.player.Player;
import at.helpch.placeholderapi.api.player.keyable.key.PlayerKeys;
import at.helpch.placeholderapi.api.server.Server;
import at.helpch.placeholderapi.expansion.time.TimeFormatter;
import at.helpch.placeholderapi.expansion.time.file.Config;
import com.google.inject.Inject;
import org.jetbrains.annotations.NotNull;

import java.sql.Time;
import java.util.function.Function;

@ExpansionDescription(
        name = "Player",
        version = "2.0.0",
        identifier = "player",
        authors = "HelpChat",
        platforms = {Platform.BUKKIT, Platform.SPONGE, Platform.NUKKIT}
)
public final class PlayerExpansion extends Expansion {

    private final TimeFormatter timeFormatter;
    private final Config config;

    @Inject
    public PlayerExpansion(@NotNull final Config config) {
        this.config = config;

        // TODO: instantiate a single TimeFormatted through out the project, with a main config
        this.timeFormatter = new TimeFormatter(config);
    }

    @Override
    public void enable() {

    }

    @Placeholder("name")
    private String name(final PlaceholderContext context) {
        return context.getPlayer().get(PlayerKeys.NAME).orElse("");
    }

}
