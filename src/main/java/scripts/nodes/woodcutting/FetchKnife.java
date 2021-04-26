package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api.Timing;
import org.tribot.api2007.Banking;
import scripts.api.Globals;
import scripts.api.Node;
import scripts.api.Task;
import scripts.api.Workable;
import scripts.dax_api.shared.helpers.BankHelper;

public class FetchKnife extends Node {

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        debug("Retrieving knife");

        if (Bank.openBank()) {
            if (Banking.isBankLoaded()) {
                if (Banking.withdraw(1, Workable.KNIFE)) {
                    boolean result = Timing.waitCondition(Workable::inventoryContainsKnife, General.random(300,400));
                    if (result) {
                        debug("Knife withdrew complete");
                    }
                }
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        return !Workable.inventoryContainsKnife() && task.shouldFletchThenBank() && BankHelper.isInBank()
                ||
                !Workable.inventoryContainsKnife() && task.shouldFletchThenDrop() && BankHelper.isInBank()
                ;
    }

    @Override
    public void debug(String status) {
        Globals.STATE = (status);
        General.println("[Knife Control] " + status);
    }
}

