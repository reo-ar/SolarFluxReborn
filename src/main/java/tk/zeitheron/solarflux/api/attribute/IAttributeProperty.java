package tk.zeitheron.solarflux.api.attribute;

import java.util.UUID;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

@SuppressWarnings("unused")
public interface IAttributeProperty extends INBTSerializable<CompoundNBT>
{
	double getValue();
	
	default long getValueL()
	{
		return Math.round(getValue());
	}
	
	default int getValueI()
	{
		return (int) Math.min(getValueL(), Integer.MAX_VALUE);
	}
	
	double getBaseValue();
	
	void setBaseValue(double value);

	IAttributeMod getModifier(UUID uuid);
	
	IAttributeMod removeModifier(UUID uuid);
	
	void removeModifier(IAttributeMod mod);
	
	void applyModifier(IAttributeMod mod, UUID uuid);
	
	double recalculateValue();
	
	void clearAttributes();
}