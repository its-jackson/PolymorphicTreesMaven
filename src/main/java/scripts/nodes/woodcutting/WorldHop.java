package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Player;
import org.tribot.api2007.Players;
import org.tribot.api2007.WorldHopper;
import org.tribot.api2007.types.RSPlayer;
import scripts.api.*;

import java.util.function.Predicate;

/**
 * Class purpose: Simply change the players world if their are variable n, players in the woodcutting area. That is
 * currently animating and thus competing.
 * for recognition,
 * n = 5;
 */

public class WorldHop extends Node {
    private boolean isPlayerMember;
    private int world;

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        setPlayerMember(Progressive.isMember());

        if (isPlayerMember()) {
            setWorld(WorldHopper.getRandomWorld(true, false));
        } else {
            setWorld(WorldHopper.getRandomWorld(false, false));
        }

        if (getWorld() != -1) {
            debug("Changing world");
            WorldHopper.changeWorld(getWorld());
        }

    }

    @Override
    public boolean validate(Task task) {
        // n or more RSPlayers which are accepted by the filter.
        return Players.exists(filter(task), Globals.worldHopFactor)
                && task.shouldWorldHop()
                && Workable.isInLocation(task, Player.getRSPlayer())

                ||

                !Workable.objectsExist(task.getActualLocation().getRSArea().getAllTiles(), task.getTree())
                        && !Workable.nearObjects(Globals.treeFactor, task.getTree())
                        && Globals.worldHopNoTreesAvailable
                        && Workable.isInLocation(task, Player.getRSPlayer())
                ;
    }

    @Override
    public void debug(String status) {
        String format = ("[World Control] ");
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    private static Predicate<RSPlayer> filter(Task task) {
        return rsPlayer -> task.getActualLocation().getRSArea().contains(rsPlayer.getPosition()) && rsPlayer.getAnimation() != 1;
    }

    public boolean isPlayerMember() {
        return isPlayerMember;
    }

    public void setPlayerMember(boolean playerMember) {
        isPlayerMember = playerMember;
    }

    public int getWorld() {
        return world;
    }

    public void setWorld(int world) {
        this.world = world;
    }
}
