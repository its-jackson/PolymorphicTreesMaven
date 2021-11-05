package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Login;
import org.tribot.api2007.Player;
import org.tribot.api2007.Players;
import org.tribot.api2007.WorldHopper;
import org.tribot.api2007.types.RSPlayer;
import scripts.api.*;
import scripts.api.antiban.AntiBan;

import java.util.function.Predicate;

/**
 * Class purpose: Simply change the players world if their are variable n, players in the woodcutting area. That is
 * currently animating and thus competing.
 * for recognition n = 5;
 *
 * - Updated 11/05/2021
 * - Added null safe checks to all methods and cached all return values.
 * - Changed naming convention for final variables.
 */

public class WorldHop extends Node {

    private boolean isPlayerMember;
    private int world;

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        setPlayerMember(Progressive.isMember());

        if (isPlayerMember()) {
            setWorld(WorldHopper.getRandomWorld(true, false));
        } else {
            setWorld(WorldHopper.getRandomWorld(false, false));
        }

        if (getWorld() != -1) {
            debug("Changing world");
            final boolean worldChangeResult = WorldHopper.changeWorld(getWorld());
            if (worldChangeResult) {
                debug("Changed world successful");
            }
        } else {
            debug("Couldn't locate a world to change");
        }

    }

    @Override
    public boolean validate(Task task) {
        // n or more RSPlayers which are accepted by the filter.
        return Players.exists(filter(task), Globals.getWorldHopFactor())
                && task.shouldWorldHop()
                && Workable.isInLocation(task, Player.getRSPlayer())
                && Login.getLoginState() == Login.STATE.INGAME

                ||

                !Workable.objectsExist(task.getActualLocation().getRSArea().getAllTiles(), task.getTree())
                        && !Workable.nearObjects(Globals.getTreeFactor(), task.getTree())
                        && Globals.isWorldHopNoTreesAvailable()
                        && Workable.isInLocation(task, Player.getRSPlayer())
                        && Login.getLoginState() == Login.STATE.INGAME
                ;
    }

    @Override
    public void debug(String status) {
        String format = ("[World Control] ");
        Globals.setState(status);
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
