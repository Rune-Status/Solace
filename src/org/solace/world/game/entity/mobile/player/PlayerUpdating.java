package org.solace.world.game.entity.mobile.player;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.solace.network.packet.PacketBuilder;
import org.solace.util.ProtocolUtils;
import org.solace.world.game.Game;
import org.solace.world.game.entity.UpdateFlags.UpdateFlag;

/**
 * 
 * @author Faris
 */
public class PlayerUpdating {

	private Player master;
	private boolean teleporting = true;
	private boolean mapRegionChanging = true;
	private List<Player> localPlayers;
	public byte chatText[] = new byte[256];
	public int chatTextEffects = 0, chatTextColor = 0;

	public PlayerUpdating(Player master) {
		this.master = master;
		localPlayers = new LinkedList<Player>();
	}

	public Player getMaster() {
		return master;
	}

	private void populateRegion(PacketBuilder out, PacketBuilder block) {
		master.getLocation().getRegion().playersWithinRegion().clear();
		Iterator<Player> it = Game.playerRepository.values().iterator();
		while (it.hasNext()) {
			Player player = it.next();
			if (getMaster().getUpdater().localPlayers.size() >= 255) {
				// list is full
				break;
			}
			if (player == null)
				continue;
			if (player == getMaster())
				continue;
			if (!getMaster().getUpdater().localPlayers.contains(player)
					&& getMaster().getLocation().withinDistance(
							player.getLocation())) {
				master.getLocation().getRegion().playersWithinRegion()
						.add(player);
				getMaster().getUpdater().localPlayers.add(player);
				addPlayer(out, player);
				updateGivenPlayer(block, player, true);
			}
		}
	}

	/**
	 * Handles the player update protocol
	 */
	public void updateMaster() {
		if (mapRegionChanging) {
			master.getPacketDispatcher().sendMapRegion();
		}
		PacketBuilder out = PacketBuilder.allocate(16384);
		PacketBuilder block = PacketBuilder.allocate(8192);
		out.createShortSizedFrame(81, master.channelContext().encryption());
		out.bitAccess();
		updateThisPlayerMovement(out);
		updateGivenPlayer(block, master, false);
		out.putBits(8, localPlayers.size());

		for (Iterator<Player> i = localPlayers.iterator(); i.hasNext();) {
			Player player = i.next();
			if (Game.playerRepository.values().contains(player)
					&& !player.getUpdater().teleporting
					&& getMaster().getLocation().withinDistance(
							player.getLocation())) {
				updatePlayerMovement(out, player);
				updateGivenPlayer(block, player, false);
			} else {
				out.putBits(1, 1); // Update Requierd
				out.putBits(2, 3); // Remove Player
				i.remove();
			}
		}
		populateRegion(out, block);
		if (block.buffer().position() > 0) {
			out.putBits(11, 2047);
			out.byteAccess();
			out.put(block.buffer());
		} else {
			out.byteAccess();
		}
		out.finishShortSizedFrame();
		out.sendTo(master.channelContext().channel());
	}

	public void updateGivenPlayer(PacketBuilder out, Player player, boolean force) {
		if (!player.getUpdateFlags().isUpdateRequired() && !force) {
			return;
		}
		int mask = 0x0;
		if (player.getUpdateFlags().get(UpdateFlag.FORCE_MOVEMENT)) {
			mask |= 0x400;
		}
		if (player.getUpdateFlags().get(UpdateFlag.GRAPHICS)) {
			mask |= 0x100;
		}
		if (player.getUpdateFlags().get(UpdateFlag.ANIMATION)) {
			mask |= 0x8;
		}
		if (player.getUpdateFlags().get(UpdateFlag.FORCED_CHAT)) {
			mask |= 0x4;
		}
		if (player.getUpdateFlags().get(UpdateFlag.CHAT)
				&& player != getMaster()) {
			mask |= 0x80;
		}
		if (player.getUpdateFlags().get(UpdateFlag.FACE_ENTITY)) {
			// mask |= 0x1;
		}
		if (player.getUpdateFlags().get(UpdateFlag.APPEARANCE) || force) {
			mask |= 0x10;
		}
		if (player.getUpdateFlags().get(UpdateFlag.FACE_COORDINATE)) {
			// mask |= 0x2;
		}
		if (player.getUpdateFlags().get(UpdateFlag.HIT)) {
			// mask |= 0x20;
		}
		if (player.getUpdateFlags().get(UpdateFlag.HIT_2)) {
			// mask |= 0x200;
		}
		if (mask >= 0x100) {
			mask |= 0x40;
			out.putByte(mask & 0xFF);
			out.putByte(mask >> 8);
		} else {
			out.putByte(mask);
		}
		checkRequiredUpdates(out, player, force);
	}

	public void updateThisPlayerMovement(PacketBuilder out) {
		if (teleporting || mapRegionChanging) {
			out.putBits(1, 1); // Update Required
			out.putBits(2, 3); // Player Teleported
			out.putBits(2, master.getLocation().getH()); // current height
			out.putBits(1, teleporting); // teleporting);
			out.putBits(1, master.getUpdateFlags().isUpdateRequired()); // update
																		// required
			out.putBits(7, master.getLocation().localY());
			out.putBits(7, master.getLocation().localX());
		} else {
			if (master.getMobilityManager().walkingDirection() == -1) {
				if (master.getUpdateFlags().isUpdateRequired()) {
					out.putBits(1, 1); // update required
					out.putBits(2, 0); // we didn't move
				} else {
					out.putBits(1, 0); // Nothing changed
				}
			} else {
				if (master.getMobilityManager().runningDirection() == -1) {
					out.putBits(1, 1); // this is update required...
					out.putBits(2, 1); // walking
					out.putBits(3, master.getMobilityManager()
							.walkingDirection()); // Direction
					out.putBits(1, master.getUpdateFlags().isUpdateRequired()); // Update
																				// block
				} else {
					out.putBits(1, 1); // updating required
					out.putBits(2, 2); // running - 2 seconds
					out.putBits(3, master.getMobilityManager()
							.walkingDirection()); // Walking
					out.putBits(3, master.getMobilityManager()
							.runningDirection()); // Running
					out.putBits(1, master.getUpdateFlags().isUpdateRequired()); // Update
																				// block
				}
			}
		}
	}

	private void updatePlayerMovement(PacketBuilder out, Player player) {
		if (player.getMobilityManager().walkingDirection() == -1) {
			if (player.getUpdateFlags().isUpdateRequired()) {
				out.putBits(1, 1); // Update required
				out.putBits(2, 0); // No movement
			} else {
				out.putBits(1, 0); // Nothing changed
			}
		} else if (player.getMobilityManager().runningDirection() == -1) {
			out.putBits(1, 1); // Update required
			out.putBits(2, 1); // Player walking one tile
			out.putBits(3, player.getMobilityManager().walkingDirection()); // Walking
			out.putBits(1, player.getUpdateFlags().isUpdateRequired()); // Update
		} else {
			out.putBits(1, 1); // Update Required
			out.putBits(2, 2); // Moved two tiles
			out.putBits(3, player.getMobilityManager().walkingDirection()); // Walking
			out.putBits(3, player.getMobilityManager().runningDirection()); // Running
			out.putBits(1, player.getUpdateFlags().isUpdateRequired()); // Update
		}
	}

	public void addPlayer(PacketBuilder out, Player otherPlayer) {
		out.putBits(11, otherPlayer.getIndex()); // Writing player index.
		out.putBits(1, 1); // Update required.
		out.putBits(1, 1); // Discard walking.
		int yPos = otherPlayer.getLocation().getY()
				- master.getLocation().getY();
		int xPos = otherPlayer.getLocation().getX()
				- master.getLocation().getX();
		out.putBits(5, yPos); // The relative coordinates.
		out.putBits(5, xPos); // The relative coordinates.
	}

	private void checkRequiredUpdates(PacketBuilder out, Player player,
			boolean force) {
		if (player.getUpdateFlags().get(UpdateFlag.GRAPHICS)) {
			appendGraphicMask(player, out);
		}
		if (player.getUpdateFlags().get(UpdateFlag.ANIMATION)) {
			appendAnimationMask(player, out);
		}
		if (player.getUpdateFlags().get(UpdateFlag.FORCED_CHAT)) {
			// out.putString(player.getUpdateFlags().getForceChatMessage());
		}
		if (player.getUpdateFlags().get(UpdateFlag.CHAT)
				&& player != getMaster()) {
			updatePlayerChat(out, player);
		}
		if (player.getUpdateFlags().get(UpdateFlag.APPEARANCE) || force) {
			updatePlayerAppearance(out, player);
		}
	}

	public void updatePlayerChat(PacketBuilder out, Player player) {
		int effects = ((player.getUpdater().chatTextColor & 0xff) << 8)
				+ (player.getUpdater().chatTextEffects & 0xff);
		out.putLEShort(effects);
		out.putByte(player.getAuthentication().getPlayerRights());
		out.putByteC(player.getUpdater().chatText.length);
		out.put(player.getUpdater().chatText);
	}

	public void updatePlayerAppearance(PacketBuilder out, Player player) {
		PacketBuilder props = PacketBuilder.allocate(128);
		props.putByte(player.getAuthentication().playerGender());
		props.putByte(player.getPlayerHeadIcon());
		props.putByte(-1);// TODO: Player Skull
		props.putByte(0); // Player Hat
		props.putByte(0); // Player Cape
		props.putByte(0); // Player Amulet
		props.putByte(0); // Player Weapon
		props.putShort(0x100 + player.getAuthentication().playerTorso()); // Body
		props.putByte(0); // Player Shield
		props.putShort(0x100 + player.getAuthentication().playerArms()); // Arms
		props.putShort(0x100 + player.getAuthentication().playerLegs()); // Legs
		props.putShort(0x100 + player.getAuthentication().playerHead()); // Head
		props.putShort(0x100 + player.getAuthentication().playerHands()); // Hands
		props.putShort(0x100 + player.getAuthentication().playerFeet()); // Feet
		if (player.getAuthentication().getPlayerAppearanceIndex(0) == 0) {
			props.putShort(0x100 + player.getAuthentication().playerJaw());
		} else {
			props.putByte(0);
		}
		props.putByte(player.getAuthentication().playerHairColour());
		props.putByte(player.getAuthentication().playerTorsoColour());
		props.putByte(player.getAuthentication().playerLegColour());
		props.putByte(player.getAuthentication().playerFeetColour());
		props.putByte(player.getAuthentication().playerSkinColour());
		props.putShort(0x328); // TODO: standAnimIndex
		props.putShort(0x337); // TODO: standTurnAnimIndex
		props.putShort(0x333); // TODO: walkAnimIndex
		props.putShort(0x334); // TODO: turn180AnimIndex
		props.putShort(0x335); // TODO: turn90CWAnimIndex
		props.putShort(0x336); // TODO: turn90CCWAnimIndex
		props.putShort(0x338); // TODO: runAnimIndex
		/**
		 * Sends player name as long
		 */
		props.putLong(ProtocolUtils.getLongString(player.getAuthentication()
				.getUsername()));
		props.putByte(3); // send combat level
		props.putShort(0); // games room title crap
		out.putByteC(props.buffer().position());
		out.put(props.buffer());
	}

	/**
	 * Sends the graphic to the client to be displayed
	 * 
	 * @param player
	 * @param out
	 */
	public void appendGraphicMask(Player player, PacketBuilder out) {
		if (player.getGraphic() != null) {
			out.putLEShort(player.getGraphic().getId());
			out.putShort(player.getGraphic().getDelay());
			out.putShort(player.getGraphic().getHeight() * (2 ^ 16));
		}
	}

	/**
	 * Sends the animation to the client to be displayed
	 * 
	 * @param player
	 * @param out
	 */
	private void appendAnimationMask(Player player, PacketBuilder out) {
		if (player.getAnimation() == null) {
			return;
		}
		out.putLEShort(player.getAnimation().getId());
		out.putByteC(player.getAnimation().getDelay());
	}

	public PlayerUpdating localPlayers(List<Player> localPlayers) {
		this.localPlayers = localPlayers;
		return this;
	}

	public PlayerUpdating setMapRegionChanging(boolean status) {
		this.mapRegionChanging = status;
		return this;
	}

	public PlayerUpdating setTeleporting(boolean status) {
		this.teleporting = status;
		return this;
	}

	public PlayerUpdating chatText(byte[] chatText) {
		this.chatText = chatText;
		return this;
	}

	public PlayerUpdating chatTextEffects(int chatTextEffects) {
		this.chatTextEffects = chatTextEffects;
		return this;
	}

	public PlayerUpdating chatTextColor(int chatTextColor) {
		this.chatTextColor = chatTextColor;
		return this;
	}

	public void resetUpdateVars() {
		chatTextEffects = chatTextColor = 0;
		chatText = new byte[256];
		setTeleporting(false);
		setMapRegionChanging(false);
		master.getUpdateFlags().reset();
	}

}
