package scripts.nodes.woodcutting;

import org.tribot.api2007.*;
import scripts.api.*;
import org.tribot.api.General;
import org.tribot.api.Timing;
import scripts.dax_api.shared.helpers.BankHelper;
import scripts.nodes.woodcutting.progressive.UpgradeAxe;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose of class: Bank the inventory, check for axe upgrade while in progressive
 * Author: Jackson Johnson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 */

public class Bank extends Node {
    private final UpgradeAxe upgrade_worker_axe_node = new UpgradeAxe();

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        debug("Opening bank");

        if (BankHelper.openBankAndWait()) {
            debug("Bank is open");
            depositInventory(task);
            debug("Deposited");
            Timing.waitCondition(() -> {
                General.sleep(200, 300);
                return !Inventory.isFull();
            }, General.random(1000, 2000));
        }

        if (task.shouldUpgradeAxe() && upgrade_worker_axe_node.validate(task)) {
            upgrade_worker_axe_node.execute(task);
        }
    }

    @Override
    public boolean validate(Task task) {
        return Inventory.isFull() && task.shouldBank() && BankHelper.isInBank()
                ||
                Inventory.isFull() && task.shouldFletchThenBank() && BankHelper.isInBank()
                ;
    }

    @Override
    public void debug(String status) {
        String format = ("[Bank Control] ");
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    private void depositInventory(Task task) {
        if (Banking.isBankScreenOpen() || Banking.isDepositBoxOpen()) {
            Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

            if (task.shouldFletchThenBank()) {
                List<Integer> blackList = new ArrayList<>();
                blackList.add(Workable.KNIFE);
                for (int i : Workable.completeAxes()) {
                    blackList.add(i);
                }
                int[] keepItems = new int[blackList.size()];
                for (int i = 0; i < keepItems.length; i++) {
                    keepItems[i] = blackList.get(i);
                }
                Banking.depositAllExcept(keepItems);
            } else {
                if (Inventory.find(Workable.completeAxes()).length > 0) {
                    Banking.depositAllExcept(Workable.completeAxes());
                } else {
                    Banking.depositAll();
                }
            }
        }
    }

    /**
     * Opens the bank if not already open
     * @return True if the bank is open; false otherwise
     */
    public static boolean openBank() {
        if (Banking.isBankScreenOpen()) {
            return true;
        }

        Banking.openBank();

        return Timing.waitCondition(() -> {
            General.sleep(200, 300);
            return Banking.isBankScreenOpen();
        }, General.random(4000, 5000));
    }

}
