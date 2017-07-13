package com.pengu.solarfluxreborn.gui;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import com.pengu.solarfluxreborn.te.SolarPanelTileEntity;

public class SlotUpgrade extends Slot
{
	public SlotUpgrade(IInventory pInventory, int pSlotIndex, int pXDisplayPosition, int pYDisplayPosition)
	{
		super(pInventory, pSlotIndex, pXDisplayPosition, pYDisplayPosition);
	}
	
	@Override
	public boolean isItemValid(ItemStack pItemStack)
	{
		// Delegate to the inventory.
		return inventory.isItemValidForSlot(getSlotIndex(), pItemStack);
	}
	
	@Override
	public int getSlotStackLimit()
	{
		if(inventory instanceof SolarPanelTileEntity)
		{
			SolarPanelTileEntity te = (SolarPanelTileEntity) inventory;
			int a = te.additionalUpgradeAllowed(getStack());
			return a == 0 ? super.getSlotStackLimit() : a;
		}
		return super.getSlotStackLimit();
	}
}