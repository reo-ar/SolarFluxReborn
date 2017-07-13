package com.pengu.solarfluxreborn.items;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.pengu.solarfluxreborn.utility.Lang;

public class CraftingItem extends SFItem
{
	public CraftingItem(String name)
	{
		super(name);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn)
	{
		tooltip.add(Lang.localise("craftitem", true));
	}
}