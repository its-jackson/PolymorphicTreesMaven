package scripts.nodes.woodcutting;

import org.tribot.api2007.*;
import org.tribot.api2007.types.RSTile;
import scripts.api.*;
import org.tribot.api.General;
import org.tribot.api.Timing;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.shared.helpers.BankHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Purpose of class: Bank the inventory, check for axe upgrade simultaneously.
 * Author: Jackson Johnson (Polymorphic~TRiBot)
 * Date: Aug 30th, 2020
 *
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
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
            boolean waitResult = Timing.waitCondition(() -> {
                General.sleep(200, 300);
                return !Inventory.isFull();
            }, General.random(1000, 2000));
            if (waitResult) {
                debug("Deposit successful");
            } else {
                debug("Deposit unsuccessful");
            }
        }

        // perform axe upgrade node while inside the bank
        if (getUpgradeWorkerAxeNode().validate(task)) {
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

    /**
     * This method is for the plank-bank option in the woodcutting guild
     * @return True if the player is at the alternative deposit box in the woodcutting guild; false otherwise.
     */
    public static boolean isInWoodcuttingGuildAlternativeBank() {
        return Player.getPosition().distanceTo(getSawmillWoodcuttingGuildAlternativeBank()) < 7;
    }

    /**
     * Deposit the inventory according to the active task such as fletch-then-bank or plank-then-bank.
     * @param task The active task that is currently running.
     */
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
                boolean depositResult = Banking.depositAllExcept(keepItems) > 0;
                if (depositResult) {
                    debug("Deposited inventory");
                } else {
                    debug("Deposit inventory unsuccessful");
                }
            } else if (task.shouldPlankThenBank()) {
                blackList.add(Workable.GOLD);
                for (int i : Workable.completeAxes()) {
                    blackList.add(i);
                }
                int[] keepItems = new int[blackList.size()];
                for (int i = 0; i < keepItems.length; i++) {
                    keepItems[i] = blackList.get(i);
                }
                boolean depositResult = Banking.depositAllExcept(keepItems) > 0;
                if (depositResult) {
                    debug("Deposited inventory");
                } else {
                    debug("Deposit inventory unsuccessful");
                }
            } else {
                if (Inventory.find(Workable.completeAxes()).length > 0) {
                    boolean depositExceptResult = Banking.depositAllExcept(Workable.completeAxes()) > 0;
                    if (depositExceptResult) {
                        debug("Deposited all except axe");
                    } else {
                        debug("Deposit all except axe unsuccessful");
                    }
                } else {
                    boolean depositAllResult = Banking.depositAll() > 0;
                    if (depositAllResult) {
                        debug("Deposited entire inventory");
                    } else {
                        debug("Deposit entire inventory unsuccessful");
                    }
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

        return Banking.openBank() && Timing.waitCondition(Banking::isBankScreenOpen, General.random(4000, 7000));
    }

    /**
     * The method for validating when the player should bank.
     * @param task The task to determine when to bank according to log disposal option.
     * @return True if the player should bank; false otherwise.
     */
    private boolean shouldBank(Task task) {
        if (Inventory.isFull() && BankHelper.isInBank() &&!task.isValidated()) {
            // validate banking for plank-then-bank
            if (task.shouldPlankThenBank()
                    && isInWoodcuttingGuildAlternativeBank()
                    && Workable.getAllPlanks().length > 0
                    && Plank.calculateOakPlankGold(Workable.getAllLogs()) == -1
                    && Workable.inventoryContainsGold()) {
                return true;
            }
            // normal banking or fletch-then-bank
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
