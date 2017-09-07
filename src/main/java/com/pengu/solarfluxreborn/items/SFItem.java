package com.pengu.solarfluxreborn.items;

import net.minecraft.item.Item;

import com.pengu.solarfluxreborn.creativetab.CreativeTabSFR;
import com.pengu.solarfluxreborn.reference.Reference;

public class SFItem extends Item
{
	public SFItem(String name)
	{
		setUnlocalizedName(Reference.MOD_ID + ":" + name);
		setCreativeTab(CreativeTabSFR.MOD_TAB);
	}
}