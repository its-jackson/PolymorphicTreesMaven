package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;
import scripts.api.*;
import scripts.api.antiban.AntiBan;

/**
 * Purpose of class: Upgrade the current axe if a better axe exists within the bank.
 *
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
 */

public class UpgradeAxe extends Node {

    private int currentEquippedAxeID;
    private int currentInventoryAxeID;

    private boolean axeUpgradeComplete;
    private boolean shouldOptimizeBank;

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        // set the players currently equipped axe
        setCurrentEquippedAxeID(generateEquippedAxeID());

        // set the players inventory axe
        setCurrentInventoryAxeID(generateInventoryAxeID());

        String format = String.format(
                "Woodcutting level: %s, attack level: %s, equipped axe id: " +
                        "%s, inventory axe " +
                        "id: %s%n",
                getWorker().getPlayerWoodcuttingLevel(),
                getWorker().getPlayerAttackLevel(),
                getCurrentEquippedAxeID(),
                getCurrentInventoryAxeID()
        );

        debug(format);

        // Once the bank is open, withdraw the best axe,
        // deposit inventory except the best axe and equip if possible.
        if (Banking.isBankLoaded()) {
            int best_axe_id;

            // calculate the best axe pertaining to account status (member / f2p)
            if (getWorker().isMember()) {
                best_axe_id =
                        FetchAxe.calculateBestAxe(getWorker().getPlayerWoodcuttingLevel(),
                        getWorker().getPlayerFiremakingLevel(), getWorker().isSongOfElvesComplete());
            } else {
                best_axe_id = FetchAxe.calculateBestAxeF2P(getWorker().getPlayerWoodcuttingLevel());
            }

            // check if axe is equipped; otherwise check if inventory has an axe
            // then proceed to upgrade.
            if (getCurrentEquippedAxeID() > 0 && best_axe_id > 0) {
                if (getCurrentEquippedAxeID() != best_axe_id) {
                    // current equipped axe is not better than best axe in bank
                    if (!(Workable.getMappedWCLevels().get(getCurrentEquippedAxeID()) > Workable.getMappedWCLevels().get(best_axe_id))) {
                        // player has an inventory axe
                        if (getCurrentInventoryAxeID() > 0) {
                            // inventory axe is not better than the best axe in bank
                            if (!(Workable.getMappedWCLevels().get(getCurrentInventoryAxeID()) > Workable.getMappedWCLevels().get(best_axe_id))) {
                                // perform the upgrade
                                if (upgradeCurrentAxe(best_axe_id, getWorker().getPlayerWoodcuttingLevel(), getWorker().getPlayerAttackLevel())) {
                                    setAxeUpgradeComplete(true);
                                    debug("Upgrade complete");
                                } else {
                                    debug("Upgrade incomplete");
                                }
                            }
                        } else {
                            // no inventory axe, perform the upgrade
                            if (upgradeCurrentAxe(best_axe_id, getWorker().getPlayerWoodcuttingLevel(), getWorker().getPlayerAttackLevel())) {
                                setAxeUpgradeComplete(true);
                                debug("Upgrade complete");
                            } else {
                                debug("Upgrade incomplete");
                            }
                        }
                    }
                }
            } else {
                // no equipped axe and no best axe in bank
                // check if axe upgrade complete, inventory axe, best axe in bank
                if (!isAxeUpgradeComplete() && getCurrentInventoryAxeID() > 0 && best_axe_id > 0) {
                    if (getCurrentInventoryAxeID() != best_axe_id) {
                        // inventory axe is not better than best axe in bank
                        if (!(Workable.getMappedWCLevels().get(getCurrentInventoryAxeID()) > Workable.getMappedWCLevels().get(best_axe_id))) {
                            // perform upgrade
                            if (upgradeCurrentAxe(best_axe_id, getWorker().getPlayerWoodcuttingLevel(), getWorker().getPlayerAttackLevel())) {
                                debug("Upgrade completed");
                            } else {
                                debug("Upgrade incomplete");
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean validate(Task task) {
        // return true if we are in progressive mode, and we should upgrade the current,
        // while inside the bank
        return task.shouldUpgradeAxe();
    }

    @Override
    public void debug(String status) {
        String format = ("[Upgrade Control] ");
        Globals.setState(status);
        General.println(format.concat(status));
    }

    /**
     * Upgrade the axe found inside the players bank.
     * @param bestAxeId The axe inside the bank.
     * @param currentWoodcuttingLevel The player's woodcutting level.
     * @param currentAttackLevel The player's attack level.
     * @return True if the upgrade was successful; otherwise false.
     */
    private boolean upgradeCurrentAxe(int bestAxeId, int currentWoodcuttingLevel, int currentAttackLevel) {
        // exit method if the current woodcutting level is not sufficient for the best axe available
        if (!(currentWoodcuttingLevel >= Workable.getMappedWCLevels().get(bestAxeId))) {
            debug("Can't upgrade axe");
            return false;
        }

        debug("Upgrading axe");

        if (!isShouldOptimizeBank()) {
            setShouldOptimizeBank(optimizeBank());
        }

        // attempt to withdraw the best axe inside the bank
        boolean withdrawResult = FetchAxe.withdrawAxe(bestAxeId);

        if (withdrawResult) {
            General.sleep(1000, 1200);
            if (FetchAxe.equipAxe(bestAxeId, currentAttackLevel)) {
                General.sleep(1000, 1200);
                boolean depositSuccessful = Banking.depositAllExcept(bestAxeId) > 0;
                if (depositSuccessful) {
                    debug("Deposited inventory");
                }
            }
        }

        // true if the axe was withdrawn
        return withdrawResult;
    }

    /**
     * Optimize the player's bank for withdrawing correctly.
     * @return True if the player's was optimized; false otherwise.
     */
    private boolean optimizeBank() {
        // hard coded
        final RSInterfaceChild show_menu = Interfaces.get(12, 111);
        // hard coded
        final RSInterface item_option = Interfaces.get(12, 50, 1);

        if (show_menu == null || item_option == null) {
            return false;
        }

        if (show_menu.getActions() != null) {
            boolean showMenuClickResult = show_menu.click("Show");
            if (showMenuClickResult) {
                debug("Clicked show menu successful");
                General.sleep(1000, 1200);
            }

        }

        boolean itemOptionClickResult = item_option.click("Show");
        if (itemOptionClickResult) {
            General.sleep(200, 400);
            return show_menu.click();
        }

        return false;
    }

    /**
     * This method will return the first axe id found within the player's inventory.
     * @return The first axe id within the player's inventory; otherwise 0 if no axe was found.
     */
    private int generateInventoryAxeID() {
        final RSItem[] axes = Inventory.find(Workable.AXES);

        if (axes.length > 0) {
            RSItemDefinition definition = axes[0].getDefinition();
            if (definition != null) {
                return definition.getID();
            }
        }

        // return 0 if no axe is found within the inventory
        return 0;
    }

    /**
     * This method will return the equipped axe found on the player.
     * @return The axe id found on the player; otherwise 0 if no axe was found.
     */
    private int generateEquippedAxeID() {
        RSItem axe = Equipment.SLOTS.WEAPON.getItem();

        if (axe != null) {
            RSItemDefinition definition = axe.getDefinition();
            if (definition != null) {
                int equippedAxeID = definition.getID();
                for (final int axe_id : Workable.AXES) {
                    if (equippedAxeID == axe_id) {
                        return axe_id;
                    }
                }
            }
        }

        // return 0 if no axe was found to be currently equipped
        return 0;
    }

    public int getCurrentEquippedAxeID() {
        return currentEquippedAxeID;
    }

    public void setCurrentEquippedAxeID(int currentEquippedAxeID) {
        this.currentEquippedAxeID = currentEquippedAxeID;
    }

    public int getCurrentInventoryAxeID() {
        return currentInventoryAxeID;
    }

    public void setCurrentInventoryAxeID(int currentInventoryAxeID) {
        this.currentInventoryAxeID = currentInventoryAxeID;
    }

    public boolean isAxeUpgradeComplete() {
        return axeUpgradeComplete;
    }

    public void setAxeUpgradeComplete(boolean axeUpgradeComplete) {
        this.axeUpgradeComplete = axeUpgradeComplete;
    }

    public boolean isShouldOptimizeBank() {
        return shouldOptimizeBank;
    }

    public void setShouldOptimizeBank(boolean shouldOptimizeBank) {
        this.shouldOptimizeBank = shouldOptimizeBank;
    }
}
