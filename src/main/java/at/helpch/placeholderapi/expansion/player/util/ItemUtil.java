package at.helpch.placeholderapi.expansion.player.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemUtil {

    public static boolean isNullOrAir(@Nullable final ItemStack itemStack) {
        return itemStack == null || itemStack.getType().name().contains("_AIR");
    }

    public static @NotNull String getMaterialName(@Nullable final ItemStack item) {
        return isNullOrAir(item) ? "" : item.getType().name();
    }

    public static @NotNull String getName(@Nullable final ItemStack item) {
        if (isNullOrAir(item)) {
            return "";
        }

        //noinspection deprecation
        return item.hasItemMeta() ? item.getItemMeta().getDisplayName() : "";
    }

    public static int getData(@Nullable final ItemStack item) {
        if (isNullOrAir(item)) {
            return 0;
        }

        if (VersionHelper.HAS_DAMAGEABLE_ITEM_META) {
            return item.hasItemMeta() ? ((Damageable) item.getItemMeta()).getDamage() : 0;
        } else {
            //noinspection deprecation
            return item.getDurability();
        }
    }

    public static int getDurability(@Nullable final ItemStack item) {
        return isNullOrAir(item) ? 0 : item.getType().getMaxDurability() - getData(item);
    }

}
