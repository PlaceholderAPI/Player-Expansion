package at.helpch.placeholderapi.expansion.player.util;

import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public final class PlayerUtil {

    private static final BlockFace[] radial = {
        BlockFace.NORTH, BlockFace.NORTH_EAST,
        BlockFace.EAST, BlockFace.SOUTH_EAST,
        BlockFace.SOUTH, BlockFace.SOUTH_WEST,
        BlockFace.WEST, BlockFace.NORTH_WEST
    };

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    public static @Nullable ItemStack getItemInMainHand(@NotNull final Player player) {
        return VersionHelper.HAS_OFF_HAND ? player.getInventory().getItemInMainHand() : player.getItemInHand();
    }

    public static @NotNull String getBedCoordinate(
        @NotNull final OfflinePlayer player, @NotNull final Function<@NotNull Location, @NotNull Number> function
    ) {
        return Optional.ofNullable(player.getBedSpawnLocation())
            .map(function)
            .map(String::valueOf)
            .orElse("0");
    }

    @SuppressWarnings("OptionalOfNullableMisuse")
    public static @NotNull String getCompassCoordinate(
        @NotNull final Player player, @NotNull final Function<@NotNull Location, @NotNull Number> function
    ) {
        return Optional.ofNullable(player.getCompassTarget())
            .map(function)
            .map(String::valueOf)
            .orElse("0");
    }

    public static int getEmptySlots(@NotNull final Player player) {
        final PlayerInventory inventory = player.getInventory();
        final List<ItemStack> items = new ArrayList<>(Arrays.asList(inventory.getContents()));
        items.add(inventory.getHelmet());
        items.add(inventory.getChestplate());
        items.add(inventory.getLeggings());
        items.add(inventory.getBoots());

        if (VersionHelper.HAS_OFF_HAND) {
            items.add(inventory.getItemInOffHand());
        }

        return (int) items.stream().filter(ItemUtil::isNullOrAir).count();
    }

    public static @NotNull BlockFace getDirection(@NotNull final Player player) {
        return radial[Math.round(player.getLocation().getYaw() / 45f) & 0x7].getOppositeFace();
    }

}
