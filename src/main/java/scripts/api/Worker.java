package scripts.api;

import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSTile;
import scripts.nodes.woodcutting.FetchAxe;

public class Worker {
    private int playerAttackLevel;
    private int playerWoodcuttingLevel;
    private int playerFiremakingLevel;
    private int playerAgilityLevel;
    private int playerFletchingLevel;

    private boolean songOfElvesComplete;
    private boolean isMember;

    public Worker() {
    }

    public Worker(int playerFletchingLevel) {
        this.playerFletchingLevel = playerFletchingLevel;
    }

    public Worker(
            int playerWoodcuttingLevel,
            int playerAttackLevel
    )
    {
        this.playerWoodcuttingLevel = playerWoodcuttingLevel;
        this.playerAttackLevel = playerAttackLevel;
    }

    public Worker(
            int playerAttackLevel,
            int playerWoodcuttingLevel,
            int playerFiremakingLevel,
            int playerAgilityLevel,
            boolean songOfElvesComplete,
            boolean isMember
    )
    {
        this.playerAttackLevel = playerAttackLevel;
        this.playerWoodcuttingLevel = playerWoodcuttingLevel;
        this.playerFiremakingLevel = playerFiremakingLevel;
        this.playerAgilityLevel = playerAgilityLevel;
        this.songOfElvesComplete = songOfElvesComplete;
        this.isMember = isMember;
    }

    public int getWorkerOptimalAxe() {
        return FetchAxe.calculateBestAxe(getPlayerWoodcuttingLevel(), getPlayerFiremakingLevel(),
                isSongOfElvesComplete());
    }
    private RSTile getWorkerPosition() {
        return Player.getPosition();
    }

    public int getPlayerAttackLevel() {
        return playerAttackLevel;
    }

    public void setPlayerAttackLevel(int playerAttackLevel) {
        this.playerAttackLevel = playerAttackLevel;
    }

    public int getPlayerWoodcuttingLevel() {
        return playerWoodcuttingLevel;
    }

    public void setPlayerWoodcuttingLevel(int playerWoodcuttingLevel) {
        this.playerWoodcuttingLevel = playerWoodcuttingLevel;
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

    public boolean isSongOfElvesComplete() {
        return songOfElvesComplete;
    }

    public void setSongOfElvesComplete(boolean songOfElvesComplete) {
        this.songOfElvesComplete = songOfElvesComplete;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setMember(boolean member) {
        isMember = member;
    }

    public int getPlayerFletchingLevel() {
        return playerFletchingLevel;
    }

    public void setPlayerFletchingLevel(int playerFletchingLevel) {
        this.playerFletchingLevel = playerFletchingLevel;
    }
}
