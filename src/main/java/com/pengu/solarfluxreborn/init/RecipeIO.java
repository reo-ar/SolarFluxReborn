package com.pengu.solarfluxreborn.init;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import com.pengu.solarfluxreborn.blocks.SolarPanelBlock;
import com.pengu.solarfluxreborn.config.BlackHoleStorageConfigs;
import com.pengu.solarfluxreborn.config.DraconicEvolutionConfigs;
import com.pengu.solarfluxreborn.items.CraftingItem;
import com.pengu.solarfluxreborn.utility.ArrayHashSet;

public class RecipeIO
{
	private static final ArrayHashSet<FurnaceRecipe> fr = new ArrayHashSet<FurnaceRecipe>();
	
	private static int lastTier = 0;
	
	private static void r2()
	{
		int i = 0;
		while(true)
		{
			boolean sor = recipe(i);
			
			bbb: if(sor)
			{
				if(i < 3)
					break bbb;
				
				SolarPanelBlock spb = (SolarPanelBlock) BlocksSFR.getSolarPanels().get(i);
				CraftingItem unprepared = ItemsSFR.getUnpreparedForPanel(spb);
				
				if(spb != null && unprepared != null)
				{
					FurnaceRecipe fr0 = new FurnaceRecipe(new ItemStack(unprepared), new ItemStack(spb));
					
					if(BlackHoleStorageConfigs.canIntegrate && BlackHoleStorageConfigs.unpreparedSolarsNeedAT)
						fr0 = toBHSRecipe(fr0, spb.getCapacity() / (Math.abs(spb.getTierIndex()) + 1) / 50);
					
					fr.add(fr0);
				}
			} else
				break;
			++i;
		}
		
		// if(ItemsSFR.mUpgradeBlank != null)
		// r.add(parseRecipe(new ItemStack(ItemsSFR.mUpgradeBlank), " c ",
		// "cmc", " c ", 'c', "cobblestone", 'm', ItemsSFR.mirror));
		// if(ItemsSFR.mUpgradeEfficiency != null)
		// r.add(parseRecipe(new ItemStack(ItemsSFR.mUpgradeEfficiency), " c ",
		// "cuc", " s ", 'c', ItemsSFR.solarCell1, 'u', ItemsSFR.mUpgradeBlank,
		// 's', ItemsSFR.solarCell2));
		// if(ItemsSFR.mUpgradeLowLight != null)
		// r.add(parseRecipe(new ItemStack(ItemsSFR.mUpgradeLowLight), "ggg",
		// "lul", "ggg", 'g', "blockGlass", 'u', ItemsSFR.mUpgradeBlank, 'l',
		// "dustGlowstone"));
		// if(ItemsSFR.mUpgradeTraversal != null)
		// r.add(parseRecipe(new ItemStack(ItemsSFR.mUpgradeTraversal), "i i",
		// "rur", "i i", 'i', "ingotIron", 'u', ItemsSFR.mUpgradeBlank, 'r',
		// "dustRedstone"));
		// if(ItemsSFR.mUpgradeTransferRate != null)
		// r.add(parseRecipe(new ItemStack(ItemsSFR.mUpgradeTransferRate),
		// "rrr", "gug", "rrr", 'u', ItemsSFR.mUpgradeBlank, 'r',
		// "dustRedstone", 'g', "ingotGold"));
		// if(ItemsSFR.mUpgradeCapacity != null)
		// r.add(parseRecipe(new ItemStack(ItemsSFR.mUpgradeCapacity), " r ",
		// "rur", "rcr", 'u', ItemsSFR.mUpgradeBlank, 'r', "dustRedstone", 'c',
		// "blockDiamond"));
		// if(ItemsSFR.mUpgradeFurnace != null)
		// r.add(parseRecipe(new ItemStack(ItemsSFR.mUpgradeFurnace), "ccc",
		// "cuc", "cfc", 'u', ItemsSFR.mUpgradeBlank, 'r', "dustRedstone", 'c',
		// Items.COAL, 'f', Blocks.FURNACE));
		
		if(DraconicEvolutionConfigs.canIntegrate)
			loadDEFRecipes();
		
		if(BlackHoleStorageConfigs.canIntegrate)
			loadBHSRecipes();
	}
	
	private static void loadDEFRecipes()
	{
		FusionRecipes.register();
	}
	
	private static void loadBHSRecipes()
	{
		SFRAtomicTransformerRecipes.register();
	}
	
	private static FurnaceRecipe toBHSRecipe(FurnaceRecipe r, long rf)
	{
		return SFRAtomicTransformerRecipes.toAtomic(r, rf);
	}
	
	private static boolean recipe(int solar)
	{
		if(solar == 0)
		{
			lastTier = 0;
			return true;
		} else if(solar == 1 && BlocksSFR.getSolarPanels().size() > solar)
		{
			lastTier = 1;
			return true;
		} else if(solar == 2 && BlocksSFR.getSolarPanels().size() > solar)
		{
			lastTier = 2;
			return true;
		} else if(solar == 3 && BlocksSFR.getSolarPanels().size() > solar)
		{
			lastTier = 3;
			return true;
		} else if(solar == 4 && BlocksSFR.getSolarPanels().size() > solar)
		{
			lastTier = 4;
			return true;
		} else if(solar == 5 && BlocksSFR.getSolarPanels().size() > solar)
		{
			lastTier = 5;
			return true;
		} else if(solar == 6 && BlocksSFR.getSolarPanels().size() > solar)
		{
			lastTier = 6;
			return true;
		} else if(solar == 7 && BlocksSFR.getSolarPanels().size() > solar)
		{
			lastTier = 7;
			return true;
		}
		
		return false;
	}
	
	public static void reload()
	{
		for(FurnaceRecipe r : fr)
			r.removeRecipe();
		r2();
		for(FurnaceRecipe r : fr)
			r.addRecipe();
	}
	
	public static class FurnaceRecipe
	{
		private final ItemStack in, out;
		private final float xp;
		
		public FurnaceRecipe(ItemStack in, ItemStack out)
		{
			this(in, out, 0);
		}
		
		public FurnaceRecipe(ItemStack in, ItemStack out, float xp)
		{
			this.in = in;
			this.out = out;
			this.xp = xp;
		}
		
		public void addRecipe()
		{
			FurnaceRecipes.instance().addSmeltingRecipe(in, out, xp);
		}
		
		public void removeRecipe()
		{
			Map<ItemStack, ItemStack> smeltingList = null;
			Map<ItemStack, Float> experienceList = null;
			
			Field[] fs = FurnaceRecipes.class.getDeclaredFields();
			
			Field f_smeltingList = fs[1];
			f_smeltingList.setAccessible(true);
			
			Field f_experienceList = fs[2];
			f_experienceList.setAccessible(true);
			
			try
			{
				smeltingList = (Map<ItemStack, ItemStack>) f_smeltingList.get(FurnaceRecipes.instance());
			} catch(Throwable err)
			{
				smeltingList = FurnaceRecipes.instance().getSmeltingList();
			}
			try
			{
				experienceList = (Map<ItemStack, Float>) f_experienceList.get(FurnaceRecipes.instance());
			} catch(Throwable err)
			{
			}
			
			smeltingList.remove(in);
			if(experienceList != null)
				experienceList.remove(out);
		}
		
		public ItemStack getIn()
		{
			return in;
		}
		
		public ItemStack getOut()
		{
			return out;
		}
		
		public float getXp()
		{
			return xp;
		}
	}
}