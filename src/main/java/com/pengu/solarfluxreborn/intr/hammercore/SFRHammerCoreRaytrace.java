package com.pengu.solarfluxreborn.intr.hammercore;

import net.minecraft.block.Block;

import com.pengu.hammercore.api.mhb.IRayCubeRegistry;
import com.pengu.hammercore.api.mhb.IRayRegistry;
import com.pengu.hammercore.api.mhb.RaytracePlugin;
import com.pengu.solarfluxreborn.blocks.BlockAbstractCable;

@RaytracePlugin
public class SFRHammerCoreRaytrace implements IRayRegistry
{
	@Override
	public void registerCubes(IRayCubeRegistry cube)
	{
		Block.REGISTRY.getKeys().forEach(rl ->
		{
			Block b = Block.REGISTRY.getObject(rl);
			if(b instanceof BlockAbstractCable)
				cube.bindBlockCubeManager((BlockAbstractCable) b, (BlockAbstractCable) b);
		});
	}
}