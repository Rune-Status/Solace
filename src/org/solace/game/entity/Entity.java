package org.solace.game.entity;

import org.solace.game.entity.UpdateFlags.UpdateFlag;
import org.solace.game.map.Location;

/**
 * 
 * @author Faris
 */
public abstract class Entity {

	/**
	 * Creates an instance of the update flags
	 */
	private UpdateFlags updateFlags = new UpdateFlags();

	/**
	 * Creates an instance of the entities location
	 */
	public Location location;

	/**
	 * Creates an instance of the graphic class
	 */
	private Graphic graphic;

	/**
	 * Creates an instance of the animation class
	 */
	private Animation animation;
	
	private long combatDelay;

	/**
	 * Constructs a new entity
	 * 
	 * @param location
	 *            The Location of the entity
	 */
	public Entity(Location location) {
		this.location = location;
	}

	/**
	 * Returns the entities location
	 * 
	 * @return
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Returns the entities update flag
	 * 
	 * @return
	 */
	public UpdateFlags getUpdateFlags() {
		return updateFlags;
	}

	/**
	 * Returns the entities current graphic
	 * 
	 * @return
	 */
	public Graphic getGraphic() {
		return graphic;
	}

	/**
	 * Sets the entities current graphic
	 * 
	 * @param graphic
	 */
	public void setGraphic(Graphic graphic) {
		this.graphic = graphic;
		getUpdateFlags().flag(UpdateFlag.GRAPHICS);
	}

	/**
	 * Returns the entities current animation
	 * 
	 * @return
	 */
	public Animation getAnimation() {
		return animation;
	}

	/**
	 * Sets the entities current animation
	 * 
	 * @param animation
	 */
	public void setAnimation(Animation animation) {
		this.animation = animation;
		getUpdateFlags().flag(UpdateFlag.ANIMATION);
	}
	
	public long getCombatDelay() {
		return combatDelay;
	}
	
	public void setCombatDelay(long delay) {
		this.combatDelay = delay;
	}

	/**
	 * The update action, must be performed by all entity types
	 */
	public abstract void update();

}
