package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSPlayer;
import scripts.api.*;
import scripts.api.antiban.AntiBan;

/**
 * Purpose of class: Basic drop algorithm, once inventory full drops ALL logs respectively.
 *                      In future will add the following:
 *
 *                      1) banking bird nests after a certain point
 *                      2) banking the inventory if other junk occur
 *                      3) custom drop algorithms COMPLETE - 04/17/2021
 */

public class Drop extends Node {

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));
        Inventory.setDroppingMethod(AntiBan.generateDroppingPreference());

        Inventory.setDroppingPattern(AntiBan.generateDroppingPattern());

        debug("Dropping preference " + Inventory.getDroppingMethod().toString().toLowerCase());
        debug("Dropping pattern " + Inventory.getDroppingPattern().toString().toLowerCase());

        switch (task.getLogOption().toLowerCase()) {
            case "drop" -> {
                final RSItem[] all_logs = Workable.getAllLogs();

                if (all_logs.length > 0) {
                    debug(String.format("Dropping %s logs", task.getTree().toLowerCase()));
                    final int drop_result = Inventory.drop(all_logs);
                    if (drop_result > 0) {
                        debug("Dropping complete");
                    }
                }
            }
            case "fletch-drop" -> {
                // todo
                final RSItem[] all_bows = Workable.getAllBows();
                final RSItem[] all_arrow_shafts = Workable.getAllArrowShafts();

                if (all_bows.length > 0) {
                    debug(String.format("Dropping %s bows", task.getTree().toLowerCase()));
                    final int drop_result = Inventory.drop(all_bows);
                    if (drop_result > 0) {
                        debug("Dropping complete");
                    }
                }

                if (all_arrow_shafts.length > 0) {
                    debug("Dropping arrow shafts");
                    final int drop_result = Inventory.drop(all_arrow_shafts);
                    if (drop_result > 0) {
                        debug("Dropping complete");
                    }
                }
            }
        }

    }

    @Override
    public boolean validate(Task task) {
        return shouldDropLogs(task, Player.getRSPlayer()) || shouldFletchThenDrop(task, Player.getRSPlayer());
    }

    @Override
    public void debug(String status) {
        String format = ("[Drop Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    private boolean shouldDropLogs(Task task, RSPlayer player) {
        return Inventory.isFull()
                && task.shouldDrop()
                && Workable.isInLocation(task, player);
    }

    private boolean shouldFletchThenDrop(Task task, RSPlayer player) {
        return Inventory.isFull()
                && task.shouldFletchThenDrop()
                && Workable.isInLocation(task, player);
    }

}
