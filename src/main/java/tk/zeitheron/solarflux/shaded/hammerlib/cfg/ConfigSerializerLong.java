package tk.zeitheron.solarflux.shaded.hammerlib.cfg;

import java.io.BufferedWriter;
import java.io.IOException;

public class ConfigSerializerLong extends ConfigEntrySerializer<ConfigEntryLong>
{
	public ConfigSerializerLong()
	{
		super("L");
	}
	
	@Override
	public void write(Configuration config, BufferedWriter writer, ConfigEntryLong entry, int indents) throws IOException
	{
		if(entry.getDescription() != null)
			writeComment(writer, entry.getDescription() + " (Default: " + entry.initialValue + ", Range: [" + entry.min + ";" + entry.max + "])", indents);
		writeIndents(writer, indents);
		writer.write("+" + type + ":" + entry.getSerializedName() + "=" + entry.getValue() + "\n\n");
	}
	
	@Override
	public ConfigEntryLong read(Configuration config, ReaderHelper reader, int indents) throws IOException
	{
		ConfigEntryLong lnt = new ConfigEntryLong(config, null);
		
		// Skips comments
		reader.until('+', true);
		
		if(reader.eat(type, true) && reader.eat(':', true))
		{
			String name = reader.until('=', true);
			String value = reader.getRest();
			if(value.contains("\n"))
				value = value.substring(0, value.indexOf('\n'));
			
			lnt.name = name;
			try
			{
				lnt.value = Math.max(lnt.min, Math.min(lnt.max, Long.parseLong(value)));
			} catch(NumberFormatException nfe)
			{
				lnt.value = null;
			}
		}
		
		return lnt;
	}
}