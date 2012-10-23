package org.solace.event.impl;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import org.solace.event.Event;
import org.solace.game.entity.mobile.player.Player;
import org.solace.game.item.Item;
import org.solace.util.Constants;

/**
 *
 * @author Faris
 */
public class PlayerLoadEvent extends Event {
    
    public PlayerLoadEvent(Player player){
        super(EventType.INDEPENDANT, 0 , false);
        this.player = player;
    }
    
    

    Player player;


    @Override
    public void execute() {
        
    }
    
    public boolean load(){
        return loadgame(player);
    }
    
    /**
     * Loads the players details from the binary file
     * @param player The player to be loaded
     * @return
     */
    public boolean loadgame(Player player) {
            String folder = Constants.PLAYER_SAVE_DIRECTORY+ player.getAuthentication().getUsername().toLowerCase().charAt(0) + "/";
            File file = new File(folder + player.getAuthentication().getUsername()+ ".dat");
            if (file.exists()) {
            try {
                FileInputStream inFile = new FileInputStream(file);
                DataInputStream load = new DataInputStream(inFile);
                String username = load.readUTF();
                if (!username.equalsIgnoreCase(player.getAuthentication().getUsername())) {
                        load.close();
                        return false;
                }
                player.getAuthentication().setUsername(username);
                String password = load.readUTF();
                if (!password.equalsIgnoreCase(player.getAuthentication().getPassword())) {
                        load.close();
                        return false;
                }
                player.getAuthentication().setPassword(password);
                player.getLocation().setX(load.readInt());
                player.getLocation().setY(load.readInt());
                player.getLocation().setH(load.readInt());
                player.getMobilityManager().running(load.readBoolean());
                player.getAuthentication().setPlayerRights(load.readInt());
                for (int i = 0; i < player.getAuthentication().appearanceIndex.length; i++) {
                        player.getAuthentication().setPlayerAppearanceIndex(i,load.readInt());
                }
                for (int i = 0; i < player.getSkills().getPlayerLevel().length; i++) {
                        player.getSkills().getPlayerLevel()[i] = load.readInt();
                }
                for (int i = 0; i < player.getSkills().getPlayerExp().length; i++) {
                        player.getSkills().getPlayerExp()[i] = load.readInt();
                }
                for (int i = 0; i < 28; i++) {
                        int id = load.readInt();
                        if (id != 65535) {
                                int amount = load.readInt();
                                Item item = new Item(id, amount);
                                player.getInventory().set(i, item);
                        }
                }
                for (int i = 0; i < 14; i++) {
                        int id = load.readInt();
                        if (id != 65535) {
                                int amount = load.readInt();
                                Item item = new Item(id, amount);
                                player.getEquipment().set(i, item);
                        }
                }
                    load.close();
            } catch (Exception e) {
            }
        }
            return true;
    }
}
