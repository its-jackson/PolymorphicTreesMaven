package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.*;
import org.tribot.api2007.types.RSItem;
import org.tribot.api2007.types.RSItemDefinition;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.shared.helpers.BankHelper;

import java.util.*;

/**
 * Purpose of class: If the player doesn't have an axe on their person,
 * then open bank and withdraw the appropriate axe.
 * Attempts equipping the axe in comparison to appropriate stats/levels.
 *
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
 *
 * Updated 11/05/2021 - Changed naming convention for final variables.
 */

public class FetchAxe extends Node {

    @Override
    public void execute(Task task) {
        debug("Sleeping " + Workable.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue()));

        // format debug
        String format = String.format(
                "Woodcutting level: %s, attack level: %s, firemaking level: %s, " +
                        "agility level: " +
                        "%s, song of elves: %s%n",
                getWorker().getPlayerWoodcuttingLevel(),
                getWorker().getPlayerAttackLevel(),
                getWorker().getPlayerFiremakingLevel(),
                getWorker().getPlayerAgilityLevel(),
                getWorker().isSongOfElvesComplete()
        );

        debug(format);

        // open the bank and equip/withdraw the best axe according to the woodcutting/attack/firemaking levels
        if (Bank.openBank()) {
            if (Banking.isBankLoaded()) {
                // withdraw the best axe for member players
                if (Progressive.isMember()) {
                    // calculate the best axe found in the bank pertaining
                    // to player woodcutting/firemaking/quests respectively
                    final int best_axe_id = calculateBestAxe(
                            getWorker().getPlayerWoodcuttingLevel(),
                            getWorker().getPlayerFiremakingLevel(),
                            getWorker().isSongOfElvesComplete()
                    );

                    // confirm the axe has been withdrawn
                    if ((best_axe_id > 0) && withdrawAxe(best_axe_id)) {
                        debug("Optimal axe found");
                        General.sleep(1000, 1200);
                        if (Inventory.find(best_axe_id).length > 0) {
                            final RSItem[] axeWithdrew = Inventory.find(best_axe_id);
                            if (axeWithdrew.length > 0 && axeWithdrew[0] != null) {
                                final RSItemDefinition definition = axeWithdrew[0].getDefinition();
                                if (definition != null) {
                                    final String name = definition.getName().toLowerCase(Locale.ROOT);
                                    debug("Withdrew " + name);
                                }
                            }
                        }
                    }

                    if (Banking.depositAllExcept(best_axe_id) > 0) {
                        debug("Deposited inventory");
                    }

                    if (Banking.close()) {
                        debug("Closed bank");
                    }

                    // now equip the axe if appropriate
                    if (equipAxe(
                            best_axe_id,
                            getWorker().getPlayerAttackLevel(),
                            getWorker().getPlayerFiremakingLevel(),
                            getWorker().getPlayerAgilityLevel())
                    ) {
                        debug("Equipped axe");
                    }

                } else {
                    // determine the best axe to withdraw from the players bank pertaining to woodcutting level
                    // for F2P players
                    final int bestAxeId = calculateBestAxeF2P(getWorker().getPlayerWoodcuttingLevel());

                    // confirm the axe has been withdrawn
                    if ((bestAxeId > 0) && withdrawAxe(bestAxeId)) {
                        debug("Optimal axe found");
                        final RSItem[] optimalAxes = Inventory.find(bestAxeId);
                        General.sleep(1000, 1200);
                        if (optimalAxes.length > 0 && optimalAxes[0] != null) {
                            final RSItemDefinition definition = optimalAxes[0].getDefinition();
                            if (definition != null) {
                                final String name = definition.getName().toLowerCase(Locale.ROOT);
                                debug("Withdrew " + name);
                            }
                        }
                    }

                    if (Banking.depositAllExcept(bestAxeId) > 0) {
                        debug("Deposited inventory");
                    }

                    if (Banking.close()) {
                        debug("Closed bank");
                    }

                    // equip the axe if appropriate
                    if (equipAxe(bestAxeId, getWorker().getPlayerAttackLevel())) {
                        debug("Equipped axe");
                    }
                }
            } else {
                Interfaces.closeAll();
            }
        } else {
            Interfaces.closeAll();
        }
    }

    @Override
    public boolean validate(Task task) {
        // return true; we are in the bank, no axe is equipped and no axe is in the inventory
        return shouldFetchAxe();
    }

    @Override
    public void debug(String status) {
        String format = ("[Axe Control] ");
        Globals.setState(status);
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
        final HashMap<Integer, Integer> mappedATTLevels =
                Workable.getMappedATTLevels();

        // validate and filter
        if (!mappedATTLevels.isEmpty() && axe > 0 && playerAttackLevel > 0) {
            // find the axe in the mapped hashmap
            for (final Integer axeID : mappedATTLevels.keySet()) {
                // find axe in hashmap first before checking against players level
                if (axe == axeID) {
                    // find the given axe in the inventory and equip
                    final RSItem[] axeToEquip = Inventory.find(axeID);
                    // player attack level greater than or equal to the mapped axe level
                    if (playerAttackLevel >= mappedATTLevels.get(axeID) && axeToEquip.length > 0 && axeToEquip[0] != null) {
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
        final HashMap<Integer, Integer> mappedATTLevels =
                Workable.getMappedATTLevels();

        // validate and filter
        if (!mappedATTLevels.isEmpty() && axe > 0 && playerAttackLevel > 0 && playerFiremakingLevel > 0
                && playerAgilityLevel > 0) {
            // find the axe in the mapped hashmap
            for (final Integer axeID : mappedATTLevels.keySet()) {
                // find axe in hashmap first before checking against players level
                if (axe == axeID) {
                    // find the given axe in the inventory and equip
                    final RSItem[] axeToEquip = Inventory.find(axeID);
                    // player attack level greater than or equal to the mapped axe level
                    if (playerAttackLevel >= mappedATTLevels.get(axeID) && axeToEquip.length > 0 && axeToEquip[0] != null) {
                        // more validation if special axe is passed as argument
                        switch (axeID) {
                            case Workable.CRYSTAL_AXE_ACTIVE:
                            case Workable.CRYSTAL_AXE_INACTIVE: {
                                if (playerAgilityLevel >= 50) {
                                    return axeToEquip[0].click("Wield");
                                }
                            }
                            break;
                            case Workable.INFERNAL_AXE_ACTIVE:
                            case Workable.INFERNAL_AXE_INACTIVE: {
                                if (playerFiremakingLevel >= 85) {
                                    return axeToEquip[0].click("Wield");
                                }
                            }
                            break;
                            default: {
                                final List<String> blackList = List.of("Wield");
                                final RSItemDefinition definition = axeToEquip[0].getDefinition();

                                if (definition != null) {
                                    String[] viableOptions = definition.getActions();
                                    if (viableOptions.length == 0) {
                                        return false;
                                    }
                                    if (Arrays.asList(viableOptions).contains(blackList.get(0))) {
                                        return axeToEquip[0].click(blackList.get(0));
                                    }
                                }
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

    // example, if player has several axes in the bank, calculate the best axe according to stats
    // such as woodcutting level, firemaking level, and quests for crystal axe.
    // if the player has a crystal axe but no requirements are met then the next option is infernal or dragon.
    // for woodcutting utilization
    public static int calculateBestAxe(int playerWoodcuttingLevel, int playerFiremakingLevel, boolean isQuestComplete) {
        final HashMap<Integer, Integer> mappedWCLevels = Workable.getMappedWCLevels();
        final RSItem[] axesInBank = findAxesInBank();
        int bestAxe = 0;

        for (final RSItem axeItem : axesInBank) {
            switch (axeItem.getID()) {
                case Workable.CRYSTAL_AXE_ACTIVE: {
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.CRYSTAL_AXE_ACTIVE)
                            && isQuestComplete && !Globals.isSpecialAxe()) {
                        bestAxe = Workable.CRYSTAL_AXE_ACTIVE;
                        break;
                    }
                }
                break;
                case Workable.CRYSTAL_AXE_INACTIVE: {
                    boolean hasCrystalAxe = false;

                    for (final RSItem axe : axesInBank) {
                        final RSItemDefinition definition = axe.getDefinition();
                        if (definition != null) {
                            if (definition.getID() == Workable.CRYSTAL_AXE_ACTIVE) {
                                hasCrystalAxe = true;
                                break;
                            }
                        }
                    }
                    if (!hasCrystalAxe && playerWoodcuttingLevel >= mappedWCLevels.get(Workable.CRYSTAL_AXE_INACTIVE)
                            && isQuestComplete && !Globals.isSpecialAxe()) {
                        // if no better axe found, use this one
                        if (!(Inventory.find(Workable.CRYSTAL_AXE_ACTIVE, Workable.CRYSTAL_AXE_INACTIVE).length > 0)
                                && !(Equipment.isEquipped(Workable.CRYSTAL_AXE_ACTIVE, Workable.CRYSTAL_AXE_INACTIVE))) {
                            bestAxe = Workable.CRYSTAL_AXE_INACTIVE;
                            break;
                        }
                    }
                }
                break;

                case Workable.INFERNAL_AXE_ACTIVE: {
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.INFERNAL_AXE_ACTIVE)
                            && Globals.isSpecialAxe()
                            && playerFiremakingLevel >= 85) { // let the user decide if they want to use this
                        bestAxe = Workable.INFERNAL_AXE_ACTIVE;
                        break;
                    }
                }
                break;

                case Workable.INFERNAL_AXE_INACTIVE: {
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.INFERNAL_AXE_INACTIVE)
                            && playerFiremakingLevel >= 85 && !Globals.isSpecialAxe()) {

                        // search for better axe before moving forward on final decision
                        int axeCount = 0;
                        for (final RSItem axe : axesInBank) {
                            final RSItemDefinition definition = axe.getDefinition();
                            if (definition != null) {
                                int id = definition.getID();
                                if (id == Workable.CRYSTAL_AXE_INACTIVE) {
                                    axeCount++;
                                }
                                if (id == Workable.CRYSTAL_AXE_ACTIVE) {
                                    axeCount++;
                                }
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
                    if (playerWoodcuttingLevel >= mappedWCLevels.get(Workable.DRAGON_AXE) && !Globals.isSpecialAxe()) {
                        // search for better axe before moving forward on final decision
                        int axeCount = 0;

                        for (final RSItem axe : axesInBank) {
                            final RSItemDefinition definition = axe.getDefinition();
                            if (definition != null) {
                                int id = definition.getID();
                                if (id == Workable.CRYSTAL_AXE_INACTIVE) {
                                    axeCount++;
                                }
                                if (id == Workable.CRYSTAL_AXE_ACTIVE) {
                                    axeCount++;
                                }
                                if (id == Workable.INFERNAL_AXE_INACTIVE) {
                                    axeCount++;
                                }
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

        // didn't find an axe to work with
        if (bestAxe == 0) {
            // find a f2p axe instead
            bestAxe = calculateBestAxeF2P(playerWoodcuttingLevel);
        }

        return bestAxe;
    }

    // a method that determines the best axe as per woodcutting level for F2P
    public static int calculateBestAxeF2P(int playerWoodcuttingLevel) {
        final HashMap<Integer, Integer> mappedWCLevels = Workable.getMappedWCLevels();
        final RSItem[] axes = findAxesInBank();
        int bestAxe = 0;

        // loop through all axes found in the players bank
        for (final RSItem axeItem : axes) {
            // switch each axe found and determine the best axe accordingly
            final RSItemDefinition definition =  axeItem.getDefinition();
            if (definition != null) {
                int id = definition.getID();
                switch (id) {
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
                            final RSItemDefinition axeDefinition = axe.getDefinition();
                            if (axeDefinition != null) {
                                if (axeDefinition.getID() == Workable.RUNE_AXE) {
                                    axeCount++;
                                }
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
                            final RSItemDefinition axeDefinition = axe.getDefinition();
                            if (axeDefinition != null) {
                                int axeID = axeDefinition.getID();
                                if (axeID == Workable.RUNE_AXE) {
                                    axeCount++;
                                }
                                if (axeID == Workable.ADAMANT_AXE) {
                                    axeCount++;
                                }
                            }
                        }

                        // if no better axe exists inside the bank then proceed to set the best axe
                        if (axeCount == 0 && playerWoodcuttingLevel >= mappedWCLevels.get(Workable.MITHRIL_AXE)) {
                            // if no better axe is on the player proceed
                            if (!(Inventory.find(Workable.RUNE_AXE, Workable.ADAMANT_AXE).length > 0) &&
                                    !(Equipment.isEquipped(Workable.RUNE_AXE, Workable.ADAMANT_AXE))) {
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
                        }

                        // find better axe in bank and keep a count
                        int axeCount = 0;
                        for (final RSItem axe : axes) {
                            axeCount = getAxeCount(axeCount, axe);
                        }

                        if (axeCount == 0 && playerWoodcuttingLevel >= mappedWCLevels.get(Workable.BLACK_AXE)) {
                            // if no better axe is on the player proceed
                            if (!(Inventory.find(Workable.RUNE_AXE, Workable.ADAMANT_AXE, Workable.MITHRIL_AXE).length > 0)
                                    && !(Equipment.isEquipped(Workable.RUNE_AXE, Workable.ADAMANT_AXE, Workable.MITHRIL_AXE))) {
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
                            final RSItemDefinition axeDefinition = axe.getDefinition();
                            if (axeDefinition != null) {
                                int axeID = axeDefinition.getID();
                                if (axeID == Workable.BLACK_AXE) {
                                    axeCount++;
                                }
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
                            final RSItemDefinition axeDefinition = axe.getDefinition();
                            if (axeDefinition != null) {
                                int axeID = axeDefinition.getID();
                                if (axeID == Workable.BLACK_AXE) {
                                    axeCount++;
                                }
                                if (axeID == Workable.STEEL_AXE) {
                                    axeCount++;
                                }
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
                                RSItemDefinition axeDefinition = axe.getDefinition();
                                if (axeDefinition != null) {
                                    int axeID = axeDefinition.getID();
                                    if (axeID == Workable.BLACK_AXE) {
                                        axeCount++;
                                    }
                                    if (axeID == Workable.STEEL_AXE) {
                                        axeCount++;
                                    }
                                    if (axeID == Workable.IRON_AXE) {
                                        axeCount++;
                                    }
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
                        }
                    }
                }
            }
        }
        return bestAxe;
    }

    private static int getAxeCount(int axeCount, RSItem axe) {
        final RSItemDefinition definition = axe.getDefinition();

        if (definition != null) {
            final int id = definition.getID();

            if (id == Workable.RUNE_AXE) {
                axeCount++;
            }
            if (id == Workable.ADAMANT_AXE) {
                axeCount++;
            }
            if (id == Workable.MITHRIL_AXE) {
                axeCount++;
            }
        }

        return axeCount;
    }

    /**
     * Validate
     *
     * @return True if the player doesn't contain an axe on their person, false otherwise.
     */
    private boolean shouldFetchAxe() {
        return !Workable.inventoryContainsAxe() && !Workable.isAxeEquipped() && BankHelper.isInBank();
    }
}
