package com.pengu.solarfluxreborn.core;

import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModMetadata;

import com.pengu.solarfluxreborn.reference.Reference;

public class SFRModC extends DummyModContainer
{
	public SFRModC()
	{
		super(new ModMetadata());
		getMetadata().modId = getModId();
		getMetadata().name = getName();
		getMetadata().version = getVersion();
	}
	
	@Override
	public String getModId()
	{
		return Reference.MOD_ID + "core";
	}
	
	@Override
	public String getName()
	{
		return Reference.MOD_NAME + ": Core";
	}
	
	@Override
	public String getVersion()
	{
		return Reference.VERSION;
	}
}