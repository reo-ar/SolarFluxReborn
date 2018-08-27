package com.zeitheron.solarflux.block.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.zeitheron.solarflux.api.SolarInfo;
import com.zeitheron.solarflux.api.SolarInstance;
import com.zeitheron.solarflux.block.BlockBaseSolar;
import com.zeitheron.solarflux.gui.ContainerBaseSolar;
import com.zeitheron.solarflux.utils.FByteHelper;
import com.zeitheron.solarflux.utils.IVariableHandler;
import com.zeitheron.solarflux.utils.InventoryDummy;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class TileBaseSolar extends TileEntity implements ITickable, IEnergyStorage, IVariableHandler
{
	protected int energy;
	
	public int currentGeneration;
	public float sunIntensity;
	
	public SolarInstance instance;
	
	public boolean renderConnectedTextures = true;
	
	public List<EntityPlayer> crafters = new ArrayList<>();
	
	public final InventoryDummy items = new InventoryDummy(5);
	public final InvWrapper itemWrapper = new InvWrapper(items);
	{
		items.validSlots = (slot, stack) -> false;
		items.fields = this;
		items.openInv = crafters::add;
		items.closeInv = crafters::remove;
	}
	
	public TileBaseSolar(SolarInstance instance)
	{
		this.instance = instance;
	}
	
	public TileBaseSolar()
	{
	}
	
	public boolean isSameLevel(TileBaseSolar other)
	{
		if(other == null)
			return false;
		if(other.instance == null || instance == null)
			return false;
		return Objects.equals(other.instance.delegate, instance.delegate);
	}
	
	@Override
	public void update()
	{
		if(getBlockType() instanceof BlockBaseSolar)
		{
			SolarInfo si = ((BlockBaseSolar) getBlockType()).solarInfo;
			renderConnectedTextures = si.connectTextures;
			
			if(si.maxGeneration <= 0)
			{
				world.destroyBlock(pos, true);
				return;
			}
			
			if(instance == null || !instance.isValid())
			{
				instance = new SolarInstance();
				si.accept(instance);
				return;
			}
		}
		
		if(world.isRemote)
			return;
		
		for(int i = 0; i < crafters.size(); ++i)
		{
			try
			{
				EntityPlayer player = crafters.get(i);
				if(player.openContainer instanceof ContainerBaseSolar)
					player.openContainer.detectAndSendChanges();
				else
					crafters.remove(i);
			} catch(Throwable err)
			{
			}
		}
		
		int gen = getGeneration();
		energy += gen;
		currentGeneration = gen;
		
		energy = MathHelper.clamp(energy, 0, instance.cap);
		{
			for(EnumFacing hor : EnumFacing.HORIZONTALS)
			{
				TileEntity tile = world.getTileEntity(pos.offset(hor));
				
				if(tile == null)
					continue;
				
				if(tile instanceof TileBaseSolar)
					autoBalanceEnergy((TileBaseSolar) tile);
			}
			
			for(EnumFacing hor : EnumFacing.VALUES)
			{
				if(hor == EnumFacing.UP)
					continue;
				
				TileEntity tile = world.getTileEntity(pos.offset(hor));
				
				if(tile == null)
					continue;
				
				if(tile.hasCapability(CapabilityEnergy.ENERGY, hor.getOpposite()))
				{
					IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, hor.getOpposite());
					if(storage.canReceive())
						energy -= storage.receiveEnergy(Math.min(energy, instance.transfer), false);
				}
			}
		}
	}
	
	public int getGeneration()
	{
		int effUpgrs = 0;
		float EfficiencyUpgradeReturnsToScale = .9F;
		float effUpgrIncr = .15F;
		float eff = instance.computeSunIntensity(this);
		if(!world.isRemote)
			sunIntensity = eff;
		double energyGeneration = instance.gen * eff;
		energyGeneration *= (1 + effUpgrIncr * Math.pow(effUpgrs, EfficiencyUpgradeReturnsToScale));
		return (int) Math.round(energyGeneration);
	}
	
	public NBTTagCompound write(NBTTagCompound nbt)
	{
		nbt.merge(instance.serializeNBT());
		nbt.setTag("Items", items.writeToNBT(new NBTTagCompound()));
		nbt.setInteger("Energy", energy);
		return nbt;
	}
	
	public void read(NBTTagCompound nbt)
	{
		instance = SolarInstance.deserialize(nbt);
		items.readFromNBT(nbt.getCompoundTag("Items"));
		energy = nbt.getInteger("Energy");
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if(capability == CapabilityEnergy.ENERGY && facing != EnumFacing.UP)
			return true;
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return true;
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if(capability == CapabilityEnergy.ENERGY && facing != EnumFacing.UP)
			return (T) this;
		if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return (T) itemWrapper;
		return super.getCapability(capability, facing);
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket()
	{
		return new SPacketUpdateTileEntity(pos, 0, write(new NBTTagCompound()));
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
	{
		read(pkt.getNbtCompound());
	}
	
	public void sync()
	{
		IBlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 11);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		read(nbt);
		super.readFromNBT(nbt);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		return super.writeToNBT(write(nbt));
	}
	
	public int autoBalanceEnergy(TileBaseSolar solar)
	{
		int delta = getEnergyStored() - solar.getEnergyStored();
		if(delta < 0)
			return solar.autoBalanceEnergy(this);
		else if(delta > 0 && solar.getEnergyStored() < solar.getMaxEnergyStored())
			return extractEnergy(solar.receiveEnergyInternal(delta / 2, false), false);
		return 0;
	}
	
	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		int energyExtracted = Math.min(energy, Math.min(instance.transfer, maxExtract));
		if(!simulate)
			energy -= energyExtracted;
		return energyExtracted;
	}
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		return 0;
	}
	
	public int receiveEnergyInternal(int maxReceive, boolean simulate)
	{
		int energyReceived = Math.min(getMaxEnergyStored() - energy, Math.min(instance.transfer, maxReceive));
		if(!simulate)
			energy += energyReceived;
		return energyReceived;
	}
	
	@Override
	public int getEnergyStored()
	{
		return getVar(0);
	}
	
	@Override
	public int getMaxEnergyStored()
	{
		return getVar(1);
	}
	
	@Override
	public boolean canExtract()
	{
		return true;
	}
	
	@Override
	public boolean canReceive()
	{
		return false;
	}
	
	@Override
	public int getVar(int id)
	{
		switch(id)
		{
		case 0:
			return energy;
		case 1:
			return instance.cap;
		case 2:
			return instance.gen;
		case 3:
			return instance.transfer;
		case 4:
			return currentGeneration;
		case 5:
			return FByteHelper.toInt(sunIntensity);
		}
		
		return 0;
	}
	
	@Override
	public void setVar(int id, int value)
	{
		switch(id)
		{
		case 0:
			energy = MathHelper.clamp(value, 0, instance.cap);
		break;
		case 1:
			instance.cap = value;
		break;
		case 2:
			instance.gen = value;
		break;
		case 3:
			instance.transfer = value;
		break;
		case 4:
			currentGeneration = value;
		break;
		case 5:
			sunIntensity = FByteHelper.toFloat(value);
		break;
		}
	}
	
	@Override
	public int getVarCount()
	{
		return 6;
	}
}