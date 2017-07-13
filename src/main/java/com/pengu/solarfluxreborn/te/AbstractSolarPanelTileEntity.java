package com.pengu.solarfluxreborn.te;

import net.minecraft.nbt.NBTTagCompound;

import com.pengu.solarfluxreborn.blocks.StatefulEnergyStorage;
import com.pengu.solarfluxreborn.reference.Reference;

public class AbstractSolarPanelTileEntity extends SolarPanelTileEntity
{
	public AbstractSolarPanelTileEntity()
	{
	}
	
	protected int maxGen, cap, transfer;
	protected String name;
	
	public AbstractSolarPanelTileEntity(String name, int cap, int transfer, int maxGen)
	{
		super(0);
		this.maxGen = maxGen;
		this.name = name;
		mEnergyStorage = new StatefulEnergyStorage(cap, transfer, transfer);
		this.cap = cap;
		this.transfer = transfer;
	}
	
	@Override
	public int getMaximumEnergyGeneration()
	{
		return maxGen;
	}
	
	@Override
	public String getTopResource()
	{
		return Reference.MOD_ID + ":blocks/" + name + "_top";
	}
	
	@Override
	public String getBaseResource()
	{
		return Reference.MOD_ID + ":blocks/" + name + "_base";
	}
	
	@Override
	public void tick()
	{
		mEnergyStorage.setMaxEnergyStored(cap);
		mEnergyStorage.setMaxTransfer(transfer);
		super.tick();
	}
	
	@Override
	protected void loadDataFromNBT(NBTTagCompound pNBT)
	{
		super.loadDataFromNBT(pNBT);
		mEnergyStorage.setMaxEnergyStored(cap);
		mEnergyStorage.setMaxTransfer(transfer);
	}
}