package org.solace.network.packet.impl;

import org.solace.game.content.Consumables;
import org.solace.game.content.skills.prayer.Prayer;
import org.solace.game.entity.mobile.player.Player;
import org.solace.network.packet.Packet;
import org.solace.network.packet.PacketHandler;

public class UseItemPacketHandler implements PacketHandler {

	public final static int ITEM_ON_ITEM = 53, ITEM_ON_OBJECT = 192,
			ITEM_ON_GROUND_OBJECT = 25, ITEM_ON_NPC = 57, USE_ITEM = 122;

	@Override
	public void handlePacket(Player player, Packet packet) {
		switch (packet.opcode()) {
		case ITEM_ON_ITEM:
			int usedWithSlot = packet.getUShort();
			int itemUsedSlot = packet.getUShortA();
			int usedWithId = player.getInventory().getItemBySlot(usedWithSlot);
			int itemUsedId = player.getInventory().getItemBySlot(itemUsedSlot);
			System.out.println("Item Used: " + itemUsedId + " Slot: "
					+ itemUsedSlot + " Used With: " + usedWithId + " Slot: "
					+ usedWithSlot);
			handleItemOnItem(player, packet, itemUsedId, usedWithId);
			break;
		case USE_ITEM:
			handleUseItem(player, packet);
			break;
		case ITEM_ON_OBJECT:
			int interfaceType = packet.getShort();
			int objectId = packet.getLEShort();
			int objectY = packet.getLEShortA();
			int itemSlot = packet.getShort();
			int objectX = packet.getLEShortA();
			int itemId = packet.getShort();
			handleItemOnObject(player, packet, interfaceType, objectId,
					objectY, itemSlot, objectX, itemId);
			break;
		case ITEM_ON_GROUND_OBJECT:
			break;
		case ITEM_ON_NPC:
			break;
		}
	}

	public void handleUseItem(Player player, Packet packet) {
		@SuppressWarnings("unused")
		int interfaceId = packet.getLEShortA();
		int slot = packet.getUShortA();
		int itemId = packet.getLEShort();
		if (slot < 0 || slot > 28) {
			return;
		}
		if (player == null)
			return;
		Prayer.buryBones(player, itemId, slot);
		Consumables.eatFood(player, itemId, slot);
	}

	public void handleItemOnItem(Player player, Packet packet, int itemUsedId,
			int usedWithId) {
	
	}

	public void handleItemOnObject(Player player, Packet packet,
			int interfaceType, int objectId, int objectY, int itemSLot,
			int objectX, int itemId) {
	
	}
}
