package org.solace.game.map;

import org.solace.game.entity.mobile.player.Player;

/**
 *
 * @author Faris
 */
public class Boundary {
    
    /**
     * The given parameters for the boundary
     * these vary per instance
     */
    private int lowestX, 
                lowestY, 
                highestX, 
                highestY;
    
    /**
     * Constructor set to assign the variables in a new instance of this class
     * @param lowest
     * @param highest 
     */
    public Boundary(Location lowest, Location highest){
        this.lowestX = lowest.getX();
        this.lowestY = lowest.getY();
        this.highestX = highest.getX();
        this.highestY = highest.getY();
    }
    
    /**
     * Standard method for usage of this class
     * @param player
     * @return 
     */
    public boolean withinBoundry(Player player){
        return (player.getLocation().getX() >= lowestX) & (player.getLocation().getX() <= highestX) & (player.getLocation().getY() >= lowestY) & (player.getLocation().getY() <= highestY);
    }

}