package org.solace.world.game.entity.mobile;

import java.util.Deque;
import java.util.LinkedList;
import org.solace.util.ProtocolUtils;
import org.solace.world.game.entity.mobile.Mobile.MovementStatus;
import org.solace.world.game.entity.mobile.Mobile.WelfareStatus;
import org.solace.world.game.entity.mobile.npc.NPC;
import org.solace.world.game.entity.mobile.player.Player;
import org.solace.world.map.Location;

/**
 *
 * @author Faris
 */
public class MobilityManager {
    
    private Mobile mobile;
    private Deque<Location> movementSteps;

    private int walkingDirection = -1, runningDirection = -1;
    private boolean running = true;

    public MobilityManager(Mobile entity2) {
            movementSteps = new LinkedList<Location>();
            entity(entity2);
    }

    public MobilityManager queueDestination(Location destination) {
        Location lastStep = movementSteps.peekLast();
        int diffX = destination.getX() - lastStep.getX();
        int diffY = destination.getY() - lastStep.getY();
        int stepsAmount = Math.max(Math.abs(diffX), Math.abs(diffY));
        for (int i = 0; i < stepsAmount; i++) {
                if (diffX < 0) {
                        diffX++;
                } else if (diffX > 0) {
                        diffX--;
                }
                if (diffY < 0) {
                        diffY++;
                } else if (diffY > 0) {
                        diffY--;
                }
                queueStep(destination.getX() - diffX, destination.getY() - diffY);
        }
        return this;
    }

    public MobilityManager queueStep(int x, int y) {
            Location currentStep = movementSteps.peekLast();
            int diffX = x - currentStep.getX();
            int diffY = y - currentStep.getY();
            if (ProtocolUtils.getDirection(diffX, diffY) > -1) {
                    movementSteps.add(new Location(x, y));
            }
            return this;
    }

    /**
     * Processes getMobile movement.
     */
    public void processMovement() {
            if (mobile.getStatus() == WelfareStatus.DEAD || mobile.getStatus() == WelfareStatus.DIEING)
                    return;
            if (mobile instanceof Player) {
                if (movementSteps.isEmpty()) {
                        return;
                }
                walkingDirection(generateDirection());
                if (running() && !movementSteps.isEmpty()) {
                        runningDirection(generateDirection());
                }
                int diffX = mobile.getLocation().getX() - mobile.getLocation().getRegion().regionX() * 8;
                int diffY = mobile.getLocation().getY() - mobile.getLocation().getRegion().regionY() * 8;
                boolean changed = diffX < 16 || diffX >= 88 || diffY < 16 || diffY >= 88;
                ((Player) mobile).getUpdater().setMapRegionChanging(changed);
            } else if (mobile instanceof NPC) {
                final int random = (int) (Math.floor(Math.random() * 7));
                switch(mobile.getMoveStatus()){
                    case STATIONARY:
                        walkingDirection(-1);
                        break;
                    case MOBILE:
                        mobile.getLocation().lastX = mobile.getLocation().getX();
                        mobile.getLocation().lastY = mobile.getLocation().getY();
                        int distanceX = Math.abs(mobile.getLocation().getX() - mobile.getTargettedLocation().getX());
                        int distanceY = Math.abs(mobile.getLocation().getY() - mobile.getTargettedLocation().getY());
                        int offsetX = ProtocolUtils.DIRECTION_DELTA_X[random];
                        int offsetY = ProtocolUtils.DIRECTION_DELTA_Y[random];
                        Location newLocation = new Location((mobile.getLocation().getX() + offsetX),(mobile.getLocation().getY() + offsetY));
                        if (mobile.getInteractingEntity() != null) {
                            if (!movementSteps.isEmpty()) {
                                    walkingDirection(generateDirection());
                            }
                        } else {
                            if (((int) (Math.random() * 10)) > 1) {
                                    walkingDirection(-1);
                            } else {
                                    if (distanceX > mobile.getMaximumWalkingDistance() || distanceY > mobile.getMaximumWalkingDistance()) {
                                        walkingDirection(-1);
                                    } else {
                                        mobile.setLocation(newLocation);
                                        walkingDirection(random);
                                    }
                            }
                        }
                        break;    
            }
        }
        return;
    }

    /**
     * Generates next walking direction.
     * @return next walking direction for player updating
     */
    public int generateDirection() {
        Location nextStep = movementSteps.poll();
        Location currentStep = mobile.getLocation();
        int diffX = nextStep.getX() - currentStep.getX();
        int diffY = nextStep.getY() - currentStep.getY();
        int direction = ProtocolUtils.getDirection(diffX, diffY);
        if (direction > -1) {
                mobile.getLocation().transform(ProtocolUtils.DIRECTION_DELTA_X[direction],ProtocolUtils.DIRECTION_DELTA_Y[direction]);
        }
        return direction;
    }

    /**
     * Prepares the movement queue for new steps.
     */
    public MobilityManager prepare() {
            walkingDirection(-1).runningDirection(-1);
            movementSteps.clear();
            movementSteps.add(mobile.getLocation());
            return this;
    }

    /**
     * Finishes queue preparation.
     */
    public MobilityManager finish() {
            movementSteps.removeFirst();
            return this;
    }

    public void walkTo(final int directionX, final int directionY) {
        Location entityLocation = getMobile().getLocation();
        int x = (entityLocation.getX() + directionX);
        int y = (entityLocation.getY() + directionY);
        prepare();
        queueDestination(new Location(x, y));
        finish();
    }

    public MobilityManager entity(Mobile entity2) {
            this.mobile = entity2;
            return this;
    }

    public Mobile getMobile() {
            return mobile;
    }

    public MobilityManager walkingDirection(int direction) {
            this.walkingDirection = direction;
            return this;
    }

    public int walkingDirection() {
            return walkingDirection;
    }

    public MobilityManager runningDirection(int direction) {
            this.runningDirection = direction;
            return this;
    }

    public int runningDirection() {
            return runningDirection;
    }

    public MobilityManager running(boolean running) {
            this.running = running;
            return this;
    }

    public boolean running() {
            return running;
    }

    /**
     * Stops the player from moving
     */
    public void stopMovement() {
            walkingDirection(-1).runningDirection(-1);
            movementSteps.clear();
    }

    public MobilityManager processTeleport(Player player, Location location) {
            player.getUpdater().setTeleporting(true);
            player.getUpdater().setMapRegionChanging(true);
            player.getUpdateFlags().setUpdateRequired(true);
            player.setLocation(location);
            return this;
    }

}