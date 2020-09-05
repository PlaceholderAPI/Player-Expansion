package at.helpch.placeholderapi.expansion.player;

import at.helpch.placeholderapi.api.expansion.Expansion;
import at.helpch.placeholderapi.api.expansion.ExpansionDescription;
import at.helpch.placeholderapi.api.expansion.Platform;

@ExpansionDescription(
        name = "Player",
        version = "2.0.0",
        identifier = "player",
        authors = "HelpChat",
        platforms = {Platform.BUKKIT, Platform.SPONGE, Platform.NUKKIT}
)
public final class PlayerExpansion extends Expansion {
    @Override
    public void enable() {

    }
}
