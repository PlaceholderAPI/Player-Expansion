package at.helpch.placeholderapi.expansion.player.handler;

import at.helpch.placeholderapi.expansion.player.util.Logging;
import at.helpch.placeholderapi.expansion.player.util.VersionHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class PlayerLocaleHandler implements Function<@NotNull Player, @NotNull String> {

    public static final PlayerLocaleHandler INSTANCE = new PlayerLocaleHandler();

    private Field locale;
    private Method getHandle;

    private PlayerLocaleHandler() { }

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
