package inhabitant;

import java.util.Random;

/**
 * Direction represents a direction in the environment.
 */
public enum Direction {
	NORTH,
	EAST,
	WEST,
	SOUTH;
	
	private static Random rand = new Random();
	
	/**
	 * Seeds the random generator.
	 * @param seed
	 */
	public static void seedRand(int seed) {
		rand = new Random(seed);
	}
	
	/**
	 * Gets a random Direction.
	 * @return a random Direction
	 */
	public static Direction getRandom() {
		return Direction.values()[rand.nextInt(Direction.values().length)];
	}
}
