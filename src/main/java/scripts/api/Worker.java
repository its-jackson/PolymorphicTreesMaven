package scripts.api;

/**
 *
 */
public class Worker implements Workable{
    private int playerAttackLevel;
    private int playerWoodcuttingLevel;
    private int playerFiremakingLevel;
    private int playerAgilityLevel;
    private int playerFletchingLevel;

    private boolean songOfElvesComplete;
    private boolean isMember;

    public Worker() {
        setCompleteWorkerState();
    }

    public void setCompleteWorkerState() {
        setPlayerAttackLevel(Progressive.generateAttackLevel());
        setPlayerWoodcuttingLevel(Progressive.generateWoodcuttingLevel());
        setPlayerFiremakingLevel(Progressive.generateFiremakingLevel());
        setPlayerAgilityLevel(Progressive.generateAgilityLevel());
        setPlayerFletchingLevel(Progressive.generateFletchingLevel());
        setSongOfElvesComplete(Progressive.songOfElvesCompletable());
        setMember(Progressive.isMember());
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

    @Override
    public String toString() {
        return "Worker{" +
                "playerAttackLevel=" + playerAttackLevel +
                ", playerWoodcuttingLevel=" + playerWoodcuttingLevel +
                ", playerFiremakingLevel=" + playerFiremakingLevel +
                ", playerAgilityLevel=" + playerAgilityLevel +
                ", playerFletchingLevel=" + playerFletchingLevel +
                ", songOfElvesComplete=" + songOfElvesComplete +
                ", isMember=" + isMember +
                '}';
    }
}
