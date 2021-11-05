package scripts.api;

import org.tribot.api.General;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSInterface;

/**
 * Purpose of class: Actively track the player stats to determine which axe to use,
 *                      which tree to chop and the location to work in.
 */

public class Progressive extends Node implements Runnable {

    private int playerWoodcuttingLevel;
    private int playerAttackLevel;
    private int playerFiremakingLevel;
    private int playerAgilityLevel;
    private int playerCombatLevel;

    private String playerWoodcuttingLocation;

    private boolean playerQuestComplete;
    private boolean playerMember;

    // upon construction time, set the global state
    public Progressive() {
        Globals.setProgressive(true);
    }

    @Override
    public void run() {
        General.sleep(1000);

        debug("Please be patient...");

        General.sleep(5000);

        while (Globals.isProgressive()) {
            General.sleep(5000, 10000);
            if (this.validate(null)) {
                this.execute(null);
            }
            General.sleep(General.random(20,80));
        }
    }

    @Override
    public void execute(Task task) {
        // initialize once logged in, no need to check anymore.. except woodcutting level
        if (getPlayerAttackLevel() == 0 && getPlayerAgilityLevel() == 0
                && getPlayerFiremakingLevel() == 0
                && getPlayerCombatLevel() == 0
                && !isPlayerQuestComplete()) {

            setPlayerAttackLevel(generateAttackLevel());

            setPlayerFiremakingLevel(generateFiremakingLevel());

            setPlayerAgilityLevel(generateAgilityLevel());

            setPlayerQuestComplete(songOfElvesCompletable());

            setPlayerCombatLevel(generateCombatLevel());

            setPlayerMember(WorldHopper.isCurrentWorldMembers().isPresent()
                    && WorldHopper.isCurrentWorldMembers().get());

            String format = String.format("Combat level: %s, Attack level: %s, firemaking level: %s, agility level: " +
                            "%s, " +
                    "song of elves: %s, member: %s%n", getPlayerCombatLevel(), getPlayerAttackLevel(),
                    getPlayerFiremakingLevel(),
                    getPlayerAgilityLevel(), isPlayerQuestComplete(), isPlayerMember());

            debug(format);
        }

        //refreshProgressive();
    }

    @Override
    public boolean validate(Task task) {
        if (Login.STATE.INGAME == Login.getLoginState() && Globals.isProgressive()) {
            return true;
        }
        return false;
    }

    @Override
    public void debug(String status) {
        String format = ("[Progressive] ");
        //Globals.STATE = (status);
        General.println(format.concat(status));
    }

    public static int generateWoodcuttingLevel() {
        return Skills.getActualLevel(Skills.SKILLS.WOODCUTTING);
    }

    public static int generateAttackLevel() {
        return Skills.getActualLevel(Skills.SKILLS.ATTACK);
    }

    public static int generateFiremakingLevel() {
        return Skills.getActualLevel(Skills.SKILLS.FIREMAKING);
    }

    public static int generateAgilityLevel() {
        return Skills.getActualLevel(Skills.SKILLS.AGILITY);
    }

    public static int generateCombatLevel() {
        return Player.getRSPlayer().getCombatLevel();
    }

    public static int generateFletchingLevel() {
        return Skills.getActualLevel(Skills.SKILLS.FLETCHING);
    }

    public static boolean isMember() {
        return WorldHopper.isCurrentWorldMembers().isPresent()
                && WorldHopper.isCurrentWorldMembers().get();
    }

    public static boolean songOfElvesCompletable() {
        // song of evolves component interface
        RSInterface songOfElvesCInterface = Interfaces.get(399, 7, 121);

        // 901389 - green = complete
        // 16711680 - red = incomplete
        if (songOfElvesCInterface != null) {
            // get the colour of the quest
            final int quest_colour = songOfElvesCInterface.getTextColour();
            // get the name of the quest
            final String quest_name = songOfElvesCInterface.getText();
            // return true if green (complete)
            return quest_colour == 901389;
        }
        return false;
    }

    // a method that will check if a quest is complete or not
    public static boolean questCompletable(int index, int child, int component) {
        // get quest component interface
        RSInterface questComponentInterface = Interfaces.get(index, child, component);

        // get the colour of the quest
        final int quest_colour = questComponentInterface.getTextColour();

        // get the name of the quest
        final String quest_name = questComponentInterface.getText();

        // 901389 - green = complete
        // 16711680 - red = incomplete
        if (quest_colour == 901389) {
            return true;
        } else {
            return false;
        }
    }

    public int getPlayerWoodcuttingLevel() {
        return playerWoodcuttingLevel;
    }

    public void setPlayerWoodcuttingLevel(int playerWoodcuttingLevel) {
        this.playerWoodcuttingLevel = playerWoodcuttingLevel;
    }

    public int getPlayerAttackLevel() {
        return playerAttackLevel;
    }

    public void setPlayerAttackLevel(int playerAttackLevel) {
        this.playerAttackLevel = playerAttackLevel;
    }

    public int getPlayerFiremakingLevel() {
        return playerFiremakingLevel;
    }

    public void setPlayerFiremakingLevel(int playerFiremakingLevel) {
        this.playerFiremakingLevel = playerFiremakingLevel;
    }

    public int getPlayerAgilityLevel() {
        return playerAgilityLevel;
    }

    public void setPlayerAgilityLevel(int playerAgilityLevel) {
        this.playerAgilityLevel = playerAgilityLevel;
    }

    public boolean isPlayerQuestComplete() {
        return playerQuestComplete;
    }

    public void setPlayerQuestComplete(boolean playerQuestComplete) {
        this.playerQuestComplete = playerQuestComplete;
    }

    public boolean isPlayerMember() {
        return playerMember;
    }

    public void setPlayerMember(boolean playerMember) {
        this.playerMember = playerMember;
    }

    public int getPlayerCombatLevel() {
        return playerCombatLevel;
    }

    public void setPlayerCombatLevel(int playerCombatLevel) {
        this.playerCombatLevel = playerCombatLevel;
    }

    public String getPlayerWoodcuttingLocation() {
        return playerWoodcuttingLocation;
    }

    public void setPlayerWoodcuttingLocation(String playerWoodcuttingLocation) {
        this.playerWoodcuttingLocation = playerWoodcuttingLocation;
    }
}
