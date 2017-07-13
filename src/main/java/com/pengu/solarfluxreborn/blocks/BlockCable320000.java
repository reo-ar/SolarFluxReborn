package com.pengu.solarfluxreborn.blocks;

import net.minecraft.util.ResourceLocation;

import com.pengu.solarfluxreborn.reference.Reference;

public class BlockCable320000 extends BlockAbstractCable
{
	public static double TRANSFER_RATE = 320000D;
	
	public BlockCable320000()
	{
		setUnlocalizedName(Reference.MOD_ID + ":wire_3");
	}
	
	@Override
	public double getTransferRate()
	{
		return TRANSFER_RATE;
	}
	
	public final ResourceLocation connection = new ResourceLocation(Reference.MOD_ID, "blocks/wire_2");
	
	@Override
	public ResourceLocation getConnection()
	{
		return connection;
	}
}