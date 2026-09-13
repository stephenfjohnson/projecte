package moze_intel.projecte.gameObjs.container.slots;

import moze_intel.projecte.api.inventory.IItemHandler;
import moze_intel.projecte.inventory.SlotItemHandler;
import net.minecraft.world.item.ItemStack;

public class InventoryContainerSlot extends SlotItemHandler implements IInventoryContainerSlot {

    public InventoryContainerSlot(IItemHandler itemHandler, int index, int x, int y) {
        super(itemHandler, index, x, y);
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(getMaxStackSize(), stack.getMaxStackSize());
    }
}