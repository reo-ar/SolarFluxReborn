package tk.zeitheron.solarflux.items;

import java.util.UUID;

import net.minecraft.item.ItemStack;
import tk.zeitheron.solarflux.InfoSF;
import tk.zeitheron.solarflux.api.attribute.AttributeModMultiply;
import tk.zeitheron.solarflux.block.SolarPanelTile;

public class ItemEfficiencyUpgrade extends UpgradeItem
{
	public ItemEfficiencyUpgrade()
	{
		super(20);
		setRegistryName(InfoSF.MOD_ID, "efficiency_upgrade");
	}
	
	public static final UUID EFFICIENCY_ATTRIBUTE_UUID = UUID.fromString("2857592a-562f-364d-88af-837a6b8f5a8c");
	
	@Override
	public void update(SolarPanelTile tile, ItemStack stack, int amount)
	{
		amount = Math.min(amount, 20);
		tile.generation.applyModifier(new AttributeModMultiply(1F + (amount * .05F)), EFFICIENCY_ATTRIBUTE_UUID);
	}
}