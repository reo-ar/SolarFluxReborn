package com.pengu.solarfluxreborn.blocks;

import net.minecraft.util.ResourceLocation;

import com.pengu.solarfluxreborn.reference.Reference;

public class BlockCable320 extends BlockAbstractCable
{
	public static double TRANSFER_RATE = 320D;
	
	public BlockCable320()
	{
		setUnlocalizedName(Reference.MOD_ID + ":wire_1");
	}
	
	@Override
	public double getTransferRate()
	{
		return TRANSFER_RATE;
	}
	
	public final ResourceLocation connection = new ResourceLocation(Reference.MOD_ID, "blocks/wire_0");
	
	@Override
	public ResourceLocation getConnection()
	{
		return connection;
	}
}