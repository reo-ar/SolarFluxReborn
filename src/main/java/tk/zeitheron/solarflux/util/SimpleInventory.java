package tk.zeitheron.solarflux.util;

import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemHandlerHelper;

public class SimpleInventory implements IItemHandlerModifiable
{
	public final NonNullList<ItemStack> items;
	
	public int stackSizeLimit = 64;
	public ToIntFunction<Integer> getSlotLimit = s -> stackSizeLimit;
	public BiPredicate<Integer, ItemStack> isStackValid = (i, s) -> true;
	
	public SimpleInventory(int slots)
	{
		this.items = NonNullList.withSize(slots, ItemStack.EMPTY);
	}
	
	@Override
	public int getSlots()
	{
		return items.size();
	}
	
	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return items.get(slot);
	}
	
	@Override
	public ItemStack insertItem(int slot, ItemStack stack, boolean simulate)
	{
		if(stack.isEmpty())
			return ItemStack.EMPTY;
		
		ItemStack stackInSlot = getStackInSlot(slot);
		
		int m;
		if(!stackInSlot.isEmpty())
		{
			if(stackInSlot.getCount() >= Math.min(stackInSlot.getMaxStackSize(), getSlotLimit(slot)))
				return stack;
			
			if(!ItemHandlerHelper.canItemStacksStack(stack, stackInSlot))
				return stack;
			
			if(!isItemValid(slot, stack))
				return stack;
			
			m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot)) - stackInSlot.getCount();
			
			if(stack.getCount() <= m)
			{
				if(!simulate)
				{
					ItemStack copy = stack.copy();
					copy.grow(stackInSlot.getCount());
					setStackInSlot(slot, copy);
				}
				
				return ItemStack.EMPTY;
			} else
			{
				// copy the stack to not modify the original one
				stack = stack.copy();
				if(!simulate)
				{
					ItemStack copy = stack.split(m);
					copy.grow(stackInSlot.getCount());
					setStackInSlot(slot, copy);
					return stack;
				} else
				{
					stack.shrink(m);
					return stack;
				}
			}
		} else
		{
			if(!isItemValid(slot, stack))
				return stack;
			
			m = Math.min(stack.getMaxStackSize(), getSlotLimit(slot));
			if(m < stack.getCount())
			{
				// copy the stack to not modify the original one
				stack = stack.copy();
				if(!simulate)
				{
					setStackInSlot(slot, stack.split(m));
					return stack;
				} else
				{
					stack.shrink(m);
					return stack;
				}
			} else
			{
				if(!simulate)
				{
					setStackInSlot(slot, stack);
				}
				return ItemStack.EMPTY;
			}
		}
		
	}
	
	@Override
	public ItemStack extractItem(int slot, int amount, boolean simulate)
	{
		if(amount == 0)
			return ItemStack.EMPTY;
		
		ItemStack stackInSlot = getStackInSlot(slot);
		
		if(stackInSlot.isEmpty())
			return ItemStack.EMPTY;
		
		if(simulate)
		{
			if(stackInSlot.getCount() < amount)
			{
				return stackInSlot.copy();
			} else
			{
				ItemStack copy = stackInSlot.copy();
				copy.setCount(amount);
				return copy;
			}
		} else
		{
			int m = Math.min(stackInSlot.getCount(), amount);
			ItemStack decrStackSize = decrStackSize(slot, m);
			return decrStackSize;
		}
	}
	
	public ItemStack decrStackSize(int slot, int amount)
	{
		ItemStack stack = getStackInSlot(slot);
		return stack.split(amount);
	}
	
	@Override
	public int getSlotLimit(int slot)
	{
		return getSlotLimit.applyAsInt(slot);
	}
	
	@Override
	public boolean isItemValid(int slot, ItemStack stack)
	{
		return isStackValid.test(slot, stack);
	}
	
	@Override
	public void setStackInSlot(int slot, ItemStack stack)
	{
		if(slot >= 0 && slot < items.size())
			items.set(slot, stack);
	}
	
	public void writeToNBT(CompoundNBT nbt, String label)
	{
		nbt.put(label, writeToNBT(new ListNBT()));
	}
	
	public void readFromNBT(CompoundNBT nbt, String label)
	{
		readFromNBT(nbt.getList(label, NBT.TAG_COMPOUND));
	}
	
	public ListNBT writeToNBT(ListNBT nbt)
	{
		for(int i = 0; i < items.size(); ++i)
		{
			ItemStack stack = items.get(i);
			if(!stack.isEmpty())
			{
				CompoundNBT tag = stack.serializeNBT();
				tag.putInt("Slot", i);
				nbt.add(tag);
			}
		}
		return nbt;
	}
	
	public void readFromNBT(ListNBT nbt)
	{
		for(int i = 0; i < nbt.size(); ++i)
		{
			CompoundNBT tag = nbt.getCompound(i);
			ItemStack stack = ItemStack.read(tag);
			int slot = tag.getShort("Slot");
			if(slot >= 0 && slot < items.size())
				items.set(slot, stack);
		}
	}
}