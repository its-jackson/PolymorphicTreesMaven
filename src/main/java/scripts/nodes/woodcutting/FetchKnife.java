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

public class FetchKnife extends Node {

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));
        debug("Retrieving knife");

        if (Bank.openBank()) {
            if (Banking.isBankLoaded()) {
                if (Inventory.isFull()) {
                    Banking.depositAll();
                }
                if (Banking.withdraw(1, Workable.KNIFE)) {
                    boolean result = Timing.waitCondition(Workable::inventoryContainsKnife, General.random(300, 400));
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

