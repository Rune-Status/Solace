/*
 * This file is part of Solace Framework.
 * Solace is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Solace is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Solace. If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.solace.network.packet.impl;

import org.solace.game.Game;
import org.solace.game.entity.mobile.player.Player;
import org.solace.game.map.Location;
import org.solace.network.packet.Packet;
import org.solace.network.packet.PacketHandler;
import org.solace.task.Task;

/**
 *
 * @author Faris
 */
public class ObjectInteractionPacket implements PacketHandler {
	
	public static final int FIRST_CLICK_OBJECT_OPCODE = 132,
			SECOND_CLICK_OBJECT_OPCODE = 252, THIRD_CLICK_OBJECT_OPCODE = 70;

    @Override
    public void handlePacket(Player player, Packet packet) {
        switch (packet.opcode()) {
        case FIRST_CLICK_OBJECT_OPCODE:
        	handleObjectFirstClick(player, packet);
        	break;
        case SECOND_CLICK_OBJECT_OPCODE:
        	handleObjectSecondClick(player, packet);
        	break;
        	
        case THIRD_CLICK_OBJECT_OPCODE:
        	handleObjectThirdClick(player, packet);
        	break;
        }
    }

	private void handleObjectFirstClick(final Player player, Packet packet) {
		final int objectX = packet.getLEShortA();
		final int objectId = packet.getUShort();
		final int objectY = packet.getUShortA();
		
		final Location location = new Location(objectX, objectY, player
				.getLocation().getH());
		player.walkToAction(new Task(1) {
			boolean arrived = false;

			@Override
			public void execute() {
				if (arrived) {
					player.getUpdateFlags().sendFaceToDirection(location);
					switch (objectId) {
					case 2213:
						player.getBanking().openBank();
						break;
					default:
							player.getPacketDispatcher().sendMessage(
									"ID: " + objectId + " X: " + objectX
											+ "  Y : " + objectY);
							break;
					}
					/*
					 * This stops the walk to action when the player has arrived
					 */
					player.walkToAction(null);
					stop();
				}
				if (player.getLocation().withinDistance(location, 2)) {
					arrived = true;
				}
			}
		});
		Game.submit(player.walkToAction());
	}

	private void handleObjectSecondClick(final Player player, Packet packet) {
		final int objectId = packet.getLEShortA();
		final int objectY = packet.getLEShort();
		final int objectX = packet.getUShortA();
		final Location location = new Location(objectX, objectY, player
				.getLocation().getH());
		player.walkToAction(new Task(1) {
			boolean arrived = false;

			@Override
			public void execute() {
				if (arrived) {
					player.getUpdateFlags().sendFaceToDirection(location);
					switch (objectId) {
					default:
							player.getPacketDispatcher().sendMessage(
									"ID: " + objectId + " X: " + objectX
											+ "  Y : " + objectY);
							break;
					}
					player.walkToAction(null);
					stop();
				}
				if (player.getLocation().withinDistance(location, 2)) {
					arrived = true;
				}
			}
		});
		Game.submit(player.walkToAction());
	}

	private void handleObjectThirdClick(final Player player, Packet packet) {
		final int objectX = packet.getLEShort();
		final int objectY = packet.getUShort();
		final int objectId = packet.getLEShortA();
		final Location location = new Location(objectX, objectY, player
				.getLocation().getH());
		player.walkToAction(new Task(1) {
			boolean arrived = false;

			@Override
			public void execute() {
				if (arrived) {
					player.getUpdateFlags().sendFaceToDirection(location);
					switch (objectId) {
					default:
							player.getPacketDispatcher().sendMessage(
									"ID: " + objectId + " X: " + objectX
											+ "  Y : " + objectY);
							break;
					}
					player.walkToAction(null);
					stop();
				}
				if (player.getLocation().withinDistance(location, 2)) {
					arrived = true;
				}
			}
		});
		Game.submit(player.walkToAction());
	}

}
