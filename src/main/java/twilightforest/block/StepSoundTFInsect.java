package twilightforest.block;

import net.minecraft.StepSound;

public class StepSoundTFInsect extends StepSound {

	public StepSoundTFInsect(String name, float volume, float pitch) {
		super(name, volume, pitch);
	}

	/**
	 * Used when a block breaks, EXA: Player break, Shep eating grass, etc..
	 */
	@Override
	public String getBreakSound() {
		return "mob.slime.big";
	}

	/**
	 * Used when a entity walks over, or otherwise interacts with the block.
	 */
	@Override
	public String getStepSound() {
		return "mob.slime.big";
	}

	/**
	 * Used when a player places a block.
	 */
	@Override
	public String getPlaceSound() {
		return "mob.slime.big";
	}

}
