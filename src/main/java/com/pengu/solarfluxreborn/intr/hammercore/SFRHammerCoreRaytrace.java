package com.pengu.solarfluxreborn.intr.hammercore;

import com.pengu.hammercore.api.mhb.RaytracePlugin;
import com.pengu.hammercore.api.mhb.iRayCubeRegistry;
import com.pengu.hammercore.api.mhb.iRayRegistry;
import com.pengu.solarfluxreborn.blocks.BlockAbstractCable;

import net.minecraft.block.Block;

@RaytracePlugin
public class SFRHammerCoreRaytrace implements iRayRegistry
{
	@Override
	public void registerCubes(iRayCubeRegistry cube)
	{
		Block.REGISTRY.getKeys().forEach(rl ->
		{
			Block b = Block.REGISTRY.getObject(rl);
			if(b instanceof BlockAbstractCable)
				cube.bindBlockCubeManager((BlockAbstractCable) b, (BlockAbstractCable) b);
		});
	}
}