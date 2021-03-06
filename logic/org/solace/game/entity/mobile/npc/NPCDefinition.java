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
package org.solace.game.entity.mobile.npc;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import org.solace.Server;
import org.solace.util.XStreamUtil;

/**
 * 
 * @author Faris
 */
public class NPCDefinition {

	private static NPCDefinition[] definitions = null;

	@SuppressWarnings("unchecked")
	public static void init() throws IOException {
            Server.logger.info("Loading npc definitions...");
		List<NPCDefinition> defs = (List<NPCDefinition>) XStreamUtil.getXStream().fromXML(new FileInputStream("./data/xml/npcs/npcDefinition.xml"));
		definitions = new NPCDefinition[6230];
		for (NPCDefinition def : defs) {
			definitions[def.getId()] = def;
		}
	}

	public static NPCDefinition forId(int id) {

		NPCDefinition d = definitions[id];

		if (d == null) {
			d = produceDefinition(id);
		}
		return d;
	}

	private int id;
	private String name, examine;
	private int respawn = 0, combat = 0, hitpoints = 1, maxHit = 0, size = 1,
			attackSpeed = 4000, attackAnim = 422, defenceAnim = 404,
			deathAnim = 2304, attackBonus = 20, defenceMelee = 20,
			defenceRange = 20, defenceMage = 20;

	private boolean attackable = false;
	private boolean aggressive = false;
	private boolean retreats = false;
	private boolean poisonous = false;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getExamine() {
		return examine;
	}

	public int getRespawn() {
		return respawn;
	}

	public int getCombat() {
		return combat;
	}

	public int getHitpoints() {
		return hitpoints;
	}

	public int getMaxHit() {
		return maxHit;
	}

	public int getSize() {
		return size;
	}

	public boolean isAggressive() {
		return aggressive;
	}

	public boolean retreats() {
		return retreats;
	}

	public boolean isPoisonous() {
		return poisonous;
	}

	public static NPCDefinition produceDefinition(int id) {
		NPCDefinition def = new NPCDefinition();
		def.id = id;
		def.name = "NPC #" + def.id;
		def.examine = "It's an NPC.";
		return def;
	}

	public int getAttackSpeed() {
		return attackSpeed;
	}

	public int getAttackAnimation() {
		return attackAnim;
	}

	public int getDefenceAnimation() {
		return defenceAnim;
	}

	public int getDeathAnimation() {
		return deathAnim;
	}

	public boolean isAttackable() {
		return attackable;
	}

	public int getAttackBonus() {
		return attackBonus;
	}

	public int getDefenceRange() {
		return defenceRange;
	}

	public int getDefenceMelee() {
		return defenceMelee;
	}

	public int getDefenceMage() {
		return defenceMage;
	}
	
	public static NPCDefinition[] getDefinitions() {
		return definitions;
	}
}
