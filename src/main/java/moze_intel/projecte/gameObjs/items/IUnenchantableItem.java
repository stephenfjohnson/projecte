package moze_intel.projecte.gameObjs.items;

/**
 * Marker for ProjectE's gear, which refuses every enchantment.
 * <p>
 * On NeoForge each item said so four times over: {@code isEnchantable}, {@code isBookEnchantable},
 * {@code isPrimaryItemFor} and {@code supportsEnchantment}. Vanilla only has the first, which covers the
 * enchanting table; {@code EnchantmentMixin} keys off this interface to cover the rest (anvils, books,
 * loot tables and villager trades), so the gear behaves as it did.
 */
public interface IUnenchantableItem {

}
