package com.pengu.solarfluxreborn.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

import com.pengu.solarfluxreborn.init.ItemsSFR;
import com.pengu.solarfluxreborn.reference.Reference;

public class CreativeTabSFR
{
	public static final CreativeTabs MOD_TAB = new CreativeTabs(Reference.MOD_ID.toLowerCase())
	{
		@Override
		public ItemStack getTabIconItem()
		{
			return new ItemStack(ItemsSFR.solarCell3);
		}
	};
}
