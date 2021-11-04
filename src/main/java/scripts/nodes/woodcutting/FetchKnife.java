package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import scripts.api.Globals;
import scripts.api.Node;
import scripts.api.Task;
import scripts.api.Workable;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.shared.helpers.BankHelper;

/**
 * Purpose of class: Fetch a knife from the player's bank
 *
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
 */

public class FetchKnife extends Node {

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        debug("Retrieving knife");

        if (Bank.openBank()) {
            if (Banking.isBankLoaded()) {
                if (Inventory.isFull()) {
                    if (Banking.depositAll() > 0) {
                        debug("Deposited inventory");
                    }
                }
                if (Banking.withdraw(1, Workable.KNIFE)) {
                    boolean result = Timing.waitCondition(Workable::inventoryContainsKnife, General.random(1000, 3000));
                    if (result) {
                        debug("Knife withdrew complete");
                    }
                }
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        if (!Workable.inventoryContainsKnife() && BankHelper.isInBank()) {
            return task.shouldFletchThenBank() || task.shouldFletchThenDrop();
        }
        return false;
    }

    @Override
    public void debug(String status) {
        Globals.setState(status);
        General.println("[Knife Control] " + status);
    }
}

