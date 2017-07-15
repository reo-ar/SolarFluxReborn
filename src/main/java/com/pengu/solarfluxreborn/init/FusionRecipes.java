package com.pengu.solarfluxreborn.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import com.brandon3055.draconicevolution.api.fusioncrafting.FusionRecipeAPI;
import com.brandon3055.draconicevolution.api.fusioncrafting.SimpleFusionRecipe;
import com.pengu.solarfluxreborn.config.DraconicEvolutionConfigs;

public class FusionRecipes
{
	public static void register()
	{
		if(DraconicEvolutionConfigs.chaoticSolar && DraconicEvolutionConfigs.draconicSolar)
			FusionRecipeAPI.addRecipe(new SimpleFusionRecipe(new ItemStack(BlocksSFR.chaoticSolar, 3), new ItemStack(Item.REGISTRY.getObject(new ResourceLocation("draconicevolution", "chaos_shard"))), 256000000, 3, BlocksSFR.draconicSolar, Item.REGISTRY.getObject(new ResourceLocation("draconicevolution", "awakened_core")), BlocksSFR.draconicSolar, Item.REGISTRY.getObject(new ResourceLocation("draconicevolution", "awakened_core")), BlocksSFR.draconicSolar, Item.REGISTRY.getObject(new ResourceLocation("draconicevolution", "awakened_core")), BlocksSFR.draconicSolar, Item.REGISTRY.getObject(new ResourceLocation("draconicevolution", "awakened_core"))));
	}
}