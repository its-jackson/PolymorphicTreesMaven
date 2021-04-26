package scripts.nodes.woodcutting.progressive;

import org.tribot.api.General;
import org.tribot.api2007.Banking;
import org.tribot.api2007.Equipment;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.types.RSInterface;
import org.tribot.api2007.types.RSInterfaceChild;
import org.tribot.api2007.types.RSItem;
import scripts.api.*;
import scripts.nodes.woodcutting.Bank;
import scripts.nodes.woodcutting.FetchAxe;

/**
 * Purpose of class: Upgrade the current axe if a better axe exists within the bank.
 */

public class UpgradeAxe extends Node {
    private final long start_time = System.currentTimeMillis();

    private int currentWoodcuttingLevel;
    private int currentAttackLevel;
    private int currentEquippedAxeID;
    private int currentInventoryAxeID;

    private boolean axeUpgradeComplete;

    private boolean shouldOptimizeBank;

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        // create worker
        final Worker worker = new Worker(
                Progressive.generateAttackLevel(),
                Progressive.generateWoodcuttingLevel(),
                Progressive.generateFiremakingLevel(),
                Progressive.generateAgilityLevel(),
                Progressive.songOfElvesCompletable(),
                Progressive.isMember()
        );

        // set the players woodcutting level
        setCurrentWoodcuttingLevel(Progressive.generateWoodcuttingLevel());

        // set the players current attack level
        setCurrentAttackLevel(Progressive.generateAttackLevel());

        // set the players currently equipped axe
        setCurrentEquippedAxeID(generateEquippedAxeID());

        // set the players inventory axe
        setCurrentInventoryAxeID(generateInventoryAxeID());

        String format = String.format("Woodcutting level: %s, attack level: %s, equipped axe id: " +
                "%s, inventory axe " +
                "id: %s%n", getCurrentWoodcuttingLevel(), getCurrentAttackLevel(), getCurrentEquippedAxeID(), getCurrentInventoryAxeID());

        debug(format);

        // Once the bank is open, withdraw the best axe,
        //  deposit inventory except the best axe and equip if possible.
        if (Banking.isBankLoaded()) {
            int best_axe_id;

            // calculate the best axe
            if (worker.isMember()) {
                best_axe_id = FetchAxe.calculateBestAxe(getCurrentWoodcuttingLevel(),
                        worker.getPlayerFiremakingLevel(), worker.isSongOfElvesComplete());
            } else {
                best_axe_id = FetchAxe.calculateBestAxeF2P(worker.getPlayerWoodcuttingLevel());
            }

            // check if axe is equipped; otherwise check if inventory has an axe
            // then proceed to upgrade.
            if (getCurrentEquippedAxeID() > 0 && best_axe_id > 0) {
                if (getCurrentEquippedAxeID() != best_axe_id) {
                    if (!(Workable.getMappedWCLevels().get(getCurrentEquippedAxeID()) > Workable.getMappedWCLevels().get(best_axe_id))) {
                        if (getCurrentInventoryAxeID() > 0) {
                            if (!(Workable.getMappedWCLevels().get(getCurrentInventoryAxeID()) > Workable.getMappedWCLevels().get(best_axe_id))) {
                                if (upgradeCurrentAxe(best_axe_id, getCurrentWoodcuttingLevel(), getCurrentAttackLevel())) {
                                    setAxeUpgradeComplete(true);
                                    debug("Upgrade complete");
                                } else {
                                    debug("Upgrade incomplete");
                                }
                            }
                        } else {
                            if (upgradeCurrentAxe(best_axe_id, getCurrentWoodcuttingLevel(), getCurrentAttackLevel())) {
                                setAxeUpgradeComplete(true);
                                debug("Upgrade complete");
                            } else {
                                debug("Upgrade incomplete");
                            }
                        }
                    }
                }
            } else {
                if (!isAxeUpgradeComplete() && getCurrentInventoryAxeID() > 0 && best_axe_id > 0) {
                    if (getCurrentInventoryAxeID() != best_axe_id) {
                        if (!(Workable.getMappedWCLevels().get(getCurrentInventoryAxeID()) > Workable.getMappedWCLevels().get(best_axe_id))) {
                            if (upgradeCurrentAxe(best_axe_id, getCurrentWoodcuttingLevel(), getCurrentAttackLevel())) {
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
        if (task.shouldUpgradeAxe())
            return true;

        return false;
    }

    @Override
    public void debug(String status) {
        String format = ("[Upgrade Control] ");
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    private boolean upgradeCurrentAxe(int bestAxeId, int currentWoodcuttingLevel, int currentAttackLevel) {
        if (!(currentWoodcuttingLevel >= Workable.getMappedWCLevels().get(bestAxeId))) {
            debug("Can't upgrade axe");
            return false;
        }

        debug("Upgrading axe");

        if (!this.shouldOptimizeBank) {
           this.shouldOptimizeBank = optimizeBank();
        }

        FetchAxe.withdrawAxe(bestAxeId);

        General.sleep(1000,1200);

        FetchAxe.equipAxe(bestAxeId, currentAttackLevel);

        General.sleep(1000, 1200);

        Banking.depositAllExcept(bestAxeId);

        General.sleep(1000,1200);

        return true;
    }

    private boolean optimizeBank() {
        final RSInterfaceChild show_menu = Interfaces.get(12, 111);

        if (show_menu == null) {
            return false;
        }

        if (show_menu.getActions() != null) {
            show_menu.click("Show");
            General.sleep(1000, 1200);
        }

        final RSInterface item_option = Interfaces.get(12, 50, 1);

        if (item_option != null) {
            item_option.click("Show");
            General.sleep(200, 400);
            show_menu.click();
            return true;
        }
        return false;
    }

    // a method that returns the players inventory axe
    private int generateInventoryAxeID() {
        final RSItem[] axes = Inventory.find(Workable.AXES);

        if (axes.length > 0) {
            return axes[0].getDefinition().getID();
        }

        // return 0 if no axe is found within the inventory
        return 0;
    }

    // a method that returns the player currently equipped axe
    private int generateEquippedAxeID() {
        if (Equipment.SLOTS.WEAPON.getItem() != null) {
            for (final int axe_id : Workable.AXES) {
                if (Equipment.SLOTS.WEAPON.getItem().getDefinition().getID() == axe_id) {
                    return axe_id;
                }
            }
        }

        // return 0 if no axe was found to be currently equipped
        return 0;
    }

    private int getCurrentAttackLevel() {
        return currentAttackLevel;
    }

    private void setCurrentAttackLevel(int currentAttackLevel) {
        this.currentAttackLevel = currentAttackLevel;
    }

    public int getCurrentEquippedAxeID() {
        return currentEquippedAxeID;
    }

    public void setCurrentEquippedAxeID(int currentEquippedAxeID) {
        this.currentEquippedAxeID = currentEquippedAxeID;
    }

    public int getCurrentWoodcuttingLevel() {
        return currentWoodcuttingLevel;
    }

    public void setCurrentWoodcuttingLevel(int currentWoodcuttingLevel) {
        this.currentWoodcuttingLevel = currentWoodcuttingLevel;
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

    public long getStartTime() {
        return start_time;
    }
}
