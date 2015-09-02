package world;

import java.util.Random;

/**
 * Resource represents some sort of consumable
 * item that exists within the Environment. It
 * has a name and various other attributes.
 */
public enum Resource {
	MEAT(10, 20, false),
	BERRIES(2, 10, false);
	
	public static final int ENTIRE_RESOURCE = -1;
	private final int value;
	private final int regenerationTime;
	private final boolean isHarmful;
	
	private static Random rand = new Random();
	
	/**
	 * Simple constructor requiring all attributes.
	 * @param valueIn
	 * @param isHarmful
	 */
	private Resource(int valueIn, int regenerationTimeIn, boolean isHarmfulIn) {
		if (valueIn < 0) {
			throw new IllegalArgumentException("Value cannot be negative. Use attributes instead.");
		}
		if (regenerationTimeIn < 0) {
			throw new IllegalArgumentException("Regeneration time cannot be negative.");
		}
		this.value = valueIn;
		this.regenerationTime = regenerationTimeIn;
		this.isHarmful = isHarmfulIn;
	}
	
	/**
	 * Getter for value.
	 * @return value
	 */
	public int getValue() {
		return this.value;
	}
	
	/**
	 * Gets the time for this resource to regenerate.
	 * @return regeneration time
	 */
	public int getRegenerationtime() {
		return this.regenerationTime;
	}
	
	/**
	 * Consumes some part of the resource.
	 * If it is a harmful resource, the
	 * returned value will be negative.
	 * Note that the inhabitant cannot
	 * know for sure if this is helpful
	 * or harmful until he has tried it 
	 * before (ie. he recognizes its name).
	 * @return amount consumed
	 */
	public int consume(int amount) {
		
		int amountConsumed = amount == ENTIRE_RESOURCE ? this.value : Math.min(this.value, amount);
		
		if (amountConsumed < 0) {
			throw new IllegalArgumentException("Cannot consume a negative amount of a resource.");
		}
		
		if (this.isHarmful) {
			amountConsumed *= -1;
		}
		
		return amountConsumed;
	}
	
	/**
	 * Seeds the random generator.
	 * @param seed
	 */
	public static void seedRand(int seed) {
		rand = new Random(seed);
	}
	
	/**
	 * Gets a random Resource.
	 * @return a random Resource
	 */
	public static Resource getRandom() {
		return Resource.values()[rand.nextInt(Resource.values().length)];
	}
}
