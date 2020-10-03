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
//    @Override
//    public void enable() {
//        register();
//    }
//
//    private void register() {
//        player("name", player -> player.getNullable(PlayerKeys.NAME));
//        player("uuid", player -> player.getNullable(PlayerKeys.UUID));
//        player("has_played_before", player -> player.getNullable(PlayerKeys.HAS_PLAYED_BEFORE));
//        player("is_whitelisted", player -> player.getNullable());
//    }
//
//    private void player(@NotNull final String name, @NotNull final Function<Player<?>, Object> function) {
//        registerPlaceholder(name, context -> function.apply(context.getPlayer()));
//    }
}
