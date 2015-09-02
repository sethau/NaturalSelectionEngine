package world;

import inhabitant.Action;
import inhabitant.Direction;
import inhabitant.Inhabitant;
import inhabitant.Movement;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Environment {
	private static final String SEED = "SEED";
	private static final String GRID_SIZE = "GRID_SIZE";
	private static final String NUM_RANDOM_RESOURCES = "NUM_RANDOM_RESOURCES";
	private static final String NUM_RANDOM_INHABITANTS = "NUM_RANDOM_INHABITANTS";
	private static final String TIME_TO_RUN = "TIME_TO_RUN";
	
	private static final int MAX_MOVEMENT_ATTEMPTS = 4;
	private static final int LOG_INTERVAL = 10;

	private static final String LOG_EXT = ".log";
	private static final String OUT_EXT = ".out";
	
	private Cell[][] grid;
	private int numTimeSteps;
	private int time;
	private Random rand = new Random();

	private FileWriter LOG;
	private FileWriter OUT;
	
	/**
	 * Constructor kicks off environment build.
	 * @param envFileName
	 * @throws IOException 
	 */
	public Environment(String envFileName) throws IOException {
		buildEnvironment(envFileName);
		
		String baseFileName = envFileName.split(Pattern.quote("."))[0];
		this.LOG = new FileWriter(new File(baseFileName + LOG_EXT), false);
		this.OUT = new FileWriter(new File(baseFileName + OUT_EXT), false);
		this.OUT.append("#Time NumInhabitants " + Inhabitant.getMetricHeader() + " NumBirths NumDeaths\r\n");
	}
	
	/**
	 * Builds the environment from a environment file.
	 * @param envFileName
	 */
	private void buildEnvironment(String envFileName) {
		if (envFileName == null) {
			throw new IllegalArgumentException("Must pass in an environment file.");
		}

		String[] nextLine = new String[0];
		int seed = 0, numRandomResources = 0, numRandomInhabitants = 0, gridX = 1, gridY = 1, numTimeSteps = 1;
		try {
			Scanner reader = new Scanner(new File(envFileName));
			while (reader.hasNext()) {
				nextLine = reader.nextLine().trim().split("=");
				
				if (SEED.equalsIgnoreCase(nextLine[0])) {
					//Seed for random number generators
					seed = Integer.parseInt(nextLine[1]);
					
				} else if (GRID_SIZE.equalsIgnoreCase(nextLine[0])) {
					//Dimensions for grid
					String[] dimensions = nextLine[1].split(",");
					gridX = Integer.parseInt(dimensions[0]);
					gridY = Integer.parseInt(dimensions[1]);
					
				} else if (NUM_RANDOM_RESOURCES.equalsIgnoreCase(nextLine[0])) {
					//Number of random resources to generate and place on the grid
					numRandomResources = Integer.parseInt(nextLine[1]);
					
				} else if (NUM_RANDOM_INHABITANTS.equalsIgnoreCase(nextLine[0])) {
					//Number of random inhabitants to generate and place on the grid
					numRandomInhabitants = Integer.parseInt(nextLine[1]);
					
				} else if (TIME_TO_RUN.equalsIgnoreCase(nextLine[0])) {
					//Number of time steps to take in the simulation
					numTimeSteps = Integer.parseInt(nextLine[1]);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException(String.format("File '%s' does not exist!", envFileName));
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			throw new IllegalArgumentException(String.format("Line '%s' in the environment file is not formatted correctly.", nextLine.toString()));
		}
		
		initializeTime(numTimeSteps);
		seedRandomGenerators(seed);
		initializeGrid(gridX, gridY);
		generateRandomResources(numRandomResources);
		generateRandomInhabitants(numRandomInhabitants);
		
		System.out.println("Initiallized!");
	}
	
	/**
	 * Initializes the time attributes.
	 * @param numTimeStepsIn
	 */
	private void initializeTime(int numTimeStepsIn) {
		if (numTimeStepsIn < 1) {
			throw new IllegalArgumentException("Must have at least one time step.");
		}
		
		this.numTimeSteps = numTimeStepsIn;
		this.time = 0;
	}
	
	/**
	 * Initializes the grid (but nothing on it).
	 * @param gridX
	 * @param gridY
	 */
	private void initializeGrid(int gridX, int gridY) {
		if (gridX <= 0 || gridY <= 0) {
			throw new IllegalArgumentException("Grid must be at least 1x1.");
		}
		
		this.grid = new Cell[gridX][gridY];
		for (int x = 0; x < this.grid.length; x++) {
			for (int y = 0; y < this.grid[0].length; y++) {
				this.grid[x][y] = new Cell();
			}
		}
	}
	
	/**
	 * Generates the specified number of random resources on the grid.
	 * @param numRandomResources
	 */
	private void generateRandomResources(int numRandomResources) {
		if (this.grid == null) {
			throw new IllegalStateException("The grid must be initialized before resources can be generated.");
		}
		
		if (numRandomResources < 0) {
			throw new IllegalArgumentException("Must not have a negative number of random resources.");
		}
		

		int gridX = this.grid.length, gridY = this.grid[0].length;
		if (numRandomResources > gridX * gridY) {
			throw new IllegalArgumentException("Cannot have more resources than grid cells.");
		}
		
		int numIterations = 0;
		boolean placed = false;
		for (int i = 0; i < numRandomResources; i++) {
			Resource resource = Resource.getRandom();
			placed = false;
			while (!placed) {
				numIterations++;
				int x = this.rand.nextInt(gridX);
				int y = this.rand.nextInt(gridY);
				//Only place a resource on a cell without a resource
				if (this.grid[x][y].putResource(resource)) {
					placed = true;
				}
				//This is to prevent pseudo-infinite loops when the random allocation is causing too many collisions
				else if (numIterations > numRandomResources * (gridX + gridY)) {
					break;
				}
			}
			
			//If the resource wasn't placed by this point, we've hit a point where random allocation won't work practically.
			if (!placed) {
				//We'll just finish out the allocation on a simple and deterministic basis
				//Allocate from bottom left to top right, by row.
				int x = 0, y = 0, remaining = numRandomResources - i;
				do {
					if (this.grid[x % gridX][y % gridY].putResource(resource)) {
						remaining--;
						resource = Resource.getRandom();
					}
					x++;
					if (x % gridX == 0) {
						y++;
					}
				} while (remaining > 0);
				//Break out of the 'for' loop, because all resources have been allocated.
				break;
			}
		}
	}
	
	private void generateRandomInhabitants(int numRandomInhabitants) {
		if (this.grid == null) {
			throw new IllegalStateException("The grid must be initialized before resources can be generated.");
		}
		
		if (numRandomInhabitants < 0) {
			throw new IllegalArgumentException("Must not have a negative number of random inhabitants.");
		}
		

		int gridX = this.grid.length, gridY = this.grid[0].length;
		if (numRandomInhabitants > Cell.MAX_INHABITANTS * gridX * gridY) {
			throw new IllegalArgumentException("Cannot have more inhabitants than can fit in the grid cells.");
		}
		
		int numIterations = 0;
		boolean placed = false;
		for (int i = 0; i < numRandomInhabitants; i++) {
			Inhabitant inhabitant = Inhabitant.getRandom(0);
			placed = false;
			while (!placed) {
				numIterations++;
				int x = this.rand.nextInt(gridX);
				int y = this.rand.nextInt(gridY);
				//Only place an inhabitant on a cell that isn't yet full
				if (this.grid[x][y].addInhabitant(inhabitant)) {
					placed = true;
				}
				//This is to prevent pseudo-infinite loops when the random allocation is causing too many collisions
				else if (numIterations > numRandomInhabitants * (gridX + gridY)) {
					break;
				}
			}
			
			//If the inhabitant wasn't placed by this point, we've hit a point where random allocation won't work practically.
			if (!placed) {
				//We'll just finish out the allocation on a simple and deterministic basis
				//Allocate from bottom left to top right, by row.
				int x = 0, y = 0, remaining = numRandomInhabitants - i;
				do {
					if (this.grid[x % gridX][y % gridY].addInhabitant(inhabitant)) {
						remaining--;
						inhabitant = Inhabitant.getRandom(0);
					}
					x++;
					if (x % gridX == 0) {
						y++;
					}
				} while (remaining > 0);
				//Break out of the 'for' loop, because all inhabitants have been allocated.
				break;
			}
		}
	}
	
	private void seedRandomGenerators(int seed) {
		this.rand = new Random(seed);
		Resource.seedRand(seed);
		Direction.seedRand(seed);
		Inhabitant.seedRand(seed);
	}
	
	public void run() {
		int numBirths = 0;
		int numDeaths = 0;
		logMetrics(String.format("%d\t%d", numBirths, numDeaths));
		while (this.time < this.numTimeSteps) {
			for (int x = 0; x < this.grid.length; x++) {
				for (int y = 0; y < this.grid[0].length; y++) {
					Cell thisCell = this.grid[x][y];
					
					//Step time for this cell
					thisCell.stepTime();
					
					//Clear out corpses
					numDeaths += thisCell.cleanOutYerDead();
					
					//Step time for inhabitants in this cell
					Collection<Inhabitant> originalInhabitants = thisCell.getInhabitants();
					//Create a shallow copy of the inhabitants list to guard against co-modification
					Collection<Inhabitant> inhabitantsToStep = new ArrayList<Inhabitant>();
					for (Inhabitant inhabitant : originalInhabitants) {
						if (inhabitant.getTime() < this.time) {
							inhabitantsToStep.add(inhabitant);
						}
					}
					
					for (Inhabitant inhabitant : inhabitantsToStep) {
						//Step time for inhabitant
						inhabitant.stepTime();
						
						//Get list of actions
						List<Action> actions = inhabitant.getActions(this, x, y);
						for (Action action : actions) {
							switch (action) {
							case TAKE_RESOURCE:
								inhabitant.takeResource(thisCell.getResource());
								break;
							case REPRODUCE:
								Inhabitant offspring = inhabitant.reproduce();
								if (thisCell.addInhabitant(offspring)) {
									numBirths++;
								}
								break;
							}
						}
						
						//Get movement decision
						int numAttempts = 0;
						boolean successful = false;
						do {
							numAttempts++;
							Movement movement = inhabitant.getMovementDecision(this, x, y);
							if (movement != null) {
								Cell newCell;
								switch (movement.getDirection()) {
								case NORTH:
									newCell = this.grid[x][(y + movement.getDistance() + this.grid[0].length) % this.grid[0].length];
									break;
								case EAST:
									newCell = this.grid[(x + movement.getDistance() + this.grid.length) % this.grid.length][y];
									break;
								case SOUTH:
									newCell = this.grid[x][(y - movement.getDistance() + this.grid[0].length) % this.grid[0].length];
									break;
								case WEST:
									newCell = this.grid[(x - movement.getDistance() + this.grid.length) % this.grid.length][y];
									break;
								default:
									throw new IllegalArgumentException(String.format("%s is not a valid Direction.", movement.getDirection()));
								}
								successful = thisCell.moveInhabitant(inhabitant, newCell);
							} else {
								successful = true;
							}
						} while (!successful && numAttempts < MAX_MOVEMENT_ATTEMPTS);
					}
				}
			}
			
			if (time % LOG_INTERVAL == 0) {
				logMetrics(String.format("%d\t%d", numBirths, numDeaths));
				numBirths = 0;
				numDeaths = 0;
			}
			time++;
		}
		
		try {
			this.LOG.close();
			this.OUT.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void logMetrics(String inputMetrics) {
		Collection<Inhabitant> inhabitants = new ArrayList<Inhabitant>();
		for (int x = 0; x < this.grid.length; x++) {
			for (int y = 0; y < this.grid[0].length; y++) {
				for (Inhabitant inhabitant : this.grid[x][y].getInhabitants()) {
					inhabitants.add(inhabitant);
				}
			}
		}
		writeLogLine(this.OUT, String.format("%d\t%s\t%s", this.time, Inhabitant.getInhabitantMetricsString(inhabitants), inputMetrics));
	}
	
	private void writeLogLine(FileWriter writer, String line) {
		try {
			writer.append(line + "\r\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		try {
			Environment env = new Environment("10x10Rand.env");
			env.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
