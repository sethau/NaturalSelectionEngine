package inhabitant;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import world.Environment;
import world.Resource;

/**
 * Inhabitant represents one inhabitant of
 * the environment. Each inhabitant has a unique
 * ID and many variable attributes. There will be
 * no subclasses of Inhabitant, as all differences
 * should be driven completely from a difference
 * in attribute values. This ensures that the simulation
 * is entirely driven by random mutation, rather than
 * a set of rigidly-separated classes of inhabitants.
 */
public class Inhabitant {
	private static int NEXT_ID = 0;
	private static int MAX_MUTATION_PERCENT = 5;
	private static Random rand = new Random();
	
	private final int id;
	private int maxHealth;
	private int health;
	private int healthDegenerationRate;
	private int minTimeToReproduce;
	private int timeLeftBeforeReproduction;
	private int time;
	
	/**
	 * Constructor requiring all attributes, except for ID and current health.
	 * @param timeIn
	 * @param maxHealthIn
	 * @param healthDegenerationRateIn
	 * @param minTimeToReproduceIn
	 */
	public Inhabitant(int timeIn, int maxHealthIn, int healthDegenerationRateIn, int minTimeToReproduceIn) {
		this(timeIn, maxHealthIn, maxHealthIn, healthDegenerationRateIn, minTimeToReproduceIn);
	}
	
	/**
	 * Constructor requiring all attributes, except for ID.
	 * @param timeIn
	 * @param healthIn
	 * @param maxHealthIn
	 * @param healthDegenerationRateIn
	 * @param minTimeToReproduceIn
	 */
	public Inhabitant(int timeIn, int healthIn, int maxHealthIn, int healthDegenerationRateIn, int minTimeToReproduceIn) {
		if (maxHealthIn <= 0) {
			throw new IllegalArgumentException("An inhabitant's initial health must be greater than 0.");
		}
		if (minTimeToReproduceIn < 0) {
			throw new IllegalArgumentException("Time to reproduce cannot be below 0.");
		}
		this.maxHealth = maxHealthIn;
		this.health = healthIn;
		this.healthDegenerationRate = healthDegenerationRateIn;
		this.id = NEXT_ID++;
		this.minTimeToReproduce = minTimeToReproduceIn;
		this.timeLeftBeforeReproduction = this.minTimeToReproduce;
		
		this.time = timeIn;
	}
	
	/**
	 * Seeds the random generator.
	 * @param seed
	 */
	public static void seedRand(int seed) {
		rand = new Random(seed);
	}
	
	/**
	 * Gets this inhabitant's current time.
	 * @return
	 */
	public int getTime() {
		return this.time;
	}
	
	/**
	 * Gets the current health.
	 * @return the current health
	 */
	public int getHealth() {
		return this.health;
	}
	
	/**
	 * Gets max health
	 * @return max health
	 */
	public int getMaxHealth() {
		return this.maxHealth;
	}
	
	/**
	 * Gets health degeneration rate.
	 * @return health degeneration rate
	 */
	public int getHealthDegenRate() {
		return this.healthDegenerationRate;
	}
	
	/**
	 * Gets the minimum time before reporoduction.
	 * @return minimum time before reporoduction.
	 */
	public int getReproductionTime() {
		return this.minTimeToReproduce;
	}
	
	/**
	 * Gets the time left before reporoduction.
	 * @return time left before reporoduction.
	 */
	public int getTimeLeftBeforeReproduction() {
		return this.timeLeftBeforeReproduction;
	}
	
	/**
	 * Gets the inhabitant's ID.
	 * @return ID
	 */
	public int getId() {
		return this.id;
	}
	
	/**
	 * Before consuming a resource, decide
	 * if you want to consume it, given
	 * only the name of the resource. This
	 * simulates seeing something, not
	 * necessarily knowing if it's harmful
	 * or not, but having a chance to recognize it.
	 * @param resourceName
	 * @return decision regarding consumption of resource
	 */
	private boolean shouldConsumeResource(String resourceName) {
		return true;
	}
	
	/**
	 * Takes possession of a resource. Currently
	 * consumes resource immediately, if it should.
	 * @param resource
	 */
	public void takeResource(Resource resource) {
		if (resource != null && shouldConsumeResource(resource.name())) {
			consumeResource(resource);
		}
	}
	
	/**
	 * Consumes the given resource.
	 * @param resource
	 */
	private void consumeResource(Resource resource) {
		if (resource != null) {
			this.health = Math.min(this.health + resource.consume(Resource.ENTIRE_RESOURCE), this.maxHealth);
		}
	}
	
	/**
	 * Modifies the health by some positive or negative amount.
	 * @param healthMod
	 */
	public void modifyHealth(int healthMod) {
		this.health = Math.min(this.maxHealth, this.health + healthMod);
	}
	
	/**
	 * Returns true if dead, false otherwise
	 * @return true if dead, false otherwise
	 */
	public boolean isDead() {
		return this.health <= 0;
	}
	
	/**
	 * Gets the set of actions that the inhabitant
	 * plans to carry out in the current location.
	 * @param env
	 * @param x
	 * @param y
	 * @return ordered list of actions
	 */
	public List<Action> getActions(Environment env, int x, int y) {
		List<Action> actions = new ArrayList<Action>();

		actions.add(Action.TAKE_RESOURCE);
		actions.add(Action.REPRODUCE);
		
		return actions;
	}
	
	/**
	 * Given access to the environment and the inhabitant's
	 * current coordinates, determine where to move
	 * (or null for no movement).
	 * @param env
	 * @param x
	 * @param y
	 * @return movement decision
	 */
	public Movement getMovementDecision(Environment env, int x, int y) {
		return new Movement(Direction.getRandom(), 1);
	}
	
	/**
	 * Reproduces, if able, with certain random mutations.
	 * Reproducing requires donating half of the current
	 * health to the new offspring.
	 * @return new Inhabitant representing offspring
	 */
	public Inhabitant reproduce() {
		if (this.timeLeftBeforeReproduction > 0) {
			return null;
		}
		
		this.timeLeftBeforeReproduction = this.minTimeToReproduce;
		
		this.health /= 2;
		
		return new Inhabitant(this.time,
				this.health,
				mutate(this.maxHealth),
				mutate(this.healthDegenerationRate),
				mutate(this.minTimeToReproduce));
	}
	
	/**
	 * Mutates a given integer attribute by
	 * up to MAX_MUTATION_PERCENT in either
	 * a positive or negative direction.
	 * @param attribute
	 * @return mutated attribute
	 */
	private int mutate(int attribute) {
		double randPercentChange = (rand.nextInt(200) - 100) * 0.01;
		int result = (int) (randPercentChange * MAX_MUTATION_PERCENT * 0.01 * attribute) + attribute;
		return result;
	}
	
	/**
	 * Mutates a given boolean attribute by
	 * inverting the value with a probability
	 * of MAX_MUTATION_PERCENT / 100.
	 * @param attribute
	 * @return mutated attribute
	 */
	private boolean mutate(boolean attribute) {
		return rand.nextInt(100) > MAX_MUTATION_PERCENT ? attribute : !attribute;
	}
	
	/**
	 * Steps time forward one unit.
	 */
	public void stepTime() {
		this.time++;
		this.timeLeftBeforeReproduction = Math.max(this.timeLeftBeforeReproduction - 1, 0);
		this.health = Math.min(this.health - this.healthDegenerationRate, this.maxHealth);
	}
	
	/**
	 * Generates a random Inhabitant.
	 * @return random Inhabitant
	 */
	public static Inhabitant getRandom(int timeIn) {
		int MIN_MAX_HEALTH = 30, MAX_MAX_HEALTH = 70;
		int MIN_HEALTH_DEGEN_RATE = 1, MAX_HEALTH_DEGEN_RATE = 5;
		int MIN_REPRODUCE_TIME = 30, MAX_REPRODUCE_TIME = 70;
		
		return new Inhabitant(timeIn,
				rand.nextInt(MAX_MAX_HEALTH - MIN_MAX_HEALTH) + MIN_MAX_HEALTH,
				rand.nextInt(MAX_HEALTH_DEGEN_RATE - MIN_HEALTH_DEGEN_RATE) + MIN_HEALTH_DEGEN_RATE,
				rand.nextInt(MAX_REPRODUCE_TIME - MIN_REPRODUCE_TIME) + MIN_REPRODUCE_TIME);
	}
	
	public static String getMetricHeader() {
		return "AvgMaxHealth AvgHealth AvgHealthDegenerationRate AvgMinTimeToReproduce AvgTimeLeftBeforeReproduction";
	}
	
	public static String getInhabitantMetricsString(Collection<Inhabitant> inhabitants) {
		String formatString = "%d\t%d\t%d\t%d\t%d\t%d";
		int totalMaxHealth = 0;
		int totalHealth = 0;
		int totalHealthDegenerationRate = 0;
		int totalMinTimeToReproduce = 0;
		int totalTimeLeftBeforeReproduction = 0;
		
		for (Inhabitant inhabitant : inhabitants) {
			totalMaxHealth += inhabitant.getMaxHealth();
			totalHealth += inhabitant.getHealth();
			totalHealthDegenerationRate += inhabitant.getHealthDegenRate();
			totalMinTimeToReproduce += inhabitant.getReproductionTime();
			totalTimeLeftBeforeReproduction += inhabitant.getTimeLeftBeforeReproduction();
		}
		
		if (inhabitants.size() == 0) {
			return String.format(formatString, 0, 0, 0, 0, 0, 0);
		}
		
		return String.format(formatString,
				inhabitants.size(),
				totalMaxHealth / inhabitants.size(),
				totalHealth / inhabitants.size(),
				totalHealthDegenerationRate / inhabitants.size(),
				totalMinTimeToReproduce / inhabitants.size(),
				totalTimeLeftBeforeReproduction / inhabitants.size());
	}
}
