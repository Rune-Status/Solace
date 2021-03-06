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

import org.solace.event.impl.PickupGroundItemService;
import org.solace.game.Game;
import org.solace.game.entity.mobile.player.Player;
import org.solace.game.map.Location;
import org.solace.network.packet.Packet;
import org.solace.network.packet.PacketHandler;
import org.solace.task.Task;

public class PickupGroundItemPacket implements PacketHandler {

	@Override
	public void handlePacket(final Player player, Packet packet) {
		final int itemY = packet.getLEShort();
		final int itemIndex = packet.getUShort();
		final int itemX = packet.getLEShort();

		final Location location = new Location(itemX, itemY, player.getLocation().getH());
		
		/*
		 * Post new walk to action task.
		 */
		player.walkToAction(new Task(1) {
			boolean arrived = false;
			@Override
			public void execute() {
				if (arrived) {
					new PickupGroundItemService(player, location, itemIndex).execute();
					player.walkToAction(null);
					stop();
				}
				if (player.getLocation().sameAs(location)) {
					arrived = true;
				}
			}
		});
		Game.submit(player.walkToAction());
	}

}
