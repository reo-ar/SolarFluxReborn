package com.pengu.solarfluxreborn.blocks;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import com.pengu.hammercore.api.mhb.BlockTraceable;
import com.pengu.hammercore.api.mhb.ICubeManager;
import com.pengu.hammercore.common.utils.WorldUtil;
import com.pengu.hammercore.vec.Cuboid6;
import com.pengu.solarfluxreborn.creativetab.ModCreativeTab;
import com.pengu.solarfluxreborn.reference.Reference;
import com.pengu.solarfluxreborn.te.cable.TileCustomCable;

public class BlockAbstractCable extends BlockTraceable implements ITileEntityProvider, ICubeManager
{
	public BlockAbstractCable()
	{
		super(Material.IRON);
		setSoundType(SoundType.METAL);
		setLightOpacity(255);
		useNeighborBrightness = true;
		setCreativeTab(ModCreativeTab.MOD_TAB);
		setHardness(3.0F);
		setHarvestLevel("pickaxe", 0);
		setResistance(5.0F);
	}
	
	public double getTransferRate()
	{
		return 0D;
	}
	
	public ResourceLocation getConnection()
	{
		return new ResourceLocation(Reference.MOD_ID, "blocks/wire_3");
	}
	
	@Override
	public TileEntity createNewTileEntity(World arg0, int arg1)
	{
		return new TileCustomCable(getTransferRate(), getConnection());
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState p_isOpaqueCube_1_)
	{
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState p_isFullCube_1_)
	{
		return false;
	}
	
	@Override
	public Cuboid6[] getCuboids(World world, BlockPos pos, IBlockState state)
	{
		TileCustomCable cc = WorldUtil.cast(world.getTileEntity(pos), TileCustomCable.class);
		return cc != null ? cc.getCuboids() : new Cuboid6[0];
	}
	
	@Override
	public int getLightOpacity(IBlockState p_getLightOpacity_1_)
	{
		return 0;
	}
	
	@Override
	public int getLightOpacity(IBlockState p_getLightOpacity_1_, IBlockAccess p_getLightOpacity_2_, BlockPos p_getLightOpacity_3_)
	{
		return 0;
	}
	
	@Override
	public AxisAlignedBB getFullBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		return FULL_BLOCK_AABB;
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.INVISIBLE;
	}
	
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor)
	{
		TileCustomCable tcc = WorldUtil.cast(world.getTileEntity(pos), TileCustomCable.class);
		if(tcc != null)
			tcc.cubs = tcc.bake();
	}
}