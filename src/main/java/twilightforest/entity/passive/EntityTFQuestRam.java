package twilightforest.entity.passive;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.AxisAlignedBB;
import net.minecraft.Block;
import net.minecraft.ChunkCoordinates;
import net.minecraft.EntityAgeable;
import net.minecraft.EntityAILookIdle;
import net.minecraft.EntityAIPanic;
import net.minecraft.EntityAISwimming;
import net.minecraft.EntityAITempt;
import net.minecraft.EntityAIWander;
import net.minecraft.EntityAnimal;
import net.minecraft.EntityPlayer;
import net.minecraft.EntitySheep;
import net.minecraft.EnumParticle;
import net.minecraft.Item;
import net.minecraft.ItemStack;
import net.minecraft.MathHelper;
import net.minecraft.NBTTagCompound;
import net.minecraft.SharedMonsterAttributes;
import net.minecraft.World;
import twilightforest.TFAchievementPage;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.TFFeature;
import twilightforest.entity.ai.EntityAITFEatLoose;
import twilightforest.entity.ai.EntityAITFFindLoose;
import twilightforest.item.TFItems;


public class EntityTFQuestRam extends EntityAnimal {
	
    private int randomTickDivider;

    /** Client side: the colour flags we last animated, -1 until the first tick has been seen. */
    private int lastAnimatedColorFlags = -1;


	public EntityTFQuestRam(World par1World) {
		super(par1World);
		//this.texture = TwilightForestMod.MODEL_DIR + "questram.png";
		this.setSize(1.25F, 2.9F);
		this.randomTickDivider = 0;
		

        this.getNavigator().setAvoidsWater(true);
        this.tasks.addTask(0, new EntityAISwimming(this));
        this.tasks.addTask(1, new EntityAIPanic(this, 1.38F));
        this.tasks.addTask(2, new EntityAITempt(this, 1.0F, Block.cloth.blockID, false));
        this.tasks.addTask(3, new EntityAITFEatLoose(this, Item.getItem(Block.cloth)));
        this.tasks.addTask(4, new EntityAITFFindLoose(this, 1.0F, Item.getItem(Block.cloth)));
        this.tasks.addTask(5, new EntityAIWander(this, 1.0F));
//        this.tasks.addTask(4, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
        this.tasks.addTask(6, new EntityAILookIdle(this));
	}

	@Override
	public EntityAgeable createChild(EntityAgeable entityanimal)
	{
		return null;
	}

	/**
	 * Set monster attributes
	 */
	@Override
    protected void applyEntityAttributes()
    {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(70.0D); // max health
        this.getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.23000000417232513D);
    }
	
    @Override
	protected void entityInit()
    {
        super.entityInit();
        this.dataWatcher.addObject(16, Integer.valueOf(0));
        this.dataWatcher.addObject(17, Byte.valueOf((byte)0));
    }

    // MITE: EntityAnimal.isAIEnabled() is final (always true), so no override here.
    
    /**
     * Determines if an entity can be despawned, used on idle far away entities
     */
    @Override
	protected boolean canDespawn()
    {
        return false;
    }

    /**
     * Quest rams are not food animals.
     */
    @Override
    public int getExperienceValue()
    {
        return 0;
    }
    
    
    /**
     * main AI tick function, replaces updateEntityActionState
     */
    @Override
	protected void updateAITick()
    {
        if (--this.randomTickDivider <= 0)
        {
            this.randomTickDivider = 70 + this.rand.nextInt(50);
            
            // check if we're near a quest grove and if so, set that as home
            int chunkX = MathHelper.floor_double(this.posX) / 16;
            int chunkZ = MathHelper.floor_double(this.posZ) / 16;
            
            TFFeature nearFeature = TFFeature.getNearestFeature(chunkX, chunkZ, this.worldObj);

            if (nearFeature != TFFeature.questGrove)
            {
                this.detachHome();
            }
            else
            {
            	// set our home position to the center of the quest grove
                ChunkCoordinates cc = TFFeature.getNearestCenterXYZ(MathHelper.floor_double(this.posX), MathHelper.floor_double(this.posZ), worldObj);
                this.setHomeArea(cc.posX, cc.posY, cc.posZ, 13);
            }
            
            // do we need to reward the player?
            if (countColorsSet() > 15 && !getRewarded()) {
            	rewardQuest();
            	setRewarded(true);
            }
            
        }

        super.updateAITick();
    }
    
    /**
     * Pay out!  (MITE_BALANCE 12 #11: low tier materials instead of five mineral blocks.)
     */
    private void rewardQuest() {
    	List<ItemStack> rewards = parseRewards(TFConfig.questRamReward);
    	if (rewards.isEmpty()) {
    		rewards = parseRewards(DEFAULT_REWARD);
    	}
    	for (ItemStack reward : rewards) {
    		this.dropItemStack(reward, 1.0F);
    	}

    	rewardNearbyPlayers(this.worldObj, this.posX, this.posY, this.posZ);
	}

    /** MITE_BALANCE 12 #11 default of the questRam.reward config key. */
    private static final String DEFAULT_REWARD = "iron3,gold2,lapis6,shardEmerald1,horn";

    /**
     * Parses the questRam.reward config value: comma separated {@code name[count]} entries (count defaults to 1), e.g. {@code iron3,horn}.
     * Known names: iron, gold, copper, silver, lapis, coal, emerald, diamond, shardEmerald, shardDiamond, horn. Unknown names are skipped.
     */
    static List<ItemStack> parseRewards(String config) {
    	List<ItemStack> result = new ArrayList<ItemStack>();
    	if (config == null) {
    		return result;
    	}
    	for (String token : config.split(",")) {
    		token = token.trim();
    		int end = token.length();
    		while (end > 0 && Character.isDigit(token.charAt(end - 1))) {
    			end--;
    		}
    		String name = token.substring(0, end).toLowerCase();
    		int count = 1;
    		if (end < token.length()) {
    			try {
    				count = Math.max(1, Math.min(64, Integer.parseInt(token.substring(end))));
    			} catch (NumberFormatException e) {
    				continue;
    			}
    		}
    		ItemStack stack = switch (name) {
    			case "iron" -> new ItemStack(Item.ingotIron, count);
    			case "gold" -> new ItemStack(Item.ingotGold, count);
    			case "copper" -> new ItemStack(Item.ingotCopper, count);
    			case "silver" -> new ItemStack(Item.ingotSilver, count);
    			case "lapis" -> new ItemStack(Item.dyePowder, count, 4);
    			case "coal" -> new ItemStack(Item.coal, count);
    			case "emerald" -> new ItemStack(Item.emerald, count);
    			case "diamond" -> new ItemStack(Item.diamond, count);
    			case "shardemerald" -> new ItemStack(Item.shardEmerald, count);
    			case "sharddiamond" -> new ItemStack(Item.shardDiamond, count);
    			case "horn" -> new ItemStack(TFItems.crumbleHorn, 1);
    			default -> null;
    		};
    		if (stack != null) {
    			result.add(stack);
    		} else {
    			TwilightForestMod.LOGGER.warn("questRam.reward: unknown entry '{}'", token);
    		}
    	}
    	return result;
    }

    /**
     * Give achievement to nearby players
     */
	private void rewardNearbyPlayers(World world, double posX, double posY, double posZ) {
		// scan for players nearby to give the achievement
		List<?> nearbyPlayers = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1).expand(16.0D, 16.0D, 16.0D));
		
		for (Object o : nearbyPlayers) {
			((EntityPlayer)o).triggerAchievement(TFAchievementPage.twilightQuestRam);
		}
	}
    
	/**
     * Called when a player interacts with a mob. (MITE: onEntityRightClicked, runs on both sides.)
     */
    @Override
	public boolean onEntityRightClicked(EntityPlayer par1EntityPlayer, ItemStack currentItem)
    {
        if (currentItem != null && currentItem.getItem() == Item.getItem(Block.cloth) && !isColorPresent(currentItem.getItemDamage()))
        {
            if (par1EntityPlayer.onServer())
            {
                int color = currentItem.getItemDamage();
                this.setColorPresent(color);
                this.animateAddColor(color, 50);

                if (!par1EntityPlayer.inCreativeMode())
                {
                    par1EntityPlayer.convertOneOfHeldItem(null);
                }
            }

            return true;
        }
        else
        {
            return super.onEntityRightClicked(par1EntityPlayer, currentItem);
        }
    }
    
	
    /**
     * Called frequently so the entity can update its state every tick as required. For example, zombies and skeletons
     * use this to react to sunlight and start to burn.
     */
    @Override
	public void onLivingUpdate()
    {
    	super.onLivingUpdate();

    	if (this.worldObj.isRemote) {
    		// the colour animation is client side only: watch the synced flags for newly added colours
    		int flags = this.getColorFlags();
    		if (this.lastAnimatedColorFlags != -1 && flags != this.lastAnimatedColorFlags) {
    			for (int i = 0; i < 16; i++) {
    				int bit = 1 << i;
    				if ((flags & bit) != 0 && (this.lastAnimatedColorFlags & bit) == 0) {
    					this.animateAddColor(i, 50);
    				}
    			}
    		}
    		this.lastAnimatedColorFlags = flags;
    	}

    	checkAndAnimateColors();
    }
    

    /**
     * Called every tick.  If we have got all the colors and have not paid out the reward yet, do a colorful animation.
     */
    public void checkAndAnimateColors() {
		if (countColorsSet() > 15 && !getRewarded()) {
			animateAddColor(this.rand.nextInt(16), 5);
		}
	}

	/**
     * (abstract) Protected helper method to write subclass entity data to NBT.
     */
    @Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.writeEntityToNBT(par1NBTTagCompound);
        par1NBTTagCompound.setInteger("ColorFlags", this.getColorFlags());
        par1NBTTagCompound.setBoolean("Rewarded", this.getRewarded());
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    @Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readEntityFromNBT(par1NBTTagCompound);
        this.setColorFlags(par1NBTTagCompound.getInteger("ColorFlags"));
        this.setRewarded(par1NBTTagCompound.getBoolean("Rewarded"));
    }
    
    /**
     * Returns an int representing which of the 16 colors this ram currently has
     */
    public int getColorFlags()
    {
        return this.dataWatcher.getWatchableObjectInt(16);
    }

    /**
     * Sets the color flags int
     */
    public void setColorFlags(int par1)
    {
    	this.dataWatcher.updateObject(16, Integer.valueOf(par1));
    }

    /**
     * @param color
     * @return true if the specified color is marked present
     */
    public boolean isColorPresent(int color) {
    	int flags = this.getColorFlags();
    	
    	return (flags & (1 << (color & 15))) != 0;
    }
    
    public void setColorPresent(int color) {
    	int flags = this.getColorFlags();
    	
    	setColorFlags(flags | (1 << (color & 15)));
    }
    
    /**
     * Have we paid out the reward yet?
     */
    public boolean getRewarded()
    {
        return this.dataWatcher.getWatchableObjectByte(17) != (byte)0;
    }

    /**
     * Sets whether we have paid the quest reward yet
     */
    public void setRewarded(boolean par1)
    {
    	this.dataWatcher.updateObject(17, par1 ? Byte.valueOf((byte)1) : Byte.valueOf((byte)0));
    }


    
    /**
     * Do a little animation for when we successfully add a new color.
     * Particles are spawned client side only, the sound server side only.
     * @param color
     */
    public void animateAddColor(int color, int iterations) {
    	if (this.worldObj.isRemote) {
    		float[] rgb = EntitySheep.fleeceColorTable[color & 15];
    		for (int i = 0; i < iterations; i++) {
    			this.worldObj.spawnParticle(EnumParticle.mobSpell, this.posX + (this.rand.nextDouble() - 0.5D) * this.width * 1.5, this.posY + this.rand.nextDouble() * this.height * 1.5, this.posZ + (this.rand.nextDouble() - 0.5D) * this.width * 1.5, rgb[0], rgb[1], rgb[2]);
    		}
    	} else {
    		//TODO: it would be nice to play a custom sound
    		playLivingSound();
    	}
    }
    
    /**
     * 
     * @return how many colors are present currently
     */
    public int countColorsSet() {
    	int count = 0;
    	
    	for (int i = 0; i < 16; i++) {
    		if (isColorPresent(i)) {
    			count++;
    		}
    	}
    	
    	return count;
    }
    
    /**
     * Plays living's sound at its position
     */
    public void playLivingSound()
    {
        this.makeSound("mob.sheep.say");
    }
    
    /**
     * Returns the volume for the sounds this mob makes.
     */
    @Override
	protected float getSoundVolume(String sound)
    {
        return 5.0F;
    }
    
    /**
     * Gets the pitch of living sounds in living entities.
     */
    @Override
	protected float getSoundPitch(String sound)
    {
        return (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 0.7F;
    }

    /**
     * Returns the sound this mob makes while it's alive.
     */
    @Override
	protected String getLivingSound()
    {
        return "mob.sheep.say";
    }

    /**
     * Returns the sound this mob makes when it is hurt.
     */
    @Override
	protected String getHurtSound()
    {
        return "mob.sheep.say";
    }

    /**
     * Returns the sound this mob makes on death.
     */
    @Override
	protected String getDeathSound()
    {
        return "mob.sheep.say";
    }

    @Override
	protected void playStepSound(int par1, int par2, int par3, int par4)
    {
        this.playSound("mob.sheep.step", 0.15F, 1.0F);
    }

    public float getMaximumHomeDistance()
    {
        return this.func_110174_bM();
    }
}
