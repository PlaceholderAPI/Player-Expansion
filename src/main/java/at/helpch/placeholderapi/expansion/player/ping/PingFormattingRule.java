package at.helpch.placeholderapi.expansion.player.ping;

import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class PingFormattingRule implements Predicate<Integer> {

    private final Predicate<@NotNull Integer> predicate;
    private final String formatting;

    public static @NotNull PingFormattingRule equalTo(final int pingValue, @NotNull final String formatting) {
        return new PingFormattingRule((ping) -> ping == pingValue, formatting);
    }

    public static @NotNull PingFormattingRule greaterThan(final int pingValue, @NotNull final String formatting) {
        return new PingFormattingRule((ping) -> ping > pingValue, formatting);
    }

    public static @NotNull PingFormattingRule lessThan(final int pingValue, @NotNull final String formatting) {
        return new PingFormattingRule((ping) -> ping < pingValue, formatting);
    }

    private PingFormattingRule(Predicate<Integer> predicate, String formatting) {
        this.predicate = predicate;
        this.formatting = formatting;
    }

    @Override
    public boolean test(@NotNull final Integer ping) {
        return predicate.test(ping);
    }

    public String getFormatting() {
        return formatting;
    }

}
