package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSItem;
import scripts.api.*;
import scripts.dax_api.shared.helpers.BankHelper;

import java.util.*;

/**
 * Gedankenexperiment
 *
 * Purpose of class: If the player doesn't have an axe on their person,
 * then open bank and withdraw the appropriate axe.
 * Attempts equipping the axe in comparison to appropriate stats/levels.
 */

public class FetchAxe extends Node {
    private Worker worker;

    @Override
    public void execute(Task task) {
        Workable.sleep(Globals.waitTimes, Globals.humanFatigue);

        worker = new Worker(
                Progressive.generateAttackLevel(),
                Progressive.generateWoodcuttingLevel(),
                Progressive.generateFiremakingLevel(),
                Progressive.generateAgilityLevel(),
                Progressive.songOfElvesCompletable(),
                Progressive.isMember()
        );

        // format debug
        String format = String.format(
                "Woodcutting level: %s, attack level: %s, firemaking level: %s, " +
                        "agility level: " +
                        "%s, song of elves: %s%n",
                worker.getPlayerWoodcuttingLevel(),
                worker.getPlayerAttackLevel(),
                worker.getPlayerFiremakingLevel(),
                worker.getPlayerAgilityLevel(),
                worker.isSongOfElvesComplete()
        );

        debug(format);

        // open the bank and equip/withdraw the best axe according to the woodcutting/attack/firemaking levels
        if (Bank.openBank()) {
            if (Banking.isBankLoaded()) {
                // withdraw the best axe for member players
                if (Progressive.isMember()) {
                    // calculate the best axe found in the bank pertaining
                    // to player woodcutting/firemaking/quests respectively
                    final int best_axe_id = calculateBestAxe(worker.getPlayerWoodcuttingLevel(), worker.getPlayerFiremakingLevel(),
                            worker.isSongOfElvesComplete());

                    // confirm the axe has been withdrew
                    if ((best_axe_id > 0) && withdrawAxe(best_axe_id)) {
                        debug("Optimal axe found");
                        General.sleep(1000, 1200);
                        if (Inventory.find(best_axe_id).length > 0) {
                            RSItem[] axeWithdrew = Inventory.find(best_axe_id);
                            if (axeWithdrew[0] != null) {
                                debug("Withdrew " + axeWithdrew[0].getDefinition().getName().toLowerCase(Locale.ROOT));
                            }
                        }
                    }

                    if (Inventory.getAll().length > 0) {
                        Banking.depositAllExcept(best_axe_id);
                    }

                    Banking.close();

                    // now equip the axe if appropriate
                    if (equipAxe(best_axe_id, worker.getPlayerAttackLevel(), worker.getPlayerFiremakingLevel(),
                            worker.getPlayerAgilityLevel())) {
                        debug("Equipped axe");
                    }

                } else {
                    // determine the best axe to withdraw from the players bank pertaining to woodcutting level
                    // for F2P players
                    final int best_axe_id = calculateBestAxeF2P(worker.getPlayerWoodcuttingLevel());
                    RSItem[] optimalAxes;

                    // confirm the axe has been withdrawn
                    if ((best_axe_id > 0) && withdrawAxe(best_axe_id)) {
                        debug("Optimal axe found");
                        optimalAxes = Inventory.find(best_axe_id);
                        General.sleep(1000, 1200);
                        if (optimalAxes.length > 0) {
                            String withdrew = optimalAxes[0].getDefinition().getName().toLowerCase();
                            debug("Withdrew " + withdrew);
                        }
                    }

                    if (Inventory.getAll().length > 0) {
                        Banking.depositAllExcept(best_axe_id);
                    }

                    Banking.close();

                    // equip the axe if appropriate
                    if (equipAxe(best_axe_id, worker.getPlayerAttackLevel())) {
                        debug("Equipped axe");
                    }
                }
            } else {
                Interfaces.closeAll();
            }
        }
        else {
            Interfaces.closeAll();
        }
    }

    @Override
    public boolean validate(Task task) {
        // return true; we are in the bank, no axe is equipped and no axe is in the inventory
        return shouldFetchAxe(task);
    }

    @Override
    public void debug(String status) {
        String format = ("[Axe Control] ");
        Globals.STATE = (status);
        General.println(format.concat(status));
    }

    // a method that returns all axes in the players bank in form of RSItem[]
    // return an array of length zero if no axe was found
    private static RSItem[] findAxesInBank() {
        return Banking.find(Workable.completeAxes());
    }

    // a method that will withdraw the one axe
    public static boolean withdrawAxe(int axe) {
        return Banking.withdraw(1, axe);
    }

    /**
     * A method that equips an axe pertaining to the players attack level
     *
     * @param axe               The axe to be equipped
     * @param playerAttackLevel The attack level in relation to the player
     * @return True if the axe was equipped; false otherwise.
     */
    public static boolean equipAxe(int axe, int playerAttackLevel) {
        // utilize mapped axe levels for appropriate axe to be equipped
        HashMap<Integer, Integer> mappedATTLevels;
        mappedATTLevels = Workable.getMappedATTLevels();
        boolean result = false;

        // validate and filter
        if (!mappedATTLevels.isEmpty() && axe > 0 && playerAttackLevel > 0) {
            // find the axe in the mapped hashmap
            for (Integer axeID : mappedATTLevels.keySet()) {
                // find axe in hashmap first before checking against players level
                if (axe == axeID) {
                    // find the given axe in the inventory and equip
                    RSItem[] axeToEquip = Inventory.find(axeID);
                    // player attack level greater than or equal to the mapped axe level
                    if (playerAttackLevel >= mappedATTLevels.get(axeID) && axeToEquip.length > 0 && axeToEquip[0] != null) {
                        // more validation if special axe is passed as argument
                        //List<String> blackList = new ArrayList<>();
                        //blackList.add("Wield");
                        return axeToEquip[0].click("Wield");
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method will equip the axe pertaining to the players stats including
     * attack/firemaking/agility levels
     *
     * @param axe The axe to be equipped
     * @return True if the axe was equipped; false otherwise.
     */
    public static boolean equipAxe(int axe, int playerAttackLevel, int playerFiremakingLevel, int playerAgilityLevel) {
        // utilize mapped axe levels for appropriate axe to be equipped
        HashMap<Integer, Integer> mappedATTLevels;
        mappedATTLevels = Workable.getMappedATTLevels();

        // validate and filter
        if (!mappedATTLevels.isEmpty() && axe > 0 && playerAttackLevel > 0 && playerFiremakingLevel > 0
                && playerAgilityLevel > 0) {
            // find the axe in the mapped hashmap
            for (Integer axeID : mappedATTLevels.keySet()) {
                // find axe in hashmap first before checking against players level
                if (axe == axeID) {
                    // find the given axe in the inventory and equip
                    RSItem[] axeToEquip = Inventory.find(axeID);
                    // player attack level greater than or equal to the mapped axe level
                    if (playerAttackLevel >= mappedATTLevels.get(axeID) && axeToEquip.length > 0 && axeToEquip[0] != null) {
                        // more validation if special axe is passed as argument
                        switch (axeID) {
                            case Workable.CRYSTAL_AXE_ACTIVE:
                            case Workable.CRYSTAL_AXE_INACTIVE: {
                                if (playerAgilityLevel >= 50) {
                                    axeToEquip[0].click("Wield");
                                    return true;
                                }
                            }
                            break;
                            case Workable.INFERNAL_AXE_ACTIVE:
                            case Workable.INFERNAL_AXE_INACTIVE: {
                                if (playerFiremakingLevel >= 85) {
                                    axeToEquip[0].click("Wield");
                                    return true;
                                }
                            }
                            break;
                            default: {
                                List<String> blackList = new ArrayList<>();
                                blackList.add("Wield");
                                String[] viableOptions = axeToEquip[0].getDefinition().getActions();

                                if (viableOptions.length == 0) {
                                    return false;
                                }

                                if (Arrays.asList(viableOptions).contains(blackList)) ;
                                {
                                    return axeToEquip[0].click(blackList.get(0));
                                }
                                //axeToEquip[0].click("Wield");
                                //return true;
                            }
                        }
                    } else {
                        assert axeToEquip[0] != null;
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * This method will equip the axe pertaining to the players attack level
     *
     * @param axe The axe chosen to be equipped
     * @return True if axe was equipped; false otherwise.
     */
    private boolean equipAxe(String axe) {
        // utilize mapped axe levels for appropriate axe
        HashMap<Integer, Integer> mappedAxeLevels;
        mappedAxeLevels = Workable.getMappedWCLevels();

        // find the axe in the inventory
        RSItem[] axeToEquip = Inventory.find(axe);

        // validate and filter
        if ((axeToEquip.length > 0) && (axe.getBytes().length > 0) && axeToEquip[0] != null) {
            // extract the id from the axe to wield
            final int axe_id = axeToEquip[0].getDefinition().getID();

            // player attack level greater than or equal to the mapped axe level
            // proceed to wield the axe
            if (worker.getPlayerAttackLevel() >= mappedAxeLevels.get(axe_id) && axeToEquip[0] != null && mappedAxeLevels.get(axe_id) != null) {
                final RSItem axe_item = axeToEquip[0];
                axe_item.click("Wield");
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    // example, if player has several axes in the bank, calculate the best axe according to stats
    // such as woodcutting level, firemaking level, and quests for crystal axe.
    // if the player has a crystal axe but no requirements are met then the next option is infernal or dragon..
    // for woodcutting utilization;
    public static int calculateBestAxe(int playerWoodcuttingLevel, int playerFiremakingLevel, boolean isQuestComplete) {
        final HashMap<Integer, Integer> mapped_wc_levels = Workable.getMappedWCLevels();
        final RSItem[] axes = findAxesInBank();
        int bestAxe = 0;

        for (final RSItem axe_item : axes) {

            switch (axe_item.getID()) {

                case Workable.CRYSTAL_AXE_ACTIVE: {
                    if (playerWoodcuttingLevel >= mapped_wc_levels.get(Workable.CRYSTAL_AXE_ACTIVE)
                            && isQuestComplete && !Globals.specialAxe) {
                        bestAxe = Workable.CRYSTAL_AXE_ACTIVE;
                        break;
                    } else {
                    }
                }
                break;

                case Workable.CRYSTAL_AXE_INACTIVE: {
                    boolean hasCrystalAxe = false;

                    for (final RSItem axe : axes) {
                        if (axe.getDefinition().getID() == Workable.CRYSTAL_AXE_ACTIVE) {
                            hasCrystalAxe = true;
                            break;
                        }
                    }
                    if (!hasCrystalAxe && playerWoodcuttingLevel >= mapped_wc_levels.get(Workable.CRYSTAL_AXE_INACTIVE)
                            && isQuestComplete && !Globals.specialAxe) {
                        // if no better axe found, use this one
                            if (!(Inventory.find(Workable.CRYSTAL_AXE_ACTIVE, Workable.CRYSTAL_AXE_INACTIVE).length > 0)
                                    && !(Equipment.isEquipped(Workable.CRYSTAL_AXE_ACTIVE, Workable.CRYSTAL_AXE_INACTIVE))) {
                                bestAxe = Workable.CRYSTAL_AXE_INACTIVE;
                                break;
                            }
                    } else {
                    }
                }
                break;

                case Workable.INFERNAL_AXE_ACTIVE: {
                    if (playerWoodcuttingLevel >= mapped_wc_levels.get(Workable.INFERNAL_AXE_ACTIVE)
                            && Globals.specialAxe
                            && playerFiremakingLevel >= 85) { // let the user decide if they want to use this
                        bestAxe = Workable.INFERNAL_AXE_ACTIVE;
                        break;
                    }
                }
                break;

                case Workable.INFERNAL_AXE_INACTIVE: {
                    if (playerWoodcuttingLevel >= mapped_wc_levels.get(Workable.INFERNAL_AXE_INACTIVE)
                            && playerFiremakingLevel >= 85 && !Globals.specialAxe) {

                        // search for better axe before moving forward on final decision
                        int axeCount = 0;
                        for (final RSItem axe : axes) {
                            if (axe.getDefinition().getID() == Workable.CRYSTAL_AXE_INACTIVE) {
                                axeCount++;
                            }
                            if (axe.getDefinition().getID() == Workable.CRYSTAL_AXE_ACTIVE) {
                                axeCount++;
                            }
                        }

                        // if no better axe found, use this one
                        if (axeCount == 0) {
                            if (!(Inventory.find(Workable.CRYSTAL_AXE_ACTIVE, Workable.CRYSTAL_AXE_INACTIVE).length > 0)
                                    && !(Equipment.isEquipped(Workable.CRYSTAL_AXE_ACTIVE, Workable.CRYSTAL_AXE_INACTIVE))) {

                                bestAxe = Workable.INFERNAL_AXE_INACTIVE;

                                break;
                            }
                        }
                    }
                }
                break;

                case Workable.DRAGON_AXE: {

                    if (playerWoodcuttingLevel >= mapped_wc_levels.get(Workable.DRAGON_AXE) && !Globals.specialAxe) {
                        // search for better axe before moving forward on final decision
                        int axeCount = 0;

                        for (final RSItem axe : axes) {
                            if (axe.getDefinition().getID() == Workable.CRYSTAL_AXE_INACTIVE) {
                                axeCount++;
                            }
                            if (axe.getDefinition().getID() == Workable.CRYSTAL_AXE_ACTIVE) {
                                axeCount++;
                            }
                            if (axe.getDefinition().getID() == Workable.INFERNAL_AXE_INACTIVE) {
                                axeCount++;
                            }
                        }

                        // check if player has better axe in inventory/equipped
                        // if no better axe or same axe is found, proceed to set as the best axe for player.
                        if (axeCount == 0) {
                            if (!(Inventory.find(Workable.CRYSTAL_AXE_ACTIVE, Workable.CRYSTAL_AXE_INACTIVE,
                                    Workable.INFERNAL_AXE_INACTIVE).length > 0)

                                    && !(Equipment.isEquipped(Workable.CRYSTAL_AXE_ACTIVE, Workable.CRYSTAL_AXE_INACTIVE,
                                    Workable.INFERNAL_AXE_INACTIVE))) {

                                bestAxe = Workable.DRAGON_AXE;
                                break;

                            }
                        }
                    }
                }
                break;
            }

        }

        // didnt find an axe to work with
        if (bestAxe == 0) {
            // find a f2p axe instead
            bestAxe = calculateBestAxeF2P(playerWoodcuttingLevel);
        }

        return bestAxe;
    }

    // a method that determines the best axe per woodcutting level for F2P's
    public static int calculateBestAxeF2P(int playerWoodcuttingLevel) {
        final HashMap<Integer, Integer> mappedWCLevels = Workable.getMappedWCLevels();
        final RSItem[] axes = findAxesInBank();
        int bestAxe = 0;

        // loop through all axes found in the players bank
        for (final RSItem axe_item : axes) {
            // switch each axe found and determine the best axe accordingly
            switch (axe_item.getDefinition().getID()) {
                case Workable.RUNE_AXE: {
                    // valid and set as best axe if appropriate
                    // control using param
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.RUNE_AXE)) {
                        bestAxe = Workable.RUNE_AXE;
                        break;
                    }
                }
                break;
                case Workable.ADAMANT_AXE: {
                    // check player woodcutting level, if greater than or equal to adamant and less than rune
                    // set as best axe
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.ADAMANT_AXE)
                            && playerWoodcuttingLevel < mappedWCLevels.get(Workable.RUNE_AXE)) {
                        bestAxe = Workable.ADAMANT_AXE;
                        break;
                    }
                    // find better axe in bank and keep a count
                    int axeCount = 0;
                    for (final RSItem axe : axes) {
                        if (axe.getDefinition().getID() == Workable.RUNE_AXE) {
                            axeCount++;
                        }
                    }

                    // if no better axe exists inside the bank then proceed to set the best axe as adamant
                    if (axeCount == 0 && playerWoodcuttingLevel >= mappedWCLevels.get(Workable.ADAMANT_AXE)) {
                        // if no better axe is on the player proceed
                        if (!(Inventory.find(Workable.RUNE_AXE).length > 0) && !(Equipment.isEquipped(Workable.RUNE_AXE))) {
                            bestAxe = Workable.ADAMANT_AXE;
                            break;
                        }
                    }
                }
                break;

                case Workable.MITHRIL_AXE: {
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.MITHRIL_AXE)
                            && playerWoodcuttingLevel < mappedWCLevels.get(Workable.ADAMANT_AXE)) {
                        bestAxe = Workable.MITHRIL_AXE;
                        break;
                    }

                    // find better axe in bank and keep a count
                    int axeCount = 0;
                    for (final RSItem axe : axes) {
                        if (axe.getDefinition().getID() == Workable.RUNE_AXE) {
                            axeCount++;
                        }
                        if (axe.getDefinition().getID() == Workable.ADAMANT_AXE) {
                            axeCount++;
                        }
                    }

                    // if no better axe exists inside the bank then proceed to set the best axe
                    if (axeCount == 0 && playerWoodcuttingLevel >= mappedWCLevels.get(Workable.MITHRIL_AXE)) {
                        // if no better axe is on the player proceed
                        if (!(Inventory.find(Workable.RUNE_AXE, Workable.ADAMANT_AXE).length > 0) && !(Equipment.isEquipped(Workable.RUNE_AXE, Workable.ADAMANT_AXE))) {
                            bestAxe = Workable.MITHRIL_AXE;
                            break;
                        }
                    }
                }
                break;

                case Workable.BLACK_AXE: {
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.BLACK_AXE)
                            && playerWoodcuttingLevel < mappedWCLevels.get(Workable.MITHRIL_AXE)) {
                        bestAxe = Workable.BLACK_AXE;
                        break;
                    } else {
                    }

                    // find better axe in bank and keep a count
                    int axeCount = 0;
                    for (final RSItem axe : axes) {
                        axeCount = getAxeCount(axeCount, axe);
                    }

                    if (axeCount == 0 && playerWoodcuttingLevel >= mappedWCLevels.get(Workable.BLACK_AXE)) {
                        // if no better axe is on the player proceed
                        if (!(Inventory.find(Workable.RUNE_AXE, Workable.ADAMANT_AXE, Workable.MITHRIL_AXE).length > 0) && !(Equipment.isEquipped(Workable.RUNE_AXE, Workable.ADAMANT_AXE, Workable.MITHRIL_AXE))) {
                            bestAxe = Workable.BLACK_AXE;
                            break;
                        }
                    }
                }
                break;

                case Workable.STEEL_AXE: {
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.STEEL_AXE)
                            && playerWoodcuttingLevel < mappedWCLevels.get(Workable.BLACK_AXE)) {
                        bestAxe = Workable.STEEL_AXE;
                        break;
                    }

                    // find better axe in bank and keep a count
                    int axeCount = 0;
                    for (final RSItem axe : axes) {
                        axeCount = getAxeCount(axeCount, axe);
                        if (axe.getDefinition().getID() == Workable.BLACK_AXE) {
                            axeCount++;
                        }
                    }

                    // no better axe found, proceed
                    if (axeCount == 0 && playerWoodcuttingLevel >= mappedWCLevels.get(Workable.STEEL_AXE)) {
                        // if no better axe is on the player proceed
                        if (!(Inventory.find(Workable.RUNE_AXE, Workable.ADAMANT_AXE, Workable.MITHRIL_AXE,
                                Workable.BLACK_AXE).length > 0) && !(Equipment.isEquipped(Workable.RUNE_AXE,
                                Workable.ADAMANT_AXE, Workable.MITHRIL_AXE, Workable.BLACK_AXE))) {
                            bestAxe = Workable.STEEL_AXE;
                            break;
                        }
                    }
                }
                break;

                case Workable.IRON_AXE: {
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.IRON_AXE)
                            && playerWoodcuttingLevel < mappedWCLevels.get(Workable.STEEL_AXE)) {
                        bestAxe = Workable.IRON_AXE;
                        break;
                    }

                    // find better axe in bank and keep a count
                    int axeCount = 0;
                    for (final RSItem axe : axes) {
                        axeCount = getAxeCount(axeCount, axe);
                        if (axe.getDefinition().getID() == Workable.BLACK_AXE) {
                            axeCount++;
                        }
                        if (axe.getDefinition().getID() == Workable.STEEL_AXE) {
                            axeCount++;
                        }
                    }

                    // no better axe found, proceed
                    if (axeCount == 0 && playerWoodcuttingLevel >= mappedWCLevels.get(Workable.IRON_AXE)) {
                        // if no better axe is on the player proceed
                        if (!(Inventory.find(Workable.RUNE_AXE, Workable.ADAMANT_AXE, Workable.MITHRIL_AXE,
                                Workable.BLACK_AXE, Workable.STEEL_AXE).length > 0) && !(Equipment.isEquipped(Workable.RUNE_AXE,
                                Workable.ADAMANT_AXE, Workable.MITHRIL_AXE, Workable.BLACK_AXE, Workable.STEEL_AXE))) {
                            bestAxe = Workable.IRON_AXE;
                            break;
                        }
                    }
                }
                break;

                case Workable.BRONZE_AXE: {
                    // found bronze axe, check if player woodcutting level is appropriate.
                    // greater than or equal to bronze wc level and less than steel
                    // pick this axe
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.BRONZE_AXE)
                            && playerWoodcuttingLevel < mappedWCLevels.get(Workable.STEEL_AXE)) {

                        // search player bank if other axe exists
                        // keep count of available axes to player bank
                        int axeCount = 0;

                        for (final RSItem axe : axes) {

                            axeCount = getAxeCount(axeCount, axe);

                            if (axe.getDefinition().getID() == Workable.BLACK_AXE) {
                                axeCount++;
                            }
                            if (axe.getDefinition().getID() == Workable.STEEL_AXE) {
                                axeCount++;
                            }
                            if (axe.getDefinition().getID() == Workable.IRON_AXE) {
                                axeCount++;
                            }
                        }

                        // no better axe found proceed
                        if (axeCount == 0) {
                            // proceed to check if player has better axe in inventory or equipped
                            // if not set bestAxe id to Bronze etc...
                            if (!(Inventory.find(Workable.RUNE_AXE, Workable.ADAMANT_AXE, Workable.MITHRIL_AXE,
                                    Workable.BLACK_AXE, Workable.STEEL_AXE, Workable.IRON_AXE).length > 0) && !(Equipment.isEquipped(Workable.RUNE_AXE,
                                    Workable.ADAMANT_AXE, Workable.MITHRIL_AXE, Workable.BLACK_AXE,
                                    Workable.STEEL_AXE, Workable.IRON_AXE))) {

                                bestAxe = Workable.BRONZE_AXE;
                                break;
                            }
                        }
                        break;
                    } else {
                    }
                }
            }
        }
        return bestAxe;
    }

    private static int getAxeCount(int axeCount, RSItem axe) {
        if (axe.getDefinition().getID() == Workable.RUNE_AXE) {
            axeCount++;
        }
        if (axe.getDefinition().getID() == Workable.ADAMANT_AXE) {
            axeCount++;
        }
        if (axe.getDefinition().getID() == Workable.MITHRIL_AXE) {
            axeCount++;
        }
        return axeCount;
    }

    /**
     * Validate
     *
     * @return True if the player doesn't contain an axe on their person, false otherwise.
     */
    private static boolean shouldFetchAxe(Task task) {
        return !Workable.inventoryContainsAxe() && !Workable.isAxeEquipped() && BankHelper.isInBank();
    }
}
