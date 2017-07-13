package com.pengu.solarfluxreborn.blocks;

import net.minecraft.util.ResourceLocation;

import com.pengu.solarfluxreborn.reference.Reference;

public class BlockCable3200 extends BlockAbstractCable
{
	public static double TRANSFER_RATE = 3200D;
	
	public BlockCable3200()
	{
		setUnlocalizedName(Reference.MOD_ID + ":wire_2");
	}
	
	@Override
	public double getTransferRate()
	{
		return TRANSFER_RATE;
	}
	
	public final ResourceLocation connection = new ResourceLocation(Reference.MOD_ID, "blocks/wire_1");
	
	@Override
	public ResourceLocation getConnection()
	{
		return connection;
	}
}