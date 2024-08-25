package at.helpch.placeholderapi.expansion.player.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("deprecation")
public final class ItemUtil {

    /**
     * Whether an item is null or its type is air (ends with _AIR)
     *
     * @param itemStack item to check
     * @return whether the item is null or air
     */
    public static boolean isNullOrAir(@Nullable final ItemStack itemStack) {
        return itemStack == null || itemStack.getType().name().equals("AIR") || itemStack.getType().name().contains("_AIR");
    }

    /**
     * Get the material name of an item
     *
     * @param item item
     * @return {@link Material#name()} or empty string if the item {@link #isNullOrAir(ItemStack) is null or air}
     */
    public static @NotNull String getMaterialName(@Nullable final ItemStack item) {
        return isNullOrAir(item) ? "" : item.getType().name();
    }

    /**
     * Get the {@link ItemMeta#getDisplayName() display name} of an item
     *
     * @param item item
     * @return {@link ItemMeta#getDisplayName()} or empty string if the item {@link #isNullOrAir(ItemStack) is null or air}
     */
    public static @NotNull String getName(@Nullable final ItemStack item) {
        if (isNullOrAir(item)) {
            return "";
        }

        //noinspection DataFlowIssue
        return item.hasItemMeta() ? item.getItemMeta().getDisplayName() : "";
    }

    public static int getDamage(@Nullable final ItemStack item) {
        if (isNullOrAir(item)) {
            return 0;
        }

        if (VersionHelper.HAS_DAMAGEABLE_ITEM_META) {
            //noinspection DataFlowIssue
            return item.hasItemMeta() ? ((Damageable) item.getItemMeta()).getDamage() : 0;
        } else {
            //noinspection deprecation
            return item.getDurability();
        }
    }

    /**
     * Get the durability left of an item
     *
     * @param item item
     * @return durability left or 0 if the item {@link #isNullOrAir(ItemStack) is null or air}
     */
    public static int getDurability(@Nullable final ItemStack item) {
        return isNullOrAir(item) ? 0 : item.getType().getMaxDurability() - getDamage(item);
    }

    public static @Nullable Enchantment getEnchantmentFromString(@NotNull final String string) {
        if (VersionHelper.HAS_KEYED_API) {
            final NamespacedKey key = (string.contains(":")) ? NamespacedKey.fromString(string) : NamespacedKey.minecraft(string);
            return Optional.ofNullable(key)
                .map(Enchantment::getByKey)
                .orElseGet(() -> Enchantment.getByName(string));
        }

        return Enchantment.getByName(string);
    }

    public static @NotNull String getEnchantmentLevel(@Nullable final ItemStack item, @NotNull final String enchantmentNameOrKey) {
        if (isNullOrAir(item)) {
            return "0";
        }

        return Optional.ofNullable(getEnchantmentFromString(enchantmentNameOrKey))
            .map(item::getEnchantmentLevel)
            .map(String::valueOf)
            .orElse("0");
    }

}
