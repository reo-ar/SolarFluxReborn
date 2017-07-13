package com.pengu.solarfluxreborn.init;

import net.minecraft.item.ItemStack;

import com.pengu.holestorage.api.atomictransformer.AtomicTransformerRecipes;
import com.pengu.holestorage.api.atomictransformer.SimpleTransformerRecipe;
import com.pengu.solarfluxreborn.config.BlackHoleStorageConfigs;
import com.pengu.solarfluxreborn.init.RecipeIO.FurnaceRecipe;

public class SFRAtomicTransformerRecipes
{
	public static void register()
	{
		if(BlackHoleStorageConfigs.DMSolarRequiresTransformation && BlackHoleStorageConfigs.darkMatterSolar)
			AtomicTransformerRecipes.register(new ItemStack(ItemsSFR.unprepareddmsolar), new ItemStack(BlocksSFR.darkMatterSolar), 4000000000L);
	}
	
	public static void register(FurnaceRecipe recipe, long rf)
	{
		AtomicTransformerRecipes.register(recipe.getIn(), recipe.getOut(), rf);
	}
	
	public static FurnaceRecipe toAtomic(FurnaceRecipe r, long rf)
	{
		return new AtomicFurnaceRecipe(r.getIn(), r.getOut(), rf);
	}
	
	public static class AtomicFurnaceRecipe extends FurnaceRecipe
	{
		private final SimpleTransformerRecipe recipe;
		
		public AtomicFurnaceRecipe(ItemStack in, ItemStack out, long rf)
		{
			super(in, out);
			recipe = new SimpleTransformerRecipe(in, out, rf);
		}
		
		@Override
		public void addRecipe()
		{
			AtomicTransformerRecipes.register(recipe);
		}
		
		@Override
		public void removeRecipe()
		{
			AtomicTransformerRecipes.getRecipes().remove(recipe);
		}
	}
}