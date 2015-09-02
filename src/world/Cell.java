package world;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import inhabitant.Inhabitant;

/**
 * Cell represents one unit of occupiable
 * space in the Environment. It can hold a limited
 * number of resources and inhabitants.
 */
public class Cell {
	public static final int MAX_INHABITANTS = 2;
	
	private Resource resource;
	private int timeToRegeneration;
	private List<Inhabitant> inhabitants;
	
	/**
	 * Default constructor initializes attributes.
	 */
	public Cell() {
		this.resource = null;
		this.timeToRegeneration = 0;
		this.inhabitants = new ArrayList<Inhabitant>();
	}
	
	/**
	 * Puts a resource in this cell.
	 * @param resourceIn
	 */
	public boolean putResource(Resource resourceIn) {
		if (this.resource == null) {
			this.resource = resourceIn;
			this.timeToRegeneration = 0;
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the resource in this cell, effectively
	 * removing it from the cell.
	 * @return resource
	 */
	public Resource getResource() {
		if (this.resource != null && this.timeToRegeneration == 0) {
			this.timeToRegeneration = this.resource.getRegenerationtime();
			return this.resource;
		}
		return null;
	}
	
	/**
	 * Steps time forward one unit.
	 */
	public void stepTime() {
		this.timeToRegeneration = Math.max(this.timeToRegeneration - 1, 0);
	}
	
	/**
	 * Gets the list of inhabitants in the cell.
	 * @return an un-modifiable list of the inhabitants
	 */
	public List<Inhabitant> getInhabitants() {
		return Collections.unmodifiableList(this.inhabitants);
	}
	
	/**
	 * Adds an inhabitant, if the cell is not already full.
	 * @param inhabitantIn
	 * @return true or false representing successful add
	 */
	public boolean addInhabitant(Inhabitant inhabitantIn) {
		if (inhabitantIn != null && this.inhabitants.size() < MAX_INHABITANTS) {
			this.inhabitants.add(inhabitantIn);
			return true;
		}
		return false;
	}
	
	/**
	 * Transfers an inhabitant from one cell to another,
	 * returning false in error conditions.
	 * @param inhabitant
	 * @param newCell
	 * @return boolean for successful transfer
	 */
	public boolean moveInhabitant(Inhabitant inhabitant, Cell newCell) {
		if (this.inhabitants.contains(inhabitant) && newCell.addInhabitant(inhabitant)) {
			return this.inhabitants.remove(inhabitant);
		}
		return false;
	}
	
	/**
	 * Cleans out the dead ones.
	 * @return number of dead inhabitants
	 */
	public int cleanOutYerDead() {
		Collection<Inhabitant> corpseCatalog = null;
		for (Inhabitant inhabitant : this.inhabitants) {
			if (inhabitant.isDead()) {
				if (corpseCatalog == null) {
					corpseCatalog = new ArrayList<Inhabitant>();
				}
				corpseCatalog.add(inhabitant);
			}
		}
		
		int numDead = 0;
		if (corpseCatalog != null) {
			for (Inhabitant deadOne : corpseCatalog) {
				this.inhabitants.remove(deadOne);
				numDead++;
			}
		}
		return numDead;
	}
	
}
