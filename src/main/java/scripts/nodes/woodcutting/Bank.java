package scripts.nodes.woodcutting;

import org.tribot.api2007.*;
import org.tribot.api2007.types.RSTile;
import scripts.api.*;
import org.tribot.api.General;
import org.tribot.api.Timing;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.shared.helpers.BankHelper;
import scripts.nodes.woodcutting.progressive.UpgradeAxe;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose of class: Bank the inventory, check for axe upgrade simultaneously.
 * Author: Jackson Johnson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 */

public class Bank extends Node {
    private final UpgradeAxe upgrade_worker_axe_node = new UpgradeAxe();

    private static final RSTile sawmill_woodcutting_guild_alternative_bank = new RSTile(1650, 3498, 0);

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));
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

        if (task.shouldUpgradeAxe() && getUpgradeWorkerAxeNode().validate(task)) {
            getUpgradeWorkerAxeNode().execute(task);
        }
    }

    @Override
    public boolean validate(Task task) {
        return shouldBank(task);
    }

    @Override
    public void debug(String status) {
        String format = ("[Bank Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    public static boolean isInWoodcuttingGuildAlternativeBank() {
        return Player.getPosition().distanceTo(getSawmillWoodcuttingGuildAlternativeBank()) < 7;
    }

    public void depositInventory(Task task) {
        if (Banking.isBankScreenOpen() || Banking.isDepositBoxOpen()) {

            debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

            List<Integer> blackList = new ArrayList<>();

            if (task.shouldFletchThenBank()) {
                blackList.add(Workable.KNIFE);
                for (int i : Workable.completeAxes()) {
                    blackList.add(i);
                }
                int[] keepItems = new int[blackList.size()];
                for (int i = 0; i < keepItems.length; i++) {
                    keepItems[i] = blackList.get(i);
                }
                Banking.depositAllExcept(keepItems);
            } else if (task.shouldPlankThenBank()) {
                blackList.add(Workable.GOLD);
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

        return Timing.waitCondition(Banking::isBankScreenOpen, General.random(4000, 5000));
    }

    private boolean shouldBank(Task task) {
        if (Inventory.isFull() && BankHelper.isInBank() &&!task.isValidated()) {
            if (task.shouldPlankThenBank()
                    && isInWoodcuttingGuildAlternativeBank()
                    && Workable.getAllPlanks().length > 0
                    && Plank.calculateOakPlankGold(Workable.getAllLogs()) == -1
                    && Workable.inventoryContainsGold()) {
                return true;
            }
            return task.shouldBank() || task.shouldFletchThenBank();
        }

        return false;
    }

    public static RSTile getSawmillWoodcuttingGuildAlternativeBank() {
        return sawmill_woodcutting_guild_alternative_bank;
    }

    public UpgradeAxe getUpgradeWorkerAxeNode() {
        return upgrade_worker_axe_node;
    }
}
