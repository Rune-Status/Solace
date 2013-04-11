package org.solace.game.content.dialogue;

/**
 * Loads the definitions for the dialogue from the XML file
 * 
 * @author Arithium
 * 
 */
public class DialogueDefinition {

	private int dialogueId;

	private int nextDialogue;

	private int emoticon;

	private boolean playerChat;

	private String[] dialogues;

	public int dialogueId() {
		return dialogueId;
	}

	public int getNextDialogue() {
		return nextDialogue;
	}

	public int getEmoticon() {
		return emoticon;
	}

	public String[] getDialogues() {
		return dialogues;
	}

	public boolean isPlayerChat() {
		return playerChat;
	}

}
