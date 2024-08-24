package at.helpch.placeholderapi.expansion.player.handler;

import at.helpch.placeholderapi.expansion.player.util.Logging;
import at.helpch.placeholderapi.expansion.player.util.VersionHelper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.Function;

public class PlayerPingHandler implements Function<@NotNull Player, @NotNull Integer> {

    public static final PlayerPingHandler INSTANCE = new PlayerPingHandler();

    private Field ping;
    private Method getHandle;

    private PlayerPingHandler() { }

    private void cacheReflection(@NotNull final Player player) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, NoSuchFieldException {
        getHandle = player.getClass().getDeclaredMethod("getHandle");
        getHandle.setAccessible(true);

        ping = getHandle.invoke(player).getClass().getDeclaredField("ping");
        ping.setAccessible(true);
    }

    @Override
    public Integer apply(@NotNull final Player player) {
        if (VersionHelper.HAS_PLAYER_PING_METHOD) {
            return player.getPing();
        }

        if (ping == null) {
            try {
                cacheReflection(player);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException |
                     NoSuchFieldException e) {
                Logging.error(e, "Could not cache reflection for {0} (player: {1})", getClass().getSimpleName(), player.getName());
            }
        }

        if (ping != null) {
            try {
                return ping.getInt(getHandle.invoke(player));
            } catch (IllegalAccessException | InvocationTargetException e) {
                Logging.error(e, "Could not get the ping of {0}, fallback to -1", player.getName());
            }
        }

        return -1;
    }

}
