package com.pengu.solarfluxreborn.init;

import java.util.List;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;

import com.google.common.collect.Lists;
import com.pengu.solarfluxreborn.blocks.SolarPanelBlock;
import com.pengu.solarfluxreborn.config.BlackHoleStorageConfigs;
import com.pengu.solarfluxreborn.config.ModConfiguration;
import com.pengu.solarfluxreborn.items.CraftingItem;
import com.pengu.solarfluxreborn.items.UpgradeItem;
import com.pengu.solarfluxreborn.reference.Reference;
import com.pengu.solarfluxreborn.utility.Lang;

public class ItemsSFR
{
	private static final List<CraftingItem> unpreparedSolarPanels = Lists.newArrayList();
	
	public static final Item mirror = new CraftingItem("mirror");
	public static final Item solarCell1 = new CraftingItem("solarCell1");
	public static final Item solarCell2 = new CraftingItem("solarCell2");
	public static final Item solarCell3 = new CraftingItem("solarCell3");
	public static final Item solarCell4 = new CraftingItem("solarCell4");
	
	// Upgrades
	public static Item mUpgradeBlank;
	public static Item mUpgradeEfficiency;
	public static Item mUpgradeLowLight;
	public static Item mUpgradeTraversal;
	public static Item mUpgradeTransferRate;
	public static Item mUpgradeCapacity;
	public static Item mUpgradeFurnace;
	
	public static final Item solarcelldarkmatter = new CraftingItem("solarcelldarkmatter"), unprepareddmsolar = new CraftingItem("unprepareddmsolar");
	
	private ItemsSFR()
	{
	}
	
	public static void initialize()
	{
		if(ModConfiguration.addUnprepared)
		{
			unpreparedSolarPanels.clear();
			for(int tierIndex = 3; tierIndex < ModConfiguration.getTierConfigurations().size(); tierIndex++)
			{
				String name = "unpreparedsolar" + tierIndex;
				CraftingItem unprepared = new CraftingItem(name);
				register(unprepared, name);
				unpreparedSolarPanels.add(unprepared);
			}
		}
		
		register(mirror, "mirror");
		register(solarCell1, "solarcell1");
		register(solarCell2, "solarcell2");
		register(solarCell3, "solarcell3");
		register(solarCell4, "solarcell4");
		
		if(BlackHoleStorageConfigs.canIntegrate)
		{
			if(BlackHoleStorageConfigs.solarcellDM)
			{
				register(solarcelldarkmatter, "solarcelldarkmatter");
			}
			
			if(BlackHoleStorageConfigs.DMSolarRequiresTransformation)
			{
				register(unprepareddmsolar, "unprepareddmsolar");
			}
		}
		
		boolean anyUpgrade = false;
		if(ModConfiguration.isEfficiencyUpgradeActive())
		{
			List<String> infos = Lists.newArrayList();
			infos.add(String.format(Lang.localise("upgrade.efficiency"), ModConfiguration.getEfficiencyUpgradeIncrease() * 100));
			infos.add(localiseReturnsToScale(ModConfiguration.getEfficiencyUpgradeReturnsToScale()));
			mUpgradeEfficiency = new UpgradeItem("upgradeEfficiency", ModConfiguration.getEfficiencyUpgradeMax(), infos);
			register(mUpgradeEfficiency, "upgradeefficiency");
			anyUpgrade = true;
		}
		if(ModConfiguration.isLowLightUpgradeActive())
		{
			mUpgradeLowLight = new UpgradeItem("upgradeLowLight", ModConfiguration.getLowLightUpgradeMax(), Lists.newArrayList(Lang.localise("upgrade.low.light")));
			register(mUpgradeLowLight, "upgradelowlight");
			anyUpgrade = true;
		}
		if(ModConfiguration.isTraversalUpgradeActive())
		{
			mUpgradeTraversal = new UpgradeItem("upgradeTraversal", ModConfiguration.getTraversalUpgradeMax(), Lists.newArrayList(String.format(Lang.localise("upgrade.traversal"), ModConfiguration.getTraversalUpgradeIncrease())));
			register(mUpgradeTraversal, "upgradetraversal");
			anyUpgrade = true;
		}
		if(ModConfiguration.isTransferRateUpgradeActive())
		{
			List<String> infos = Lists.newArrayList();
			infos.add(String.format(Lang.localise("upgrade.transfer"), ModConfiguration.getTransferRateUpgradeIncrease() * 100));
			infos.add(localiseReturnsToScale(ModConfiguration.getTransferRateUpgradeReturnsToScale()));
			mUpgradeTransferRate = new UpgradeItem("upgradeTransferRate", ModConfiguration.getTransferRateUpgradeMax(), infos);
			register(mUpgradeTransferRate, "upgradetransferrate");
			anyUpgrade = true;
		}
		
		if(ModConfiguration.isCapacityUpgradeActive())
		{
			List<String> infos = Lists.newArrayList();
			infos.add(String.format(Lang.localise("upgrade.capacity"), ModConfiguration.getCapacityUpgradeIncrease() * 100));
			infos.add(localiseReturnsToScale(ModConfiguration.getCapacityUpgradeReturnsToScale()));
			mUpgradeCapacity = new UpgradeItem("upgradeCapacity", ModConfiguration.getCapacityUpgradeMax(), infos);
			register(mUpgradeCapacity, "upgradecapacity");
			anyUpgrade = true;
		}
		
		if(ModConfiguration.isFurnaceUpgradeActive())
		{
			mUpgradeFurnace = new UpgradeItem("upgradeFurnace", 1, Lists.newArrayList(Lang.localise("upgrade.furnace")));
			register(mUpgradeFurnace, "upgradefurnace");
			anyUpgrade = true;
		}
		
		if(anyUpgrade)
		{
			mUpgradeBlank = new CraftingItem("upgradeBlank");
			register(mUpgradeBlank, "upgradeblank");
		}
	}
	
	public static CraftingItem getUnpreparedForPanel(SolarPanelBlock panel)
	{
		int tier = panel.getTierIndex() - 3;
		if(unpreparedSolarPanels.size() > tier)
			return unpreparedSolarPanels.get(tier);
		return null;
	}
	
	private static String localiseReturnsToScale(float pValue)
	{
		if(pValue < 1)
		{
			return Lang.localise("decreasingReturnsToScale");
		} else if(pValue > 1)
		{
			return Lang.localise("increasingReturnsToScale");
		}
		return Lang.localise("constantReturnsToScale");
	}
	
	public static Item register(Item item, String name)
	{
		item.setRegistryName(Reference.MOD_ID, name);
		com.pengu.hammercore.init.ItemsHC.items.add(item);
		GameRegistry.findRegistry(Item.class).register(item);
		return item;
	}
}
