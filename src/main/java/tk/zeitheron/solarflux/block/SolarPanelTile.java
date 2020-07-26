package tk.zeitheron.solarflux.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import tk.zeitheron.solarflux.api.attribute.SimpleAttributeProperty;
import tk.zeitheron.solarflux.container.SolarPanelContainer;
import tk.zeitheron.solarflux.items.UpgradeItem;
import tk.zeitheron.solarflux.panels.SolarPanel;
import tk.zeitheron.solarflux.panels.SolarPanelInstance;
import tk.zeitheron.solarflux.panels.SolarPanels;
import tk.zeitheron.solarflux.util.BlockPosFace;
import tk.zeitheron.solarflux.util.SimpleInventory;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

public class SolarPanelTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider, IEnergyStorage
{
	public long energy;
	
	public long currentGeneration;
	public float sunIntensity;

	private SolarPanel delegate;
	private SolarPanelInstance instance;
	
	public final SimpleInventory upgradeInventory = new SimpleInventory(5);
	public final SimpleInventory chargeInventory = new SimpleInventory(1);
	
	public final List<BlockPosFace> traversal = new ArrayList<>();
	
	public final SimpleAttributeProperty generation = new SimpleAttributeProperty();
	public final SimpleAttributeProperty transfer = new SimpleAttributeProperty();
	public final SimpleAttributeProperty capacity = new SimpleAttributeProperty();
	
	public SolarPanelTile()
	{
		super(SolarPanels.SOLAR_PANEL_TYPE);
	}
	
	public int getUpgrades(Item type)
	{
		int c = 0;
		for(int i = 0; i < upgradeInventory.getSlots(); ++i)
		{
			ItemStack stack = upgradeInventory.getStackInSlot(i);
			if(!stack.isEmpty() && stack.getItem() == type)
				c += stack.getCount();
		}
		return c;
	}

	@SuppressWarnings("unused")
	public boolean isSameLevel(SolarPanelTile other)
	{
		if(other == null)
			return false;
		if(other.getDelegate() == null || getDelegate() == null)
			return false;
		return Objects.equals(other.getDelegate(), getDelegate());
	}
	
	public SolarPanel getDelegate()
	{
		if(delegate == null)
		{
			Block blk = getBlockState().getBlock();
			if(blk instanceof SolarPanelBlock)
				this.delegate = ((SolarPanelBlock) blk).panel;
			else
				delegate = SolarPanels.CORE_PANELS[0];
		}
		return delegate;
	}
	
	public SolarPanelInstance getInstance()
	{
		if(instance == null || instance.getDelegate() != getDelegate())
			instance = getDelegate().createInstance(this);
		return instance;
	}
	
	List<ResourceLocation> tickedUpgrades = new ArrayList<>();
	
	public void tickUpgrades()
	{
		ItemStack stack;
		ResourceLocation id;
		
		generation.clearAttributes();
		transfer.clearAttributes();
		capacity.clearAttributes();
		
		for(int i = 0; i < upgradeInventory.getSlots(); ++i)
		{
			stack = upgradeInventory.getStackInSlot(i);
			if(!stack.isEmpty())
			{
				if(stack.getItem() instanceof UpgradeItem && ((UpgradeItem) stack.getItem()).canStayInPanel(this, stack, upgradeInventory))
				{
					id = stack.getItem().getRegistryName();
					if(!tickedUpgrades.contains(id))
					{
						UpgradeItem iu = (UpgradeItem) stack.getItem();
						iu.update(this, stack, getUpgrades(iu));
						tickedUpgrades.add(id);
					}
				} else
				{
					// Why non-upgrade items would end up in this inventory?
					// idk, let's drop them!
					ItemStack s = upgradeInventory.getStackInSlot(i);
					s.copy();
					upgradeInventory.setStackInSlot(i, ItemStack.EMPTY);
					assert world != null;
					if(!world.isRemote)
						world.addEntity(new ItemEntity(world, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack));
				}
			}
		}
		
		if(energy > 0L && getInstance() != null)
			for(int i = 0; i < chargeInventory.getSlots(); ++i)
			{
				stack = chargeInventory.getStackInSlot(i);
				if(!stack.isEmpty())
				{
					stack.getCapability(CapabilityEnergy.ENERGY, null).filter(e -> e.getEnergyStored() < e.getMaxEnergyStored()).ifPresent(e ->
					{
						transfer.setBaseValue(getInstance().transfer);
						int transfer = this.transfer.getValueI();
						energy -= e.receiveEnergy(Math.min(getEnergyStored(), transfer), false);
					});
				}
			}
		
		tickedUpgrades.clear();
	}
	
	@Override
	public void tick()
	{
		if(voxelTimer > 0)
			--voxelTimer;
		Block blk = getBlockState().getBlock();
		if(blk instanceof SolarPanelBlock)
			this.delegate = ((SolarPanelBlock) blk).panel;
		else
			return;
		
		if(cache$seeSkyTimer > 0)
			--cache$seeSkyTimer;

		assert world != null;
		if(world.isRemote)
			return;
		
		if(world.getDayTime() % 20L == 0L)
			traversal.clear();
		
		tickUpgrades();
		
		int gen = getGeneration();
		capacity.setBaseValue(getInstance().cap);
		energy += Math.min(capacity.getValueL() - energy, gen);
		currentGeneration = gen;
		
		energy = Math.min(Math.max(energy, 0), capacity.getValueL());
		{
			for(Direction hor : Direction.values())
				if(hor.getAxis() != Axis.Y)
				{
					TileEntity tile = world.getTileEntity(pos.offset(hor));
					if(tile instanceof SolarPanelTile)
						autoBalanceEnergy((SolarPanelTile) tile);
				}
			
			transfer.setBaseValue(getInstance().transfer);
			int transfer = this.transfer.getValueI();
			
			for(Direction hor : Direction.values())
			{
				if(hor == Direction.UP)
					continue;
				
				TileEntity tile = world.getTileEntity(pos.offset(hor));
				
				if(tile == null)
					continue;
				
				tile.getCapability(CapabilityEnergy.ENERGY, hor.getOpposite()).ifPresent(storage ->
				{
					if(storage.canReceive())
						energy -= storage.receiveEnergy(Math.min(getEnergyStored(), transfer), false);
				});
			}
			
			if(!traversal.isEmpty())
			{
				for(BlockPosFace traverse : traversal)
				{
					TileEntity tile = world.getTileEntity(traverse.pos);
					
					if(energy < 1L)
						break;
					if(tile == null)
						continue;
					
					tile.getCapability(CapabilityEnergy.ENERGY, traverse.face).ifPresent(storage ->
					{
						if(storage.canReceive())
							energy -= storage.receiveEnergy(Math.min(getEnergyStored(), Math.round(transfer * traverse.rate)), false);
					});
				}
			}
		}
		
		world.updateComparatorOutputLevel(pos, getBlockState().getBlock());
	}
	
	public int getGeneration()
	{
		float eff = getInstance().computeSunIntensity(this);
		assert world != null;
		if(!world.isRemote)
			sunIntensity = eff;
		float energyGeneration = getInstance().gen * eff;
		generation.setBaseValue(energyGeneration);
		return generation.getValueI();
	}
	
	public int autoBalanceEnergy(SolarPanelTile solar)
	{
		int delta = getEnergyStored() - solar.getEnergyStored();
		if(delta < 0)
			return solar.autoBalanceEnergy(this);
		else if(delta > 0)
			return extractEnergy(solar.receiveEnergyInternal(extractEnergy(solar.receiveEnergyInternal(delta / 2, true), true), false), false);
		return 0;
	}
	
	public boolean cache$seeSky;
	public byte cache$seeSkyTimer;
	
	public boolean doesSeeSky()
	{
		if(cache$seeSkyTimer < 1)
		{
			cache$seeSkyTimer = 20;
			cache$seeSky = (world != null && world.getLightFor(LightType.SKY, pos) > 0 && pos != null) && world.canBlockSeeSky(pos);
		}
		return cache$seeSky;
	}
	
	public static final ModelProperty<World> WORLD_PROP = new ModelProperty<>();
	public static final ModelProperty<BlockPos> POS_PROP = new ModelProperty<>();
	
	@Nonnull
	@Override
	public IModelData getModelData()
	{
		return new ModelDataMap.Builder().withInitial(WORLD_PROP, world).withInitial(POS_PROP, pos).build();
	}
	
	private void writeNBT(CompoundNBT nbt)
	{
		upgradeInventory.writeToNBT(nbt, "Upgrades");
		chargeInventory.writeToNBT(nbt, "Chargeable");
		nbt.putLong("Energy", energy);
	}
	
	private void readNBT(CompoundNBT nbt)
	{
		upgradeInventory.readFromNBT(nbt, "Upgrades");
		chargeInventory.readFromNBT(nbt, "Chargeable");
		energy = nbt.getLong("Energy");
	}
	
	LazyOptional<?> chargeableItems, energyStorageTile;
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, Direction side)
	{
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
		{
			if(chargeableItems == null)
				chargeableItems = LazyOptional.of(() -> chargeInventory);
			return chargeableItems.cast();
		} else if(cap == CapabilityEnergy.ENERGY)
		{
			if(energyStorageTile == null)
				energyStorageTile = LazyOptional.of(() -> SolarPanelTile.this);
			return energyStorageTile.cast();
		}
		return super.getCapability(cap, side);
	}
	
	@Nonnull
	@Override
	public CompoundNBT write(CompoundNBT compound)
	{
		CompoundNBT panel;
		writeNBT(panel = new CompoundNBT());
		compound.put("panel", panel);
		return super.write(compound);
	}
	
	@Override
	public void read(CompoundNBT compound)
	{
		readNBT(compound.getCompound("panel"));
		super.read(compound);
	}
	
	int voxelTimer = 0;
	VoxelShape shape;
	
	public void resetVoxelShape()
	{
		shape = null;
	}
	
	public VoxelShape getShape(SolarPanelBlock block)
	{
		if(shape == null || voxelTimer <= 0)
		{
			assert world != null;
			shape = block.recalcShape(world, pos);
			voxelTimer = 20;
		}
		return shape;
	}
	
	@Nonnull
	@Override
	public CompoundNBT getUpdateTag()
	{
		CompoundNBT nbt = new CompoundNBT();
		writeNBT(nbt);
		return nbt;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket()
	{
		return new SUpdateTileEntityPacket(pos, 0, getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt)
	{
		readNBT(pkt.getNbtCompound());
	}

	@SuppressWarnings("unused")
	public void sync()
	{
		assert world != null;
		BlockState state = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, state, state, 11);
	}
	
	@Override
	public Container createMenu(int windowId, @Nonnull PlayerInventory playerInv, @Nonnull PlayerEntity arg2)
	{
		return new SolarPanelContainer(windowId, playerInv, this);
	}
	
	@Nonnull
	@Override
	public ITextComponent getDisplayName()
	{
		return new TranslationTextComponent(getBlockState().getBlock().getTranslationKey());
	}
	
	@Override
	public int extractEnergy(int maxExtract, boolean simulate)
	{
		transfer.setBaseValue(getInstance().transfer);
		int transfer = this.transfer.getValueI();
		int energyExtracted = Math.min(getEnergyStored(), Math.min(transfer, maxExtract));
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
		transfer.setBaseValue(getInstance().transfer);
		int transfer = this.transfer.getValueI();
		capacity.setBaseValue(getInstance().cap);
		long cap = capacity.getValueL();
		int energyReceived = Math.min((int) Math.min(cap - energy, Integer.MAX_VALUE), Math.min(transfer, maxReceive));
		if(!simulate)
			energy += energyReceived;
		return energyReceived;
	}
	
	@Override
	public int getEnergyStored()
	{
		return (int) Math.min(energy, Integer.MAX_VALUE);
	}
	
	@Override
	public int getMaxEnergyStored()
	{
		return (int) Math.min(getInstance().cap, Integer.MAX_VALUE);
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
	
	public ItemStack generateItem(IItemProvider item)
	{
		ItemStack stack = new ItemStack(item);
		stack.setTag(new CompoundNBT());
		assert stack.getTag() != null;
		stack.getTag().putLong("Energy", energy - Math.round(energy * SolarPanels.LOOSE_ENERGY / 100D));
		upgradeInventory.writeToNBT(stack.getTag(), "Upgrades");
		chargeInventory.writeToNBT(stack.getTag(), "Chargeable");
		return stack;
	}
	
	public void loadFromItem(ItemStack stack)
	{
		if(stack.hasTag())
		{
			assert stack.getTag() != null;
			energy = stack.getTag().getLong("Energy");
			upgradeInventory.readFromNBT(stack.getTag(), "Upgrades");
			chargeInventory.readFromNBT(stack.getTag(), "Chargeable");
		}
	}

	public void setDelegate(SolarPanel delegate)
	{
		this.delegate = delegate;
	}
}