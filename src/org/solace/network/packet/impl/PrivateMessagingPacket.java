package org.solace.network.packet.impl;

import org.solace.network.packet.Packet;
import org.solace.network.packet.PacketHandler;
import org.solace.game.entity.mobile.player.Player;

/**
 * Private messaging packet handler.
 * 
 * @author Arithium
 */
public class PrivateMessagingPacket implements PacketHandler {

	@Override
	public void handlePacket(Player player, Packet packet) {
		long username;

		switch (packet.opcode()) {
		/*
		 * Add friend packet.
		 */
		case ADD_FRIEND_OPCODE:
			username = packet.readLong();
			player.getPrivateMessaging().addToFriendsList(username);
			break;

		/*
		 * Add ignore packet.
		 */
		case ADD_IGNORE_OPCODE:
			username = packet.readLong();
			player.getPrivateMessaging().addToIgnoresList(username);
			break;

		/*
		 * Remove friend packet.
		 */
		case REMOVE_FRIEND_OPCODE:
			username = packet.readLong();
			player.getPrivateMessaging().removeFromList(player.getPrivateMessaging().getFriends(), username);
			break;

		/*
		 * Remove ignore packet.
		 */
		case REMOVE_IGNORE_OPCODE:
			username = packet.readLong();
			player.getPrivateMessaging().removeFromList(player.getPrivateMessaging().getIgnores(), username);
			break;
		case SEND_PM_OPCODE:
			username = packet.readLong();
			int size = packet.length() - 8;
			byte[] message = packet.readBytes(size);
			player.getPrivateMessaging()
					.sendPm(player, username, size, message);
			break;
		}
	}

	public static final int ADD_FRIEND_OPCODE = 188;
	public static final int REMOVE_FRIEND_OPCODE = 215;
	public static final int ADD_IGNORE_OPCODE = 133;
	public static final int REMOVE_IGNORE_OPCODE = 74;
	public static final int SEND_PM_OPCODE = 126;

}