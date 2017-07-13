package com.pengu.solarfluxreborn.intr.waila;

import java.util.List;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.pengu.solarfluxreborn.te.SolarPanelTileEntity;
import com.pengu.solarfluxreborn.te.cable.TileCustomCable;
import com.pengu.solarfluxreborn.utility.Lang;

public class WDataPump implements IWailaDataProvider
{
	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return null;
	}
	
	@Override
	public List<String> getWailaHead(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}
	
	@Override
	public List<String> getWailaBody(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		TileEntity te = accessor.getTileEntity();
		
		if(te instanceof SolarPanelTileEntity)
		{
			SolarPanelTileEntity t = (SolarPanelTileEntity) te;
			currenttip.add(Lang.localise("energy.stored") + ": " + String.format("%,d", t.getEnergyStored()) + " RF");
			currenttip.add(Lang.localise("energy.capacity") + ": " + String.format("%,d", t.getMaxEnergyStored()) + " RF");
			currenttip.add(Lang.localise("energy.generation") + ": " + String.format("%,d", t.getCurrentEnergyGeneration()) + " RF/t");
			currenttip.add(Lang.localise("energy.efficiency") + ": " + Math.round(100D * t.getCurrentEnergyGeneration() / t.getMaximumEnergyGeneration()) + "%");
		}
		
		if(te instanceof TileCustomCable)
		{
			TileCustomCable t = (TileCustomCable) te;
			currenttip.add(Lang.localise("energy.transfer") + ": " + t.capacityAdded);
		}
		
		return currenttip;
	}
	
	@Override
	public List<String> getWailaTail(ItemStack itemStack, List<String> currenttip, IWailaDataAccessor accessor, IWailaConfigHandler config)
	{
		return currenttip;
	}
	
	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te, NBTTagCompound tag, World world, BlockPos pos)
	{
		return null;
	}
	
}