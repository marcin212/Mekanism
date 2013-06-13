package mekanism.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import mekanism.api.EnumColor;
import mekanism.api.IConfigurable;
import mekanism.api.IUpgradeManagement;
import mekanism.api.Object3D;
import mekanism.common.PacketHandler.Transmission;
import mekanism.common.network.PacketTileEntity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import universalelectricity.core.electricity.ElectricityPack;

public class ItemConfigurator extends ItemEnergized
{
	public final int ENERGY_PER_CONFIGURE = 400;
	public final int ENERGY_PER_ITEM_DUMP = 8;
	
    public ItemConfigurator(int id)
    {
        super(id, 60000, 120);
    }
    
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer entityplayer, List list, boolean flag)
	{
		super.addInformation(itemstack, entityplayer, list, flag);
		list.add(EnumColor.PINK + "State: " + EnumColor.GREY + getState(getState(itemstack)));
	}
    
    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
    {
    	if(!world.isRemote)
    	{
    		if(player.isSneaking())
    		{
	    		if(world.getBlockTileEntity(x, y, z) instanceof TileEntityMechanicalPipe)
	    		{
	    			TileEntityMechanicalPipe tileEntity = (TileEntityMechanicalPipe)world.getBlockTileEntity(x, y, z);
	    			tileEntity.isActive = !tileEntity.isActive;
	    			PacketHandler.sendPacket(Transmission.ALL_CLIENTS, new PacketTileEntity(Object3D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())));
	    			return true;
	    		}
	    		else if(world.getBlockTileEntity(x, y, z) instanceof TileEntityElectricPump)
	    		{
	    			TileEntityElectricPump tileEntity = (TileEntityElectricPump)world.getBlockTileEntity(x, y, z);
	    			tileEntity.recurringNodes.clear();
	    			tileEntity.cleaningNodes.clear();
	    			
	    			player.sendChatToPlayer(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "Reset Electric Pump calculation.");
	    			return true;
	    		}
    		}
    		
    		if(getState(stack) == 0)
    		{
	    		if(world.getBlockTileEntity(x, y, z) instanceof IConfigurable)
	    		{
	    			IConfigurable config = (IConfigurable)world.getBlockTileEntity(x, y, z);
	    			
	    			if(!player.isSneaking())
	    			{
	        			player.sendChatToPlayer(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Current color: " + config.getSideData().get(config.getConfiguration()[MekanismUtils.getBaseOrientation(side, config.getOrientation())]).color.getName());
	        			return true;
	    			}
	    			else {
	    				if(getEnergy(stack) >= ENERGY_PER_CONFIGURE)
	    				{
	    					setEnergy(stack, getEnergy(stack) - ENERGY_PER_CONFIGURE);
		    				MekanismUtils.incrementOutput(config, MekanismUtils.getBaseOrientation(side, config.getOrientation()));
		    				player.sendChatToPlayer(EnumColor.DARK_BLUE + "[Mekanism]" + EnumColor.GREY + " Color bumped to: " + config.getSideData().get(config.getConfiguration()[MekanismUtils.getBaseOrientation(side, config.getOrientation())]).color.getName());
		    				
		    				if(config instanceof TileEntityBasicBlock)
		    				{
		    					TileEntityBasicBlock tileEntity = (TileEntityBasicBlock)config;
		    					PacketHandler.sendPacket(Transmission.CLIENTS_RANGE, new PacketTileEntity(Object3D.get(tileEntity), tileEntity.getNetworkedData(new ArrayList())), Object3D.get(tileEntity), 50D);
		    				}
		    				return true;
	    				}
	    			}
	    		}
    		}
    		else if(getState(stack) == 1)
    		{
    			if(world.getBlockTileEntity(x, y, z) instanceof TileEntityContainerBlock)
    			{
    				int itemAmount = 0;
    				Random random = new Random();
    				TileEntityContainerBlock tileEntity = (TileEntityContainerBlock)world.getBlockTileEntity(x, y, z);
    				
    				if(!(tileEntity instanceof TileEntityElectricChest || (((TileEntityElectricChest)tileEntity).canAccess())))
    				{
	    				for(int i = 0; i < tileEntity.getSizeInventory(); ++i)
	    	            {
	    	                ItemStack slotStack = tileEntity.getStackInSlot(i);
	    	                itemAmount += slotStack != null ? slotStack.stackSize : 0;
	
	    	                if(slotStack != null)
	    	                {
	    	                    float xRandom = random.nextFloat() * 0.8F + 0.1F;
	    	                    float yRandom = random.nextFloat() * 0.8F + 0.1F;
	    	                    float zRandom = random.nextFloat() * 0.8F + 0.1F;
	
	    	                    while(slotStack.stackSize > 0)
	    	                    {
	    	                        int j = random.nextInt(21) + 10;
	
	    	                        if(j > slotStack.stackSize)
	    	                        {
	    	                            j = slotStack.stackSize;
	    	                        }
	
	    	                        slotStack.stackSize -= j;
	    	                        EntityItem item = new EntityItem(world, x + xRandom, y + yRandom, z + zRandom, new ItemStack(slotStack.itemID, j, slotStack.getItemDamage()));
	
	    	                        if(slotStack.hasTagCompound())
	    	                        {
	    	                            item.getEntityItem().setTagCompound((NBTTagCompound)slotStack.getTagCompound().copy());
	    	                        }
	
	    	                        float k = 0.05F;
	    	                        item.motionX = random.nextGaussian() * k;
	    	                        item.motionY = random.nextGaussian() * k + 0.2F;
	    	                        item.motionZ = random.nextGaussian() * k;
	    	                        world.spawnEntityInWorld(item);
	    	                    }
	    	                }
	    	            }
	    				
	    				tileEntity.inventory = new ItemStack[tileEntity.getSizeInventory()];
	    				onProvide(new ElectricityPack((ENERGY_PER_ITEM_DUMP*itemAmount)/120, 120), stack);
    				}
    				else {
    					player.addChatMessage(EnumColor.DARK_BLUE + "[Mekanism] " + EnumColor.GREY + "You are not authenticated on this chest.");
	    				return true;
    				}
    			}
    		}
    		else if(getState(stack) == 2)
    		{
    			if(world.getBlockTileEntity(x, y, z) instanceof IUpgradeManagement)
    			{
    				Random random = new Random();
    				IUpgradeManagement management = (IUpgradeManagement)world.getBlockTileEntity(x, y, z);
    				ItemStack[] upgradeStacks = new ItemStack[] {new ItemStack(Mekanism.SpeedUpgrade, management.getSpeedMultiplier()), new ItemStack(Mekanism.EnergyUpgrade, management.getEnergyMultiplier())};
    				
    				for(ItemStack upgrade : upgradeStacks)
    				{
    					if(upgrade.stackSize > 0)
    					{
    						float xRandom = random.nextFloat() * 0.8F + 0.1F;
    	                    float yRandom = random.nextFloat() * 0.8F + 0.1F;
    	                    float zRandom = random.nextFloat() * 0.8F + 0.1F;
    	                    
    	                    EntityItem item = new EntityItem(world, x + xRandom, y + yRandom, z + zRandom, upgrade);
    	                    
                            float k = 0.05F;
	                        item.motionX = random.nextGaussian() * k;
	                        item.motionY = random.nextGaussian() * k + 0.2F;
	                        item.motionZ = random.nextGaussian() * k;
	                        world.spawnEntityInWorld(item);
    					}
    				}
    				
    				management.setSpeedMultiplier(0);
    				management.setEnergyMultiplier(0);
    				return true;
    			}
    		}
    		else if(getState(stack) == 3)
    		{
    			TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
    			
    			if(tileEntity instanceof TileEntityBasicBlock)
    			{
    				TileEntityBasicBlock basicBlock = (TileEntityBasicBlock)tileEntity;
    				int newSide = basicBlock.facing;
    				
    				if(!player.isSneaking())
    				{
    					newSide = side;
    				}
    				else {
    					newSide = ForgeDirection.OPPOSITES[side];
    				}
    				
    				if(basicBlock.canSetFacing(newSide))
    				{
    					basicBlock.setFacing((short)newSide);
	    				world.playSoundEffect(x, y, z, "random.click", 1.0F, 1.0F);
    				}
    				
    				return true;
    			}
    		}
    	}
    	
        return false;
    }
    
    public String getState(int state)
    {
    	switch(state)
    	{
    		case 0:
    			return "modify";
    		case 1:
    			return "empty";
    		case 2:
    			return "upgrade dump";
    		case 3:
    			return "wrench";
    	}
    	
    	return "unknown";
    }
    
    public EnumColor getColor(int state)
    {
    	switch(state)
    	{
    		case 0:
    			return EnumColor.BRIGHT_GREEN;
    		case 1:
    			return EnumColor.AQUA;
    		case 2:
    			return EnumColor.YELLOW;
    		case 3:
    			return EnumColor.ORANGE;
    	}
    	
    	return EnumColor.GREY;
    }
    
    public void setState(ItemStack itemstack, byte state)
    {
		if(itemstack.stackTagCompound == null)
		{
			itemstack.setTagCompound(new NBTTagCompound());
		}
		
		itemstack.stackTagCompound.setByte("state", state);
    }
    
    public byte getState(ItemStack itemstack)
    {
		if(itemstack.stackTagCompound == null)
		{
			return 0;
		}
		
		byte state = 0;
		
		if(itemstack.stackTagCompound.getTag("state") != null)
		{
			state = itemstack.stackTagCompound.getByte("state");
		}
		
		return state;
    }
    
	@Override
	public ElectricityPack getProvideRequest(ItemStack itemStack)
	{
		return new ElectricityPack();
	}
	
	@Override
	public boolean canSend(ItemStack itemStack)
	{
		return false;
	}
}
