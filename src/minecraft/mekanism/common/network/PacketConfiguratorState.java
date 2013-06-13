package mekanism.common.network;

import java.io.DataOutputStream;

import mekanism.common.ItemConfigurator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.common.io.ByteArrayDataInput;

public class PacketConfiguratorState implements IMekanismPacket
{
	public byte state;
	
	public PacketConfiguratorState(byte b)
	{
		state = b;
	}
	
	public PacketConfiguratorState() {}
	
	@Override
	public String getName()
	{
		return "ConfiguratorState";
	}

	@Override
	public void read(ByteArrayDataInput dataStream, EntityPlayer player, World world) throws Exception 
	{
 		byte state = dataStream.readByte();
		
		ItemStack itemstack = player.getCurrentEquippedItem();
		
		if(itemstack != null && itemstack.getItem() instanceof ItemConfigurator)
		{
			((ItemConfigurator)itemstack.getItem()).setState(itemstack, (byte)state);
		}
	}

	@Override
	public void write(DataOutputStream dataStream) throws Exception
	{
		dataStream.writeByte(state);
	}
}
