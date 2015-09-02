package inhabitant;

/**
 * Movement represents a vector of motion.
 */
public class Movement {
	private Direction direction;
	private int distance;
	
	/**
	 * Simple constructor requiring all attributes.
	 * @param directionIn
	 * @param distanceIn
	 */
	public Movement(Direction directionIn, int distanceIn) {
		this.direction = directionIn;
		this.distance = distanceIn;
	}
	
	/**
	 * Gets the direction.
	 * @return the direction
	 */
	public Direction getDirection() {
		return this.direction;
	}
	
	/**
	 * Gets the distance.
	 * @return the distance
	 */
	public int getDistance() {
		return this.distance;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof Movement)) {
			return false;
		}
		
		Movement otherMovement = (Movement) obj;
		
		return this.direction == otherMovement.getDirection()
				&& this.distance == otherMovement.getDistance();
	}
	
	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.direction != null) {
			hashCode += this.direction.hashCode();
		}
		return hashCode + 10 * this.distance;
	}
}
