package com.pengu.solarfluxreborn.te;

import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.pengu.hammercore.common.InterItemStack;
import com.pengu.hammercore.common.capabilities.CapabilityEJ;
import com.pengu.hammercore.common.inventory.InventoryNonTile;
import com.pengu.hammercore.energy.IPowerStorage;
import com.pengu.hammercore.tile.ITileDroppable;
import com.pengu.hammercore.tile.TileSyncableTickable;
import com.pengu.hammercore.utils.WorldLocation;
import com.pengu.solarfluxreborn.blocks.SolarPanelBlock;
import com.pengu.solarfluxreborn.blocks.StatefulEnergyStorage;
import com.pengu.solarfluxreborn.blocks.modules.EnergySharingModule;
import com.pengu.solarfluxreborn.blocks.modules.ITileEntityModule;
import com.pengu.solarfluxreborn.blocks.modules.SimpleEnergyDispenserModule;
import com.pengu.solarfluxreborn.blocks.modules.TraversalEnergyDispenserModule;
import com.pengu.solarfluxreborn.config.ModConfiguration;
import com.pengu.solarfluxreborn.config.RemoteConfigs;
import com.pengu.solarfluxreborn.config.TierConfiguration;
import com.pengu.solarfluxreborn.init.ItemsSFR;
import com.pengu.solarfluxreborn.items.UpgradeItem;
import com.pengu.solarfluxreborn.reference.NBTConstants;
import com.pengu.solarfluxreborn.reference.Reference;
import com.pengu.solarfluxreborn.utility.Utils;

public class SolarPanelTileEntity extends TileSyncableTickable implements IInventory, IPowerStorage, IEnergyStorage, ITileDroppable
{
	public static final int INVENTORY_SIZE = 5;
	public static final Range<Integer> UPGRADE_SLOTS = Range.closedOpen(0, INVENTORY_SIZE);
	protected StatefulEnergyStorage mEnergyStorage;
	/**
	 * Index of this tier of SolarPanel.
	 */
	private int mTierIndex;
	private ITileEntityModule mEnergySharingModule;
	private ITileEntityModule mEnergyDispenserModule = new SimpleEnergyDispenserModule(this);
	/**
	 * The amount of RF currently generated per tick.
	 */
	private int mCurrentEnergyGeneration;
	
	/**
	 * The amount of light that the solar panel can see. This is only updated on
	 * the server and is sent to the client.
	 */
	private float mSunIntensity;
	
	private InventoryNonTile mInventory = new InventoryNonTile(INVENTORY_SIZE);
	private Map<Item, Integer> mUpgradeCache = Maps.newHashMap();
	
	public InventoryNonTile getInventory()
	{
		return mInventory;
	}
	
	public SolarPanelTileEntity()
	{
		this(0);
	}
	
	public int getTier()
	{
		return mTierIndex;
	}
	
	public SolarPanelTileEntity(int pTierIndex)
	{
		mTierIndex = pTierIndex;
		mEnergyStorage = new StatefulEnergyStorage(getCapacity(), getTransfer());
		if(ModConfiguration.doesAutoBalanceEnergy())
			mEnergySharingModule = new EnergySharingModule(this);
	}
	
	private TierConfiguration getTierConfiguration()
	{
		return ModConfiguration.getTierConfigurations().get(mTierIndex);
	}
	
	@Override
	public void tick()
	{
		updateCurrentEnergyGeneration(!world.isRemote ? pos.up() : pos);
		if(!world.isRemote)
		{
			if(atTickRate(20))
				refreshUpgradeCache();
			
			mEnergyDispenserModule.tick();
			generateEnergy();
			if(mEnergySharingModule != null)
				mEnergySharingModule.tick();
			if(atTickRate(20))
				sync();
		}
	}
	
	/**
	 * Returns the internal RF storage of the SolarPanel.
	 */
	public StatefulEnergyStorage getEnergyStorage()
	{
		return mEnergyStorage;
	}
	
	public int getCurrentEnergyGeneration()
	{
		return mCurrentEnergyGeneration;
	}
	
	/**
	 * Updates the amount of RF currently generated per tick. This must only be
	 * used on the client to set the value received by the server.
	 */
	public void setCurrentEnergyGeneration(int pCurrentEnergyGeneration)
	{
		mCurrentEnergyGeneration = pCurrentEnergyGeneration;
	}
	
	public int getMaximumEnergyGeneration()
	{
		return getMaxGen();
	}
	
	/**
	 * Updates the current amount of RF generated per tick.
	 */
	public void updateCurrentEnergyGeneration(BlockPos pos)
	{
		computeSunIntensity(pos);
		double energyGeneration = getMaxGen() * mSunIntensity;
		energyGeneration *= (1 + ModConfiguration.getEfficiencyUpgradeIncrease() * Math.pow(getUpgradeCount(ItemsSFR.mUpgradeEfficiency), ModConfiguration.getEfficiencyUpgradeReturnsToScale()));
		mCurrentEnergyGeneration = (int) Math.round(energyGeneration);
	}
	
	public float getSunIntensity()
	{
		return mSunIntensity;
	}
	
	/**
	 * This must only be used on the client to set the value received by the
	 * server.
	 */
	public void setSunIntensity(float pSunIntensity)
	{
		mSunIntensity = pSunIntensity;
	}
	
	/**
	 * Compute the intensity of the sun that can be used by the Solar Panel.
	 */
	public void computeSunIntensity(BlockPos at)
	{
		if(world.canBlockSeeSky(at))
		{
			// Intensity based on the position of the sun.
			float multiplicator = 1.5f - (getUpgradeCount(ItemsSFR.mUpgradeLowLight) * 0.122f);
			float displacement = 1.2f + (getUpgradeCount(ItemsSFR.mUpgradeLowLight) * 0.08f);
			// Celestial angle == 0 at zenith.
			float celestialAngleRadians = world.getCelestialAngleRadians(1.0f);
			if(celestialAngleRadians > Math.PI)
				celestialAngleRadians = (float) (2 * Math.PI - celestialAngleRadians);
			
			mSunIntensity = multiplicator * MathHelper.cos(celestialAngleRadians / displacement);
			mSunIntensity = Math.max(0, mSunIntensity);
			mSunIntensity = Math.min(1, mSunIntensity);
			
			if(mSunIntensity > 0)
			{
				if(world.isRaining())
				{
					mSunIntensity *= ModConfiguration.getRainGenerationFactor();
				}
				if(world.isThundering())
				{
					mSunIntensity *= ModConfiguration.getThunderGenerationFactor();
				}
			}
		} else
		{
			mSunIntensity = 0;
		}
	}
	
	protected void generateEnergy()
	{
		if(mCurrentEnergyGeneration > 0)
		{
			getEnergyStorage().receiveEnergy(mCurrentEnergyGeneration, false);
		}
	}
	
	/**
	 * Goes through the inventory and counts the number of each upgrade.
	 */
	private void refreshUpgradeCache()
	{
		mUpgradeCache.clear();
		for(int i = 0; i < getSizeInventory(); ++i)
		{
			ItemStack itemStack = getStackInSlot(i);
			if(itemStack != null && itemStack.getItem() instanceof UpgradeItem)
			{
				if(mUpgradeCache.containsKey(itemStack.getItem()))
				{
					mUpgradeCache.put(itemStack.getItem(), itemStack.getCount() + mUpgradeCache.get(itemStack.getItem()));
				} else
				{
					mUpgradeCache.put(itemStack.getItem(), itemStack.getCount());
				}
			}
		}
		
		// Do we have any upgrade for traversal?
		if(getUpgradeCount(ItemsSFR.mUpgradeTraversal) > 0 && mEnergyDispenserModule instanceof SimpleEnergyDispenserModule)
		{
			mEnergyDispenserModule = new TraversalEnergyDispenserModule(this);
		} else if(getUpgradeCount(ItemsSFR.mUpgradeTraversal) == 0 && mEnergyDispenserModule instanceof TraversalEnergyDispenserModule)
		{
			mEnergyDispenserModule = new SimpleEnergyDispenserModule(this);
		}
		
		// Apply effect of transfer rate upgrade.
		getEnergyStorage().setMaxTransfer((int) (getTransfer() * (1 + ModConfiguration.getTransferRateUpgradeIncrease() * Math.pow(getUpgradeCount(ItemsSFR.mUpgradeTransferRate), ModConfiguration.getTransferRateUpgradeReturnsToScale()))));
		
		// Apply effect of capacity upgrade
		getEnergyStorage().setMaxEnergyStored((int) (getCapacity() * (1 + ModConfiguration.getCapacityUpgradeIncrease() * Math.pow(getUpgradeCount(ItemsSFR.mUpgradeCapacity), ModConfiguration.getCapacityUpgradeReturnsToScale()))));
	}
	
	public int getTransfer()
	{
		if(this instanceof AbstractSolarPanelTileEntity)
			return ((AbstractSolarPanelTileEntity) this).transfer;
		return RemoteConfigs.getTierConfiguration(mTierIndex).getMaximumEnergyTransfer();
	}
	
	public int getMaxGen()
	{
		if(this instanceof AbstractSolarPanelTileEntity)
			return ((AbstractSolarPanelTileEntity) this).getMaximumEnergyGeneration();
		return RemoteConfigs.getTierConfiguration(mTierIndex).getMaximumEnergyGeneration();
	}
	
	public int getCapacity()
	{
		if(this instanceof AbstractSolarPanelTileEntity)
			return ((AbstractSolarPanelTileEntity) this).cap;
		return RemoteConfigs.getTierConfiguration(mTierIndex).getCapacity();
	}
	
	public int getTotalUpgradeInstalled()
	{
		int count = 0;
		for(int value : mUpgradeCache.values())
			count += value;
		return count;
	}
	
	/**
	 * Returns the number of upgrade that can still be added.
	 */
	public int additionalUpgradeAllowed(ItemStack pItemStack)
	{
		if(pItemStack != null)
		{
			Item item = pItemStack.getItem();
			if(item instanceof UpgradeItem)
			{
				UpgradeItem upgrade = (UpgradeItem) item;
				return upgrade.getMaximumPerSolarPanel() - getUpgradeCount(upgrade);
			}
		}
		return 0;
	}
	
	public int getUpgradeCount(Item pItem)
	{
		if(pItem != null)
		{
			Integer count = mUpgradeCache.get(pItem);
			return count == null ? 0 : count;
		}
		return 0;
	}
	
	protected void loadDataFromNBT(NBTTagCompound pNBT)
	{
		mTierIndex = pNBT.getInteger(NBTConstants.TIER_INDEX);
		mEnergyStorage.setMaxEnergyStored(getCapacity());
		mEnergyStorage.setMaxTransfer(getTransfer());
		mInventory.readFromNBT(pNBT);
		refreshUpgradeCache();
		mEnergyStorage.readFromNBT(pNBT);
		mCurrentEnergyGeneration = pNBT.getInteger("�urrentGen");
		
		if(this instanceof AbstractSolarPanelTileEntity)
		{
			AbstractSolarPanelTileEntity t = (AbstractSolarPanelTileEntity) this;
			t.maxGen = pNBT.getInteger("MaxGen");
			t.transfer = pNBT.getInteger("MaxTransfer");
			t.cap = pNBT.getInteger("MaxCap");
		}
	}
	
	protected void addDataToNBT(NBTTagCompound pNBT)
	{
		pNBT.setInteger(NBTConstants.TIER_INDEX, mTierIndex);
		mInventory.writeToNBT(pNBT);
		mEnergyStorage.writeToNBT(pNBT);
	}
	
	public void setEnergyStored(int pEnergy)
	{
		getEnergyStorage().setEnergyStored(pEnergy);
	}
	
	public double getScaledEnergyStoredFraction(int pScale)
	{
		double v = pScale;
		v *= (double) getEnergyStored();
		v /= (double) getMaxEnergyStored();
		return v;
	}
	
	public int getPercentageEnergyStored()
	{
		return (int) getScaledEnergyStoredFraction(100);
	}
	
	@Override
	public int getEnergyStored()
	{
		return getEnergyStorage().getEnergyStored();
	}
	
	@Override
	public int getMaxEnergyStored()
	{
		return getEnergyStorage().getMaxEnergyStored();
	}
	
	@Override
	public int getSizeInventory()
	{
		return mInventory.getSizeInventory();
	}
	
	@Override
	public ItemStack getStackInSlot(int pSlotIndex)
	{
		if(pSlotIndex < 0 || pSlotIndex >= mInventory.getSizeInventory())
			return InterItemStack.NULL_STACK;
		return mInventory.getStackInSlot(pSlotIndex);
	}
	
	@Override
	public ItemStack decrStackSize(int pSlotIndex, int pDecrementAmount)
	{
		return mInventory.decrStackSize(pSlotIndex, pDecrementAmount);
	}
	
	@Override
	public void setInventorySlotContents(int pSlotIndex, ItemStack pItemStack)
	{
		mInventory.setInventorySlotContents(pSlotIndex, pItemStack);
	}
	
	@Override
	public int getInventoryStackLimit()
	{
		return mInventory.getInventoryStackLimit();
	}
	
	@Override
	public boolean isUsableByPlayer(EntityPlayer pEntityPlayer)
	{
		return mInventory.isUsableByPlayer(pEntityPlayer, pos);
	}
	
	@Override
	public boolean isItemValidForSlot(int pSlotIndex, ItemStack pItemStack)
	{
		return pItemStack.getItem() instanceof UpgradeItem && additionalUpgradeAllowed(pItemStack) >= pItemStack.getCount();
	}
	
	@Override
	public void markDirty()
	{
		super.markDirty();
		refreshUpgradeCache();
	}
	
	@Override
	public ITextComponent getDisplayName()
	{
		return mInventory.getDisplayName();
	}
	
	@Override
	public String getName()
	{
		return mInventory.getName();
	}
	
	@Override
	public boolean hasCustomName()
	{
		return mInventory.hasCustomName();
	}
	
	@Override
	public void clear()
	{
		mInventory.clear();
	}
	
	@Override
	public void closeInventory(EntityPlayer arg0)
	{
		mInventory.closeInventory(arg0);
	}
	
	@Override
	public int getField(int arg0)
	{
		return mInventory.getField(arg0);
	}
	
	@Override
	public int getFieldCount()
	{
		return mInventory.getFieldCount();
	}
	
	@Override
	public void openInventory(EntityPlayer arg0)
	{
		mInventory.openInventory(arg0);
	}
	
	@Override
	public ItemStack removeStackFromSlot(int arg0)
	{
		return mInventory.removeStackFromSlot(arg0);
	}
	
	@Override
	public void setField(int arg0, int arg1)
	{
		mInventory.setField(arg0, arg1);
	}
	
	@Override
	public void readNBT(NBTTagCompound nbt)
	{
		loadDataFromNBT(nbt);
	}
	
	@Override
	public void writeNBT(NBTTagCompound nbt)
	{
		nbt.setInteger(NBTConstants.TIER_INDEX, mTierIndex);
		mInventory.writeToNBT(nbt);
		mEnergyStorage.writeToNBT(nbt);
		nbt.setInteger("�urrentGen", mCurrentEnergyGeneration);
		if(this instanceof AbstractSolarPanelTileEntity)
		{
			AbstractSolarPanelTileEntity t = (AbstractSolarPanelTileEntity) this;
			nbt.setInteger("MaxGen", t.getMaxGen());
			nbt.setInteger("MaxTransfer", t.transfer);
			nbt.setInteger("MaxCap", t.cap);
		}
	}
	
	public String getTopResource()
	{
		return Reference.MOD_ID + ":blocks/solar" + mTierIndex + "_top";
	}
	
	public String getBaseResource()
	{
		return Reference.MOD_ID + ":blocks/solar" + mTierIndex + "_base";
	}
	
	@Override
	public boolean isEmpty()
	{
		return getInventory().isEmpty();
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing)
	{
		if((capability == CapabilityEnergy.ENERGY || capability == CapabilityEJ.ENERGY) && facing != EnumFacing.UP)
			return true;
		return super.hasCapability(capability, facing);
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing)
	{
		if((capability == CapabilityEnergy.ENERGY || capability == CapabilityEJ.ENERGY) && facing != EnumFacing.UP)
			return (T) this;
		return super.getCapability(capability, facing);
	}
	
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate)
	{
		return 0;
	}
	
	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		return getEnergyStorage().extractEnergy(maxExtract, simulate);
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
    public void createDrop(EntityPlayer player, World world, BlockPos pos)
    {
		WorldLocation loc = new WorldLocation(world, pos);
		if(player != null && !player.capabilities.isCreativeMode && loc.getBlock() instanceof SolarPanelBlock)
			((SolarPanelBlock) loc.getBlock()).dismantleBlock(world, pos);
    }
}