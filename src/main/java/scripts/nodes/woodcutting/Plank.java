package scripts.nodes.woodcutting;

import org.tribot.api.General;
import org.tribot.api2007.Interfaces;
import org.tribot.api2007.Inventory;
import org.tribot.api2007.NPCs;
import org.tribot.api2007.Player;
import org.tribot.api2007.types.*;
import scripts.api.*;
import scripts.api.antiban.AntiBan;
import scripts.dax_api.walker_engine.interaction_handling.NPCInteraction;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Purpose of class: Will perform the planking option by right-clicking the sawmill operator.
 * Author: Jackson (Polymorphic~TRiBot)
 * <p>
 * Updated 11/04/2021 - Added null safe checks to all methods and cached all return values.
 * <p>
 * Updated 11/05/2021 - Changed naming convention for final variables.
 */

public class Plank extends Node {

    // filter for finding any sawmill operator
    // constant
    private static Predicate<RSNPC> sawmill_operator_npc_filter() {
        return rsnpc -> rsnpc.getName()
                .toLowerCase()
                .contains("sawmill operator");
    }

    @Override
    public void execute(Task task) {
        AntiBan.sleep(Globals.getWaitTimes(), AntiBan.getHumanFatigue());

        final int plankGoldStart = Plank.calculateOakPlankGold(Workable.getAllLogs());

        if (plankGoldStart != -1) {
            // set the players gold currently inside inventory
            debug("Start gold = " + plankGoldStart);
        }

        // fetch all logs inside the player's inventory
        final RSItem[] logs = Workable.getAllLogs();

        // find the plank option such as oak plank
        final String plankOption = calculatePlankOption(logs);

        if (plankOption != null) {
            // if the plank interface is open, then click plank option
            final boolean firstClickResult = clickPlankOption(plankOption);
            // if failed to click plank option, then proceed to click
            if (!firstClickResult) {
                // open plank interface
                debug("Utilizing sawmill operator");
                if (openBuyPlankInterface()) {
                    General.sleep(1200, 1400);
                    // now select the plank option
                    final boolean finalClickResult = clickPlankOption(plankOption);
                    if (finalClickResult) {
                        debug("Planking complete");
                    }
                }
            } else {
                debug("Planking complete!");
            }

            General.sleep(1000, 3000);

            final int plankGoldEnd = Plank.calculateOakPlankGold(Workable.getAllLogs());

            // if their are oak logs leftover check the plank gold
            if (plankGoldEnd != -1 && plankGoldStart != -1) {
                // set the amount of gold that wasn't spent
                Gold.setGoldSpentTotal(plankGoldStart - plankGoldEnd);
            } else {
                Gold.setGoldSpentTotal(plankGoldStart);
            }

            debug("Gold spent = " + Gold.getGoldSpentTotal());
        } else {
            debug("Planking incomplete");
        }
    }

    @Override
    public boolean validate(Task task) {
        return task.shouldPlankThenBank()
                && shouldMakePlank(task);
    }

    @Override
    public void debug(String status) {
        Globals.setState(status);
        General.println("[Plank Control] ".concat(status));
    }

    /**
     * Method determines if player can actually make oak planks.
     *
     * @param logs The logs to work with, oak logs specifically.
     * @return True if the player has the correct amount of inventory gold to make oak planks
     * equal to the amount of oak logs in the inventory, otherwise; false.
     */
    public static boolean canActuallyMakeOakPlank(RSItem[] logs) {
        // no logs, or oak logs, or logs null, leave now
        if (logs == null || logs.length == 0) {
            return false;
        }

        final int plankGold = calculateOakPlankGold(logs);

        RSItem[] goldArray = Workable.getAllGold();

        int currentInventoryGold = 0;

        if (goldArray.length > 0) {
            currentInventoryGold = goldArray[0].getStack();
        }

        return plankGold != -1 && currentInventoryGold >= Workable.OAK_FEE;
    }

    /**
     * Method calculates how many oak planks could be made
     * Multiply factor of 250 by oak log inventory count.
     *
     * @param logs To perform stream manipulation.
     * @return The unsigned integer of the gold required to make oak planks based on inventory count of logs;
     * otherwise return -1 if the inventory doesn't contain any logs or the logs aren't type Oak or logs null.
     */
    public static int calculateOakPlankGold(RSItem[] logs) {
        // no logs or logs null, leave now return -1
        if (logs == null || logs.length == 0) {
            return -1;
        }

        final boolean isOakLog = Arrays.stream(logs)
                .map(RSItem::getDefinition)
                .filter(Objects::nonNull)
                .map(RSItemDefinition::getName)
                .anyMatch(rsItemName -> rsItemName.toLowerCase().contains("oak"));

        // no oak logs inside the inventory, leave now return -1
        if (!isOakLog) {
            return -1;
        }

        // return the all oak logs in the inventory and multiply the count by 250
        return (int) (Arrays.stream(logs)
                .map(RSItem::getDefinition)
                .filter(Objects::nonNull)
                .map(RSItemDefinition::getName)
                .filter(rsItemName -> rsItemName.toLowerCase().contains("oak"))
                .count()
                * Workable.OAK_FEE
        );
    }

    public static boolean isAtSawmill() {
        final RSNPC[] sawmillNpcs = NPCs.findNearest(sawmill_operator_npc_filter());

        if (sawmillNpcs.length == 0) {
            return false;
        }

        final RSNPC sawmillNPC = sawmillNpcs[0];

        return Player.getPosition().distanceTo(sawmillNPC) < 7;
    }

    private boolean openBuyPlankInterface() {
        return NPCInteraction.clickNpc(sawmill_operator_npc_filter(), "Buy-plank");
    }

    private String calculatePlankOption(RSItem[] logs) {
        String option = null;

        final boolean isOak = Arrays.stream(logs)
                .map(RSItem::getDefinition)
                .filter(Objects::nonNull)
                .map(RSItemDefinition::getName)
                .anyMatch(rsItemName -> rsItemName.toLowerCase().contains("oak"));

        if (isOak) {
            option = "Oak - 250gp";
        }

        return option;
    }

    private boolean clickPlankOption(String option) {
        final RSInterfaceMaster masterInterface = Interfaces.get(270);

        boolean clickResult = false;

        if (masterInterface != null) {
            final Optional<RSInterfaceChild> fletchOptionInterface = Arrays.stream(masterInterface.getChildren())
                    .filter(rsInterfaceChild -> rsInterfaceChild.getComponentName().contains(option))
                    .findFirst();

            if (fletchOptionInterface.isPresent()) {
                clickResult = fletchOptionInterface
                        .map(rsInterfaceChild -> rsInterfaceChild.click("Make"))
                        .orElse(false);
            }
        }

        return clickResult;
    }

    private boolean shouldMakePlank(Task task) {
        // goldSpent has to be less than or equal to playerChoiceGold for the plank node to execute
        // if using goldPerTask.
        // once the goldSpent has reached the actual gold limit, then the task is complete, can't make planks.
        // if using all gold then doesn't matter if goldSpent has reached the limited.
        if (task.shouldPlankThenBank() && Inventory.isFull()) {
            if (isAtSawmill() && Workable.inventoryContainsGold()) {
                return !task.isValidated() && canActuallyMakeOakPlank(Workable.getAllLogs());
            }
        }
        return false;
    }
}
