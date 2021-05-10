package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.types.RSItem;
import scripts.api.*;
import scripts.dax_api.shared.helpers.BankHelper;

import java.util.function.Predicate;

/**
 * Purpose of class: Retrieve the amount of gold or all gold from the bank, when we run out of gold.
 * The player can either start with any amount of gold in the inventory or start without gold. =)
 */

public class FetchGold extends Node {

    private static final Predicate<RSItem> gold_filter = rsItem -> rsItem.getDefinition().getName().toLowerCase().contains("coins");

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        debug("Retrieving gold");

        if (Bank.openBank()) {
            if (Banking.isBankLoaded()) {
                if (Banking.find(gold_filter).length > 0) {
                    // find the coins and withdraw accordingly
                    RSItem[] coins = Banking.find(gold_filter);

                    if (Globals.useAllGold) {
                        // count - The amount to withdraw. Use '0' for all, or '-1' for all but one.
                        Banking.withdrawItem(coins[0], 0);
                        debug("Withdrew all gold");
                    } else {
                        // withdraw the remaining gold from the bank for the given task
                        // actualPlayerChosenGold - actualPlayerSpentGold = amount of gold to withdraw for given task
                        if (Gold.getGoldSpentTotal() != 0) {
                            int gold_to_withdraw = (Gold.calculateActualGoldRegex(Gold.getGoldRegex()) - Gold.getGoldSpentTotal());
                            Banking.withdrawItem(coins[0], gold_to_withdraw);
                            debug("Withdrew " + gold_to_withdraw + " gold");
                        } else {
                            Banking.withdrawItem(coins[0], Gold.calculateActualGoldRegex(Gold.getGoldRegex()));
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
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    private boolean shouldFetchGold(Task task) {
        if (task.shouldPlankThenBank() && !task.isValidated() && BankHelper.isInBank()) {
            return !Workable.inventoryContainsGold();
        }
        return false;
    }

}
