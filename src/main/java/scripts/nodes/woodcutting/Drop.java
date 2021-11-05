package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;
import scripts.api.*;
import scripts.api.antiban.AntiBan;

/**
 * Purpose of class: Basic drop algorithm, once inventory full drops ALL logs respectively.
 *                      In future will add the following:
 *
 *                      1) banking bird nests after a certain point
 *                      2) banking the inventory if other junk occur
 *                      3) custom drop algorithms COMPLETE - 04/17/2021
 *
 * Updated 11/05/2021 - Changed naming convention for final variables.
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
            case "drop": {
                final RSItem[] allLogs = Workable.getAllLogs();

                if (allLogs.length > 0) {
                    debug(String.format("Dropping %s logs", task.getTree().toLowerCase()));
                    final int dropResult = Inventory.drop(allLogs);
                    if (dropResult > 0) {
                        debug("Dropping complete");
                    }
                }
            }
            break;
            case "fletch-drop": {
                // todo
                final RSItem[] allBows = Workable.getAllBows();
                final RSItem[] allArrowShafts = Workable.getAllArrowShafts();

                if (allBows.length > 0) {
                    debug(String.format("Dropping %s bows", task.getTree().toLowerCase()));
                    final int dropResult = Inventory.drop(allBows);
                    if (dropResult > 0) {
                        debug("Dropping complete");
                    }
                }

                if (allArrowShafts.length > 0) {
                    debug("Dropping arrow shafts");
                    final int dropResult = Inventory.drop(allArrowShafts);
                    if (dropResult > 0) {
                        debug("Dropping complete");
                    }
                }
            }
        }

    }

    @Override
    public boolean validate(Task task) {
        return shouldDropLogs(task) || shouldFletchThenDrop(task);
    }

    @Override
    public void debug(String status) {
        String format = ("[Drop Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    private boolean shouldDropLogs(Task task) {
        return Inventory.isFull()
                && task.shouldDrop();

    }

    private boolean shouldFletchThenDrop(Task task) {
        return Inventory.isFull()
                && task.shouldFletchThenDrop();

    }
}
