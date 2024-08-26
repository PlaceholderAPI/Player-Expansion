package at.helpch.placeholderapi.expansion.player.util;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    private static final LocalTime SIX_O_CLOCK = LocalTime.of(6, 0);
    private static final double MINECRAFT_SECONDS_PER_TICK = 3.6d;

    private static final DateTimeFormatter TWENTY_FOUR_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter TWELVE_FORMAT = DateTimeFormatter.ofPattern("hh:mm a");

    public static String formatWorldTime(final long ticks, final boolean twentyFourHourFormat) {
        final LocalTime time = SIX_O_CLOCK.plusSeconds((long) (ticks * MINECRAFT_SECONDS_PER_TICK));
        return time.format(twentyFourHourFormat ? TWENTY_FOUR_FORMAT : TWELVE_FORMAT);
    }

}
