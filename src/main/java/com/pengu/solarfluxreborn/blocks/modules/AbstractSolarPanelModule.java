package com.pengu.solarfluxreborn.blocks.modules;

import com.pengu.solarfluxreborn.te.SolarPanelTileEntity;

public abstract class AbstractSolarPanelModule extends AbstractTileEntityModule<SolarPanelTileEntity>
{
	protected AbstractSolarPanelModule(SolarPanelTileEntity pSolarPanelTileEntity)
	{
		super(pSolarPanelTileEntity);
	}
	
	protected boolean atRate(int pDesiredTickRate)
	{
		return getTileEntity().atTickRate(pDesiredTickRate);
	}
}
