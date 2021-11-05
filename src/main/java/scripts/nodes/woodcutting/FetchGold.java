package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSItem;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.shared.helpers.BankHelper;

/**
 * Purpose of class: Retrieve the amount of gold or all gold from the bank, when we run out of gold.
 * The player can either start with any amount of gold in the inventory or start without gold.
 *
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
 *
 * Updated 11/05/2021 - Changed naming convention for final variables.
 */

public class FetchGold extends Node {

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        debug("Retrieving gold");

        if (Bank.openBank()) {
            if (Banking.isBankLoaded()) {
                if (Inventory.isFull()) {
                    if (Banking.depositAll() > 0) {
                        debug("Deposited inventory");
                    }
                }
                if (Banking.find(Workable.GOLD).length > 0) {
                    // find the coins and withdraw accordingly
                    final RSItem[] coins = Banking.find(Workable.GOLD);

                    if (Globals.isUseAllGold()) {
                        // count - The amount to withdraw. Use '0' for all, or '-1' for all but one.
                        if (Banking.withdrawItem(coins[0], 0)) {
                            debug("Withdrew all gold");
                        }
                    } else {
                        // withdraw the remaining gold from the bank for the given task
                        // actualPlayerChosenGold - actualPlayerSpentGold = amount of gold to withdraw for given task
                        final int goldToWithdraw;
                        if (Gold.getGoldSpentTotal() != 0) {
                            goldToWithdraw = (Gold.calculateActualGoldRegex(Gold.getGoldRegex()) - Gold.getGoldSpentTotal());
                        } else {
                            goldToWithdraw = Gold.calculateActualGoldRegex(Gold.getGoldRegex());
                        }
                        if (Banking.withdrawItem(coins[0], goldToWithdraw)) {
                            debug("Withdrew " + goldToWithdraw + " gold");
                        }
                    }
                } else {
                    debug("Bank gold depleted!");
                    // no gold inside the bank, set to zero.
                    Gold.setGoldTotalBank(0);
                }
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        return shouldFetchGold(task);
    }

    @Override
    public void debug(String status) {
        String format = ("[Gold Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    private boolean shouldFetchGold(Task task) {
        if (task.shouldPlankThenBank() && !task.isValidated() && BankHelper.isInBank()) {
            return !Workable.inventoryContainsGold();
        }
        return false;
    }

}
